package com.flab.user.controller

import com.flab.user.application.AdminService
import com.flab.user.domain.dto.AdminCommonRequest
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/admin/users")
class AdminController(
    private val adminService: AdminService
) {
    @PatchMapping("/{userId}/suspend")
    @Operation(summary = "사용자 정지 API", description = "사용자를 정지하고 모든 활성 토큰을 무효화합니다.")
    fun suspendUser(
        @PathVariable userId: Long,
        @RequestBody @Valid request: AdminCommonRequest
    ): ResponseEntity<Void> {
        adminService.suspendUser(userId, request.adminId)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{userId}/unsuspend")
    @Operation(summary = "사용자 정지 해제 API", description = "정지된 사용자를 다시 활성화합니다.")
    fun unsuspendUser(
        @PathVariable userId: Long,
        @RequestBody @Valid request: AdminCommonRequest
    ): ResponseEntity<Void> {
        adminService.unsuspendUser(userId, request.adminId)
        return ResponseEntity.noContent().build()
    }
}
