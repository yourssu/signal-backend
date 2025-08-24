package com.yourssu.signal.infrastructure.payment.exception

import com.yourssu.signal.handler.InternalServerError
import org.springframework.http.HttpStatus


class KakaoPayException(
    message: String = "카카오페이 API 호출 중 오류가 발생했습니다."
) : InternalServerError(
    status = HttpStatus.INTERNAL_SERVER_ERROR,
    message = message
)
