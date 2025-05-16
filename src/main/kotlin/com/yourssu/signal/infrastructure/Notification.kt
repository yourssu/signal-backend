package com.yourssu.signal.infrastructure

import com.yourssu.signal.domain.verification.implement.domain.Verification
import com.yourssu.signal.domain.verification.implement.domain.VerificationCode
import com.yourssu.signal.infrastructure.deposit.SMSMessage
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

object Notification {
    fun notifyTicketIssued(verification: Verification, ticket: Int, availableTicket: Int) {
        logger.info { "Issued ticket&${verification.verificationCode.value} ${verification.uuid.value.take(8)} $ticket $availableTicket" }
    }

    fun notifyRetryTicketIssued(message: String, verification: Verification, ticket: Int, availableTicket: Int) {
        logger.info { "RetryIssuedTicket&${verification.verificationCode.value} ${verification.uuid.value.take(8)} $ticket $availableTicket ${message.trim()}" }
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

    fun notifyNoFirstPurchasedTicket(price: Int, code: VerificationCode) {
        logger.info { "NoFirstPurchasedTicket&${code.value} $price" }
    }

    fun notifyDeposit(message: String, verificationCode: Int) {
        logger.info { "PayNotification&${validateMessage(message)} $verificationCode" }
    }

    private fun validateMessage(message: String): String {
        val sanitizedMessage = message.replace(Regex("[\\r\\n\\t\\x0b\\x0c\\s]+"), "")
        return sanitizedMessage.filter { it.isISOControl().not() }
    }
}
