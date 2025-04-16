package com.yourssu.ssugaeting.domain.profile.implement.exception

import com.yourssu.ssugaeting.handler.BadRequestException

class GenderNotNullException : BadRequestException(message = "등록되지 않은 Viewer는 성별을 입력해야 합니다.") {
}
