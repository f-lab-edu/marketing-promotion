package com.flab.user.domain.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class TokenValidationResponse(
    val userId: Long,
    val email: String,
    val role: String,
    val valid: Boolean
)
