package com.yourssu.signal.domain.blacklist.implement.exception

import com.yourssu.signal.handler.ForbiddenException

class AdminBlacklistCannotBeRemovedException : ForbiddenException(message = "관리자가 등록한 블랙리스트는 사용자가 삭제할 수 없습니다.")