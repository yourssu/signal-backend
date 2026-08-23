package com.yourssu.signal.domain.profile.implement.exception

import com.yourssu.signal.handler.BadRequestException

class AnimalGenderMismatchException : BadRequestException(message = "성별에 해당하지 않는 동물상입니다.")
