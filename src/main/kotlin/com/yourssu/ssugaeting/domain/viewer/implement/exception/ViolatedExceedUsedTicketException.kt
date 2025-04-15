package com.yourssu.ssugaeting.domain.viewer.implement.exception

import com.yourssu.ssugaeting.handler.BadRequestException

class ViolatedExceedUsedTicketException : BadRequestException(message = "사용할 수 있는 티켓을 초과했습니다.") {
}
