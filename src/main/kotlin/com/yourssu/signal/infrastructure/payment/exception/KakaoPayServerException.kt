package com.yourssu.signal.infrastructure.payment.exception

import com.yourssu.signal.handler.InternalServerError
import org.springframework.http.HttpStatus

class KakaoPayServerException(
    message: String = "카카오페이 서버 오류가 발생했습니다."
) : InternalServerError(
    status = HttpStatus.INTERNAL_SERVER_ERROR,
    message = message
)