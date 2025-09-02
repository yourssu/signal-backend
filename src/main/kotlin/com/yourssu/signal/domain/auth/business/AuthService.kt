package com.yourssu.signal.domain.auth.business

import com.yourssu.signal.api.dto.DevTokenRequest
import com.yourssu.signal.config.properties.AdminConfigurationProperties
import com.yourssu.signal.config.properties.JwtProperties
import com.yourssu.signal.config.security.exception.InvalidJwtTokenException
import com.yourssu.signal.config.security.exception.JwtUtils
import com.yourssu.signal.domain.auth.business.command.GoogleOAuthCommand
import com.yourssu.signal.domain.auth.business.dto.TokenResponse
import com.yourssu.signal.domain.auth.business.exception.InvalidGoogleCodeException
import com.yourssu.signal.domain.auth.implement.EmailUserReader
import com.yourssu.signal.domain.auth.implement.EmailUserWriter
import com.yourssu.signal.domain.auth.implement.OAuthOutputPort
import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.user.implement.User
import com.yourssu.signal.domain.user.implement.UserReader
import com.yourssu.signal.domain.user.implement.UserWriter
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
    private val emailUserReader: EmailUserReader,
    private val emailUserWriter: EmailUserWriter,
    private val oAuthOutputPort: OAuthOutputPort,
) {
    @Transactional
    fun register(): TokenResponse {
        val user = userWriter.generateUser()
        return generateTokenResponse(user)
    }

    fun refreshToken(refreshToken: String): TokenResponse {
        if (!jwtUtils.validateToken(refreshToken)) {
            throw InvalidJwtTokenException()
        }
        val uuid = jwtUtils.getUserUuidFromToken(refreshToken)
        val user = userReader.getByUuid(Uuid(uuid))
        return generateTokenResponse(user)
    }

    fun generateDevToken(request: DevTokenRequest): TokenResponse {
        if (!adminProperties.isValidAccessKey(request.accessKey)) {
            throw AdminPermissionDeniedException()
        }
        val user = try {
            userReader.getByUuid(Uuid(request.uuid))
        } catch (_: Exception) {
            userWriter.generateUser(request.uuid)
        }
        return generateTokenResponse(user)
    }

    @Transactional
    fun loginWithGoogle(command: GoogleOAuthCommand): TokenResponse {
        val idToken = oAuthOutputPort.exchangeCodeForIdToken(command.code)
            ?: throw InvalidGoogleCodeException()
        val identifier = jwtUtils.getSubWithoutVerifying(idToken)
        val user = userReader.getByUuid(command.toUuid())
        val isExistsUser = emailUserReader.existsByEmailAndUuid(identifier, user.uuid)
        if (isExistsUser) {
            return generateTokenResponse(user)
        }
        val uuid = emailUserReader.findUuidByEmail(identifier)
        if (uuid == null) {
            emailUserWriter.save(command.toDomain(identifier))
            return generateTokenResponse(user)
        }
        val previousUser = userReader.getByUuid(uuid)
        return generateTokenResponse(previousUser)
    }

    private fun generateTokenResponse(user: User): TokenResponse {
        val newAccessToken = jwtUtils.generateAccessToken(user.uuid.value)
        val refreshToken = jwtUtils.generateRefreshToken(user.uuid.value)
        return TokenResponse(
            accessToken = newAccessToken,
            refreshToken = refreshToken,
            accessTokenExpiresIn = jwtProperties.accessTokenExpiration,
            refreshTokenExpiresIn = jwtProperties.refreshTokenExpiration
        )
    }
}
