package com.yourssu.signal.domain.auth.business.exception

import com.yourssu.signal.handler.UnauthorizedException

class InvalidGoogleCodeException : UnauthorizedException(message = "구글 로그인에 실패했습니다. 잠시 후 다시 시도해주세요.") {
}
