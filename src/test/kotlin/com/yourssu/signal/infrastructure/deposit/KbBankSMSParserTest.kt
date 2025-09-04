package com.yourssu.signal.infrastructure.deposit

import com.yourssu.signal.infrastructure.deposit.exception.NotDepositMessageException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class KbBankSMSParserTest {
    
    private val parser = KbBankSMSParser

    @Test
    fun `정상적인 KB 입금 SMS를 파싱한다`() {
        val message = """
            [Web발신]
            [KB]09/04 00:03
            834702**717
            남궁현
            입금
            1
            잔액11,892
        """.trimIndent()

        val result = parser.parse(message)

        assertEquals(1, result.depositAmount)
        assertEquals("남궁현", result.name)
        assertEquals(11892, result.remainingAmount)
    }

    @Test
    fun `입금 문자열이 없는 SMS에서 예외가 발생한다`() {
        val message = """
            [Web발신]
            [KB]09/04 00:03
            834702**717
            남궁현
            출금
            1000
            잔액11,892
        """.trimIndent()

        assertThrows<NotDepositMessageException> {
            parser.parse(message)
        }
    }

    @Test
    fun `큰 금액을 올바르게 파싱한다`() {
        val message = """
            [Web발신]
            [KB]09/04 00:03
            834702**717
            김철수
            입금
            50000
            잔액1,200,892
        """.trimIndent()

        val result = parser.parse(message)

        assertEquals(50000, result.depositAmount)
        assertEquals("김철수", result.name)
        assertEquals(1200892, result.remainingAmount)
    }

    @Test
    fun `콤마가 포함된 잔액을 올바르게 파싱한다`() {
        val message = """
            [Web발신]
            [KB]09/04 00:03
            834702**717
            이영희
            입금
            100
            잔액123,456,789
        """.trimIndent()

        val result = parser.parse(message)

        assertEquals(100, result.depositAmount)
        assertEquals("이영희", result.name)
        assertEquals(123456789, result.remainingAmount)
    }
}