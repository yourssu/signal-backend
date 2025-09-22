package com.yourssu.signal.config.security.exception

import com.fasterxml.jackson.databind.ObjectMapper
import com.yourssu.signal.config.properties.JwtProperties
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtUtils(
    private val jwtProperties: JwtProperties,
    private val objectMapper: ObjectMapper,
) {
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())

    fun generateAccessToken(userUuid: String): String {
        return generateToken(userUuid, jwtProperties.accessTokenExpiration)
    }

    fun generateRefreshToken(userUuid: String): String {
        return generateToken(userUuid, jwtProperties.refreshTokenExpiration)
    }

    private fun generateToken(userUuid: String, expirationTime: Long): String {
        val now = Date()
        val expiryDate = Date(now.time + expirationTime)

        return Jwts.builder()
            .subject(userUuid)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        return try {
            val claims = getClaims(token)
            !claims.expiration.before(Date())
        } catch (e: Exception) {
            false
        }
    }

    fun getUserUuidFromToken(token: String): String {
        return getClaims(token).subject
    }

    private fun getClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    fun getSubWithoutVerifying(token: String): String {
        try {
            val payload = token.split('.')[1]
            val decodedPayload = String(Base64.getUrlDecoder().decode(payload))
            val claims = objectMapper.readTree(decodedPayload)
            return claims.get("sub").asText()
        } catch (_: Exception) {
            throw InvalidJwtTokenException()
        }
    }

    fun getEmailWithoutVerifying(token: String): String? {
        try {
            val payload = token.split('.')[1]
            val decodedPayload = String(Base64.getUrlDecoder().decode(payload))
            val claims = objectMapper.readTree(decodedPayload)
            return claims.get("email").asText()
        } catch (_: Exception) {
            return null
        }
    }
}
