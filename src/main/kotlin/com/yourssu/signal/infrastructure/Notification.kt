package com.yourssu.signal.infrastructure

import com.yourssu.signal.domain.verification.implement.domain.Verification
import com.yourssu.signal.infrastructure.deposit.SMSMessage
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

object Notification {
    fun notifyTicketIssued(verification: Verification, ticket: Int, availableTicket: Int) {
        logger.info { "Issued ticket&${verification.verificationCode.value} $ticket $availableTicket" }
    }

    fun notifyConsumedTicket(nickname: String, ticket: Int) {
        logger.info { "Consumed ticket&$nickname $ticket" }
    }

    fun notifyIssueTicketByBankDepositSms(message: SMSMessage) {
        logger.info { "IssueTicketByBankDepositSms&${message.name} ${message.depositAmount}" }
    }

    fun notifyIssueFailedTicketByBankDepositSms(message: SMSMessage) {
        logger.info { "IssueFailedTicketByBankDepositSms&${message.name} ${message.depositAmount}" }
    }
}
