package com.yourssu.signal.domain.profile.implement.exception

import com.yourssu.signal.handler.BadRequestException

class ContactFormatViolatedException : BadRequestException(message = "연락처 형식이 올바르지 않습니다.")
