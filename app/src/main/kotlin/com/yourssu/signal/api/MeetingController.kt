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
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
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
        description = "생성 가능 여부, 슬롯별 열린 방과 최근 30초 이내 매칭 안내를 조회합니다.",
        security = [SecurityRequirement(name = "bearerAuth")],
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
    @GetMapping("/rooms/{roomId}")
    @RequireAuth
    fun getRoom(
        @Parameter(hidden = true) @UserUuid uuid: String,
        @Parameter(description = "미팅 방 ID", example = "1") @PathVariable @Positive roomId: Long,
    ): ResponseEntity<Response<MeetingRoomDetailResponse>> = ResponseEntity.ok(
        Response(result = meetingService.getRoom(uuid, roomId))
    )

    @Operation(
        summary = "미팅 방 참여 및 매칭",
        description = "신청자 팀 정보와 대표 연락처로 열린 방에 참여해 즉시 매칭합니다.",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @PostMapping("/rooms/{roomId}/matches")
    @RequireAuth
    fun match(
        @Parameter(hidden = true) @UserUuid uuid: String,
        @Parameter(description = "미팅 방 ID", example = "1") @PathVariable @Positive roomId: Long,
        @Valid @RequestBody request: MeetingMatchRequest,
    ): ResponseEntity<Response<MeetingMatchResponse>> = ResponseEntity.status(HttpStatus.CREATED)
        .body(Response(result = meetingService.match(request.toCommand(uuid, roomId))))

    @Operation(
        summary = "미팅 방 취소",
        description = "방 생성자가 열린 미팅 방을 취소합니다.",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @PostMapping("/rooms/{roomId}/cancel")
    @RequireAuth
    fun cancel(
        @Parameter(hidden = true) @UserUuid uuid: String,
        @Parameter(description = "미팅 방 ID", example = "1") @PathVariable @Positive roomId: Long,
    ): ResponseEntity<Void> {
        meetingService.cancel(uuid, roomId)
        return ResponseEntity.noContent().build()
    }

    @Operation(
        summary = "미팅 결과 조회",
        description = "매칭 당사자가 상대 팀 대표 연락처를 조회합니다.",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @GetMapping("/rooms/{roomId}/result")
    @RequireAuth
    fun getResult(
        @Parameter(hidden = true) @UserUuid uuid: String,
        @Parameter(description = "미팅 방 ID", example = "1") @PathVariable @Positive roomId: Long,
    ): ResponseEntity<Response<MeetingResultResponse>> = ResponseEntity.ok(
        Response(result = meetingService.getResult(uuid, roomId))
    )
}
