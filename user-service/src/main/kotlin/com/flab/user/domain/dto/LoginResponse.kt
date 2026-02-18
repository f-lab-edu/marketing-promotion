package com.flab.user.domain.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class LoginResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long
)
