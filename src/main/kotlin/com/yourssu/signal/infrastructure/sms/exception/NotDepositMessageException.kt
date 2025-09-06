package com.yourssu.signal.infrastructure.sms.exception

import com.yourssu.signal.handler.BadRequestException

class NotDepositMessageException: BadRequestException(message = "입금 메세지가 아닙니다.") {
}
