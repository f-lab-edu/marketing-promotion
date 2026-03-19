package com.flab.promotion.controller

import com.flab.promotion.application.ParticipationCompletionService
import com.flab.promotion.application.ParticipationQueryService
import com.flab.promotion.domain.dto.ParticipationHistoryResponse
import com.flab.promotion.domain.dto.ParticipationHistorySearchRequest
import com.flab.promotion.domain.enums.ParticipationStatus
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/v1/participations")
class ParticipationController(
    private val participationCompletionService: ParticipationCompletionService,
    private val participationQueryService: ParticipationQueryService
) {
    /**
     * 내 참여이력 조회
     *
     * GET /v1/participations/history?fromDate=2024-01-01&toDate=2024-12-31&status=REWARD_COMPLETED&page=0
     * X-User-Id: {userId}
     *
     * - 참여일자 역순 정렬
     * - 10개씩 페이징
     * - fromDate, toDate, status 는 모두 선택 조건
     */
    @GetMapping("/history")
    fun getHistory(
        @RequestHeader("X-User-Id") userId: Long,
        @RequestParam(required = false) fromDate: LocalDate?,
        @RequestParam(required = false) toDate: LocalDate?,
        @RequestParam(required = false) status: ParticipationStatus?,
        @RequestParam(defaultValue = "0") page: Int
    ): ResponseEntity<Page<ParticipationHistoryResponse>> {
        val request = ParticipationHistorySearchRequest(fromDate, toDate, status, page)
        return ResponseEntity.ok(participationQueryService.getHistory(userId, request))
    }

    @PostMapping("/{participationId}/complete")
    fun complete(
        @PathVariable participationId: Long,
        @RequestHeader("X-User-Id") userId: Long
    ): ResponseEntity<Void> {
        participationCompletionService.complete(participationId, userId)
        return ResponseEntity.ok().build()
    }
}
