package com.yourssu.signal.infrastructure.deposit

import com.yourssu.signal.infrastructure.deposit.exception.InvalidBankException

private const val KAKAO_BANK = "kakao"

interface SMSParser {
    fun parse(message: String): SMSMessage

    companion object {
        fun of(type: String): SMSParser {
            return when (type) {
                KAKAO_BANK -> KakaoBankSMSParser
                else -> throw InvalidBankException(type)
            }
        }
    }
}
