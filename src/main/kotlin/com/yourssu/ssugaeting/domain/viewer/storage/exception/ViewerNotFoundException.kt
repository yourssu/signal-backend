package com.yourssu.ssugaeting.domain.viewer.storage.exception

import com.yourssu.ssugaeting.handler.NotFoundException

class ViewerNotFoundException : NotFoundException(message = "해당하는 조회자를 찾을 수 없습니다.") {
}
