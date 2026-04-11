package com.yourssu.signal.api

import com.yourssu.signal.config.security.annotation.RequireAuth
import com.yourssu.signal.domain.common.business.dto.Response
import com.yourssu.signal.api.dto.NicknameSuggestedRequest
import com.yourssu.signal.infrastructure.openai.dto.NicknameSuggestedResponse
import com.yourssu.signal.infrastructure.openai.ChatModel
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Profile", description = "Profile management APIs")
@RestController
@RequestMapping("/api/profiles")
class NicknameGeneratedController(
    private val chatModel: ChatModel,
) {
    @Operation(
        summary = "닉네임 추천",
        description = "소개 문장을 기반으로 AI를 사용하여 닉네임을 추천합니다. 응답은 캐시됩니다. 개발 및 운영 환경에서는 OpenAI API를 사용하며, 로컬 환경은 분리되어 있어야 합니다.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @PostMapping("/nickname")
    @RequireAuth
    fun suggestedNickname(@Valid @RequestBody request: NicknameSuggestedRequest): ResponseEntity<Response<NicknameSuggestedResponse>> {
        val response = chatModel.suggestNickname(request.introSentences)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Response(result = response))
    }
}
