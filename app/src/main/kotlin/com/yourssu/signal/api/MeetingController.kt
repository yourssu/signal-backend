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
import io.swagger.v3.oas.annotations.Parameter
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

@RestController
@RequestMapping("/api/meetings")
class MeetingController(
    private val meetingService: MeetingService,
) {
    @GetMapping("/board")
    @RequireAuth
    fun getBoard(
        @Parameter(hidden = true) @UserUuid uuid: String,
    ): ResponseEntity<Response<MeetingBoardResponse>> = ResponseEntity.ok(
        Response(result = meetingService.getBoard(uuid))
    )

    @PostMapping("/rooms")
    @RequireAuth
    fun createRoom(
        @Parameter(hidden = true) @UserUuid uuid: String,
        @Valid @RequestBody request: MeetingRoomCreateRequest,
    ): ResponseEntity<Response<MeetingRoomResponse>> = ResponseEntity.status(HttpStatus.CREATED)
        .body(Response(result = meetingService.createRoom(request.toCommand(uuid))))

    @GetMapping("/rooms/{roomId}")
    @RequireAuth
    fun getRoom(
        @Parameter(hidden = true) @UserUuid uuid: String,
        @PathVariable @Positive roomId: Long,
    ): ResponseEntity<Response<MeetingRoomDetailResponse>> = ResponseEntity.ok(
        Response(result = meetingService.getRoom(uuid, roomId))
    )

    @PostMapping("/rooms/{roomId}/matches")
    @RequireAuth
    fun match(
        @Parameter(hidden = true) @UserUuid uuid: String,
        @PathVariable @Positive roomId: Long,
        @Valid @RequestBody request: MeetingMatchRequest,
    ): ResponseEntity<Response<MeetingMatchResponse>> = ResponseEntity.status(HttpStatus.CREATED)
        .body(Response(result = meetingService.match(request.toCommand(uuid, roomId))))

    @PostMapping("/rooms/{roomId}/cancel")
    @RequireAuth
    fun cancel(
        @Parameter(hidden = true) @UserUuid uuid: String,
        @PathVariable @Positive roomId: Long,
    ): ResponseEntity<Void> {
        meetingService.cancel(uuid, roomId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/rooms/{roomId}/result")
    @RequireAuth
    fun getResult(
        @Parameter(hidden = true) @UserUuid uuid: String,
        @PathVariable @Positive roomId: Long,
    ): ResponseEntity<Response<MeetingResultResponse>> = ResponseEntity.ok(
        Response(result = meetingService.getResult(uuid, roomId))
    )
}
