package com.yourssu.signal.domain.order.implement.exception

import com.yourssu.signal.handler.BadRequestException

class OrderHistoryUpdateFailedException() : BadRequestException(message = "status update failed")
