package com.yourssu.signal.infrastructure.logging

import com.yourssu.signal.domain.profile.implement.Profile
import com.yourssu.signal.domain.verification.implement.Verification
import com.yourssu.signal.infrastructure.sms.SMSMessage
import java.time.LocalDateTime
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger("com.yourssu.signal.infrastructure.logging.Notification")
private val diagnosticLogger = KotlinLogging.logger("com.yourssu.signal.infrastructure.logging.BusinessEvent")

object Notification {
    fun notifyFalseContactReport(reportId: Long, profileId: Long, contact: String, createdTime: LocalDateTime) {
        logger.info {
            "FalseContactReport&$reportId&$profileId&${escapeEventField(contact)}&$createdTime"
        }
    }

    fun notifyCreatedProfile(profile: Profile) {
        logger.info {
            "CreateProfile&${profile.id}" +
                    "&${escapeEventField(profile.department)}" +
                    "&${escapeEventField(profile.contact)}" +
                    "&${escapeEventField(profile.nickname)}" +
                    "&${escapeEventField(profile.introSentences.joinToString(","))}"
        }
        diagnosticLogger.info { "eventType=CREATE_PROFILE profileId=${profile.id} outcome=SUCCESS" }
    }

    fun notifyContactExceedsLimitWarning(contactLimitPolicy: Int) {
        logger.info { "ContactExceedsLimitWarning&${contactLimitPolicy + 1}" }
        diagnosticLogger.info { "eventType=CONTACT_LIMIT_WARNING outcome=SUCCESS" }
    }

    fun notifyFailedProfileContactExceedsLimit(contactLimitPolicy: Int) {
        logger.info { "FailedProfileContactExceedsLimit&${contactLimitPolicy + 1}" }
        diagnosticLogger.info { "eventType=CREATE_PROFILE outcome=FAILURE reason=CONTACT_LIMIT" }
    }

    fun notifyTicketIssued(verification: Verification, ticket: Int, availableTicket: Int) {
        logger.info { "Issued ticket&${verification.verificationCode.value} ${verification.uuid.value.take(8)} $ticket $availableTicket" }
        diagnosticLogger.info { "eventType=ISSUE_TICKET userId=${verification.uuid.value.take(8)} outcome=SUCCESS" }
    }

    fun notifyRetryTicketIssued(message: String, verification: Verification, ticket: Int, availableTicket: Int) {
        logger.info { "RetryIssuedTicket&${verification.verificationCode.value} ${verification.uuid.value.take(8)} $ticket $availableTicket ${message.trim()}" }
        diagnosticLogger.info { "eventType=RETRY_ISSUE_TICKET userId=${verification.uuid.value.take(8)} outcome=SUCCESS" }
    }

    fun notifyConsumedTicket(nickname: String, ticket: Int) {
        logger.info { "Consumed ticket&$nickname $ticket" }
        diagnosticLogger.info { "eventType=CONSUME_TICKET outcome=SUCCESS" }
    }

    fun notifyIssueTicketByBankDepositSms(message: SMSMessage) {
        logger.info { "IssueTicketByBankDepositSms&${message.name} ${message.depositAmount} ${message.remainingAmount ?: 0}" }
        diagnosticLogger.info { "eventType=BANK_DEPOSIT_TICKET outcome=SUCCESS" }
    }

    fun notifyIssueFailedTicketByDepositAmount(message: SMSMessage) {
        logger.info { "IssueFailedTicketByDepositAmount&${message.name} ${message.depositAmount}" }
        diagnosticLogger.info { "eventType=BANK_DEPOSIT_TICKET outcome=FAILURE reason=AMOUNT_MISMATCH" }
    }

    fun notifyIssueFailedTicketByUnMatchedVerification(message: SMSMessage) {
        logger.info { "IssueFailedTicketByUnMatchedVerification&${message.name} ${message.depositAmount}" }
        diagnosticLogger.info { "eventType=BANK_DEPOSIT_TICKET outcome=FAILURE reason=VERIFICATION_NOT_FOUND" }
    }

    fun notifyPayDeposit(message: String, verificationCode: Int) {
        logger.info { "PayNotification&${validateMessage(message)} $verificationCode" }
        diagnosticLogger.info { "eventType=PAYMENT_NOTIFICATION outcome=SUCCESS" }
    }

    private fun validateMessage(message: String): String {
        val sanitizedMessage = message.replace(Regex("[\\r\\n\\t\\x0b\\x0c\\s]+"), "")
        return sanitizedMessage.filter { it.isISOControl().not() }
    }

    private fun escapeEventField(value: String): String = buildString {
        value.forEach { character ->
            append(
                when {
                    character == '%' -> "%25"
                    character == '&' -> "%26"
                    character.isISOControl() -> "%u${character.code.toString(16).padStart(4, '0')}"
                    else -> character
                }
            )
        }
    }
}
