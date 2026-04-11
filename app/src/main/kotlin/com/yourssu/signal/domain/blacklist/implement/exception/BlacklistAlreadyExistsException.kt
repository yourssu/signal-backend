package com.yourssu.signal.domain.blacklist.implement.exception

import com.yourssu.signal.handler.ConflictException

class BlacklistAlreadyExistsException : ConflictException(message = "이미 블랙리스트에 등록된 프로필입니다.")