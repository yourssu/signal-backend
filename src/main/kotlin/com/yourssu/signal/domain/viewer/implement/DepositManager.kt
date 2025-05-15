package com.yourssu.signal.domain.viewer.implement

import com.yourssu.signal.domain.verification.implement.domain.VerificationCode
import com.yourssu.signal.domain.viewer.business.exception.TicketIssuedFailedException
import com.yourssu.signal.infrastructure.Notification
import com.yourssu.signal.infrastructure.deposit.SMSMessage
import com.yourssu.signal.infrastructure.deposit.SMSParser
import org.springframework.stereotype.Component

@Component
class DepositManager(
    private val ticketPricePolicy: TicketPricePolicy,
    private val verificationReader: VerificationReader,
) {
    fun processDepositSms(type: String, message: String): Pair<VerificationCode, Int> {
        val messageParser = SMSParser.of(type)
        val message = messageParser.parse(message = message)
        Notification.notifyIssueTicketByBankDepositSms(message)
        validateSenderName(message)
        val code = VerificationCode.from(message.name)
        val ticket = ticketPricePolicy.calculateTicketQuantity(message.depositAmount)
        validateAmount(ticket, message)
        return Pair(code, ticket)
    }

    private fun validateSenderName(message: SMSMessage) {
        if (message.name.toIntOrNull() == null) {
            Notification.notifyIssueFailedTicketByUnMatchedVerification(message)
            throw TicketIssuedFailedException()
        }
        val code = VerificationCode.from(message.name)
        if (!verificationReader.existsByCode(code)) {
            Notification.notifyIssueFailedTicketByUnMatchedVerification(message)
            throw TicketIssuedFailedException()
        }
    }

    private fun validateAmount(
        ticket: Int,
        message: SMSMessage,
    ) {
        if (ticket == 0) {
            Notification.notifyIssueFailedTicketByDepositAmount(message)
            throw TicketIssuedFailedException()
        }
    }
}
