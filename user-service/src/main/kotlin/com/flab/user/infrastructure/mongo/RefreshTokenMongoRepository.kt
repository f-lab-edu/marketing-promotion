package com.flab.user.infrastructure.mongo

import com.flab.user.domain.entity.mongo.RefreshToken
import org.springframework.data.mongodb.repository.MongoRepository

interface RefreshTokenMongoRepository : MongoRepository<RefreshToken, String> {
    fun findByRefreshTokenHash(refreshTokenHash: String): RefreshToken?
    fun deleteAllByUserId(userId: Long)
}
