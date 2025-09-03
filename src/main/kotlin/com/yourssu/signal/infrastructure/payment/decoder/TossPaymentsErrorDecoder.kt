package com.yourssu.signal.infrastructure.payment.decoder

import com.yourssu.signal.infrastructure.payment.exception.*
import feign.Response
import feign.codec.ErrorDecoder

class TossPaymentsErrorDecoder : ErrorDecoder {
    override fun decode(methodKey: String, response: Response): Exception {
        return when (response.status()) {
            400 -> TossPaymentsBadRequestException("토스페이먼츠 요청 오류: ${response.reason()}")
            401 -> TossPaymentsUnauthorizedException("토스페이먼츠 인증 실패: ${response.reason()}")
            403 -> TossPaymentsForbiddenException("토스페이먼츠 API 접근 권한 없음: ${response.reason()}")
            404 -> TossPaymentsNotFoundException("토스페이먼츠 리소스를 찾을 수 없음: ${response.reason()}")
            500, 502, 503, 504 -> TossPaymentsServerException("토스페이먼츠 서버 오류: ${response.reason()}")
            else -> TossPaymentsException("토스페이먼츠 API 호출 실패: ${response.reason()}")
        }
    }
}