package com.yourssu.signal.domain.profile.implement.exception

import com.yourssu.signal.handler.BadRequestException

class IntroSentenceSizeViolatedException : BadRequestException(message = "Intro sentence size violated") {
}
