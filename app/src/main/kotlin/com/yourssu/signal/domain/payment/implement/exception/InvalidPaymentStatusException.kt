package com.yourssu.signal.domain.payment.implement.exception

import com.yourssu.signal.domain.payment.implement.OrderStatus
import com.yourssu.signal.handler.BadRequestException

class InvalidPaymentStatusException(status: OrderStatus) : BadRequestException(message = "이미 처리된 결제입니다. 현재 상태: $status")
