package com.yourssu.signal.domain.auth.business

import com.yourssu.signal.config.properties.AdminConfigurationProperties
import com.yourssu.signal.config.properties.JwtProperties
import com.yourssu.signal.config.security.exception.JwtUtils
import com.yourssu.signal.domain.auth.business.command.GoogleOAuthCommand
import com.yourssu.signal.domain.auth.business.exception.GoogleAccountAlreadyLinkedException
import com.yourssu.signal.domain.auth.business.exception.InvalidGoogleCodeException
import com.yourssu.signal.domain.auth.implement.GoogleUserReader
import com.yourssu.signal.domain.auth.implement.GoogleUserWriter
import com.yourssu.signal.domain.auth.implement.OAuthOutputPort
import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.user.implement.User
import com.yourssu.signal.domain.user.implement.UserReader
import com.yourssu.signal.domain.user.implement.UserWriter
import com.yourssu.signal.domain.viewer.implement.Viewer
import com.yourssu.signal.domain.viewer.implement.ViewerWriter
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.*

class AuthServiceTest : DescribeSpec({

    val jwtUtils = mock<JwtUtils>()
    val jwtProperties = mock<JwtProperties>()
    val userWriter = mock<UserWriter>()
    val userReader = mock<UserReader>()
    val adminProperties = mock<AdminConfigurationProperties>()
    val googleUserReader = mock<GoogleUserReader>()
    val googleUserWriter = mock<GoogleUserWriter>()
    val oAuthOutputPort = mock<OAuthOutputPort>()
    val viewerWriter = mock<ViewerWriter>()

    val authService = AuthService(
        jwtUtils = jwtUtils,
        jwtProperties = jwtProperties,
        userWriter = userWriter,
        userReader = userReader,
        adminProperties = adminProperties,
        googleUserReader = googleUserReader,
        googleUserWriter = googleUserWriter,
        oAuthOutputPort = oAuthOutputPort,
        viewerWriter = viewerWriter,
    )

    fun stubTokenGeneration(uuid: String) {
        whenever(jwtUtils.generateAccessToken(uuid)).thenReturn("access-token")
        whenever(jwtUtils.generateRefreshToken(uuid)).thenReturn("refresh-token")
        whenever(jwtProperties.accessTokenExpiration).thenReturn(1800000L)
        whenever(jwtProperties.refreshTokenExpiration).thenReturn(2592000000L)
    }

    beforeEach {
        reset(jwtUtils, jwtProperties, userWriter, userReader, adminProperties,
            googleUserReader, googleUserWriter, oAuthOutputPort, viewerWriter)
    }

    describe("register") {
        context("신규 유저 등록 시") {
            it("User를 생성하고 ticket=0인 Viewer를 초기화한다") {
                val newUuid = Uuid("new-uuid")
                val newUser = User(id = 1L, uuid = newUuid)
                val newViewer = Viewer(uuid = newUuid, ticket = 0, updatedTime = null)
                whenever(userWriter.generateUser()).thenReturn(newUser)
                whenever(viewerWriter.create(newUuid)).thenReturn(newViewer)
                stubTokenGeneration(newUuid.value)

                val result = authService.register()

                verify(userWriter).generateUser()
                verify(viewerWriter).create(newUuid)
                result.accessToken shouldBe "access-token"
            }
        }
    }

    describe("loginWithGoogle") {

        val fakeIdToken = "header.payload.signature"
        val googleIdentifier = "google-sub-12345"
        val googleEmail = "test@gmail.com"
        val currentUuid = "uuid-B"
        val linkedUuid = Uuid("uuid-A")

        beforeEach {
            whenever(oAuthOutputPort.exchangeCodeForIdToken(any())).thenReturn(fakeIdToken)
            whenever(jwtUtils.getSubWithoutVerifying(fakeIdToken)).thenReturn(googleIdentifier)
            whenever(jwtUtils.getEmailWithoutVerifying(fakeIdToken)).thenReturn(googleEmail)
        }

        context("Google ID가 이미 기존 UUID(A)에 연결돼 있으면") {
            it("현재 UUID(B)가 아닌 UUID(A)의 토큰을 반환한다") {
                val command = GoogleOAuthCommand(code = "auth-code", uuid = currentUuid)
                val linkedUser = User(id = 1L, uuid = linkedUuid)

                whenever(googleUserReader.findUuidByIdentifier(googleIdentifier)).thenReturn(linkedUuid)
                whenever(userReader.getByUuid(linkedUuid)).thenReturn(linkedUser)
                stubTokenGeneration(linkedUuid.value)

                val result = authService.loginWithGoogle(command)

                result.accessToken shouldBe "access-token"
                verify(userReader).getByUuid(linkedUuid)
                verify(googleUserWriter, never()).save(any())
            }
        }

        context("Google ID가 처음 연결되고 현재 UUID에 다른 Google 계정이 없으면") {
            it("현재 UUID에 Google 계정을 연결하고 토큰을 반환한다") {
                val command = GoogleOAuthCommand(code = "auth-code", uuid = currentUuid)
                val currentUser = User(id = 2L, uuid = Uuid(currentUuid))

                whenever(googleUserReader.findUuidByIdentifier(googleIdentifier)).thenReturn(null)
                whenever(userReader.getByUuid(Uuid(currentUuid))).thenReturn(currentUser)
                whenever(googleUserReader.existsByUuid(Uuid(currentUuid))).thenReturn(false)
                stubTokenGeneration(currentUuid)

                val result = authService.loginWithGoogle(command)

                result.accessToken shouldBe "access-token"
                verify(googleUserWriter).save(any())
            }
        }

        context("현재 UUID에 이미 다른 Google 계정이 연결돼 있으면") {
            it("GoogleAccountAlreadyLinkedException을 던진다") {
                val command = GoogleOAuthCommand(code = "auth-code", uuid = currentUuid)
                val currentUser = User(id = 2L, uuid = Uuid(currentUuid))

                whenever(googleUserReader.findUuidByIdentifier(googleIdentifier)).thenReturn(null)
                whenever(userReader.getByUuid(Uuid(currentUuid))).thenReturn(currentUser)
                whenever(googleUserReader.existsByUuid(Uuid(currentUuid))).thenReturn(true)

                shouldThrow<GoogleAccountAlreadyLinkedException> {
                    authService.loginWithGoogle(command)
                }
                verify(googleUserWriter, never()).save(any())
            }
        }

        context("Google 인증 코드 교환이 실패하면") {
            it("InvalidGoogleCodeException을 던진다") {
                val command = GoogleOAuthCommand(code = "invalid-code", uuid = currentUuid)
                whenever(oAuthOutputPort.exchangeCodeForIdToken("invalid-code")).thenReturn(null)

                shouldThrow<InvalidGoogleCodeException> {
                    authService.loginWithGoogle(command)
                }
            }
        }
    }
})
