package com.flab.promotion.domain.dto

import com.flab.promotion.domain.enums.CompletionPolicy
import com.flab.promotion.domain.enums.PolicyCode
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class CreatePromotionRequest(
    @field:NotNull val adminId: Long,

    @field:NotBlank val name: String,
    @field:NotBlank val slogan: String,
    @field:NotBlank val contentUrl: String,
    @field:NotBlank val imageUrl: String,

    @field:NotNull @field:Min(1) val maxParticipationCount: Int,
    @field:NotNull @field:Min(0) val rewardAmount: Long,

    @field:NotNull val participationStartAt: LocalDateTime,
    @field:NotNull val participationEndAt: LocalDateTime,

    @field:NotNull val completionPolicy: CompletionPolicy,

    // D_PLUS_N 정책 사용 필드
    val contentDeadlineDays: Int? = null,

    // FROM_TO 정책 사용 필드
    val contentStartAt: LocalDateTime? = null,
    val contentEndAt: LocalDateTime? = null,

    @field:Valid val policies: List<CreatePolicyRequest> = emptyList()
)

data class CreatePolicyRequest(
    @field:NotNull val policyCode: PolicyCode,

    // PREREQUISITE_PROMOTION 정책 사용 필드
    val prerequisitePromotionId: Long? = null,

    // PERSONAL_PARTICIPATION_LIMIT 정책 사용 필드
    @field:Min(1) val personalParticipationLimit: Int? = null
)
