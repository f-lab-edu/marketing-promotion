package com.flab.promotion.application

import com.flab.promotion.config.exception.BusinessException
import com.flab.promotion.config.exception.MessageCode
import com.flab.promotion.domain.dto.CreatePromotionRequest
import com.flab.promotion.domain.dto.CreatePromotionResponse
import com.flab.promotion.domain.enums.CompletionPolicy
import com.flab.promotion.domain.enums.PolicyCode
import com.flab.promotion.domain.mapper.PromotionMapper
import com.flab.promotion.infrastructure.persistence.ParticipationPolicyRepository
import com.flab.promotion.infrastructure.persistence.PromotionRepository
import com.flab.promotion.infrastructure.redis.PromotionRedisRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminService(
    private val promotionRepository: PromotionRepository,
    private val participationPolicyRepository: ParticipationPolicyRepository,
    private val redisRepository: PromotionRedisRepository,
    private val promotionMapper: PromotionMapper
) {
    @Transactional
    fun createPromotion(request: CreatePromotionRequest): CreatePromotionResponse {
        validateRequest(request)

        val promotion = promotionMapper.toPromotion(request)
        val saved = promotionRepository.save(promotion)

        // 연관관계 없이 promotionId FK 로 정책을 직접 저장
        request.policies.forEach { policyRequest ->
            validatePolicyRequest(policyRequest.policyCode, policyRequest.prerequisitePromotionId, policyRequest.personalParticipationLimit)
            participationPolicyRepository.save(
                promotionMapper.toParticipationPolicy(policyRequest, request.adminId, saved.id!!)
            )
        }

        redisRepository.register(
            promotionId = saved.id!!,
            startAt = saved.participationStartAt,
            maxCount = saved.maxParticipationCount,
            endAt = saved.participationEndAt,
            rewardAmount = saved.rewardAmount
        )

        return CreatePromotionResponse(promotionId = saved.id!!)
    }

    private fun validateRequest(request: CreatePromotionRequest) {
        if (request.participationEndAt.isBefore(request.participationStartAt)) {
            throw BusinessException(MessageCode.DATA_INTEGRITY_VIOLATION_EXCEPTION, "참여 마감일시는 시작일시 이후여야 합니다")
        }
        when (request.completionPolicy) {
            CompletionPolicy.D_PLUS_N -> {
                if (request.contentDeadlineDays == null || request.contentDeadlineDays < 1) {
                    throw BusinessException(MessageCode.DATA_INTEGRITY_VIOLATION_EXCEPTION, "D+N 정책은 컨텐츠 마감기한(일수)이 필요합니다")
                }
            }
            CompletionPolicy.FROM_TO -> {
                if (request.contentStartAt == null || request.contentEndAt == null) {
                    throw BusinessException(MessageCode.DATA_INTEGRITY_VIOLATION_EXCEPTION, "FROM_TO 정책은 컨텐츠 시작일시와 마감일시가 필요합니다")
                }
                if (request.contentEndAt.isBefore(request.contentStartAt)) {
                    throw BusinessException(MessageCode.DATA_INTEGRITY_VIOLATION_EXCEPTION, "컨텐츠 마감일시는 시작일시 이후여야 합니다")
                }
            }
        }
    }

    private fun validatePolicyRequest(policyCode: PolicyCode, prerequisitePromotionId: Long?, personalParticipationLimit: Int?) {
        when (policyCode) {
            PolicyCode.PREREQUISITE_PROMOTION -> {
                if (prerequisitePromotionId == null) {
                    throw BusinessException(MessageCode.DATA_INTEGRITY_VIOLATION_EXCEPTION, "선행 프로모션 정책은 선행 프로모션 ID가 필요합니다")
                }
            }
            PolicyCode.PERSONAL_PARTICIPATION_LIMIT -> {
                if (personalParticipationLimit == null || personalParticipationLimit < 1) {
                    throw BusinessException(MessageCode.DATA_INTEGRITY_VIOLATION_EXCEPTION, "개인 참여횟수 제한 정책은 허용 횟수가 필요합니다")
                }
            }
        }
    }
}
