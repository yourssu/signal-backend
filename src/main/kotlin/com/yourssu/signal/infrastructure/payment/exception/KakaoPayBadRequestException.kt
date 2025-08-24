package com.yourssu.signal.infrastructure.payment.exception

import com.yourssu.signal.handler.BadRequestException
import org.springframework.http.HttpStatus

class KakaoPayBadRequestException(
    message: String = "카카오페이 요청이 잘못되었습니다."
) : BadRequestException(
    status = HttpStatus.BAD_REQUEST,
    message = message
)