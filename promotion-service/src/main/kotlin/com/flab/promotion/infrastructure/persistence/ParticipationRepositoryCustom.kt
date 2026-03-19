package com.flab.promotion.infrastructure.persistence

import com.flab.promotion.domain.dto.ParticipationHistoryResponse
import com.flab.promotion.domain.dto.ParticipationHistorySearchRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ParticipationRepositoryCustom {
    fun findHistory(userId: Long, request: ParticipationHistorySearchRequest, pageable: Pageable): Page<ParticipationHistoryResponse>
}
