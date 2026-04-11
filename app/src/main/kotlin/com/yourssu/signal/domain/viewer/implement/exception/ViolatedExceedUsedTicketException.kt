package com.yourssu.signal.domain.viewer.implement.exception

import com.yourssu.signal.handler.BadRequestException

class ViolatedExceedUsedTicketException : BadRequestException(message = "사용할 수 있는 티켓을 초과했습니다.") {
}
