package com.yourssu.signal.domain.payment.implement.exception

import com.yourssu.signal.handler.ForbiddenException

class OrderOwnerMismatchException : ForbiddenException(message = "Order owner mismatch.")
