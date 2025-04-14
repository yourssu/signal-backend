package com.yourssu.ssugaeting.domain.profile.implement.exception

import com.yourssu.ssugaeting.handler.NotFoundException

class RandomProfileNotFoundException : NotFoundException(message = "조회할 수 있는 랜덤 프로필이 없습니다.") {
}
