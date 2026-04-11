package com.yourssu.signal.domain.profile.implement.exception

import com.yourssu.signal.handler.BadRequestException

class NicknameLengthViolatedException : BadRequestException(message = "Nickname field must be less than 32 characters") {
}
