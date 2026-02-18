package com.flab.user.controller

import com.flab.user.application.UserService
import com.flab.user.domain.dto.SignUpRequest
import com.flab.user.domain.dto.SignUpResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
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

    @DeleteMapping("/me")
    @Operation(summary = "회원 탈퇴 API", description = "본인 계정을 탈퇴 처리하고 모든 토큰을 무효화합니다.")
    fun withdraw(
        @RequestHeader("Authorization") @Parameter(description = "Bearer 토큰") authorization: String,
        httpRequest: HttpServletRequest,
        httpResponse: HttpServletResponse
    ): ResponseEntity<Void> {
        val accessToken = authorization.removePrefix("Bearer ")
        val ip = httpRequest.remoteAddr
        val userAgent = httpRequest.getHeader("User-Agent")

        userService.withdraw(accessToken, ip, userAgent)

        clearRefreshTokenCookie(httpResponse)

        return ResponseEntity.noContent().build()
    }

    private fun clearRefreshTokenCookie(httpResponse: HttpServletResponse) {
        val cookie = Cookie("refresh_token", "")
        cookie.isHttpOnly = true
        cookie.secure = false
        cookie.path = "/v1/auth"
        cookie.maxAge = 0
        cookie.setAttribute("SameSite", "Strict")
        httpResponse.addCookie(cookie)
    }
}
