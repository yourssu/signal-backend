package com.yourssu.signal.api

import com.yourssu.signal.api.dto.meeting.MeetingMatchRequest
import com.yourssu.signal.api.dto.meeting.MeetingRoomCreateRequest
import com.yourssu.signal.config.resolver.UserUuid
import com.yourssu.signal.config.security.annotation.RequireAuth
import com.yourssu.signal.domain.common.business.dto.Response
import com.yourssu.signal.domain.meeting.business.MeetingService
import com.yourssu.signal.domain.meeting.business.dto.MeetingBoardResponse
import com.yourssu.signal.domain.meeting.business.dto.MeetingMatchResponse
import com.yourssu.signal.domain.meeting.business.dto.MeetingResultResponse
import com.yourssu.signal.domain.meeting.business.dto.MeetingRoomDetailResponse
import com.yourssu.signal.domain.meeting.business.dto.MeetingRoomResponse
import com.yourssu.signal.handler.dto.ErrorResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse as OpenApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Meeting", description = "미팅 방 생성, 참여 및 결과 조회 API")
@RestController
@RequestMapping("/api/meetings")
class MeetingController(
    private val meetingService: MeetingService,
) {
    @Operation(
        summary = "미팅 보드 조회",
        description = "생성 가능 여부, 7개 슬롯의 열린 방과 전체 방 중 최근 30초 이내 최신 매칭 안내를 조회합니다. 방 또는 최신 매칭이 없으면 해당 필드는 생략됩니다.",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @ApiResponses(
        value = [
            OpenApiResponse(responseCode = "200", description = "보드 조회 성공"),
            OpenApiResponse(responseCode = "401", description = "인증 실패", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ]
    )
    @GetMapping("/board")
    @RequireAuth
    fun getBoard(
        @Parameter(hidden = true) @UserUuid uuid: String,
    ): ResponseEntity<Response<MeetingBoardResponse>> = ResponseEntity.ok(
        Response(result = meetingService.getBoard(uuid))
    )

    @Operation(
        summary = "미팅 방 생성",
        description = "선택한 슬롯에 본인과 동행자로 구성된 2~4인 방을 생성합니다. 방은 1시간 동안 열립니다.",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @ApiResponses(
        value = [
            OpenApiResponse(responseCode = "201", description = "방 생성 성공"),
            OpenApiResponse(responseCode = "400", description = "슬롯·초대 문구·동행자 검증 실패 (code: INVALID_MEETING_SLOT, INVALID_MEETING_INVITATION, INVALID_MEETING_MEMBER)", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
            OpenApiResponse(responseCode = "401", description = "인증 실패 (code 없음)", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
            OpenApiResponse(responseCode = "409", description = "프로필 없음, 일일 생성·매칭 제한, 생성한 방 진행 중 또는 슬롯 사용 중 (code: PROFILE_REQUIRED, DAILY_CREATION_LIMIT_EXCEEDED, DAILY_MEETING_LIMIT_EXCEEDED, ACTIVE_ROOM_EXISTS, SLOT_ALREADY_OCCUPIED)", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ]
    )
    @PostMapping("/rooms")
    @RequireAuth
    fun createRoom(
        @Parameter(hidden = true) @UserUuid uuid: String,
        @Valid @RequestBody request: MeetingRoomCreateRequest,
    ): ResponseEntity<Response<MeetingRoomResponse>> = ResponseEntity.status(HttpStatus.CREATED)
        .body(Response(result = meetingService.createRoom(request.toCommand(uuid))))

    @Operation(
        summary = "미팅 방 상세 조회",
        description = "미팅 방의 상태와 양쪽 팀 구성원을 조회합니다.",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @ApiResponses(
        value = [
            OpenApiResponse(responseCode = "200", description = "방 상세 조회 성공"),
            OpenApiResponse(responseCode = "400", description = "방 ID 검증 실패 (code 없음)", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
            OpenApiResponse(responseCode = "401", description = "인증 실패 (code 없음)", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
            OpenApiResponse(responseCode = "404", description = "방 없음 (code: MEETING_ROOM_NOT_FOUND)", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
            OpenApiResponse(responseCode = "409", description = "이미 매칭·취소·만료된 방 (code: ROOM_ALREADY_MATCHED, ROOM_CANCELLED, ROOM_EXPIRED)", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ]
    )
    @GetMapping("/rooms/{roomId}")
    @RequireAuth
    fun getRoom(
        @Parameter(hidden = true) @UserUuid uuid: String,
        @Parameter(description = "미팅 방 ID", example = "1", schema = Schema(minimum = "1"))
        @PathVariable @Positive roomId: Long,
    ): ResponseEntity<Response<MeetingRoomDetailResponse>> = ResponseEntity.ok(
        Response(result = meetingService.getRoom(uuid, roomId))
    )

    @Operation(
        summary = "미팅 방 참여 및 매칭",
        description = "신청자 팀 정보와 대표 연락처로 열린 방에 참여해 즉시 매칭합니다. 대표와 동행자를 합친 인원은 기존 방의 인원수와 같아야 합니다.",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @ApiResponses(
        value = [
            OpenApiResponse(responseCode = "201", description = "매칭 성공"),
            OpenApiResponse(responseCode = "400", description = "방 ID·팀 인원·참여자·연락처 검증 실패 (code: INVALID_COMPANION_COUNT, INVALID_MEETING_MEMBER)", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
            OpenApiResponse(responseCode = "401", description = "인증 실패 (code 없음)", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
            OpenApiResponse(responseCode = "404", description = "방 없음 (code: MEETING_ROOM_NOT_FOUND)", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
            OpenApiResponse(responseCode = "409", description = "본인 방 신청, 일일 생성·참여 제한, 생성한 방 진행 중 또는 이미 매칭·취소·만료된 방 (code: SELF_MATCH_NOT_ALLOWED, DAILY_MEETING_LIMIT_EXCEEDED, ACTIVE_ROOM_EXISTS, ROOM_ALREADY_MATCHED, ROOM_CANCELLED, ROOM_EXPIRED)", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ]
    )
    @PostMapping("/rooms/{roomId}/matches")
    @RequireAuth
    fun match(
        @Parameter(hidden = true) @UserUuid uuid: String,
        @Parameter(description = "미팅 방 ID", example = "1", schema = Schema(minimum = "1"))
        @PathVariable @Positive roomId: Long,
        @Valid @RequestBody request: MeetingMatchRequest,
    ): ResponseEntity<Response<MeetingMatchResponse>> = ResponseEntity.status(HttpStatus.CREATED)
        .body(Response(result = meetingService.match(request.toCommand(uuid, roomId))))

    @Operation(
        summary = "미팅 방 취소",
        description = "방 생성자가 열린 미팅 방을 취소합니다.",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @ApiResponses(
        value = [
            OpenApiResponse(responseCode = "204", description = "방 취소 성공"),
            OpenApiResponse(responseCode = "400", description = "방 ID 검증 실패 (code 없음)", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
            OpenApiResponse(responseCode = "401", description = "인증 실패 (code 없음)", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
            OpenApiResponse(responseCode = "403", description = "방 생성자가 아님 (code: MEETING_ROOM_CANCEL_FORBIDDEN)", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
            OpenApiResponse(responseCode = "404", description = "방 없음 (code: MEETING_ROOM_NOT_FOUND)", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
            OpenApiResponse(responseCode = "409", description = "이미 매칭·취소·만료된 방 (code: ROOM_ALREADY_MATCHED, ROOM_CANCELLED, ROOM_EXPIRED)", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ]
    )
    @PostMapping("/rooms/{roomId}/cancel")
    @RequireAuth
    fun cancel(
        @Parameter(hidden = true) @UserUuid uuid: String,
        @Parameter(description = "미팅 방 ID", example = "1", schema = Schema(minimum = "1"))
        @PathVariable @Positive roomId: Long,
    ): ResponseEntity<Void> {
        meetingService.cancel(uuid, roomId)
        return ResponseEntity.noContent().build()
    }

    @Operation(
        summary = "미팅 결과 조회",
        description = "매칭 당사자가 상대 팀 대표 연락처를 조회합니다.",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @ApiResponses(
        value = [
            OpenApiResponse(responseCode = "200", description = "매칭 결과 조회 성공"),
            OpenApiResponse(responseCode = "400", description = "방 ID 검증 실패 (code 없음)", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
            OpenApiResponse(responseCode = "401", description = "인증 실패 (code 없음)", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
            OpenApiResponse(responseCode = "403", description = "매칭 당사자가 아님 (code: MEETING_RESULT_FORBIDDEN)", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
            OpenApiResponse(responseCode = "404", description = "방 또는 매칭 결과 없음 (code: MEETING_ROOM_NOT_FOUND)", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
            OpenApiResponse(responseCode = "409", description = "취소·만료된 방 (code: ROOM_CANCELLED, ROOM_EXPIRED)", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ]
    )
    @GetMapping("/rooms/{roomId}/result")
    @RequireAuth
    fun getResult(
        @Parameter(hidden = true) @UserUuid uuid: String,
        @Parameter(description = "미팅 방 ID", example = "1", schema = Schema(minimum = "1"))
        @PathVariable @Positive roomId: Long,
    ): ResponseEntity<Response<MeetingResultResponse>> = ResponseEntity.ok(
        Response(result = meetingService.getResult(uuid, roomId))
    )
}
