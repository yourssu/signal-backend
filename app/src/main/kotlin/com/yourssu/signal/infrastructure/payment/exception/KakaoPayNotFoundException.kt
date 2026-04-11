package com.yourssu.signal.infrastructure.payment.exception

import com.yourssu.signal.handler.NotFoundException
import org.springframework.http.HttpStatus

class KakaoPayNotFoundException(
    message: String = "카카오페이 리소스를 찾을 수 없습니다."
) : NotFoundException(
    status = HttpStatus.NOT_FOUND,
    message = message
)