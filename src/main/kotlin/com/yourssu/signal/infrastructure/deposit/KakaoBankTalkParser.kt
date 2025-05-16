package com.yourssu.signal.infrastructure.deposit

object KakaoBankTalkParser : SMSParser {
    override fun parse(message: String): SMSMessage {
        val lines = message.split("\n")
        val depositLine = lines.find { it.contains("입금") } ?: ""
        val depositAmount = depositLine.filter { it.isDigit() }.toInt()
        val name = lines[lines.indexOf(depositLine) + 1].split("→")[0].trim()
        return SMSMessage(depositAmount, name)
    }
}
