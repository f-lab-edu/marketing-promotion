package com.flab.user.config.util

import com.flab.user.config.exception.BusinessException
import com.flab.user.config.exception.MessageCode
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.boot.context.properties.ConfigurationProperties
import java.util.Date
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@ConfigurationProperties(prefix = "jwt")
class JwtUtil(
    private val secret: String,
    private val accessTokenExpiration: Long = 1800000,
    private val refreshTokenExpiration: Long = 7200000,
    private val tokenHashSecret: String = ""
) {
    data class TokenInfo(
        val userId: Long,
        val email: String,
        val role: String
    )

    private val signingKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray())
    }

    fun generateAccessToken(userId: Long, email: String, role: String): String {
        val now = Date()
        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .claim("role", role)
            .claim("type", "access")
            .issuedAt(now)
            .expiration(Date(now.time + accessTokenExpiration))
            .signWith(signingKey)
            .compact()
    }

    fun generateRefreshToken(userId: Long, email: String, role: String): String {
        val now = Date()
        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .claim("role", role)
            .claim("type", "refresh")
            .issuedAt(now)
            .expiration(Date(now.time + refreshTokenExpiration))
            .signWith(signingKey)
            .compact()
    }

    fun getAccessExpiresInSeconds(): Long = accessTokenExpiration / 1000

    fun getRefreshExpiresInSeconds(): Long = refreshTokenExpiration / 1000

    fun parseAccessToken(token: String): TokenInfo {
        val claims = parseClaim(token, MessageCode.INVALIDATE_CLAIMS_EXCEPTION)
        val userId = claims.subject.toLongOrNull()
            ?: throw BusinessException(MessageCode.INVALIDATE_CLAIMS_EXCEPTION)
        return TokenInfo(
            userId = userId,
            email = claims["email"] as? String ?: "",
            role = claims["role"] as? String ?: ""
        )
    }

    fun parseRefreshToken(token: String): TokenInfo {
        val claims = parseClaim(token, MessageCode.INVALIDATE_REFRESH_TOKEN)
        if (claims["type"] as? String != "refresh") {
            throw BusinessException(MessageCode.INVALIDATE_REFRESH_TOKEN)
        }
        val userId = claims.subject.toLongOrNull()
            ?: throw BusinessException(MessageCode.INVALIDATE_REFRESH_TOKEN)
        return TokenInfo(
            userId = userId,
            email = claims["email"] as? String ?: "",
            role = claims["role"] as? String ?: ""
        )
    }

    fun hash(token: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(tokenHashSecret.toByteArray(), "HmacSHA256"))
        return mac.doFinal(token.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    private fun parseClaim(token: String, errorCode: MessageCode): Claims {
        return try {
            Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: Exception) {
            throw BusinessException(errorCode)
        }
    }
}
