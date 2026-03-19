package com.flab.promotion.infrastructure.persistence

import com.flab.promotion.domain.dto.AvailablePromotionSearchRequest
import com.flab.promotion.domain.entity.persistence.Promotion
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface PromotionRepositoryCustom {
    fun findAvailablePromotionsWithQueryDsl(
        userId: Long,
        request: AvailablePromotionSearchRequest,
        pageable: Pageable
    ): Page<Promotion>
}
