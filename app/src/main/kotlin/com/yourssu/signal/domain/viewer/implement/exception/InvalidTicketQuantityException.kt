package com.yourssu.signal.domain.viewer.implement.exception

import com.yourssu.signal.handler.BadRequestException

class InvalidTicketQuantityException(
    val requestedQuantity: Int,
    val expectedQuantity: Int,
    val price: Int
) : BadRequestException(
    message = "요청한 티켓 수량($requestedQuantity)과 가격 정책에 따른 티켓 수량($expectedQuantity)이 일치하지 않습니다. (${price}원)"
)
