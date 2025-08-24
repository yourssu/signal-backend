package com.yourssu.signal.infrastructure.payment.exception

import com.yourssu.signal.handler.UnauthorizedException
import org.springframework.http.HttpStatus

class KakaoPayUnauthorizedException(
    message: String = "카카오페이 인증에 실패했습니다."
) : UnauthorizedException(
    status = HttpStatus.UNAUTHORIZED,
    message = message
)