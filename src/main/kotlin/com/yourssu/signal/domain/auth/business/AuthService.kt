package com.yourssu.signal.domain.auth.business

import com.yourssu.signal.config.properties.AdminConfigurationProperties
import com.yourssu.signal.config.properties.JwtProperties
import com.yourssu.signal.config.security.exception.InvalidJwtTokenException
import com.yourssu.signal.config.security.exception.JwtUtils
import com.yourssu.signal.domain.auth.application.dto.DevTokenRequest
import com.yourssu.signal.domain.auth.application.dto.TokenResponse
import com.yourssu.signal.domain.auth.implement.UserReader
import com.yourssu.signal.domain.auth.implement.UserWriter
import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.viewer.implement.exception.AdminPermissionDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val jwtUtils: JwtUtils,
    private val jwtProperties: JwtProperties,
    private val userWriter: UserWriter,
    private val userReader: UserReader,
    private val adminProperties: AdminConfigurationProperties,
) {
    @Transactional
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

    fun refreshToken(refreshToken: String): TokenResponse {
        if (!jwtUtils.validateToken(refreshToken)) {
            throw InvalidJwtTokenException()
        }
        val uuid = jwtUtils.getUserUuidFromToken(refreshToken)
        userReader.getByUuid(Uuid(uuid))
        val accessToken = jwtUtils.generateAccessToken(uuid)
        val newRefreshToken = jwtUtils.generateRefreshToken(uuid)
        return TokenResponse(
            accessToken = accessToken,
            refreshToken = newRefreshToken,
            accessTokenExpiresIn = jwtProperties.accessTokenExpiration,
            refreshTokenExpiresIn = jwtProperties.refreshTokenExpiration
        )
    }

    fun generateDevToken(request: DevTokenRequest): TokenResponse {
        if (!adminProperties.isValidAccessKey(request.accessKey)) {
            throw AdminPermissionDeniedException()
        }
        val user = userReader.getByUuid(Uuid(request.uuid))
        val accessToken = jwtUtils.generateAccessToken(user.uuid.value)
        val refreshToken = jwtUtils.generateRefreshToken(user.uuid.value)
        return TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            accessTokenExpiresIn = jwtProperties.accessTokenExpiration,
            refreshTokenExpiresIn = jwtProperties.refreshTokenExpiration
        )
    }
}
