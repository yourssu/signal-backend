package com.yourssu.signal.domain.viewer.implement

import com.yourssu.signal.domain.verification.implement.domain.VerificationCode
import com.yourssu.signal.domain.viewer.implement.exception.TicketIssuedFailedException
import com.yourssu.signal.infrastructure.Notification
import com.yourssu.signal.infrastructure.deposit.SMSMessage
import com.yourssu.signal.infrastructure.deposit.SMSParser
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.ConcurrentHashMap

@Component
class DepositManager(
    private val ticketPricePolicy: TicketPricePolicy,
    private val verificationReader: VerificationReader,
) {
    private val smsRecord = ConcurrentHashMap<String, SMSMessage>()

    fun processDepositSms(type: String, message: String): Pair<VerificationCode, Int> {
        val messageParser = SMSParser.of(type)
        val message = messageParser.parse(message = message)
        return toCodeAndTicket(message)
    }

    @Transactional
    fun retryDepositSms(message: String, verificationCode: VerificationCode): Int {
        val smsMessage = smsRecord[message] ?: throw TicketIssuedFailedException("No message found for $message")
        smsRecord.remove(message)
        val ticket = ticketPricePolicy.calculateTicketQuantity(smsMessage.depositAmount, verificationCode)
        validateAmount(ticket, smsMessage)
        return ticket
    }

    private fun toCodeAndTicket(message: SMSMessage): Pair<VerificationCode, Int> {
        Notification.notifyIssueTicketByBankDepositSms(message)
        val code = toVerificationCode(message)
        val ticket = ticketPricePolicy.calculateTicketQuantity(message.depositAmount, code)
        validateAmount(ticket, message)
        return Pair(code, ticket)
    }

    private fun toVerificationCode(message: SMSMessage): VerificationCode {
        if (message.name.toIntOrNull() == null) {
            smsRecord[message.name] = message
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
        if (ticket == NO_MATCH_TICKET_AMOUNT) {
            Notification.notifyIssueFailedTicketByDepositAmount(message)
            throw TicketIssuedFailedException("${message.depositAmount} is not a valid ticket amount")
        }
    }

    fun existsByMessage(message: String): Boolean {
        return smsRecord.containsKey(message)
    }
}
