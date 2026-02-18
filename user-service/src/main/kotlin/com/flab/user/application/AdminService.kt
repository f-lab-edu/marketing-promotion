package com.flab.user.application

import com.flab.user.config.exception.BusinessException
import com.flab.user.config.exception.MessageCode
import com.flab.user.infrastructure.mongo.AccessTokenMongoRepository
import com.flab.user.infrastructure.mongo.RefreshTokenMongoRepository
import com.flab.user.infrastructure.persistence.UserRepository
import com.flab.user.infrastructure.redis.TokenRedisRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminService(
    private val userRepository: UserRepository,
    private val tokenRedisRepository: TokenRedisRepository,
    private val refreshTokenMongoRepository: RefreshTokenMongoRepository,
    private val accessTokenMongoRepository: AccessTokenMongoRepository
) {
    @Transactional
    fun suspendUser(userId: Long, adminId: Long) {
        val user = userRepository.findById(userId).orElseThrow {
            BusinessException(MessageCode.USER_NOT_FOUND)
        }
        user.setSuspend(adminId)
        revokeAllUserTokens(userId)
    }

    @Transactional
    fun unsuspendUser(userId: Long, adminId: Long) {
        val user = userRepository.findById(userId).orElseThrow {
            BusinessException(MessageCode.USER_NOT_FOUND)
        }
        user.setUnsuspend(adminId)
    }

    private fun revokeAllUserTokens(userId: Long) {
        val accessTokenHashes = accessTokenMongoRepository.findAllByUserId(userId).map { it.accessTokenHash }
        tokenRedisRepository.removeAllTokens(accessTokenHashes)
        accessTokenMongoRepository.deleteAllByUserId(userId)
        refreshTokenMongoRepository.deleteAllByUserId(userId)
    }
}
