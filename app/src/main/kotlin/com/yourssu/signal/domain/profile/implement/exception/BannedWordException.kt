package com.yourssu.signal.domain.profile.implement.exception

import com.yourssu.signal.handler.BadRequestException

class BannedWordException : BadRequestException(message = "부적절한 입력입니다.")
