package com.yourssu.signal.domain.profile.storage.execption

import com.yourssu.signal.handler.NotFoundException

class ProfileNotFoundException : NotFoundException(message = "해당하는 프로필을 찾을 수 없습니다.") {
}
