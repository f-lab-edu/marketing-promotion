package com.flab.user.config.exception

import org.springframework.http.HttpStatus

enum class MessageCode(
    val code: String,
    val message: String,
    val status: HttpStatus
) {

    UNKNOWN_EXCEPTION("UNKNOWN_EXCEPTION", "알 수 없는 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR)
    , METHOD_ARGUMENT_NOT_VALID_EXCEPTION("METHOD_ARGUMENT_NOT_VALID_EXCEPTION", "잘못된 데이터입력", HttpStatus.BAD_REQUEST)
    , DUPLICATED_EMAIL("DUPLICATED_EMAIL","이미 사용중인 이메일입니다.", HttpStatus.CONFLICT)

    , USER_NOT_FOUND("USER_NOT_FOUND_EXCEPTION", "사용자가 없음", HttpStatus.NOT_FOUND)
    , DATA_INTEGRITY_VIOLATION_EXCEPTION("DATA_INTEGRITY_VIOLATION_EXCEPTION", "데이터 오류", HttpStatus.BAD_REQUEST)
    , BAD_CREDENTIAL_EXCEPTION("BAD_CREDENTIAL_EXCEPTION", "잘못된 회원정보", HttpStatus.UNAUTHORIZED)
    , INVALIDATE_CLAIMS_EXCEPTION("INVALIDATE_CLAIMS_EXCEPTION", "인증 오류", HttpStatus.UNAUTHORIZED)
    , INVALIDATE_REFRESH_TOKEN("INVALIDATE_REFRESH_TOKEN_EXCEPTION", "유효하지 않는 리프레시 토큰", HttpStatus.UNAUTHORIZED)
    , TOKEN_REVOKED("TOKEN_REVOKED", "토큰이 만료되었거나 취소되었습니다", HttpStatus.UNAUTHORIZED)
    , USER_SUSPENDED("USER_SUSPENDED", "정지된 사용자입니다", HttpStatus.UNAUTHORIZED)
    , USER_ALREADY_SUSPENDED("USER_ALREADY_SUSPENDED", "이미 정지된 사용자입니다", HttpStatus.CONFLICT)
    , REFRESH_LIMIT_EXCEEDED("REFRESH_LIMIT_EXCEEDED", "토큰 갱신 횟수를 초과했습니다", HttpStatus.TOO_MANY_REQUESTS)
    , USER_ALREADY_WITHDRAWN("USER_ALREADY_WITHDRAWN", "이미 탈퇴한 사용자입니다", HttpStatus.CONFLICT)
    , USER_WITHDRAWN("USER_WITHDRAWN", "탈퇴한 사용자입니다", HttpStatus.UNAUTHORIZED)
    , LOGIN_ATTEMPT_EXCEEDED("LOGIN_ATTEMPT_EXCEEDED", "로그인 5회 실패로 계정이 정지되었습니다", HttpStatus.UNAUTHORIZED)
}