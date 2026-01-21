package com.flab.user.common.exception

import org.springframework.http.HttpStatus

class CommonHttpException : RuntimeException {
    val errorCodes: ErrorCodes
    val httpStatus: HttpStatus
    val customMessage: String?

    /**
     * 기본 메시지 사용 (ErrorCodes의 message 사용)
     */
    constructor(
        errorCodes: ErrorCodes,
        httpStatus: HttpStatus
    ) : super(errorCodes.message) {
        this.errorCodes = errorCodes
        this.httpStatus = httpStatus
        this.customMessage = null
    }

    /**
     * 커스텀 메시지 사용 (동적 메시지 전달)
     */
    constructor(
        errorCodes: ErrorCodes,
        httpStatus: HttpStatus,
        customMessage: String
    ) : super(customMessage) {
        this.errorCodes = errorCodes
        this.httpStatus = httpStatus
        this.customMessage = customMessage
    }
}
