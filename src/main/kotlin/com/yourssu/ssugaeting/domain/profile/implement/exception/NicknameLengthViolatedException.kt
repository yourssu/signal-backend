package com.yourssu.ssugaeting.domain.profile.implement.exception

import com.yourssu.ssugaeting.handler.BadRequestException

class NicknameLengthViolatedException : BadRequestException(message = "Nickname field must be less than 32 characters") {
}
