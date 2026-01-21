package com.flab.user.common.exception

enum class ErrorCodes(
    val message: String,
    val code: Long
) {
    INVALID_INPUT("유효하지 않은 입력값입니다.", 100L),
    DUPLICATED_EMAIL("이미 사용중인 이메일입니다.", 200L),
    // 999: 시스템 오류
    SYSTEM_ERROR("시스템 오류", 999L)
}