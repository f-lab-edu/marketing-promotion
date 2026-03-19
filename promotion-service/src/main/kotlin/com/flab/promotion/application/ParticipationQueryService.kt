package com.flab.promotion.application

import com.flab.promotion.domain.dto.ParticipationHistoryResponse
import com.flab.promotion.domain.dto.ParticipationHistorySearchRequest
import com.flab.promotion.infrastructure.persistence.ParticipationRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

private const val PAGE_SIZE = 10

@Service
class ParticipationQueryService(
    private val participationRepository: ParticipationRepository
) {
    fun getHistory(userId: Long, request: ParticipationHistorySearchRequest): Page<ParticipationHistoryResponse> {
        val pageable = PageRequest.of(
            request.page,
            PAGE_SIZE,
            Sort.by(Sort.Direction.DESC, "participationDate", "createdAt")
        )
        return participationRepository.findHistory(userId, request, pageable)
    }
}
