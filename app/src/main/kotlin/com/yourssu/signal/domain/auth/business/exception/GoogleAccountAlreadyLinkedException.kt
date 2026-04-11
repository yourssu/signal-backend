package com.yourssu.signal.domain.auth.business.exception

import com.yourssu.signal.handler.ConflictException

class GoogleAccountAlreadyLinkedException: ConflictException(message = "구글 계정이 이미 다른 계정과 연동되어 있습니다. 다른 구글 계정을 사용해주세요.") {
}
