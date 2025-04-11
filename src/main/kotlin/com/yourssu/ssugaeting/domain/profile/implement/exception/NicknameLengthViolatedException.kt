package com.yourssu.ssugaeting.domain.profile.implement.exception

import com.yourssu.soongpt.common.handler.BadRequestException

class NicknameLengthViolatedException : BadRequestException(message = "Nickname field must be less than 32 characters") {
}
