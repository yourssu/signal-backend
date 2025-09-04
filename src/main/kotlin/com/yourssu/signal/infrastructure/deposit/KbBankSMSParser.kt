package com.yourssu.signal.infrastructure.deposit

import com.yourssu.signal.infrastructure.deposit.exception.NotDepositMessageException

object KbBankSMSParser: SMSParser {
    override fun parse(message: String): SMSMessage {
        val lines = message.split("\n").map { it.trim() }
        if (lines[4] != "입금") {
            throw NotDepositMessageException()
        }
        val name = lines[3]
        val depositAmount = lines[5].filter { it.isDigit() }.toInt()
        val remainingAmount = lines[6].replace("잔액", "").filter { it.isDigit() }.toInt()
        return SMSMessage(depositAmount, name, remainingAmount)
    }
}
