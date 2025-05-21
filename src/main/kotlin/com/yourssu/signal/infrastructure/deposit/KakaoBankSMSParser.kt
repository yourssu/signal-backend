package com.yourssu.signal.infrastructure.deposit

import com.yourssu.signal.infrastructure.deposit.exception.NotDepositMessageException

object KakaoBankSMSParser: SMSParser {
    override fun parse(message: String): SMSMessage {
        val lines = message.split("\n")
        val depositLine = lines.find { it.contains("입금") } ?: throw NotDepositMessageException()
        val depositAmount = depositLine.filter { it.isDigit() }.toInt()
        val name = lines[lines.indexOf(depositLine) + 1]
        return SMSMessage(depositAmount, name)
    }
}
