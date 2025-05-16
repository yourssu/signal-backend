package com.yourssu.signal.infrastructure.deposit

import com.yourssu.signal.infrastructure.deposit.exception.InvalidBankException

private const val KAKAO_BANK_SMS = "kakao_sms"
private const val KAKAO_BANK_TALK = "kakao_talk"

interface SMSParser {
    fun parse(message: String): SMSMessage

    companion object {
        fun of(type: String): SMSParser {
            return when (type) {
                KAKAO_BANK_SMS -> KakaoBankSMSParser
                KAKAO_BANK_TALK -> KakaoBankTalkParser
                else -> throw InvalidBankException(type)
            }
        }
    }
}
