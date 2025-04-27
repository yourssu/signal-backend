package com.yourssu.signal.domain.viewer.storage.exception

import com.yourssu.signal.handler.NotFoundException

class ViewerNotFoundException : NotFoundException(message = "해당하는 Viewer를 찾을 수 없습니다.") {
}
