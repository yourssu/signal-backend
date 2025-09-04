package com.yourssu.signal.api

import com.yourssu.signal.api.dto.ReferralCodeRequest
import com.yourssu.signal.config.resolver.UserUuid
import com.yourssu.signal.config.security.annotation.RequireAuth
import com.yourssu.signal.domain.common.business.dto.Response
import com.yourssu.signal.domain.referral.business.ReferralService
import com.yourssu.signal.domain.referral.business.dto.ReferralCodeResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Referral", description = "Referral management APIs")
@RestController
@RequestMapping("/api/referrals")
class ReferralController(
    private val referralService: ReferralService,
) {
    @Operation(
        summary = "내 추천인 코드 발급",
        description = "인증된 사용자의 추천인 코드를 발급합니다. RequestBody를 제공하지 않으면 시스템에서 자동으로 생성된 코드를 발급합니다.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @PostMapping
    @RequireAuth
    fun generateMyReferralCode(
        @Parameter(hidden = true) @UserUuid uuid: String,
        @RequestBody(required = false) request: ReferralCodeRequest = ReferralCodeRequest()
    ): ResponseEntity<Response<ReferralCodeResponse>> {
        val response = referralService.generateReferralCode(request.toCommand(uuid))
        return ResponseEntity.ok(Response(result = response))
    }
}
