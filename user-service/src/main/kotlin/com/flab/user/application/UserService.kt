package com.flab.user.application

import com.flab.user.config.exception.BusinessException
import com.flab.user.config.exception.MessageCode
import com.flab.user.config.util.JwtUtil
import com.flab.user.domain.dto.SignUpRequest
import com.flab.user.domain.dto.SignUpResponse
import com.flab.user.domain.entity.persistence.LoginAction
import com.flab.user.domain.entity.persistence.LoginHistory
import com.flab.user.domain.mapper.UserMapper
import com.flab.user.infrastructure.mongo.AccessTokenMongoRepository
import com.flab.user.infrastructure.mongo.RefreshTokenMongoRepository
import com.flab.user.infrastructure.persistence.UserLoginHistoryRepository
import com.flab.user.infrastructure.persistence.UserRepository
import com.flab.user.infrastructure.redis.TokenRedisRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userMapper: UserMapper,
    private val jwtUtil: JwtUtil,
    private val tokenRedisRepository: TokenRedisRepository,
    private val accessTokenMongoRepository: AccessTokenMongoRepository,
    private val refreshTokenMongoRepository: RefreshTokenMongoRepository,
    private val userLoginHistoryRepository: UserLoginHistoryRepository
) {
    @Transactional
    fun signUp(req: SignUpRequest): SignUpResponse {
        val user = userMapper.toUser(req, BCryptPasswordEncoder().encode(req.password)!!)

        if (userRepository.existsByEmail(user.email)) {
            throw BusinessException(MessageCode.DUPLICATED_EMAIL)
        }

        val savedUser = userRepository.save(user)
        savedUser.createdBy = savedUser.id!!
        savedUser.updatedBy = savedUser.id!!

        return userMapper.toSignUpResponse(savedUser)
    }

    @Transactional
    fun withdraw(accessToken: String, ip: String?, userAgent: String?) {
        val tokenInfo = jwtUtil.parseAccessToken(accessToken)

        val user = userRepository.findById(tokenInfo.userId).orElseThrow {
            BusinessException(MessageCode.USER_NOT_FOUND)
        }

        user.setWithdraw()

        revokeAllUserTokens(tokenInfo.userId)

        userLoginHistoryRepository.save(
            LoginHistory(userId = tokenInfo.userId, action = LoginAction.WITHDRAW, ip = ip, userAgent = userAgent)
        )
    }

    private fun revokeAllUserTokens(userId: Long) {
        val accessTokenHashes = accessTokenMongoRepository.findAllByUserId(userId).map { it.accessTokenHash }
        tokenRedisRepository.removeAllTokens(accessTokenHashes)
        accessTokenMongoRepository.deleteAllByUserId(userId)
        refreshTokenMongoRepository.deleteAllByUserId(userId)
    }
}
