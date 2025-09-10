package com.yourssu.signal.domain.auth.business.exception

import com.yourssu.signal.handler.UnauthorizedException

class InvalidGoogleCodeException : UnauthorizedException(message = "구글 로그인에 실패했습니다. 다시 시도해주세요.") {
}
