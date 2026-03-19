package com.flab.promotion.application

import com.flab.promotion.config.exception.BusinessException
import com.flab.promotion.config.exception.MessageCode
import com.flab.promotion.domain.enums.CompletionPolicy
import com.flab.promotion.domain.mapper.ParticipationMapper
import com.flab.promotion.infrastructure.persistence.ParticipationHistoryRepository
import com.flab.promotion.infrastructure.persistence.ParticipationRepository
import com.flab.promotion.infrastructure.persistence.PromotionRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.LocalTime

@Service
class ParticipationCompletionService(
    private val participationRepository: ParticipationRepository,
    private val promotionRepository: PromotionRepository,
    private val participationHistoryRepository: ParticipationHistoryRepository,
    private val participationMapper: ParticipationMapper
) {
    private val logger = KotlinLogging.logger {}

    /**
     * 참여 완료 처리
     *
     * 상태 전이: PROCESSING → REWARD_PENDING
     *
     * 1. participationId + userId 로 참여 이력 조회 (타인의 참여 완료 방지)
     * 2. 현재 상태가 PROCESSING 인지 확인
     * 3. 프로모션의 completionPolicy 에 따라 콘텐츠 제출 기간 검증
     * 4. 상태 변경 후 이력 저장
     */
    @Transactional
    fun complete(participationId: Long, userId: Long) {
        val now = LocalDateTime.now()

        // 1. 참여 이력 조회 (userId 검증 포함)
        val participation = participationRepository.findByIdAndUserId(participationId, userId)
            ?: throw BusinessException(MessageCode.PARTICIPATION_NOT_FOUND)

        // 2. 완료 처리 가능한 상태인지 확인
        if (participation.participationStatus != ParticipationStatus.PROCESSING) {
            throw BusinessException(MessageCode.PARTICIPATION_NOT_FOUND)
        }

        // 3. 프로모션 조회 → completionPolicy 검증
        val promotion = promotionRepository.findById(participation.promotionId)
            .orElseThrow { BusinessException(MessageCode.PROMOTION_NOT_FOUND) }

        when (promotion.completionPolicy) {
            CompletionPolicy.D_PLUS_N -> {
                // 참여일 + N일 자정까지 제출 가능
                val deadline = participation.participationDate
                    .plusDays(promotion.contentDeadlineDays!!.toLong())
                    .atTime(LocalTime.MAX)
                if (now.isAfter(deadline)) {
                    throw BusinessException(MessageCode.CONTENT_DEADLINE_EXPIRED)
                }
            }
            CompletionPolicy.FROM_TO -> {
                // 고정된 콘텐츠 제출 기간 내에만 가능
                val contentStartAt = promotion.contentStartAt!!
                val contentEndAt   = promotion.contentEndAt!!
                if (now.isBefore(contentStartAt) || now.isAfter(contentEndAt)) {
                    throw BusinessException(MessageCode.CONTENT_SUBMISSION_PERIOD_INVALID)
                }
            }
        }

        // 4. 상태 전이: PROCESSING → REWARD_PENDING
        participation.participationStatus = ParticipationStatus.REWARD_PENDING
        participation.updatedBy = userId

        participationHistoryRepository.save(
            participationMapper.toParticipationHistory(participation, userId)
        )

        logger.info { "참여 완료 처리 - participationId=$participationId, userId=$userId" }
    }
}
