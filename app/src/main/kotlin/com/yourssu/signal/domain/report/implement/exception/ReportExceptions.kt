package com.yourssu.signal.domain.report.implement.exception

import com.yourssu.signal.handler.ConflictException
import com.yourssu.signal.handler.ForbiddenException
import com.yourssu.signal.handler.NotFoundException

class ReportAlreadyExistsException : ConflictException(message = "이미 신고한 프로필입니다.")
class ReportNotFoundException : NotFoundException(message = "신고를 찾을 수 없습니다.")
class ReportAlreadyProcessedException : ConflictException(message = "이미 승인된 신고입니다.")
class ReportNotEligibleException : ForbiddenException(message = "연락처를 열람한 프로필만 신고할 수 있습니다.")
