package com.yourssu.signal.domain.profile.implement.exception

import com.yourssu.signal.handler.BadRequestException

class DummyContactException : BadRequestException(message = "실제 사용하는 연락처를 입력해 주세요.")
