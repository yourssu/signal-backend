package com.yourssu.signal.domain.auth.business

import com.yourssu.signal.config.properties.JwtProperties
import com.yourssu.signal.config.security.exception.InvalidJwtTokenException
import com.yourssu.signal.config.security.exception.JwtUtils
import com.yourssu.signal.domain.auth.application.dto.RefreshTokenRequest
import com.yourssu.signal.domain.auth.application.dto.TokenResponse
import com.yourssu.signal.domain.auth.implement.UserWriter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AuthService(
    private val jwtUtils: JwtUtils,
    private val jwtProperties: JwtProperties,
    private val userWriter: UserWriter,
) {
    fun register(): TokenResponse {
        val user = userWriter.generateUser()
        val accessToken = jwtUtils.generateAccessToken(user.uuid.value)
        val refreshToken = jwtUtils.generateRefreshToken(user.uuid.value)
        return TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            accessTokenExpiresIn = jwtProperties.accessTokenExpiration,
            refreshTokenExpiresIn = jwtProperties.refreshTokenExpiration
        )
    }
    
    fun refreshToken(request: RefreshTokenRequest): TokenResponse {
        if (!jwtUtils.validateToken(request.refreshToken)) {
            throw InvalidJwtTokenException()
        }
        val uuid = jwtUtils.getUserUuidFromToken(request.refreshToken)
        val accessToken = jwtUtils.generateAccessToken(uuid)
        val refreshToken = jwtUtils.generateRefreshToken(uuid)
        return TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            accessTokenExpiresIn = jwtProperties.accessTokenExpiration,
            refreshTokenExpiresIn = jwtProperties.refreshTokenExpiration
        )
    }
}
