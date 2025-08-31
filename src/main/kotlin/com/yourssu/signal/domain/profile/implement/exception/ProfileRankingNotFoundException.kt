package com.yourssu.signal.domain.profile.implement.exception

import com.yourssu.signal.handler.NotFoundException

class ProfileRankingNotFoundException: NotFoundException(message = "프로필 랭킹을 찾을 수 없습니다.") {
}
