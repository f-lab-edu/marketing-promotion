package com.flab.promotion.application

import com.flab.promotion.config.exception.BusinessException
import com.flab.promotion.config.exception.MessageCode
import com.flab.promotion.domain.dto.PromotionParticipationEvent
import com.flab.promotion.domain.entity.persistence.Participation
import com.flab.promotion.domain.entity.persistence.ParticipationHistory
import com.flab.promotion.domain.enums.ParticipationStatus
import com.flab.promotion.domain.enums.PolicyCode
import com.flab.promotion.infrastructure.persistence.ParticipationHistoryRepository
import com.flab.promotion.infrastructure.persistence.ParticipationPolicyRepository
import com.flab.promotion.infrastructure.persistence.ParticipationRepository
import com.flab.promotion.infrastructure.persistence.PromotionRepository
import com.flab.promotion.infrastructure.redis.PromotionRedisRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ParticipationProcessingService(
    private val promotionRepository: PromotionRepository,
    private val participationPolicyRepository: ParticipationPolicyRepository,
    private val participationRepository: ParticipationRepository,
    private val participationHistoryRepository: ParticipationHistoryRepository,
    private val promotionRedisRepository: PromotionRedisRepository
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Transactional
    fun process(event: PromotionParticipationEvent) {
        val participationDate = event.participatedAt.toLocalDate()

        // 1. 멱등성 체크: 이미 처리된 참여이면 원복 없이 그냥 종료
        if (participationRepository.existsByPromotionIdAndUserIdAndParticipationDate(
                event.promotionId, event.userId, participationDate
            )
        ) {
            logger.info { "이미 처리된 참여 - promotionId=${event.promotionId}, userId=${event.userId}, date=$participationDate" }
            return
        }

        // 2. 프로모션 조회
        val promotion = promotionRepository.findById(event.promotionId)
            .orElseThrow { BusinessException(MessageCode.PROMOTION_NOT_FOUND) }

        // 3. 참여 정책 검증 - 실패 시 Redis 참여가능횟수 원복 후 종료
        val policies = participationPolicyRepository.findByPromotion_Id(event.promotionId)
        for (policy in policies) {
            when (policy.policyCode) {
                PolicyCode.PERSONAL_PARTICIPATION_LIMIT -> {
                    val limit = policy.personalParticipationLimit ?: continue
                    val count = participationRepository.countByPromotionIdAndUserId(event.promotionId, event.userId)
                    if (count >= limit) {
                        logger.warn { "개인 참여횟수 초과 - promotionId=${event.promotionId}, userId=${event.userId}, count=$count, limit=$limit" }
                        promotionRedisRepository.rollbackCount(event.promotionId, event.startAt)
                        return
                    }
                }
                PolicyCode.PREREQUISITE_PROMOTION -> {
                    val prerequisitePromotionId = policy.prerequisitePromotionId ?: continue
                    val hasParticipated = participationRepository.existsByPromotionIdAndUserId(
                        prerequisitePromotionId, event.userId
                    )
                    if (!hasParticipated) {
                        logger.warn { "선행 프로모션 미참여 - promotionId=${event.promotionId}, userId=${event.userId}, prerequisiteId=$prerequisitePromotionId" }
                        promotionRedisRepository.rollbackCount(event.promotionId, event.startAt)
                        return
                    }
                }
            }
        }

        // 4. 참여 저장 (진행중)
        val participation = participationRepository.save(
            Participation(
                promotionId = event.promotionId,
                userId = event.userId,
                participationDate = participationDate,
                participationStatus = ParticipationStatus.PROCESSING,
                rewardAmount = promotion.rewardAmount,
                createdBy = event.userId,
                updatedBy = event.userId
            )
        )

        // 5. 참여 이력 저장 (진행중)
        participationHistoryRepository.save(
            ParticipationHistory(
                participationId = participation.id!!,
                participationStatus = ParticipationStatus.PROCESSING,
                createdBy = event.userId,
                updatedBy = event.userId
            )
        )

        // 6. 프로모션 원장 누적 참여횟수 증가 (더티체킹)
        promotion.accumulatedParticipationCount++

        logger.info { "참여 처리 완료 - promotionId=${event.promotionId}, userId=${event.userId}, participationId=${participation.id}" }
    }
}
