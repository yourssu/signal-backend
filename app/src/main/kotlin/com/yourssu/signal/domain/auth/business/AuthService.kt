package com.yourssu.signal.domain.auth.business

import com.yourssu.signal.api.dto.DevTokenRequest
import com.yourssu.signal.config.properties.AdminConfigurationProperties
import com.yourssu.signal.config.properties.JwtProperties
import com.yourssu.signal.config.security.exception.InvalidJwtTokenException
import com.yourssu.signal.config.security.exception.JwtUtils
import com.yourssu.signal.domain.auth.business.command.GoogleOAuthCommand
import com.yourssu.signal.domain.auth.business.dto.TokenResponse
import com.yourssu.signal.domain.auth.business.exception.GoogleAccountAlreadyLinkedException
import com.yourssu.signal.domain.auth.business.exception.InvalidGoogleCodeException
import com.yourssu.signal.domain.auth.implement.GoogleUserReader
import com.yourssu.signal.domain.auth.implement.GoogleUserWriter
import com.yourssu.signal.domain.auth.implement.OAuthOutputPort
import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.user.implement.User
import com.yourssu.signal.domain.user.implement.UserReader
import com.yourssu.signal.domain.user.implement.UserWriter
import com.yourssu.signal.domain.profile.implement.ProfileReader
import com.yourssu.signal.domain.viewer.implement.ViewerReader
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
    private val googleUserReader: GoogleUserReader,
    private val googleUserWriter: GoogleUserWriter,
    private val oAuthOutputPort: OAuthOutputPort,
    private val profileReader: ProfileReader,
    private val viewerReader: ViewerReader,
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
        val email = jwtUtils.getEmailWithoutVerifying(idToken)
        val user = userReader.getByUuid(command.toUuid())
        val uuid = googleUserReader.findUuidByIdentifier(identifier)
        val isExistsUser = googleUserReader.existsByUuid(user.uuid)
        val isCurrentUserAlreadyLinked = (uuid == null && isExistsUser)
        if (isCurrentUserAlreadyLinked || isGoogleAccountLinkedToOtherViewer(uuid, user)) {
            throw GoogleAccountAlreadyLinkedException()
        }
        if (uuid == null) {
            googleUserWriter.save(command.toDomain(identifier, email))
            return generateTokenResponse(user)
        }
        val linkedUser = userReader.getByUuid(uuid)
        return generateTokenResponse(linkedUser)
    }

    private fun isGoogleAccountLinkedToOtherViewer(uuid: Uuid?, user: User): Boolean {
        val linkedViewer = viewerReader.existsByUuid(user.uuid)
        return uuid != null && uuid != user.uuid && linkedViewer
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
