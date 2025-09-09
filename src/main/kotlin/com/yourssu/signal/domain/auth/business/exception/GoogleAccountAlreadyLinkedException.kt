package com.yourssu.signal.domain.auth.business.exception

import com.yourssu.signal.handler.ConflictException

class GoogleAccountAlreadyLinkedException: ConflictException(message = "Google account already linked") {
}
