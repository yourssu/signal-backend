package com.yourssu.signal.infrastructure

import com.yourssu.signal.domain.verification.implement.domain.Verification
import com.yourssu.signal.infrastructure.deposit.SMSMessage
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

object Notification {
    fun notifyTicketIssued(verification: Verification, ticket: Int, availableTicket: Int) {
        logger.info { "Issued ticket&${verification.verificationCode.value} ${verification.uuid.value} $ticket $availableTicket" }
    }

    fun notifyConsumedTicket(nickname: String, ticket: Int) {
        logger.info { "Consumed ticket&$nickname $ticket" }
    }

    fun notifyIssueTicketByBankDepositSms(message: SMSMessage) {
        logger.info { "IssueTicketByBankDepositSms&${message.name} ${message.depositAmount}" }
    }

    fun notifyIssueFailedTicketByDepositAmount(message: SMSMessage) {
        logger.info { "IssueFailedTicketByDepositAmount&${message.name} ${message.depositAmount}" }
    }

    fun notifyIssueFailedTicketByUnMatchedVerification(message: SMSMessage) {
        logger.info { "IssueFailedTicketByUnMatchedVerification&${message.name} ${message.depositAmount}" }
    }

    fun notifyDeposit(message: String) {
        logger.info { "PayNotification&${validateMessage(message)}" }
    }

    fun validateMessage(message: String): String {
        val sanitizedMessage = message.replace(Regex("[\\r\\n\\t\\x0b\\x0c]+"), "")
        return sanitizedMessage.filter { it.isISOControl().not() }
    }
}
