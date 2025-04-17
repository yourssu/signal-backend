package com.yourssu.signal.domain.profile.implement.exception

import com.yourssu.signal.handler.ConflictException

class GenderMismatchException : ConflictException(message = "성별이 일치하지 않습니다.") {
}
