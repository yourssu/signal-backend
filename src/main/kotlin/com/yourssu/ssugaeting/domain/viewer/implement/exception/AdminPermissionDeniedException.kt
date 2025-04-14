package com.yourssu.ssugaeting.domain.viewer.implement.exception

import com.yourssu.ssugaeting.handler.ForbiddenException

class AdminPermissionDeniedException : ForbiddenException(message = "올바르지 않은 관리자 기능 접근 토큰입니다.") {
}
