package com.yourssu.ssugaeting.domain.profile.application

import com.yourssu.ssugaeting.domain.common.business.dto.Response
import com.yourssu.ssugaeting.domain.profile.application.dto.NicknameSuggestedRequest
import com.yourssu.ssugaeting.infrastructure.dto.NicknameSuggestedResponse
import com.yourssu.ssugaeting.infrastructure.ChatModel
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/profiles")
class NicknameGeneratedController(
    private val chatModel: ChatModel,
) {
    @PostMapping("/nickname")
    fun suggestedNickname(@Valid @RequestBody request: NicknameSuggestedRequest): ResponseEntity<Response<NicknameSuggestedResponse>> {
        val response = chatModel.suggestNickname(request.description)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Response(result = response))
    }
}
