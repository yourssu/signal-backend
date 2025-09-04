package com.yourssu.signal.domain.order.implement.exception

import com.yourssu.signal.handler.NotFoundException

class OrderHistoryNotFoundException() : NotFoundException(message = "Order history not found")
