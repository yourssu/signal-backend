package com.yourssu.signal.domain.profile.implement.exception

import com.yourssu.signal.handler.BadRequestException

class ContactLimitExceededException(contactPolicy: Int): BadRequestException(message = "등록 가능 연락처 개수 초과: $contactPolicy 개") {
}
