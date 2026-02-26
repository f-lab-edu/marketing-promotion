package com.flab.promotion.controller

import com.flab.promotion.application.AdminService
import com.flab.promotion.domain.dto.CreatePromotionRequest
import com.flab.promotion.domain.dto.CreatePromotionResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin/promotions")
class AdminController(
    private val adminService: AdminService
) {
    @PostMapping
    fun createPromotion(
        @RequestBody @Valid request: CreatePromotionRequest
    ): ResponseEntity<CreatePromotionResponse> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(adminService.createPromotion(request))
    }
}
