package com.flab.promotion.application

import com.flab.promotion.domain.dto.AvailablePromotionResponse
import com.flab.promotion.domain.dto.AvailablePromotionSearchRequest
import com.flab.promotion.domain.mapper.PromotionMapper
import com.flab.promotion.infrastructure.persistence.PromotionRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class PromotionService(
    private val promotionRepository: PromotionRepository,
    private val promotionMapper: PromotionMapper
) {
    companion object {
        private const val PAGE_SIZE = 10
    }

    @Transactional(readOnly = true)
    fun findAvailableWithHint(
        userId: Long,
        request: AvailablePromotionSearchRequest
    ): Page<AvailablePromotionResponse> {
        val pageable = PageRequest.of(request.page, PAGE_SIZE)
        return promotionRepository.findAvailablePromotionsWithHint(
            userId    = userId,
            today     = LocalDate.now(),
            now       = LocalDateTime.now(),
            name      = request.name,
            startDate = request.startDate,
            endDate   = request.endDate,
            pageable  = pageable
        ).map { promotionMapper.toAvailablePromotionResponse(it) }
    }

    @Transactional(readOnly = true)
    fun findAvailableWithQueryDsl(
        userId: Long,
        request: AvailablePromotionSearchRequest
    ): Page<AvailablePromotionResponse> {
        val pageable = PageRequest.of(request.page, PAGE_SIZE)
        return promotionRepository
            .findAvailablePromotionsWithQueryDsl(userId, request, pageable)
            .map { promotionMapper.toAvailablePromotionResponse(it) }
    }
}
