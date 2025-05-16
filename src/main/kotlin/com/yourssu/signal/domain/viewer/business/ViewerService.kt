package com.yourssu.signal.domain.viewer.business

import com.yourssu.signal.domain.profile.business.dto.PurchasedProfileResponse
import com.yourssu.signal.domain.profile.implement.PurchasedProfileReader
import com.yourssu.signal.domain.verification.implement.VerificationWriter
import com.yourssu.signal.domain.viewer.business.command.*
import com.yourssu.signal.domain.viewer.business.dto.ProcessDepositSmsCommand
import com.yourssu.signal.domain.viewer.business.dto.VerificationResponse
import com.yourssu.signal.domain.viewer.business.dto.ViewerDetailResponse
import com.yourssu.signal.domain.viewer.business.dto.ViewerResponse
import com.yourssu.signal.domain.viewer.business.exception.TicketIssuedFailedException
import com.yourssu.signal.domain.viewer.implement.*
import com.yourssu.signal.infrastructure.Notification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ViewerService(
    private val verificationWriter: VerificationWriter,
    private val verificationReader: VerificationReader,
    private val viewerWriter: ViewerWriter,
    private val viewerReader: ViewerReader,
    private val purchasedProfileReader: PurchasedProfileReader,
    private val adminAccessChecker: AdminAccessChecker,
    private val depositManager: DepositManager,
) {
    fun issueVerificationCode(command: VerificationCommand): VerificationResponse {
        val code = verificationWriter.issueVerificationCode(uuid = command.toUuid())
        return VerificationResponse.from(code)
    }

    fun issueTicket(command: TicketIssuedCommand): ViewerResponse {
        adminAccessChecker.validateAdminAccess(command.secretKey)
        val verification = verificationReader.findByCode(command.toVerificationCode())
        val viewer = viewerWriter.issueTicket(
            uuid = verification.uuid,
            ticket = command.ticket,
            )
        verificationWriter.remove(verification.uuid)
        Notification.notifyTicketIssued(verification, command.ticket, viewer.ticket - viewer.usedTicket)
        return ViewerResponse.from(viewer)
    }

    fun issueTicket(command: ProcessDepositSmsCommand): ViewerResponse {
        adminAccessChecker.validateAdminAccess(command.secretKey)
        val (code, ticket) = depositManager.processDepositSms(type = command.type, message = command.message)
        val ticketIssuedCommand = TicketIssuedCommand(secretKey = command.secretKey, verificationCode = code.value, ticket = ticket)
        return issueTicket(ticketIssuedCommand)
    }

    fun getViewer(command: ViewerFoundCommand): ViewerDetailResponse {
        val viewer = viewerReader.get(command.toDomain())
        val purchasedProfiles = purchasedProfileReader.findByViewerId(viewer.id!!)
            .map { PurchasedProfileResponse.from(it) }
        return ViewerDetailResponse.from(viewer, purchasedProfiles)
    }

    fun findAllViewers(command: AllViewersFoundCommand): List<ViewerResponse> {
        adminAccessChecker.validateAdminAccess(command.secretKey)
        val viewers = viewerReader.findAll()
        return viewers.map { ViewerResponse.from(it) }
    }

    @Transactional
    fun issueTicketByDepositName(command: NotificationDepositCommand): ViewerResponse {
        if (!depositManager.existsByMessage(command.message)) {
            Notification.notifyDeposit(command.message, command.verificationCode)
            throw TicketIssuedFailedException("${command.message} is not a valid deposit name")
        }
        val code = command.toCode()
        val verification = verificationReader.findByCode(code)
        val ticket = depositManager.retryDepositSms(command.message, code)
        val viewer = viewerWriter.issueTicket(
            uuid = verification.uuid,
            ticket = ticket,
        )
        verificationWriter.remove(verification.uuid)
        Notification.notifyTicketIssued(verification, ticket, viewer.ticket - viewer.usedTicket)
        return ViewerResponse.from(viewer)
    }
}
