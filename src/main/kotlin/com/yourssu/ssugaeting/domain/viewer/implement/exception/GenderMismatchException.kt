package com.yourssu.ssugaeting.domain.viewer.implement.exception

import com.yourssu.ssugaeting.handler.ConflictException

class GenderMismatchException : ConflictException(message = "성별이 일치하지 않습니다.") {
}
