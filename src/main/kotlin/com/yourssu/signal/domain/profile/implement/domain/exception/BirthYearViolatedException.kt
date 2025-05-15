package com.yourssu.signal.domain.profile.implement.domain.exception

import com.yourssu.signal.handler.BadRequestException

class BirthYearViolatedException() : BadRequestException(message = "출생년도가 유효하지 않습니다.") {
}
