package com.yourssu.ssugaeting.domain.profile.implement.exception

import com.yourssu.soongpt.common.handler.BadRequestException

class GenderNotFoundException: BadRequestException(message = "해당하는 성별을 찾을 수 없습니다.") {
}
