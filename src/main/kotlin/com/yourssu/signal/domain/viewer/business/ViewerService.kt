package com.yourssu.signal.domain.viewer.business

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.order.implement.OrderHistory
import com.yourssu.signal.domain.order.implement.OrderHistoryWriter
import com.yourssu.signal.domain.order.implement.OrderType
import com.yourssu.signal.domain.order.implement.OrderStatus
import com.yourssu.signal.domain.profile.business.dto.PurchasedProfileResponse
import com.yourssu.signal.domain.profile.implement.PurchasedProfileReader
import com.yourssu.signal.domain.referral.business.ReferralService
import com.yourssu.signal.domain.verification.implement.VerificationWriter
import com.yourssu.signal.domain.viewer.business.command.*
import com.yourssu.signal.domain.viewer.business.dto.TicketPackagesResponses
import com.yourssu.signal.domain.viewer.business.dto.VerificationResponse
import com.yourssu.signal.domain.viewer.business.dto.ViewerDetailResponse
import com.yourssu.signal.domain.viewer.business.dto.ViewerResponse
import com.yourssu.signal.domain.viewer.business.exception.TicketIssuedFailedException
import com.yourssu.signal.domain.viewer.implement.*
import com.yourssu.signal.infrastructure.logging.Notification
import org.springframework.stereotype.Service

@Service
class ViewerService(
    private val verificationWriter: VerificationWriter,
    private val verificationReader: VerificationReader,
    private val viewerWriter: ViewerWriter,
    private val viewerReader: ViewerReader,
    private val purchasedProfileReader: PurchasedProfileReader,
    private val orderHistoryWriter: OrderHistoryWriter,
    private val referralService: ReferralService,
    private val adminAccessChecker: AdminAccessChecker,
    private val depositManager: DepositManager,
    private val ticketPricePolicy: TicketPricePolicy,
) {
    fun issueVerificationCode(command: IssuedVerificationCommand): VerificationResponse {
        val code = verificationWriter.issueVerificationCode(uuid = Uuid(command.uuid))
        referralService.createReferralOrder(command.referralCode, command.toUuid())
        return VerificationResponse.from(code)
    }

    fun issueTicketForAdmin(command: TicketIssuedCommand): ViewerResponse {
        val response = issueTicket(command)
        createOrderHistory(uuid = response.uuid, quantity = command.ticket, orderType = OrderType.ADMIN_CHARGE)
        return response
    }

    fun issueTicket(command: ProcessDepositSmsCommand): ViewerResponse {
        adminAccessChecker.validateAdminAccess(command.secretKey)
        val depositResult = depositManager.processDepositSms(type = command.type, message = command.message)
        val ticketIssuedCommand = TicketIssuedCommand(
            secretKey = command.secretKey,
            verificationCode = depositResult.verificationCode.value,
            ticket = depositResult.ticket,
        )
        val response = issueTicket(ticketIssuedCommand)
        createOrderHistory(response.uuid, depositResult.depositAmount, depositResult.ticket, OrderType.DEPOSIT_SMS)
        return response
    }

    private fun issueTicket(command: TicketIssuedCommand): ViewerResponse {
        adminAccessChecker.validateAdminAccess(command.secretKey)
        val verification = verificationReader.findByCode(command.toVerificationCode())
        val viewer = viewerWriter.issueTicket(
            uuid = verification.uuid,
            ticket = command.ticket,
        )
        verificationWriter.remove(verification.uuid)
        Notification.notifyTicketIssued(verification, command.ticket, viewer.ticket - viewer.usedTicket)
        referralService.processReferralBonus(viewer, command.ticket, verification)
        return ViewerResponse.from(viewer)
    }

    private fun createOrderHistory(uuid: String, amount: Int = 0, quantity: Int, orderType: OrderType) {
        val orderHistory = OrderHistory(
            uuid = Uuid(uuid),
            amount = amount,
            quantity = quantity,
            orderType = orderType,
            status = OrderStatus.COMPLETED,
        )
        orderHistoryWriter.createOrderHistory(orderHistory)
    }

    fun getViewer(uuid: String): ViewerDetailResponse {
        val viewer = viewerReader.get(Uuid(uuid))
        val purchasedProfiles = purchasedProfileReader.findByViewerId(viewer.id!!)
            .map { PurchasedProfileResponse.from(it) }
        return ViewerDetailResponse.from(viewer, purchasedProfiles)
    }

    fun findAllViewers(command: AllViewersFoundCommand): List<ViewerResponse> {
        adminAccessChecker.validateAdminAccess(command.secretKey)
        val viewers = viewerReader.findAll()
        return viewers.map { ViewerResponse.from(it) }
    }

    fun issueTicketByDepositName(command: NotificationDepositCommand): ViewerResponse {
        validateUnMatchedDeposit(command)
        val verification = verificationReader.findByCode(command.toCode())
        val ticket = depositManager.retryDepositSms(command.message, command.toCode())
        val viewer = viewerWriter.issueTicket(
            uuid = verification.uuid,
            ticket = ticket,
        )
        verificationWriter.remove(verification.uuid)
        Notification.notifyRetryTicketIssued(command.message, verification, ticket, viewer.ticket - viewer.usedTicket)
        return ViewerResponse.from(viewer)
    }

    private fun validateUnMatchedDeposit(command: NotificationDepositCommand) {
        if (!depositManager.existsByMessage(command.message)) {
            Notification.notifyPayDeposit(command.message, command.verificationCode)
            throw TicketIssuedFailedException("${command.message} is not a valid deposit name")
        }
    }

    fun getTicketPackages(): TicketPackagesResponses {
        val ticketPackages = ticketPricePolicy.getTicketPackages()
        return TicketPackagesResponses.from(ticketPackages)
    }
}
