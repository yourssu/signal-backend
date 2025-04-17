package com.yourssu.signal.domain.viewer.storage.exception

import com.yourssu.signal.handler.NotFoundException

class ViewerNotFoundException : NotFoundException(message = "Viewer not found") {
}
