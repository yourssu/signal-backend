package com.yourssu.signal.api.dto

import jakarta.validation.Validation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ReportRequestsTest {
    private val validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    fun `신고 profileId는 양수여야 한다`() {
        assertEquals(1, validator.validate(ReportCreatedRequest(0)).size)
        assertEquals(0, validator.validate(ReportCreatedRequest(1)).size)
    }

    @Test
    fun `승인 secretKey는 공백일 수 없다`() {
        assertEquals(1, validator.validate(ReportApprovedRequest(" ")).size)
    }
}
