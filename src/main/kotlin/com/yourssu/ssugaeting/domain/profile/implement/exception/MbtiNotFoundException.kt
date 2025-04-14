package com.yourssu.ssugaeting.domain.profile.implement.exception

import com.yourssu.soongpt.common.handler.BadRequestException

class MbtiNotFoundException: BadRequestException(message = "해당하는 MBTI를 찾을 수 없습니다.") {
}
