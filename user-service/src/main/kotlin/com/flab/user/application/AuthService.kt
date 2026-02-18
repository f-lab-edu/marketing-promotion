package com.flab.user.application

import com.flab.user.config.exception.BusinessException
import com.flab.user.config.exception.MessageCode
import com.flab.user.config.util.JwtUtil
import com.flab.user.domain.dto.LoginRequest
import com.flab.user.domain.dto.LoginResult
import com.flab.user.domain.dto.TokenRefreshResponse
import com.flab.user.domain.dto.TokenValidationResponse
import com.flab.user.domain.entity.persistence.LoginAction
import com.flab.user.domain.entity.persistence.LoginHistory
import com.flab.user.domain.entity.mongo.AccessToken
import com.flab.user.domain.enums.UserStatus
import com.flab.user.domain.entity.mongo.RefreshToken
import com.flab.user.infrastructure.persistence.UserLoginHistoryRepository
import com.flab.user.infrastructure.mongo.AccessTokenMongoRepository
import com.flab.user.infrastructure.mongo.RefreshTokenMongoRepository
import com.flab.user.infrastructure.persistence.UserRepository
import com.flab.user.infrastructure.redis.TokenRedisRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Date

@Service
class AuthService(
    private val jwtUtil: JwtUtil,
    private val userRepository: UserRepository,
    private val tokenRedisRepository: TokenRedisRepository,
    private val refreshTokenMongoRepository: RefreshTokenMongoRepository,
    private val accessTokenMongoRepository: AccessTokenMongoRepository,
    private val userLoginHistoryRepository: UserLoginHistoryRepository
) {
    private val passwordEncoder = BCryptPasswordEncoder()

    @Transactional(noRollbackFor = [Exception::class])
    fun login(request: LoginRequest, ip: String?, userAgent: String?): LoginResult {
        val user = userRepository.findByEmail(request.email) ?: throw BusinessException(MessageCode.USER_NOT_FOUND)

        try {
            validateLonginUser(user, request.password, ip, userAgent)
            user.resetFailedLoginCount()

            val accessToken = issueAccessToken(user.id!!, user.email, user.role.name)
            val refreshToken = issueRefreshToken(user.id!!, user.email, user.role.name)
            saveLoginHistory(user.id!!, LoginAction.LOGIN, ip, userAgent)

            return LoginResult(
                accessToken = accessToken,
                refreshToken = refreshToken,
                accessExpiresIn = jwtUtil.getAccessExpiresInSeconds(),
                refreshMaxAgeSeconds = jwtUtil.getRefreshExpiresInSeconds()
            )
        } catch (e: BusinessException) {
            saveLoginHistory(user.id!!, LoginAction.LOGIN_FAIL, ip, userAgent, e.messageCode.code)
            throw e
        } catch (e: Exception) {
            saveLoginHistory(user.id!!, LoginAction.LOGIN_FAIL, ip, userAgent, MessageCode.UNKNOWN_EXCEPTION.code)
            throw e
        }
    }

    @Transactional
    fun logout(accessToken: String, refreshToken: String, ip: String?, userAgent: String?) {
        val tokenInfo = jwtUtil.parseAccessToken(accessToken)

        val accessTokenHash = jwtUtil.hash(accessToken)
        tokenRedisRepository.removeToken(accessTokenHash)

        accessTokenMongoRepository.findByAccessTokenHash(accessTokenHash)?.let {
            accessTokenMongoRepository.delete(it)
        }

        refreshTokenMongoRepository.findByRefreshTokenHash(jwtUtil.hash(refreshToken))?.let {
            refreshTokenMongoRepository.delete(it)
        }

        saveLoginHistory(tokenInfo.userId, LoginAction.LOGOUT, ip, userAgent)
    }

    fun validateToken(token: String): TokenValidationResponse {
        val tokenInfo = jwtUtil.parseAccessToken(token)

        if (!tokenRedisRepository.existsToken(jwtUtil.hash(token))) {
            throw BusinessException(MessageCode.TOKEN_REVOKED)
        }

        return TokenValidationResponse(
            userId = tokenInfo.userId,
            email = tokenInfo.email,
            role = tokenInfo.role,
            valid = true
        )
    }

    fun refreshToken(refreshToken: String): TokenRefreshResponse {
        val tokenInfo = jwtUtil.parseRefreshToken(refreshToken)

        val userRefreshToken = refreshTokenMongoRepository.findByRefreshTokenHash(jwtUtil.hash(refreshToken)) ?: throw BusinessException(MessageCode.INVALIDATE_REFRESH_TOKEN)

        if (userRefreshToken.refreshCount >= 4) {
            throw BusinessException(MessageCode.REFRESH_LIMIT_EXCEEDED)
        }

        val newAccessToken = jwtUtil.generateAccessToken(tokenInfo.userId, tokenInfo.email, tokenInfo.role)
        val newAccessTokenHash = jwtUtil.hash(newAccessToken)

        tokenRedisRepository.addToken(newAccessTokenHash, jwtUtil.getAccessExpiresInSeconds() * 1000)

        accessTokenMongoRepository.save(
            AccessToken(
                userId = tokenInfo.userId,
                accessTokenHash = newAccessTokenHash,
                expireAt = Date(System.currentTimeMillis() + jwtUtil.getAccessExpiresInSeconds() * 1000)
            )
        )

        userRefreshToken.refreshCount++
        refreshTokenMongoRepository.save(userRefreshToken)

        return TokenRefreshResponse(
            accessToken = newAccessToken,
            expiresIn = jwtUtil.getAccessExpiresInSeconds()
        )
    }

    private fun validateLonginUser(
        user: com.flab.user.domain.entity.persistence.User,
        password: String,
        ip: String?,
        userAgent: String?
    ) {
        if (user.status == UserStatus.WITHDRAWN) {
            throw BusinessException(MessageCode.USER_WITHDRAWN)
        }

        if (user.status == UserStatus.SUSPENDED) {
            throw BusinessException(MessageCode.USER_SUSPENDED)
        }

        if (!passwordEncoder.matches(password, user.password)) {
            val suspended = user.incrementFailedLoginCount()
            if (suspended) {
                revokeAllUserTokens(user.id!!)
                throw BusinessException(MessageCode.LOGIN_ATTEMPT_EXCEEDED)
            }
            throw BusinessException(MessageCode.BAD_CREDENTIAL_EXCEPTION)
        }
    }

    private fun issueAccessToken(userId: Long, email: String, role: String): String {
        val accessToken = jwtUtil.generateAccessToken(userId, email, role)
        val accessTokenHash = jwtUtil.hash(accessToken)

        tokenRedisRepository.addToken(accessTokenHash, jwtUtil.getAccessExpiresInSeconds() * 1000)
        accessTokenMongoRepository.save(
            AccessToken(
                userId = userId,
                accessTokenHash = accessTokenHash,
                expireAt = Date(System.currentTimeMillis() + jwtUtil.getAccessExpiresInSeconds() * 1000)
            )
        )
        return accessToken
    }

    private fun issueRefreshToken(userId: Long, email: String, role: String): String {
        val refreshToken = jwtUtil.generateRefreshToken(userId, email, role)
        val refreshTokenHash = jwtUtil.hash(refreshToken)

        refreshTokenMongoRepository.save(
            RefreshToken(
                userId = userId,
                refreshTokenHash = refreshTokenHash,
                expireAt = Date(System.currentTimeMillis() + jwtUtil.getRefreshExpiresInSeconds() * 1000)
            )
        )
        return refreshToken
    }

    private fun saveLoginHistory(userId: Long, action: LoginAction, ip: String?, userAgent: String?, errorCode: String? = null) {
        userLoginHistoryRepository.save(LoginHistory(userId = userId, action = action, ip = ip, userAgent = userAgent, errorCode = errorCode))
    }

    private fun revokeAllUserTokens(userId: Long) {
        val accessTokenHashes = accessTokenMongoRepository.findAllByUserId(userId).map { it.accessTokenHash }
        tokenRedisRepository.removeAllTokens(accessTokenHashes)
        accessTokenMongoRepository.deleteAllByUserId(userId)
        refreshTokenMongoRepository.deleteAllByUserId(userId)
    }
}
