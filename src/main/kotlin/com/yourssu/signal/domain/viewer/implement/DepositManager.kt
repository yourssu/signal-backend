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
        val code = toVerificationCode(message)
        val ticket = ticketPricePolicy.calculateTicketQuantity(message.depositAmount, code)
        validateAmount(ticket, message)
        return Pair(code, ticket)
    }

    private fun toVerificationCode(message: SMSMessage): VerificationCode {
        if (message.name.toIntOrNull() == null) {
            Notification.notifyIssueFailedTicketByUnMatchedVerification(message)
            throw TicketIssuedFailedException("${message.name} is not a number")
        }
        val code = VerificationCode.from(message.name)
        if (!verificationReader.existsByCode(code)) {
            Notification.notifyIssueFailedTicketByUnMatchedVerification(message)
            throw TicketIssuedFailedException("${message.name} is not in verification code list")
        }
        return code
    }

    private fun validateAmount(
        ticket: Int,
        message: SMSMessage,
    ) {
        if (ticket == 0) {
            Notification.notifyIssueFailedTicketByDepositAmount(message)
            throw TicketIssuedFailedException("${message.depositAmount} is not a valid ticket amount")
        }
    }
}
