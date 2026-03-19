package com.flab.user.config.handler

import com.flab.user.config.exception.BusinessException
import com.flab.user.config.exception.ErrorResponse
import com.flab.user.config.exception.MessageCode
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.MethodArgumentNotValidException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = KotlinLogging.logger {}

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<Any> {
        val code = e.messageCode
        var message = code.message
        if (e.customMessage != null) message += " (${e.customMessage})"
        logger.error(e) { "BusinessException: $message" }
        return ResponseEntity(ErrorResponse(code.code, message), code.status)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<Any> {
        val code = MessageCode.METHOD_ARGUMENT_NOT_VALID_EXCEPTION
        logger.error(e) { "MethodArgumentNotValidException: ${code.message}" }
        return ResponseEntity(ErrorResponse(code.code, code.message), code.status)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(e: Exception): ResponseEntity<Any> {
        val code = MessageCode.UNKNOWN_EXCEPTION
        logger.error(e) { "Exception: ${e.message}" }
        return ResponseEntity(ErrorResponse(code.code, code.message), code.status)
    }
}
