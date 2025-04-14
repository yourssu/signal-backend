package com.yourssu.ssugaeting.domain.viewer.storage.exception

import com.yourssu.ssugaeting.handler.NotFoundException

class ViewerNotFoundException : NotFoundException(message = "Viewer not found") {
}
