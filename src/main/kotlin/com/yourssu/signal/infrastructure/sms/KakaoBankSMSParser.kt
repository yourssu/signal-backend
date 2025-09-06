package com.yourssu.signal.infrastructure.sms

import com.yourssu.signal.infrastructure.sms.exception.NotDepositMessageException

object KakaoBankSMSParser: SMSParser {
    override val type: String = "kakao_sms"
    
    override fun parse(message: String): SMSMessage {
        val lines = message.split("\n")
        val depositLine = lines.find { it.contains("입금") } ?: throw NotDepositMessageException()
        val depositAmount = depositLine.filter { it.isDigit() }.toInt()
        val name = lines[lines.indexOf(depositLine) + 1]
        return SMSMessage(depositAmount, name)
    }
}
