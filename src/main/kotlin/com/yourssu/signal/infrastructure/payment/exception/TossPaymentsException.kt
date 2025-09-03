package com.yourssu.signal.infrastructure.payment.exception

import com.yourssu.signal.handler.InternalServerError
import org.springframework.http.HttpStatus


class TossPaymentsException(
    message: String = "토스페이먼츠 API 호출 중 오류가 발생했습니다."
) : InternalServerError(
    status = HttpStatus.INTERNAL_SERVER_ERROR,
    message = message
)

class TossPaymentsBadRequestException(
    message: String = "토스페이먼츠 요청 오류입니다."
) : InternalServerError(
    status = HttpStatus.INTERNAL_SERVER_ERROR,
    message = message
)

class TossPaymentsUnauthorizedException(
    message: String = "토스페이먼츠 인증 실패입니다."
) : InternalServerError(
    status = HttpStatus.INTERNAL_SERVER_ERROR,
    message = message
)

class TossPaymentsForbiddenException(
    message: String = "토스페이먼츠 API 접근 권한이 없습니다."
) : InternalServerError(
    status = HttpStatus.INTERNAL_SERVER_ERROR,
    message = message
)

class TossPaymentsNotFoundException(
    message: String = "토스페이먼츠 리소스를 찾을 수 없습니다."
) : InternalServerError(
    status = HttpStatus.INTERNAL_SERVER_ERROR,
    message = message
)

class TossPaymentsServerException(
    message: String = "토스페이먼츠 서버 오류입니다."
) : InternalServerError(
    status = HttpStatus.INTERNAL_SERVER_ERROR,
    message = message
)