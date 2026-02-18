package com.flab.user.infrastructure.mongo

import com.flab.user.domain.entity.mongo.AccessToken
import org.springframework.data.mongodb.repository.MongoRepository

interface AccessTokenMongoRepository : MongoRepository<AccessToken, String> {
    fun findByAccessTokenHash(accessTokenHash: String): AccessToken?
    fun findAllByUserId(userId: Long): List<AccessToken>
    fun deleteAllByUserId(userId: Long)
}
