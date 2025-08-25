package com.yourssu.signal.domain.user.application

import com.yourssu.signal.config.resolver.UserUuid
import com.yourssu.signal.config.security.annotation.RequireAuth
import com.yourssu.signal.domain.common.business.dto.Response
import com.yourssu.signal.domain.user.business.UserService
import com.yourssu.signal.domain.user.business.dto.UserInfoResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "User", description = "User management APIs")
@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {
    @Operation(
        summary = "내 정보 조회",
        description = "JWT 토큰에서 인증된 사용자의 UUID를 조회합니다.",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @GetMapping("/me")
    @RequireAuth
    fun getMyInfo(@Parameter(hidden = true) @UserUuid uuid: String): ResponseEntity<Response<UserInfoResponse>> {
        val response = userService.getMyInfo(uuid)
        return ResponseEntity.ok(Response(result = response))
    }
}