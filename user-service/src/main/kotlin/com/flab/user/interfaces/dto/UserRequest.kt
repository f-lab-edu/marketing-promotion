package com.flab.user.interfaces.dto

import com.flab.user.domain.User
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Past
import jakarta.validation.constraints.Pattern
import java.time.LocalDate

data class SignUpRequest(

    @field:NotBlank(message = "이메일은 필수값입니다")
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    val email: String,

    @field:NotBlank(message = "이름은 필수값입니다")
    val name: String,

    @field:NotNull(message = "생년월일은 필수값입니다")
    @field:Past(message = "생년월일은 과거 날짜여야 합니다")
    val birthDate: LocalDate,

    @field:NotBlank(message = "비밀번호는 필수값입니다")
    @field:Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@\$!%*#?&])[A-Za-z\\d@\$!%*#?&]{6,20}\$",
        message = "비밀번호는 영문, 숫자, 특수문자를 포함한 6~20자여야 합니다"
    )
    val password: String
) {
    fun toUser(encodedPassword: String): User = User(
        email = email,
        name = name,
        birthDate = birthDate,
        password = encodedPassword
    )
}
