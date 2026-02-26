package com.flab.promotion.config.exception

class BusinessException : RuntimeException {
    val messageCode: MessageCode
    val customMessage: String?

    constructor(
        messageCode: MessageCode
    ) : super(messageCode.message) {
        this.messageCode = messageCode
        this.customMessage = null
    }

    constructor(
        messageCode: MessageCode,
        customMessage: String
    ) : super(customMessage) {
        this.messageCode = messageCode
        this.customMessage = customMessage
    }
}
