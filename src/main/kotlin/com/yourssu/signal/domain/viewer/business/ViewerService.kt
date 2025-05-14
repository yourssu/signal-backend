package com.yourssu.signal.domain.viewer.business

import com.yourssu.signal.domain.profile.business.dto.PurchasedProfileResponse
import com.yourssu.signal.domain.profile.implement.PurchasedProfileReader
import com.yourssu.signal.domain.verification.implement.VerificationWriter
import com.yourssu.signal.domain.verification.implement.domain.VerificationCode
import com.yourssu.signal.domain.viewer.business.command.AllViewersFoundCommand
import com.yourssu.signal.domain.viewer.business.command.TicketIssuedCommand
import com.yourssu.signal.domain.viewer.business.command.VerificationCommand
import com.yourssu.signal.domain.viewer.business.command.ViewerFoundCommand
import com.yourssu.signal.domain.viewer.business.dto.ProcessDepositSmsCommand
import com.yourssu.signal.domain.viewer.business.dto.VerificationResponse
import com.yourssu.signal.domain.viewer.business.dto.ViewerDetailResponse
import com.yourssu.signal.domain.viewer.business.dto.ViewerResponse
import com.yourssu.signal.domain.viewer.implement.*
import com.yourssu.signal.infrastructure.Notification
import com.yourssu.signal.infrastructure.SMSParser
import org.springframework.stereotype.Service

@Service
class ViewerService(
    private val verificationWriter: VerificationWriter,
    private val verificationReader: VerificationReader,
    private val viewerWriter: ViewerWriter,
    private val viewerReader: ViewerReader,
    private val purchasedProfileReader: PurchasedProfileReader,
    private val adminAccessChecker: AdminAccessChecker,
    private val ticketPricePolicy: TicketPricePolicy,
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
        val message = SMSParser.parse(command.message)
        val code = VerificationCode.from(message.name)
        val ticket = ticketPricePolicy.calculateTicketQuantity(message.depositAmount)
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
}
