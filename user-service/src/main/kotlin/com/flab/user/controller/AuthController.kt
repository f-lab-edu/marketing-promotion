package com.flab.user.controller

import com.flab.user.application.AuthService
import com.flab.user.config.exception.BusinessException
import com.flab.user.config.exception.MessageCode
import com.flab.user.domain.dto.LoginRequest
import com.flab.user.domain.dto.LoginResponse
import com.flab.user.domain.dto.TokenRefreshResponse
import com.flab.user.domain.dto.TokenValidationResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/auth")
class AuthController(
    private val authService: AuthService
) {
    companion object {
        private const val REFRESH_TOKEN_COOKIE = "refresh_token"
        private const val COOKIE_PATH = "/v1/auth"
    }

    @PostMapping("/login")
    @Operation(summary = "로그인 API", description = "이메일과 비밀번호로 로그인하여 액세스 토큰을 발급합니다. 리프레시 토큰은 HttpOnly Cookie로 설정됩니다.")
    fun login(
        @Valid @RequestBody @Parameter(description = "로그인 정보") request: LoginRequest,
        httpRequest: HttpServletRequest,
        httpResponse: HttpServletResponse
    ): ResponseEntity<LoginResponse> {
        val ip = httpRequest.remoteAddr
        val userAgent = httpRequest.getHeader("User-Agent")
        val result = authService.login(request, ip, userAgent)

        addRefreshTokenCookie(httpResponse, result.refreshToken, result.refreshMaxAgeSeconds.toInt())

        return ResponseEntity.ok(
            LoginResponse(
                accessToken = result.accessToken,
                expiresIn = result.accessExpiresIn
            )
        )
    }

    @GetMapping("/validate")
    @Operation(summary = "토큰 검증 API", description = "액세스 토큰의 유효성을 검증합니다 (Redis only).")
    fun validateToken(
        @RequestHeader("Authorization") @Parameter(hidden = true) authorization: String
    ): ResponseEntity<TokenValidationResponse> {
        val token = authorization.removePrefix("Bearer ")
        val response = authService.validateToken(token)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃 API", description = "액세스 토큰과 리프레시 토큰을 무효화합니다.")
    fun logout(
        @RequestHeader("Authorization") @Parameter(hidden = true) authorization: String,
        @CookieValue(REFRESH_TOKEN_COOKIE) @Parameter(hidden = true) refreshToken: String,
        httpRequest: HttpServletRequest,
        httpResponse: HttpServletResponse
    ): ResponseEntity<Void> {
        val accessToken = authorization.removePrefix("Bearer ")
        val ip = httpRequest.remoteAddr
        val userAgent = httpRequest.getHeader("User-Agent")
        authService.logout(accessToken, refreshToken, ip, userAgent)

        clearRefreshTokenCookie(httpResponse)

        return ResponseEntity.noContent().build()
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신 API", description = "리프레시 토큰(Cookie)으로 새 액세스 토큰을 발급합니다. (최대 4회)")
    fun refreshToken(
        @CookieValue(REFRESH_TOKEN_COOKIE, required = false) @Parameter(hidden = true) refreshToken: String?
    ): ResponseEntity<TokenRefreshResponse> {
        if (refreshToken.isNullOrBlank()) {
            throw BusinessException(MessageCode.INVALIDATE_REFRESH_TOKEN)
        }
        val response = authService.refreshToken(refreshToken)
        return ResponseEntity.ok(response)
    }

    // secure=false: 운영 환경에서는 HTTPS 적용 후 secure=true로 변경 필요
    private fun addRefreshTokenCookie(response: HttpServletResponse, refreshToken: String, maxAgeSeconds: Int) {
        val cookie = Cookie(REFRESH_TOKEN_COOKIE, refreshToken)
        cookie.isHttpOnly = true
        cookie.secure = false
        cookie.path = COOKIE_PATH
        cookie.maxAge = maxAgeSeconds
        cookie.setAttribute("SameSite", "Strict")
        response.addCookie(cookie)
    }

    private fun clearRefreshTokenCookie(response: HttpServletResponse) {
        val cookie = Cookie(REFRESH_TOKEN_COOKIE, "")
        cookie.isHttpOnly = true
        cookie.secure = false
        cookie.path = COOKIE_PATH
        cookie.maxAge = 0
        cookie.setAttribute("SameSite", "Strict")
        response.addCookie(cookie)
    }
}
