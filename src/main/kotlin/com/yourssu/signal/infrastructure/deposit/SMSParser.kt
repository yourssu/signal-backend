package com.yourssu.signal.infrastructure.deposit

import com.yourssu.signal.infrastructure.deposit.exception.InvalidBankException

interface SMSParser {
    val type: String

    fun parse(message: String): SMSMessage

    companion object {
        private val parsers: List<SMSParser> = listOf(
            KakaoBankSMSParser,
            KbBankSMSParser
        )

        fun of(type: String): SMSParser {
            return parsers.find { it.type == type }
                ?: throw InvalidBankException(type)
        }
    }
}
