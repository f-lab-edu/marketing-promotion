package com.flab.user.domain.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Past
import jakarta.validation.constraints.Pattern
import java.time.LocalDate

data class SignUpRequest(

    @Schema(description = "이메일", example = "kildong@flab.com")
    @field:NotBlank(message = "이메일은 필수값입니다")
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    val email: String,

    @Schema(description = "이름", example = "홍길동")
    @field:NotBlank(message = "이름은 필수값입니다")
    val name: String,

    @Schema(description = "생년월일", example = "1990-01-01")
    @field:NotNull(message = "생년월일은 필수값입니다")
    @field:Past(message = "생년월일은 과거 날짜여야 합니다")
    val birthDate: LocalDate,

    @Schema(description = "비밀번호")
    @field:NotBlank(message = "비밀번호는 필수값입니다")
    @field:Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@\$!%*#?&])[A-Za-z\\d@\$!%*#?&]{6,20}\$",
        message = "비밀번호는 영문, 숫자, 특수문자를 포함한 6~20자여야 합니다"
    )
    val password: String
)
