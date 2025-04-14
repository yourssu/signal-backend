package com.yourssu.ssugaeting.domain.profile.storage.execption

import com.yourssu.ssugaeting.handler.NotFoundException

class ProfileNotFoundException : NotFoundException(message = "해당하는 프로필을 찾을 수 없습니다.") {
}
