package com.flab.promotion.domain.dto

import java.time.LocalDateTime

data class PromotionParticipationEvent(
    val promotionId: Long,
    val userId: Long,
    val participatedAt: LocalDateTime,
    val startAt: LocalDateTime
)
