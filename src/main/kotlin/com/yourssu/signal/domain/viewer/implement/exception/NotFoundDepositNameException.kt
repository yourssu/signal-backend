package com.yourssu.signal.domain.viewer.implement.exception

import com.yourssu.signal.handler.NotFoundException

class NotFoundDepositNameException: NotFoundException(message = "Not Found Deposit Name") {
}
