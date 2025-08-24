package com.yourssu.signal.domain.auth.storage.exception

import com.yourssu.signal.handler.NotFoundException

class NotFoundUserException: NotFoundException(message = "User not found"){
}
