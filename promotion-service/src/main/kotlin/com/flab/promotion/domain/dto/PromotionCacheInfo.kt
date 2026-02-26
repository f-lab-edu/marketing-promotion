package com.flab.promotion.domain.dto

import java.time.LocalDateTime

data class PromotionCacheInfo(
    val maxCount: Int,
    val startAt: LocalDateTime,
    val endAt: LocalDateTime
)
