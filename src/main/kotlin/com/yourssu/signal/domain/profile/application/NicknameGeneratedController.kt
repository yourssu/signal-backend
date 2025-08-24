package com.yourssu.signal.domain.profile.application

import com.yourssu.signal.config.security.annotation.RequireAuth
import com.yourssu.signal.domain.common.business.dto.Response
import com.yourssu.signal.domain.profile.application.dto.NicknameSuggestedRequest
import com.yourssu.signal.infrastructure.dto.NicknameSuggestedResponse
import com.yourssu.signal.infrastructure.ChatModel
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
    @RequireAuth
    fun suggestedNickname(@Valid @RequestBody request: NicknameSuggestedRequest): ResponseEntity<Response<NicknameSuggestedResponse>> {
        val response = chatModel.suggestNickname(request.introSentences)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Response(result = response))
    }
}
