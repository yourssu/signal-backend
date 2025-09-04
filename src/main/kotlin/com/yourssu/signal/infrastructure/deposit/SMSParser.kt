package com.yourssu.signal.infrastructure.deposit

import com.yourssu.signal.infrastructure.deposit.exception.InvalidBankException

private const val KAKAO_BANK_SMS = "kakao_sms"
private const val KAKAO_BANK_TALK = "kakao_talk"
private const val KB_BANK_SMS = "kb_sms"

interface SMSParser {
    fun parse(message: String): SMSMessage

    companion object {
        fun of(type: String): SMSParser {
            return when (type) {
                KAKAO_BANK_SMS -> KakaoBankSMSParser
                KAKAO_BANK_TALK -> KakaoBankTalkParser
                KB_BANK_SMS -> KbBankSMSParser
                else -> throw InvalidBankException(type)
            }
        }
    }
}
