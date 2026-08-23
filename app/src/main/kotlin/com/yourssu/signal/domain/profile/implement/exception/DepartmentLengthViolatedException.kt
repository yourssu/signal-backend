package com.yourssu.signal.domain.profile.implement.exception

import com.yourssu.signal.handler.BadRequestException

class DepartmentLengthViolatedException : BadRequestException(message = "학과 길이가 올바르지 않습니다.")
