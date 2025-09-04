package com.yourssu.signal.domain.referral.business

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.order.implement.OrderHistoryWriter
import com.yourssu.signal.domain.order.implement.OrderType
import com.yourssu.signal.domain.order.implement.domain.OrderStatus
import com.yourssu.signal.domain.order.implement.OrderHistory
import com.yourssu.signal.domain.referral.business.command.ReferralCodeGenerateCommand
import com.yourssu.signal.domain.referral.business.dto.ReferralCodeResponse
import com.yourssu.signal.domain.referral.implement.ReferralOrderReader
import com.yourssu.signal.domain.referral.implement.ReferralOrderWriter
import com.yourssu.signal.domain.referral.implement.ReferralReader
import com.yourssu.signal.domain.referral.implement.ReferralWriter
import com.yourssu.signal.domain.referral.implement.domain.Referral
import com.yourssu.signal.domain.referral.implement.domain.ReferralOrder
import com.yourssu.signal.domain.user.implement.UserReader
import com.yourssu.signal.domain.viewer.implement.ViewerReader
import com.yourssu.signal.domain.viewer.implement.ViewerWriter
import com.yourssu.signal.domain.viewer.implement.domain.Viewer
import com.yourssu.signal.infrastructure.Notification
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class ReferralService(
    private val userReader: UserReader,
    private val referralWriter: ReferralWriter,
    private val referralReader: ReferralReader,
    private val referralOrderWriter: ReferralOrderWriter,
    private val referralOrderReader: ReferralOrderReader,
    private val viewerReader: ViewerReader,
    private val viewerWriter: ViewerWriter,
    private val orderHistoryWriter: OrderHistoryWriter,
) {
    companion object {
        private const val MIN_TICKETS_FOR_REFERRAL_BONUS = 1
        private const val REFERRAL_BONUS_AMOUNT = 1
    }

    fun generateReferralCode(command: ReferralCodeGenerateCommand): ReferralCodeResponse {
        val user = userReader.getByUuid(command.toUuid())
        val referral = referralWriter.save(
            Referral(
                origin = user.uuid.value,
                referralCode = command.referralCode()
            )
        )
        return ReferralCodeResponse(referral.referralCode)
    }

    fun createReferralOrder(referralCode: String?, uuid: Uuid) {
        if (!referralCode.isNullOrBlank() && referralReader.findByReferralCode(referralCode) != null) {
            referralOrderWriter.save(ReferralOrder(referralCode = referralCode, viewerUuid = uuid))
        }
    }

    @Transactional
    fun processReferralBonus(viewer: Viewer, ticketCount: Int, verification: com.yourssu.signal.domain.verification.implement.domain.Verification) {
        if (ticketCount <= MIN_TICKETS_FOR_REFERRAL_BONUS) {
            return
        }
        val referralOrder = referralOrderReader.findByViewerUuid(viewer.uuid.value) ?: return
        val referee = viewerReader.get(Uuid(referralOrder.viewerUuid.value))
        if (referee.uuid == viewer.uuid) {
            return
        }
        val updatedReferrer = viewerWriter.issueTicket(
            uuid = referee.uuid,
            ticket = REFERRAL_BONUS_AMOUNT,
        )
        createReferralBonusOrderHistory(referee.uuid.value)
        Notification.notifyTicketIssued(verification, REFERRAL_BONUS_AMOUNT, updatedReferrer.ticket - updatedReferrer.usedTicket)
        referralOrderWriter.delete(referralOrder)
    }

    private fun createReferralBonusOrderHistory(uuid: String) {
        val orderHistory = OrderHistory(
            uuid = Uuid(uuid),
            amount = 0,
            quantity = REFERRAL_BONUS_AMOUNT,
            orderType = OrderType.REFERRAL_BONUS,
            status = OrderStatus.COMPLETED,
        )
        orderHistoryWriter.createOrderHistory(orderHistory)
    }
}
