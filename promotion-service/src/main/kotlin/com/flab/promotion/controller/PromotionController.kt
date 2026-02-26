package com.flab.promotion.controller

import com.flab.promotion.application.ParticipationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/promotions")
class PromotionController(
    private val participationService: ParticipationService
) {
    @PostMapping("/{promotionId}/participate")
    fun participate(
        @PathVariable promotionId: Long,
        @RequestHeader("X-User-Id") userId: Long
    ): ResponseEntity<Void> {
        participationService.participate(promotionId, userId)
        return ResponseEntity.accepted().build()
    }
}
