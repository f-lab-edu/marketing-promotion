package com.flab.user.application.service

import com.flab.user.application.UserService
import com.flab.user.config.exception.BusinessException
import com.flab.user.config.exception.MessageCode
import com.flab.user.domain.dto.SignUpRequest
import com.flab.user.infrastructure.persistence.UserRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceTest(
    private val userService: UserService,
    private val userRepository: UserRepository
) : StringSpec({

    extension(SpringExtension)

    "회원가입 성공" {
        // given
        val request = SignUpRequest(
            email = "test@test.com",
            name = "홍길동",
            birthDate = LocalDate.of(1990, 1, 1),
            password = "Password1!"
        )

        // when
        val response = userService.signUp(request)

        // then
        response.userId shouldNotBe null

        val savedUser = userRepository.findById(response.userId).orElse(null)
        savedUser shouldNotBe null
        savedUser.email shouldBe request.email
        savedUser.name shouldBe request.name
        savedUser.birthDate shouldBe request.birthDate
    }

    "이메일 중복 시 예외 발생" {
        // given
        val request = SignUpRequest(
            email = "duplicate@test.com",
            name = "홍길동",
            birthDate = LocalDate.of(1990, 1, 1),
            password = "Password1!"
        )
        userService.signUp(request)

        val duplicateRequest = SignUpRequest(
            email = "duplicate@test.com",
            name = "김철수",
            birthDate = LocalDate.of(1995, 5, 5),
            password = "Password2!"
        )

        // when & then
        val exception = shouldThrow<BusinessException> {
            userService.signUp(duplicateRequest)
        }

        exception.messageCode shouldBe MessageCode.DUPLICATED_EMAIL
    }

    "회원가입 시 비밀번호가 암호화되어 저장된다" {
        // given
        val rawPassword = "Password1!"
        val request = SignUpRequest(
            email = "encrypt@test.com",
            name = "암호화테스트",
            birthDate = LocalDate.of(1990, 1, 1),
            password = rawPassword
        )

        // when
        val response = userService.signUp(request)

        // then
        val savedUser = userRepository.findById(response.userId).orElse(null)
        savedUser.password shouldNotBe rawPassword
    }
})
