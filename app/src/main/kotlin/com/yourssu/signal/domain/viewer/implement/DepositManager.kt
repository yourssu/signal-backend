package com.yourssu.signal.domain.viewer.implement

import VerificationCode
import com.yourssu.signal.domain.viewer.implement.dto.DepositResult
import com.yourssu.signal.domain.viewer.implement.exception.NotFoundDepositNameException
import com.yourssu.signal.domain.viewer.implement.exception.NotFoundVerificationCode
import com.yourssu.signal.domain.viewer.implement.exception.TicketIssuedFailedException
import com.yourssu.signal.infrastructure.logging.Notification
import com.yourssu.signal.infrastructure.sms.SMSMessage
import com.yourssu.signal.infrastructure.sms.SMSParser
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class DepositManager(
    private val ticketPricePolicy: TicketPricePolicy,
    private val verificationReader: VerificationReader,
) {
    private val smsRecord = ConcurrentHashMap<String, SMSMessage>()

    fun processDepositSms(type: String, message: String): DepositResult {
        val messageParser = SMSParser.of(type)
        val message = messageParser.parse(message = message)
        return toCodeAndTicket(message)
    }

    fun retryDepositSms(message: String, verificationCode: VerificationCode): Int {
        val smsMessage = smsRecord[message] ?: throw NotFoundDepositNameException()
        val ticket = ticketPricePolicy.calculateTicketQuantity(smsMessage.depositAmount, verificationCode)
        validateAmount(ticket, smsMessage)
        smsRecord.remove(message)
        return ticket
    }

    private fun toCodeAndTicket(message: SMSMessage): DepositResult {
        Notification.notifyIssueTicketByBankDepositSms(message)
        val code = toVerificationCode(message)
        val ticket = ticketPricePolicy.calculateTicketQuantity(message.depositAmount, code)
        validateAmount(ticket, message)
        return DepositResult(
            verificationCode = code,
            ticket = ticket,
            depositAmount = message.depositAmount
        )
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
            throw NotFoundVerificationCode(message.name)
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
