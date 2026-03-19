package com.flab.promotion.application

import com.flab.promotion.config.exception.BusinessException
import com.flab.promotion.config.exception.MessageCode
import com.flab.promotion.domain.dto.PromotionParticipationEvent
import com.flab.promotion.domain.entity.persistence.Participation
import com.flab.promotion.domain.enums.ParticipationStatus
import com.flab.promotion.infrastructure.kafka.PromotionKafkaProducer
import com.flab.promotion.infrastructure.persistence.ParticipationRepository
import com.flab.promotion.infrastructure.persistence.PromotionRepository
import com.flab.promotion.infrastructure.redis.PromotionRedisRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class ParticipationService(
    private val redisRepository: PromotionRedisRepository,
    private val participationRepository: ParticipationRepository,
    private val promotionRepository: PromotionRepository,
    private val promotionKafkaProducer: PromotionKafkaProducer
) {
    private val logger = KotlinLogging.logger {}

    /**
     * 비동기 참여 (Kafka)
     * - Redis Lua: 중복 체크 + 슬롯 차감 원자적 처리
     * - Kafka 동기 발행 → 컨슈머가 DB 저장
     * - 발행 실패 시 Redis 롤백
     */
    fun participateAsync(promotionId: Long, userId: Long) {
        // 유효성 검사
        val today = LocalDate.now()
        val participatedAt = LocalDateTime.now()

        val cache = redisRepository.getCache(promotionId)
            ?: throw BusinessException(MessageCode.PROMOTION_NOT_FOUND)

        if (participatedAt.isBefore(cache.startAt)) throw BusinessException(MessageCode.PROMOTION_NOT_STARTED)

        when (redisRepository.checkAndDecrement(promotionId, userId, today, cache.endAt, cache.startAt)) {
            PromotionRedisRepository.RESULT_ALREADY_PARTICIPATED -> throw BusinessException(MessageCode.ALREADY_PARTICIPATED)
            PromotionRedisRepository.RESULT_NOT_FOUND            -> throw BusinessException(MessageCode.PROMOTION_NOT_FOUND)
            PromotionRedisRepository.RESULT_SOLD_OUT             -> throw BusinessException(MessageCode.PROMOTION_FULLY_BOOKED)
        }

        try {
            promotionKafkaProducer.sendParticipation(
                PromotionParticipationEvent(
                    promotionId = promotionId,
                    userId = userId,
                    participatedAt = participatedAt,
                    startAt = cache.startAt
                )
            )
        } catch (ex: Exception) {
            logger.error(ex) { "Kafka 발행 실패 - promotionId=$promotionId, userId=$userId. Redis 롤백 수행" }
            redisRepository.rollbackParticipation(promotionId, cache.startAt, userId, today)
            throw BusinessException(MessageCode.KAFKA_PUBLISH_FAILED)
        }
    }

    /**
     * 동기 참여 - Redis 슬롯 차감 + DB 직접 저장
     * - Redis DECR: 슬롯 차감
     * - DB unique constraint: 중복 방지
     * - accumulated_participation_count 는 배치(participationCountSyncJob)로 주기적 동기화
     */
    @Transactional
    fun participateSync(promotionId: Long, userId: Long) {
        // 유효성 검사
        val today = LocalDate.now()

        val cache = redisRepository.getCache(promotionId)
            ?: throw BusinessException(MessageCode.PROMOTION_NOT_FOUND)

        if (LocalDateTime.now().isBefore(cache.startAt)) throw BusinessException(MessageCode.PROMOTION_NOT_STARTED)

        when (redisRepository.decrementSlot(promotionId, cache.startAt)) {
            PromotionRedisRepository.RESULT_NOT_FOUND -> throw BusinessException(MessageCode.PROMOTION_NOT_FOUND)
            PromotionRedisRepository.RESULT_SOLD_OUT  -> throw BusinessException(MessageCode.PROMOTION_FULLY_BOOKED)
        }

        try {
            participationRepository.save(
                Participation(
                    promotionId = promotionId,
                    userId = userId,
                    participationDate = today,
                    participationStatus = ParticipationStatus.REWARD_PENDING,
                    rewardAmount = cache.rewardAmount,
                    createdBy = userId,
                    updatedBy = userId
                )
            )
        } catch (ex: DataIntegrityViolationException) {
            redisRepository.incrementSlot(promotionId, cache.startAt)
            throw BusinessException(MessageCode.ALREADY_PARTICIPATED)
        } catch (ex: Exception) {
            logger.error(ex) { "DB 저장 실패 - promotionId=$promotionId, userId=$userId. Redis 슬롯 복구" }
            redisRepository.incrementSlot(promotionId, cache.startAt)
            throw BusinessException(MessageCode.UNKNOWN_EXCEPTION)
        }
    }


    /**
     * 동기 참여 - DB 횟수 관리 + 멱등성 설계
     *
     * 멱등성 보장 수단:
     * - DB 중복 체크 (선제적 fast-fail): 불필요한 count 증가 시도를 줄임
     * - incrementCountIfAvailable: 단일 UPDATE로 원자적 슬롯 차감
     * - saveAndFlush: flush 지연 없이 즉시 constraint 검사 → DataIntegrityViolationException 즉시 감지
     * - @Transactional: INSERT 실패 시 incrementCountIfAvailable 도 함께 롤백
     */
    @Transactional
    fun participateWithDbCount(promotionId: Long, userId: Long) {
        // 유효성 검사
        val today = LocalDate.now()

        val promotion = promotionRepository.findById(promotionId).orElseThrow {
            BusinessException(MessageCode.PROMOTION_NOT_FOUND)
        }

        if (LocalDateTime.now().isBefore(promotion.participationStartAt)) throw BusinessException(MessageCode.PROMOTION_NOT_STARTED)
        if (LocalDateTime.now().isAfter(promotion.participationEndAt)) throw BusinessException(MessageCode.PROMOTION_NOT_FOUND)

        // 선제적 중복 체크: unique constraint 위반 전에 fast-fail
        if (participationRepository.existsByPromotionIdAndUserIdAndParticipationDate(promotionId, userId, today)) {
            throw BusinessException(MessageCode.ALREADY_PARTICIPATED)
        }

        // 원자적 슬롯 차감 (accumulated < max 조건부 UPDATE)
        if (promotionRepository.incrementCountIfAvailable(promotionId) == 0) {
            throw BusinessException(MessageCode.PROMOTION_FULLY_BOOKED)
        }

        // saveAndFlush: Hibernate flush 지연 없이 즉시 SQL 전송
        // → DataIntegrityViolationException 즉시 감지 → @Transactional이 전체 롤백
        try {
            participationRepository.saveAndFlush(
                Participation(
                    promotionId = promotionId,
                    userId = userId,
                    participationDate = today,
                    participationStatus = ParticipationStatus.REWARD_PENDING,
                    rewardAmount = promotion.rewardAmount,
                    createdBy = userId,
                    updatedBy = userId
                )
            )
        } catch (ex: DataIntegrityViolationException) {
            throw BusinessException(MessageCode.ALREADY_PARTICIPATED)
        }
    }
}
