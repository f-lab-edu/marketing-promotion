package com.flab.user.domain.entity.mongo

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import java.util.Date

@Document(collection = "access_tokens")
data class AccessToken(
    @Id val id: String? = null,
    @Indexed val userId: Long,
    val accessTokenHash: String,
    @CreatedDate
    var createdAt: LocalDateTime = LocalDateTime.now(),
    @Indexed(expireAfter = "0s") val expireAt: Date
)
