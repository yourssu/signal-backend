package com.yourssu.signal.domain.report.business

import com.yourssu.signal.domain.blacklist.implement.Blacklist
import com.yourssu.signal.domain.blacklist.implement.BlacklistReader
import com.yourssu.signal.domain.blacklist.implement.BlacklistWriter
import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.order.implement.*
import com.yourssu.signal.domain.profile.implement.ProfileReader
import com.yourssu.signal.domain.profile.implement.PurchasedProfileReader
import com.yourssu.signal.domain.report.implement.*
import com.yourssu.signal.domain.report.implement.exception.*
import com.yourssu.signal.domain.viewer.implement.AdminAccessChecker
import com.yourssu.signal.domain.viewer.implement.ViewerReader
import com.yourssu.signal.domain.viewer.implement.ViewerWriter
import com.yourssu.signal.infrastructure.logging.Notification
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.time.LocalDateTime

@Service
class ReportService(
    private val reportReader: ReportReader,
    private val reportWriter: ReportWriter,
    private val profileReader: ProfileReader,
    private val viewerReader: ViewerReader,
    private val purchasedProfileReader: PurchasedProfileReader,
    private val adminAccessChecker: AdminAccessChecker,
    private val blacklistReader: BlacklistReader,
    private val blacklistWriter: BlacklistWriter,
    private val viewerWriter: ViewerWriter,
    private val orderHistoryWriter: OrderHistoryWriter,
) {
    @Transactional
    fun create(reporterUuid: String, reportedProfileId: Long): ReportResponse {
        val uuid = Uuid(reporterUuid)
        val profile = profileReader.getById(reportedProfileId)
        val viewer = viewerReader.get(uuid)
        if (!purchasedProfileReader.exists(reportedProfileId, viewer.id!!)) throw ReportNotEligibleException()
        if (reportReader.exists(uuid, reportedProfileId)) throw ReportAlreadyExistsException()

        val report = try {
            reportWriter.save(Report(reporterUuid = uuid, reportedProfileId = reportedProfileId, reportedContact = profile.contact))
        } catch (_: DataIntegrityViolationException) {
            throw ReportAlreadyExistsException()
        }
        TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
            override fun afterCommit() {
                Notification.notifyFalseContactReport(
                    report.id!!,
                    report.reportedProfileId,
                    report.reportedContact,
                    report.createdTime ?: LocalDateTime.now(),
                )
            }
        })
        return ReportResponse(report.id!!, report.status, report.reportedProfileId)
    }

    @Transactional
    fun approve(reportId: Long, secretKey: String): ReportResponse {
        adminAccessChecker.validateAdminAccess(secretKey)
        val report = reportReader.getForUpdate(reportId)
        if (report.status != ReportStatus.PENDING) throw ReportAlreadyProcessedException()

        viewerWriter.issueTicket(report.reporterUuid, 1)
        orderHistoryWriter.createOrderHistory(
            OrderHistory(
                uuid = report.reporterUuid,
                amount = 0,
                quantity = 1,
                orderType = OrderType.REPORT_REWARD,
                status = OrderStatus.COMPLETED,
            )
        )
        reportWriter.approve(reportId)
        addAdminBlacklist(report.reportedProfileId)
        return ReportResponse(reportId, ReportStatus.APPROVED, report.reportedProfileId)
    }

    private fun addAdminBlacklist(profileId: Long) {
        if (blacklistReader.existsByProfileId(profileId)) {
            blacklistWriter.updateToAdminBlacklist(profileId)
        } else {
            blacklistWriter.save(Blacklist(profileId = profileId, createdByAdmin = true))
        }
    }
}

data class ReportResponse(val reportId: Long, val status: ReportStatus, val reportedProfileId: Long)
