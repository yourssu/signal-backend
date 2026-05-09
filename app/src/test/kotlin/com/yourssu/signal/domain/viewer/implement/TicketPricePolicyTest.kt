package com.yourssu.signal.domain.viewer.implement

import VerificationCode
import com.yourssu.signal.config.properties.PolicyConfigurationProperties
import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.profile.implement.ProfileReader
import com.yourssu.signal.domain.verification.implement.Verification
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.*

class TicketPricePolicyTest : DescribeSpec({

    val configProperties = mock<PolicyConfigurationProperties>()
    val profileReader = mock<ProfileReader>()
    val viewerReader = mock<ViewerReader>()
    val verificationReader = mock<VerificationReader>()

    // single@800n1 = 할인가, single@1000n1 = 일반가
    whenever(configProperties.ticketPriceRegisteredPolicy).thenReturn("single@800n1")
    whenever(configProperties.ticketPricePolicy).thenReturn("single@1000n1")

    val ticketPricePolicy = TicketPricePolicy(
        configProperties = configProperties,
        profileReader = profileReader,
        viewerReader = viewerReader,
        verificationReader = verificationReader,
    )

    val uuid = Uuid("test-uuid")
    val code = VerificationCode(12345)
    val verification = Verification(verificationCode = code, uuid = uuid)

    beforeEach {
        reset(profileReader, viewerReader, verificationReader)
        whenever(verificationReader.findByCode(code)).thenReturn(verification)
    }

    describe("calculateTicketQuantity") {

        context("프로필 등록 + ticket=0인 유저가 800원 입금하면") {
            it("할인가 기준 1장을 반환한다") {
                whenever(profileReader.existsByUuid(uuid)).thenReturn(true)
                whenever(viewerReader.get(uuid)).thenReturn(
                    Viewer(uuid = uuid, ticket = 0, updatedTime = null)
                )

                val result = ticketPricePolicy.calculateTicketQuantity(800, code)

                result shouldBe 1
            }
        }

        context("프로필 등록 + ticket>0인 유저가 800원 입금하면") {
            it("일반가 맵에 800원이 없으므로 NO_MATCH를 반환한다") {
                whenever(profileReader.existsByUuid(uuid)).thenReturn(true)
                whenever(viewerReader.get(uuid)).thenReturn(
                    Viewer(uuid = uuid, ticket = 1, updatedTime = null)
                )

                val result = ticketPricePolicy.calculateTicketQuantity(800, code)

                result shouldBe NO_MATCH_TICKET_AMOUNT
            }
        }

        context("프로필 등록 + ticket>0인 유저가 1000원 입금하면") {
            it("일반가 기준 1장을 반환한다") {
                whenever(profileReader.existsByUuid(uuid)).thenReturn(true)
                whenever(viewerReader.get(uuid)).thenReturn(
                    Viewer(uuid = uuid, ticket = 1, updatedTime = null)
                )

                val result = ticketPricePolicy.calculateTicketQuantity(1000, code)

                result shouldBe 1
            }
        }

        context("프로필 미등록 유저가 800원 입금하면") {
            it("일반가 맵에 800원이 없으므로 NO_MATCH를 반환한다") {
                whenever(profileReader.existsByUuid(uuid)).thenReturn(false)

                val result = ticketPricePolicy.calculateTicketQuantity(800, code)

                result shouldBe NO_MATCH_TICKET_AMOUNT
            }
        }

        context("프로필 미등록 유저가 1000원 입금하면") {
            it("일반가 기준 1장을 반환한다") {
                whenever(profileReader.existsByUuid(uuid)).thenReturn(false)

                val result = ticketPricePolicy.calculateTicketQuantity(1000, code)

                result shouldBe 1
            }
        }
    }
})
