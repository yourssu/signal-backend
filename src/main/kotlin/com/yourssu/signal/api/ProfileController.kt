package com.yourssu.signal.api

import com.yourssu.signal.api.dto.ProfileCreatedRequest
import com.yourssu.signal.api.dto.ProfilesFoundRequest
import com.yourssu.signal.api.dto.TicketConsumedRequest
import com.yourssu.signal.config.resolver.UserUuid
import com.yourssu.signal.config.security.annotation.RequireAuth
import com.yourssu.signal.domain.common.business.dto.Response
import com.yourssu.signal.domain.profile.business.ProfilesCountResponse
import com.yourssu.signal.domain.profile.business.ProfileService
import com.yourssu.signal.domain.profile.business.command.ProfileFoundCommand
import com.yourssu.signal.domain.profile.business.dto.MyProfileResponse
import com.yourssu.signal.domain.profile.business.dto.ProfileContactResponse
import com.yourssu.signal.domain.profile.business.dto.ProfileResponse
import com.yourssu.signal.domain.viewer.application.dto.RandomProfileRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Profile", description = "Profile management APIs")
@RestController
@RequestMapping("/api/profiles")
class ProfileController(
    private val profileService: ProfileService,
) {
    @Operation(
        summary = "프로필 생성",
        description = "프로필을 생성합니다. 사용자의 성별, 학과, 생년월일, 동물, 연락처, MBTI, 닉네임, 소개 문장 정보를 받아 새로운 프로필을 생성합니다.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @PostMapping
    @RequireAuth
    fun createProfile(
        @Valid @RequestBody request: ProfileCreatedRequest,
        @Parameter(hidden = true) @UserUuid uuid: String
    ): ResponseEntity<Response<MyProfileResponse>> {
        val response = profileService.createProfile(request.toCommand(uuid))
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Response(result = response))
    }

    @Operation(
        summary = "나의 프로필 조회",
        description = "인증된 사용자의 토큰에서 UUID를 추출하여 해당 사용자의 프로필 정보를 조회합니다.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @GetMapping("/me")
    @RequireAuth
    fun getMyProfile(@Parameter(hidden = true) @UserUuid uuid: String): ResponseEntity<Response<MyProfileResponse>> {
        val response = profileService.getProfile(uuid)
        return ResponseEntity.ok(Response(result = response))
    }

    @Operation(
        summary = "전체 프로필 조회",
        description = "시스템의 모든 프로필을 조회하는 개발 전용 엔드포인트입니다. 인증을 위해 관리자 비밀 키가 필요합니다. 참고: 이 엔드포인트는 프로덕션 환경에서는 비활성화됩니다."
    )
    @Profile("!prod")
    @GetMapping
    fun getAllProfiles(@Valid @ModelAttribute request: ProfilesFoundRequest): ResponseEntity<Response<List<ProfileResponse>>> {
        val response = profileService.getAllProfiles(request.toCommand())
        return ResponseEntity.ok(Response(result = response))
    }

    @Operation(
        summary = "전체 프로필 개수 조회",
        description = "전체 프로필의 개수를 조회합니다."
    )
    @GetMapping("/count")
    fun countAllProfiles(): ResponseEntity<Response<ProfilesCountResponse>> {
        val response = profileService.countAllProfiles()
        return ResponseEntity.ok(Response(result = response))
    }

    @Operation(
        summary = "성별별 프로필 개수 조회",
        description = "특정 성별(MALE 또는 FEMALE)로 필터링된 프로필의 개수를 조회합니다. 성별은 대소문자를 구분하지 않습니다."
    )
    @GetMapping("/genders/{gender}/count")
    fun countByGender(
        @Parameter(description = "Gender to filter by (MALE or FEMALE)", required = true)
        @PathVariable gender: String
    ): ResponseEntity<Response<ProfilesCountResponse>> {
        val response = profileService.countByGender(gender)
        return ResponseEntity.ok(Response(result = response))
    }

    @Operation(
        summary = "구매한 프로필 조회",
        description = "구매한 프로필의 연락처 정보를 프로필 ID로 조회합니다. 이전에 티켓을 사용하여 구매한 프로필만 조회할 수 있습니다.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @GetMapping("/{profileId}")
    @RequireAuth
    fun getProfile(
        @Parameter(hidden = true) @UserUuid uuid: String,
        @Parameter(description = "Profile ID", required = true)
        @PathVariable profileId: Long
    ): ResponseEntity<Response<ProfileContactResponse>> {
        val response = profileService.getProfile(ProfileFoundCommand(profileId = profileId, uuid = uuid))
        return ResponseEntity.ok(Response(result = response))
    }

    @Operation(
        summary = "랜덤 프로필 조회",
        description = "필터 조건에 따라 랜덤한 프로필을 조회합니다. 성별과 제외할 프로필 ID 목록을 파라미터로 받습니다.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @GetMapping("/random")
    @RequireAuth
    fun getRandomProfile(@Valid @ModelAttribute request: RandomProfileRequest, @Parameter(hidden = true) @UserUuid uuid: String): ResponseEntity<Response<ProfileResponse>> {
        val response = profileService.getRandomProfile(request.toCommand(uuid))
        return ResponseEntity.ok(Response(result = response))
    }

    @Operation(
        summary = "연락처 구매",
        description = "티켓을 소모하여 프로필의 연락처 정보를 구매하고 조회합니다. 이미 구매한 프로필의 경우 티켓을 소모하지 않습니다.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @PostMapping("/contact")
    @RequireAuth
    fun consumeTicket(@Valid @RequestBody request: TicketConsumedRequest, @Parameter(hidden = true) @UserUuid uuid: String): ResponseEntity<Response<ProfileContactResponse>> {
        val response = profileService.consumeTicket(request.toCommand(uuid))
        return ResponseEntity.ok(Response(result = response))
    }
}
