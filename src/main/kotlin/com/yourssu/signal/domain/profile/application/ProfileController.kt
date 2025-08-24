package com.yourssu.signal.domain.profile.application

import com.yourssu.signal.config.resolver.UserUuid
import com.yourssu.signal.config.security.annotation.RequireAuth
import com.yourssu.signal.domain.common.business.dto.Response
import com.yourssu.signal.domain.profile.application.dto.*
import com.yourssu.signal.domain.profile.business.ProfilesCountResponse
import com.yourssu.signal.domain.profile.business.ProfileService
import com.yourssu.signal.domain.profile.business.command.ProfileFoundCommand
import com.yourssu.signal.domain.profile.business.dto.MyProfileResponse
import com.yourssu.signal.domain.profile.business.dto.ProfileContactResponse
import com.yourssu.signal.domain.profile.business.dto.ProfileResponse
import com.yourssu.signal.domain.viewer.application.dto.RandomProfileRequest
import jakarta.validation.Valid
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/profiles")
class ProfileController(
    private val profileService: ProfileService,
) {
    @PostMapping
    @RequireAuth
    fun createProfile(
        @Valid @RequestBody request: ProfileCreatedRequest,
        @UserUuid uuid: String
    ): ResponseEntity<Response<MyProfileResponse>> {
        val response = profileService.createProfile(request.toCommand(uuid))
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Response(result = response))
    }

    @GetMapping("/uuid")
    @RequireAuth
    fun getProfile(@UserUuid uuid: String): ResponseEntity<Response<MyProfileResponse>> {
        val response = profileService.getProfile(uuid)
        return ResponseEntity.ok(Response(result = response))
    }

    @Profile("!prod")
    @GetMapping
    fun getAllProfiles(@Valid @ModelAttribute request: ProfilesFoundRequest): ResponseEntity<Response<List<ProfileResponse>>> {
        val response = profileService.getAllProfiles(request.toCommand())
        return ResponseEntity.ok(Response(result = response))
    }

    @GetMapping("/count")
    fun countAllProfiles(): ResponseEntity<Response<ProfilesCountResponse>> {
        val response = profileService.countAllProfiles()
        return ResponseEntity.ok(Response(result = response))
    }

    @GetMapping("/genders/{gender}/count")
    fun countByGender(@PathVariable gender: String): ResponseEntity<Response<ProfilesCountResponse>> {
        val response = profileService.countByGender(gender)
        return ResponseEntity.ok(Response(result = response))
    }

    @GetMapping("/{profileId}")
    @RequireAuth
    fun getProfile(
        @UserUuid uuid: String,
        @PathVariable profileId: Long
    ): ResponseEntity<Response<ProfileContactResponse>> {
        val response = profileService.getProfile(ProfileFoundCommand(profileId = profileId, uuid = uuid))
        return ResponseEntity.ok(Response(result = response))
    }

    @GetMapping("/random")
    @RequireAuth
    fun getRandomProfile(@Valid @ModelAttribute request: RandomProfileRequest, @UserUuid uuid: String): ResponseEntity<Response<ProfileResponse>> {
        val response = profileService.getRandomProfile(request.toCommand(uuid))
        return ResponseEntity.ok(Response(result = response))
    }

    @PostMapping("/contact")
    @RequireAuth
    fun consumeTicket(@Valid @RequestBody request: TicketConsumedRequest, @UserUuid uuid: String): ResponseEntity<Response<ProfileContactResponse>> {
        val response = profileService.consumeTicket(request.toCommand(uuid))
        return ResponseEntity.ok(Response(result = response))
    }
}
