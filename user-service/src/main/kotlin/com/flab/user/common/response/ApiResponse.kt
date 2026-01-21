package com.flab.user.common.response

import com.fasterxml.jackson.annotation.JsonInclude
import com.flab.user.common.exception.ErrorCodes

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val data: T? = null,
    val error: ApiErrorResponse? = null
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> {
            return ApiResponse(data = data, error = null)
        }

        /**
         * ErrorCodes의 기본 메시지 사용
         */
        fun <T> error(errorCodes: ErrorCodes): ApiResponse<T> {
            return ApiResponse(data = null, error = ApiErrorResponse(
                message = errorCodes.message,
                code = errorCodes.code
            ))
        }

        /**
         * 커스텀 메시지 사용 (동적 메시지)
         */
        fun <T> error(errorCodes: ErrorCodes, customMessage: String): ApiResponse<T> {
            return ApiResponse(data = null, error = ApiErrorResponse(
                message = customMessage,
                code = errorCodes.code
            ))
        }
    }
}
