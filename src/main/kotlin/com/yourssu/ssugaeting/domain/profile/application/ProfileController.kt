package com.yourssu.ssugaeting.domain.profile.application

import com.yourssu.ssugaeting.domain.Response
import com.yourssu.ssugaeting.domain.profile.application.dto.ProfileCreatedRequest
import com.yourssu.ssugaeting.domain.profile.business.ProfileService
import com.yourssu.ssugaeting.domain.profile.business.dto.ProfileContactResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/profiles")
class ProfileController(
    private val profileService: ProfileService,
) {
    @PostMapping
    fun createProfile(@Valid @RequestBody request: ProfileCreatedRequest): ResponseEntity<Response<ProfileContactResponse>> {
        val response = profileService.createProfile(request.toCommand())
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Response(result = response))
    }
//
//    @PostMapping("/nickname")
//    fun generateNickname(@Valid @RequestBody request: NicknameGeneratedRequest): ResponseEntity<Response<NicknameCreatedResponse>> {
//        val response = profileService.createNickname(request.toCommand())
//        return ResponseEntity.status(HttpStatus.CREATED)
//            .body(Response(result = response))
//    }
//
//    @GetMapping
//    fun getProfile(@Valid @ModelAttribute request: ProfileFoundRequest): ResponseEntity<Response<ProfileResponse>> {
//        val response = profileService.getProfile(request.toCommand())
//        return ResponseEntity.ok(Response(result = response))
//    }
//
//    @PostMapping("/contact")
//    fun consumeTicket(@Valid @RequestBody request: TicketConsumedRequest): ResponseEntity<Response<ProfileContactResponse>> {
//        val response = profileService.consumeTicket(request.toCommand())
//        return ResponseEntity.ok(Response(result = response))
//    }
}
