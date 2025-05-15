package com.yourssu.signal.domain.profile.implement.exception

import com.yourssu.signal.handler.BadRequestException

class IntroSentenceLengthViolatedException : BadRequestException(message = "Intro sentence length violated") {
}
