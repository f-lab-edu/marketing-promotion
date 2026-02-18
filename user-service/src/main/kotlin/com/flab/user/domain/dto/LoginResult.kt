package com.flab.user.domain.dto

data class LoginResult(
    val accessToken: String,
    val refreshToken: String,
    val accessExpiresIn: Long,
    val refreshMaxAgeSeconds: Long
)
