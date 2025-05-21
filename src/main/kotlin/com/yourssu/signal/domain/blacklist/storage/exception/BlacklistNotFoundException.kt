package com.yourssu.signal.domain.blacklist.storage.exception

import com.yourssu.signal.handler.NotFoundException

class BlacklistNotFoundException(): NotFoundException(message = "blacklist not found") {
}
