package com.flab.user.config.handler

import com.flab.user.config.exception.BusinessException
import com.flab.user.config.exception.MessageCode
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.MethodArgumentNotValidException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = KotlinLogging.logger {}

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<Any> {
        logger.error(e) { "BusinessException 발생" }

        val code: MessageCode = e.messageCode
        var message: String = code.message
        val status: HttpStatus = code.status
        if (e.customMessage != null) message += " ($e.customMessage)"

        return ResponseEntity(message, status)
    }

    /**
     * 유효성 검사 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<Any> {
        logger.error(e) { "유효성 검사 예외 발생" }
        return ResponseEntity(MessageCode.METHOD_ARGUMENT_NOT_VALID_EXCEPTION.message
                            , MessageCode.METHOD_ARGUMENT_NOT_VALID_EXCEPTION.status)
    }

    /**
     * 시스템 예외 처리
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(e: Exception): ResponseEntity<Any> {
        logger.error(e) { "시스템 예외 발생" }
        return ResponseEntity(MessageCode.UNKNOWN_EXCEPTION.message
                            , MessageCode.UNKNOWN_EXCEPTION.status)
    }
}
