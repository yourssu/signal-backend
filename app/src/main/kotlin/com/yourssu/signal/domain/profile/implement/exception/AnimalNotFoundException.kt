package com.yourssu.signal.domain.profile.implement.exception

import com.yourssu.signal.handler.BadRequestException

class AnimalNotFoundException : BadRequestException(message = "해당하는 동물상을 찾을 수 없습니다.")
