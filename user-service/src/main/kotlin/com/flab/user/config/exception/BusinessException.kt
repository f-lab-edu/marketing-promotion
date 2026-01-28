package com.flab.user.config.exception

import org.springframework.http.HttpStatus

class BusinessException : RuntimeException {
    val messageCode: MessageCode
    val customMessage: String?

    /**
     * 기본 메시지 사용 (ErrorCodes의 message 사용)
     */
    constructor(
        messageCode: MessageCode
    ) : super(messageCode.message) {
        this.messageCode = messageCode
        this.customMessage = null
    }

    /**
     * 커스텀 메시지 사용 (동적 메시지 전달)
     */
    constructor(
        messageCode: MessageCode,
        customMessage: String
    ) : super(customMessage) {
        this.messageCode = messageCode
        this.customMessage = customMessage
    }
}
