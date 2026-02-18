package com.flab.user.application.service

import com.flab.user.application.AuthService
import com.flab.user.application.UserService
import com.flab.user.config.TestRedisConfiguration
import com.flab.user.config.exception.BusinessException
import com.flab.user.config.exception.MessageCode
import com.flab.user.config.util.JwtUtil
import com.flab.user.domain.dto.LoginRequest
import com.flab.user.domain.dto.SignUpRequest
import com.flab.user.domain.enums.UserStatus
import com.flab.user.infrastructure.mongo.AccessTokenMongoRepository
import com.flab.user.infrastructure.mongo.RefreshTokenMongoRepository
import com.flab.user.infrastructure.persistence.UserRepository
import com.flab.user.infrastructure.redis.TokenRedisRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(TestRedisConfiguration::class)
class UserServiceTest(
    private val userService: UserService,
    private val authService: AuthService,
    private val userRepository: UserRepository,
    private val tokenRedisRepository: TokenRedisRepository,
    private val refreshTokenMongoRepository: RefreshTokenMongoRepository,
    private val accessTokenMongoRepository: AccessTokenMongoRepository,
    private val jwtUtil: JwtUtil
) : StringSpec({

    extension(SpringExtension)

    afterEach {
        refreshTokenMongoRepository.deleteAll()
        accessTokenMongoRepository.deleteAll()
    }

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

    "회원 탈퇴 시 상태가 WITHDRAWN으로 변경되고 토큰이 삭제된다" {
        val password = "Password1!"
        val signUpResponse = userService.signUp(SignUpRequest(
            email = "withdraw@test.com",
            name = "홍길동",
            birthDate = LocalDate.of(1990, 1, 1),
            password = password
        ))
        val loginResult = authService.login(LoginRequest(email = "withdraw@test.com", password = password), null, null)

        userService.withdraw(loginResult.accessToken, "127.0.0.1", "TestAgent")

        val user = userRepository.findById(signUpResponse.userId).get()
        user.status shouldBe UserStatus.WITHDRAWN

        val accessTokenHash = jwtUtil.hash(loginResult.accessToken)
        tokenRedisRepository.existsToken(accessTokenHash) shouldBe false

        val refreshTokenHash = jwtUtil.hash(loginResult.refreshToken)
        refreshTokenMongoRepository.findByRefreshTokenHash(refreshTokenHash) shouldBe null
    }

    "이미 탈퇴한 사용자 탈퇴 시 예외 발생" {
        val password = "Password1!"
        userService.signUp(SignUpRequest(
            email = "already-withdrawn@test.com",
            name = "홍길동",
            birthDate = LocalDate.of(1990, 1, 1),
            password = password
        ))
        val loginResult = authService.login(LoginRequest(email = "already-withdrawn@test.com", password = password), null, null)

        userService.withdraw(loginResult.accessToken, null, null)

        val exception = shouldThrow<BusinessException> {
            userService.withdraw(loginResult.accessToken, null, null)
        }
        exception.messageCode shouldBe MessageCode.USER_ALREADY_WITHDRAWN
    }
})
