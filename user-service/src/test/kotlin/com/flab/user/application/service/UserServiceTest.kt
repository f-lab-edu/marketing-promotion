package com.flab.user.application.service

import com.flab.user.common.exception.DuplicatedEmailException
import com.flab.user.domain.User
import com.flab.user.domain.UserStatus
import com.flab.user.infrastructure.persistence.UserRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.security.crypto.password.PasswordEncoder

class UserServiceTest : StringSpec({

    val userRepository = mockk<UserRepository>()
    val passwordEncoder = mockk<PasswordEncoder>()
    val userService = UserService(userRepository, passwordEncoder)

    beforeTest {
        clearAllMocks()
    }

    /*"회원가입 성공" {
        // given
        val email = "test@test.com"
        val name = "홍길동"
        val password = "password123"
        val encodedPassword = "encodedPassword123"

        every { userRepository.existsByEmail(email) } returns false
        every { passwordEncoder.encode(password) } returns encodedPassword
        every { userRepository.save(any()) } answers {
            val user = firstArg<User>()
            User(
                id = 1L,
                email = user.email,
                name = user.name,
                password = user.password,
                status = user.status,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt
            )
        }

        // when
        val result = userService.signUp(email, name, password)

        // then
        result.id shouldNotBe null
        result.email shouldBe email
        result.name shouldBe name
        result.password shouldBe encodedPassword
        result.status shouldBe UserStatus.ACTIVE

        verify(exactly = 1) { userRepository.existsByEmail(email) }
        verify(exactly = 1) { passwordEncoder.encode(password) }
        verify(exactly = 1) { userRepository.save(any()) }
    }

    "이메일 중복 시 예외 발생" {
        // given
        val email = "duplicate@test.com"
        val name = "홍길동"
        val password = "password123"

        every { userRepository.existsByEmail(email) } returns true

        // when & then
        shouldThrow<DuplicatedEmailException> {
            userService.signUp(email, name, password)
        }

        verify(exactly = 1) { userRepository.existsByEmail(email) }
        verify(exactly = 0) { userRepository.save(any()) }
    }*/
})
