package com.flab.promotion.config.exception

import org.springframework.http.HttpStatus

enum class MessageCode(
    val code: String,
    val message: String,
    val status: HttpStatus
) {
    UNKNOWN_EXCEPTION("UNKNOWN_EXCEPTION", "알 수 없는 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR)
    , METHOD_ARGUMENT_NOT_VALID_EXCEPTION("METHOD_ARGUMENT_NOT_VALID_EXCEPTION", "잘못된 데이터입력", HttpStatus.BAD_REQUEST)
    , DATA_INTEGRITY_VIOLATION_EXCEPTION("DATA_INTEGRITY_VIOLATION_EXCEPTION", "데이터 오류", HttpStatus.BAD_REQUEST)
    , PROMOTION_NOT_FOUND("PROMOTION_NOT_FOUND", "존재하지 않거나 종료된 프로모션입니다", HttpStatus.NOT_FOUND)
    , PROMOTION_FULLY_BOOKED("PROMOTION_FULLY_BOOKED", "참여 가능한 횟수를 초과했습니다", HttpStatus.CONFLICT)
    , ALREADY_PARTICIPATED("ALREADY_PARTICIPATED", "이미 참여한 프로모션입니다", HttpStatus.CONFLICT)
    , PROMOTION_NOT_STARTED("PROMOTION_NOT_STARTED", "아직 참여 가능한 기간이 아닙니다", HttpStatus.BAD_REQUEST)
    , PARTICIPATION_NOT_FOUND("PARTICIPATION_NOT_FOUND", "참여 이력이 없거나 이미 완료된 참여입니다", HttpStatus.NOT_FOUND)
    , CONTENT_DEADLINE_EXPIRED("CONTENT_DEADLINE_EXPIRED", "콘텐츠 제출 기한이 초과되었습니다", HttpStatus.BAD_REQUEST)
    , CONTENT_SUBMISSION_PERIOD_INVALID("CONTENT_SUBMISSION_PERIOD_INVALID", "콘텐츠 제출 가능한 기간이 아닙니다", HttpStatus.BAD_REQUEST)
    , KAFKA_PUBLISH_FAILED("KAFKA_PUBLISH_FAILED", "참여 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요", HttpStatus.SERVICE_UNAVAILABLE)
}
