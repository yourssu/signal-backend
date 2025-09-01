package com.yourssu.signal.api

import com.yourssu.signal.api.dto.BlacklistAddedRequest
import com.yourssu.signal.api.dto.BlacklistDeletedRequest
import com.yourssu.signal.config.resolver.UserUuid
import com.yourssu.signal.domain.blacklist.business.BlacklistService
import com.yourssu.signal.domain.blacklist.business.dto.BlacklistExistsResponse
import com.yourssu.signal.domain.blacklist.business.dto.BlacklistResponse
import com.yourssu.signal.domain.common.business.dto.Response
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Admin", description = "Admin APIs")
@RestController
@RequestMapping("/api/profiles/blacklists")
class BlacklistController(
    private val blacklistService: BlacklistService,
) {
    @Operation(
        summary = "블랙리스트 추가",
        description = "프로필을 블랙리스트에 추가합니다. 비밀 키가 필요합니다. (관리자 전용)"
    )
    @PostMapping
    fun addBlacklist(@Valid @RequestBody request: BlacklistAddedRequest): ResponseEntity<Response<BlacklistResponse>> {
        val response = blacklistService.addBlacklist(request.toCommand())
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Response(result = response))
    }

    @Operation(
        summary = "블랙리스트 삭제",
        description = "프로필 ID로 블랙리스트에서 프로필을 제거합니다. 비밀 키가 필요합니다. (관리자 전용)"
    )
    @DeleteMapping("/{profileId}")
    fun removeBlacklist(@ModelAttribute @Valid request: BlacklistDeletedRequest, @PathVariable profileId: Long): ResponseEntity<Void> {
        blacklistService.removeBlacklist(request.toCommand(profileId))
        return ResponseEntity.noContent().build()
    }

    @Operation(
        summary = "나의 블랙리스트 상태 확인",
        description = "현재 로그인한 사용자의 프로필이 블랙리스트에 등록되어 있는지 여부를 확인합니다."
    )
    @GetMapping
    fun checkMyBlacklistStatus(@Parameter(hidden = true) @UserUuid uuid: String): ResponseEntity<Response<BlacklistExistsResponse>> {
        val response = blacklistService.checkMyBlacklistStatus(uuid)
        return ResponseEntity.ok(Response(result = response))
    }
}
