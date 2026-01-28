package com.flab.user.controller

import com.flab.user.application.UserService
import com.flab.user.domain.dto.SignUpRequest
import com.flab.user.domain.dto.SignUpResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/users")
class UserController(
    private val userService: UserService
) {
    @PostMapping
    @Operation(summary = "회원가입 API", description = "고객정보 기반 회원가입을 진행합니다.")
    fun signUp(httpRequest: HttpServletRequest,
               @Valid @RequestBody @Parameter(description = "등록할 고객 정보") request: SignUpRequest): ResponseEntity<SignUpResponse> {
        val response = userService.signUp(request)
        return ResponseEntity(response, HttpStatus.CREATED)
    }
}
