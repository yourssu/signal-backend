package com.yourssu.signal.domain.profile.implement.exception

import com.yourssu.signal.handler.BadRequestException

class EgenTetoNotFoundException: BadRequestException(message = "해당하는 에겐/테토 유형을 찾을 수 없습니다.") {
}
