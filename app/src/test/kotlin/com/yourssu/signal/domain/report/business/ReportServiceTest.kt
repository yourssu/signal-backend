package com.yourssu.signal.domain.report.business

import com.yourssu.signal.domain.blacklist.implement.*
import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.order.implement.*
import com.yourssu.signal.domain.profile.implement.*
import com.yourssu.signal.domain.report.implement.*
import com.yourssu.signal.domain.report.implement.exception.*
import com.yourssu.signal.domain.viewer.implement.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import org.mockito.kotlin.*

class ReportServiceTest : DescribeSpec({
    val reportReader = mock<ReportReader>()
    val reportWriter = mock<ReportWriter>()
    val profileReader = mock<ProfileReader>()
    val viewerReader = mock<ViewerReader>()
    val purchasedProfileReader = mock<PurchasedProfileReader>()
    val adminAccessChecker = mock<AdminAccessChecker>()
    val blacklistReader = mock<BlacklistReader>()
    val blacklistWriter = mock<BlacklistWriter>()
    val viewerWriter = mock<ViewerWriter>()
    val orderHistoryWriter = mock<OrderHistoryWriter>()
    val service = ReportService(reportReader, reportWriter, profileReader, viewerReader,
        purchasedProfileReader, adminAccessChecker, blacklistReader, blacklistWriter,
        viewerWriter, orderHistoryWriter)

    beforeEach { reset(reportReader, reportWriter, profileReader, viewerReader, purchasedProfileReader,
        adminAccessChecker, blacklistReader, blacklistWriter, viewerWriter, orderHistoryWriter) }

    describe("신고 승인") {
        context("PENDING 신고이면") {
            it("관리자 blacklist, 티켓 1장, REPORT_REWARD 이력, APPROVED를 한 번 반영한다") {
                val uuid = Uuid("reporter")
                whenever(reportReader.getForUpdate(1)).thenReturn(Report(1, uuid, 2, "@contact"))
                whenever(blacklistReader.existsByProfileId(2)).thenReturn(false)

                service.approve(1, "secret")

                verify(adminAccessChecker).validateAdminAccess("secret")
                verify(viewerWriter).issueTicket(uuid, 1)
                verify(orderHistoryWriter).createOrderHistory(check {
                    assert(it.orderType == OrderType.REPORT_REWARD && it.quantity == 1 && it.amount == 0)
                })
                verify(reportWriter).approve(1)
                verify(blacklistWriter).save(check { assert(it.profileId == 2L && it.createdByAdmin) })
            }
        }
        context("이미 승인된 신고이면") {
            it("어떤 보상 side effect도 만들지 않는다") {
                whenever(reportReader.getForUpdate(1)).thenReturn(
                    Report(1, Uuid("reporter"), 2, "@contact", ReportStatus.APPROVED)
                )
                shouldThrow<ReportAlreadyProcessedException> { service.approve(1, "secret") }
                verify(viewerWriter, never()).issueTicket(any(), any())
                verify(orderHistoryWriter, never()).createOrderHistory(any())
                verify(blacklistWriter, never()).save(any())
                verify(blacklistWriter, never()).updateToAdminBlacklist(any())
                verify(reportWriter, never()).approve(any())
            }
        }
    }

    describe("신고 생성") {
        context("연락처를 구매하지 않았으면") {
            it("신고를 저장하지 않는다") {
                val uuid = Uuid("reporter")
                whenever(profileReader.getById(2)).thenReturn(profile(uuid = Uuid("target")))
                whenever(viewerReader.get(uuid)).thenReturn(Viewer(3, uuid, 0, updatedTime = null))
                whenever(purchasedProfileReader.exists(2, 3)).thenReturn(false)
                shouldThrow<ReportNotEligibleException> { service.create(uuid.value, 2) }
                verify(reportWriter, never()).save(any())
            }
        }
        context("같은 대상을 이미 신고했으면") {
            it("중복 신고를 거부한다") {
                val uuid = Uuid("reporter")
                whenever(profileReader.getById(2)).thenReturn(profile(uuid = Uuid("target")))
                whenever(viewerReader.get(uuid)).thenReturn(Viewer(3, uuid, 0, updatedTime = null))
                whenever(purchasedProfileReader.exists(2, 3)).thenReturn(true)
                whenever(reportReader.exists(uuid, 2)).thenReturn(true)
                shouldThrow<ReportAlreadyExistsException> { service.create(uuid.value, 2) }
            }
        }
    }
}) {
    companion object {
        private fun profile(uuid: Uuid) = Profile(
            id = 2, uuid = uuid, gender = Gender.MALE, department = "컴퓨터학부", birthYear = 2000,
            animal = Animal.DOG, contact = "@contact", mbti = "ENFP", nickname = "테스트닉네임",
            introSentences = listOf("안녕하세요"), school = "숭실대학교"
        )
    }
}
