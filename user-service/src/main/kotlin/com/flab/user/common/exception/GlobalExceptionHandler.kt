package com.flab.user.common.exception

import com.flab.user.common.response.ApiResponse
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import java.nio.charset.StandardCharsets

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(CommonHttpException::class)
    fun handleCommonPromotionHttpException(
        exception: CommonHttpException
    ): ResponseEntity<ApiResponse<Void>> {
        val body = if (exception.customMessage != null) {
            // 커스텀 메시지가 있으면 사용
            ApiResponse.error<Void>(exception.errorCodes, exception.customMessage)
        } else {
            // 없으면 ErrorCodes의 기본 메시지 사용
            ApiResponse.error<Void>(exception.errorCodes)
        }

        val contentType = MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)

        return ResponseEntity.status(exception.httpStatus)
            .contentType(contentType)
            .body(body)
    }

    /**
     * 유효성 검사 예외 처리
     */
    @ExceptionHandler(WebExchangeBindException::class)
    fun handleWebExchangeBindException(
        exception: WebExchangeBindException
    ): ResponseEntity<ApiResponse<Void>> {
        val body = ApiResponse.error<Void>(ErrorCodes.INVALID_INPUT)
        val contentType = MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)

        return ResponseEntity.badRequest()
            .contentType(contentType)
            .body(body)
    }

    /**
     * 시스템 예외 처리
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        exception: Exception
    ): ResponseEntity<ApiResponse<Void>> {
        val body = ApiResponse.error<Void>(ErrorCodes.SYSTEM_ERROR)
        val contentType = MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)

        return ResponseEntity.internalServerError()
            .contentType(contentType)
            .body(body)
    }
}