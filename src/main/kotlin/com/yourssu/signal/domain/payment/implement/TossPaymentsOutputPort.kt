package com.yourssu.signal.domain.payment.implement

import com.yourssu.signal.domain.payment.implement.dto.OrderApprovalRequest
import com.yourssu.signal.domain.payment.implement.dto.TossPaymentsApprovalResponse
import com.yourssu.signal.domain.payment.implement.dto.OrderReadyRequest
import com.yourssu.signal.domain.payment.implement.dto.TossPaymentsReadyResponse

interface TossPaymentsOutputPort {
    fun approve(request: OrderApprovalRequest): TossPaymentsApprovalResponse
}
