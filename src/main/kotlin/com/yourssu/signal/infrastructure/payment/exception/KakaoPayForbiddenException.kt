package com.yourssu.signal.infrastructure.payment.exception

import com.yourssu.signal.handler.ForbiddenException
import org.springframework.http.HttpStatus

class KakaoPayForbiddenException(
    message: String = "카카오페이 API 접근 권한이 없습니다."
) : ForbiddenException(
    status = HttpStatus.FORBIDDEN,
    message = message
)