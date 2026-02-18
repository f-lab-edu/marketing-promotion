package com.flab.user.domain.dto

import jakarta.validation.constraints.NotNull

data class AdminCommonRequest(
    @field:NotNull(message = "관리자 ID는 필수값입니다")
    val adminId: Long
)
