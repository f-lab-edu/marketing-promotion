package com.flab.promotion.application

import com.flab.promotion.config.exception.BusinessException
import com.flab.promotion.config.exception.MessageCode
import com.flab.promotion.domain.dto.PromotionParticipationEvent
import com.flab.promotion.infrastructure.kafka.PromotionKafkaProducer
import com.flab.promotion.infrastructure.redis.PromotionRedisRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class ParticipationService(
    private val redisRepository: PromotionRedisRepository,
    private val promotionKafkaProducer: PromotionKafkaProducer
) {
    fun participate(promotionId: Long, userId: Long) {
        val today = LocalDate.now()
        val participatedAt = LocalDateTime.now()

        // 1. 캐시에서 프로모션 정보 조회 (없으면 만료 또는 존재하지 않음)
        val cache = redisRepository.getCache(promotionId)
            ?: throw BusinessException(MessageCode.PROMOTION_NOT_FOUND)

        // 2. 참여 기간 검증
        if (participatedAt.isBefore(cache.startAt)) throw BusinessException(MessageCode.PROMOTION_NOT_STARTED)

        // 3. 중복 체크 + 슬롯 차감 (Lua 스크립트로 원자적 처리)
        when (redisRepository.checkAndDecrement(promotionId, userId, today, cache.endAt, cache.startAt)) {
            PromotionRedisRepository.RESULT_ALREADY_PARTICIPATED -> throw BusinessException(MessageCode.ALREADY_PARTICIPATED)
            PromotionRedisRepository.RESULT_NOT_FOUND            -> throw BusinessException(MessageCode.PROMOTION_NOT_FOUND)
            PromotionRedisRepository.RESULT_SOLD_OUT             -> throw BusinessException(MessageCode.PROMOTION_FULLY_BOOKED)
        }

        // 4. Kafka 비동기 발행 (실패 시 PromotionKafkaProducer 콜백에서 SREM + INCR 복구)
        promotionKafkaProducer.sendParticipation(
            PromotionParticipationEvent(
                promotionId = promotionId,
                userId = userId,
                participatedAt = participatedAt,
                startAt = cache.startAt
            )
        )
    }
}
