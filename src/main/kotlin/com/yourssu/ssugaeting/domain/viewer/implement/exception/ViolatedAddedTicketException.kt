package com.yourssu.ssugaeting.domain.viewer.implement.exception

import com.yourssu.soongpt.common.handler.BadRequestException

class ViolatedAddedTicketException() : BadRequestException(message = "최소 추가 수량이 입력되지 않았습니다.") {
}
