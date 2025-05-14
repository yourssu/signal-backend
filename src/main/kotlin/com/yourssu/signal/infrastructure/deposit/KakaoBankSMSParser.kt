package com.yourssu.signal.infrastructure.deposit

object KakaoBankSMSParser: SMSParser {
    override fun parse(message: String): SMSMessage {
        val lines = message.split("\n")
        val depositLine = lines.find { it.contains("입금") } ?: ""
        val depositAmount = depositLine.filter { it.isDigit() }.toInt()
        val name = lines[lines.indexOf(depositLine) + 1]
        return SMSMessage(depositAmount, name)
    }
}
