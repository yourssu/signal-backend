package com.yourssu.signal.infrastructure.payment.decoder

import com.yourssu.signal.infrastructure.payment.exception.*
import feign.Response
import feign.codec.ErrorDecoder

class KakaoPayErrorDecoder : ErrorDecoder {
    override fun decode(methodKey: String, response: Response): Exception {
        return when (response.status()) {
            400 -> KakaoPayBadRequestException("카카오페이 요청 오류: ${response.reason()}")
            401 -> KakaoPayUnauthorizedException("카카오페이 인증 실패: ${response.reason()}")
            403 -> KakaoPayForbiddenException("카카오페이 API 접근 권한 없음: ${response.reason()}")
            404 -> KakaoPayNotFoundException("카카오페이 리소스를 찾을 수 없음: ${response.reason()}")
            500, 502, 503, 504 -> KakaoPayServerException("카카오페이 서버 오류: ${response.reason()}")
            else -> KakaoPayException("카카오페이 API 호출 실패: ${response.reason()}")
        }
    }
}