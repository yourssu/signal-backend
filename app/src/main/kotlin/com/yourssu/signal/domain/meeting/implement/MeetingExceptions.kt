package com.yourssu.signal.domain.meeting.implement

import com.yourssu.signal.handler.BadRequestException
import com.yourssu.signal.handler.ConflictException
import com.yourssu.signal.handler.ForbiddenException
import com.yourssu.signal.handler.NotFoundException

class InvalidPartySizeException : BadRequestException(message = "미팅 인원은 2명부터 4명까지 가능합니다.", code = "INVALID_PARTY_SIZE")
class InvalidCompanionCountException : BadRequestException(message = "동행 인원 정보가 팀 인원수와 일치하지 않습니다.", code = "INVALID_COMPANION_COUNT")
class InvalidMeetingMemberException : BadRequestException(message = "올바르지 않은 미팅 참여자 정보입니다.", code = "INVALID_MEETING_MEMBER")
class SameGenderMatchNotAllowedException : BadRequestException(message = "해당 방은 이성만 참여할 수 있습니다.", code = "SAME_GENDER_MATCH_NOT_ALLOWED")
class InvalidMeetingSlotException : BadRequestException(message = "올바르지 않은 미팅 슬롯입니다.", code = "INVALID_MEETING_SLOT")
class InvalidMeetingInvitationException : BadRequestException(message = "미팅 초대 문구는 1자부터 500자까지 가능합니다.", code = "INVALID_MEETING_INVITATION")
class ProfileRequiredException : ConflictException(message = "방 생성에는 프로필 등록이 필요합니다.", code = "PROFILE_REQUIRED")
class DailyCreationLimitExceededException : ConflictException(message = "오늘 이미 미팅 방을 생성했습니다.", code = "DAILY_CREATION_LIMIT_EXCEEDED")
class DailyMeetingLimitExceededException : ConflictException(message = "오늘 이미 미팅 매칭이 완료되었습니다.", code = "DAILY_MEETING_LIMIT_EXCEEDED")
class ActiveRoomExistsException : ConflictException(message = "생성한 미팅 방이 진행 중입니다. 방을 취소한 뒤 신청해 주세요.", code = "ACTIVE_ROOM_EXISTS")
class SlotAlreadyOccupiedException : ConflictException(message = "이미 사용 중인 미팅 슬롯입니다.", code = "SLOT_ALREADY_OCCUPIED")
class RoomAlreadyMatchedException : ConflictException(message = "이미 매칭된 미팅 방입니다.", code = "ROOM_ALREADY_MATCHED")
class RoomCancelledException : ConflictException(message = "취소된 미팅 방입니다.", code = "ROOM_CANCELLED")
class RoomExpiredException : ConflictException(message = "만료된 미팅 방입니다.", code = "ROOM_EXPIRED")
class MeetingResultForbiddenException : ForbiddenException(message = "매칭 결과를 조회할 권한이 없습니다.", code = "MEETING_RESULT_FORBIDDEN")
class MeetingRoomNotFoundException : NotFoundException(message = "미팅 방을 찾을 수 없습니다.", code = "MEETING_ROOM_NOT_FOUND")
class MeetingRoomCancelForbiddenException : ForbiddenException(message = "방 생성자만 취소할 수 있습니다.", code = "MEETING_ROOM_CANCEL_FORBIDDEN")
class MeetingBlockedException : ForbiddenException(message = "미팅 이용이 제한된 사용자입니다.", code = "MEETING_BLOCKED")
class SelfMatchNotAllowedException : ConflictException(message = "자신이 생성한 미팅 방에는 신청할 수 없습니다.", code = "SELF_MATCH_NOT_ALLOWED")
