package com.flab.user.infrastructure.redis

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit

@Repository
class TokenRedisRepository(
    private val redisTemplate: StringRedisTemplate
) {
    companion object {
        private const val KEY_PREFIX = "token:"
    }

    private fun key(tokenHash: String): String = "$KEY_PREFIX$tokenHash"

    fun addToken(tokenHash: String, expirationMs: Long) {
        redisTemplate.opsForValue().set(key(tokenHash), "1", expirationMs, TimeUnit.MILLISECONDS)
    }

    fun existsToken(tokenHash: String): Boolean {
        return redisTemplate.hasKey(key(tokenHash))
    }

    fun removeToken(tokenHash: String) {
        redisTemplate.delete(key(tokenHash))
    }

    fun removeAllTokens(tokenHashes: List<String>) {
        if (tokenHashes.isNotEmpty()) {
            redisTemplate.delete(tokenHashes.map { key(it) })
        }
    }
}
