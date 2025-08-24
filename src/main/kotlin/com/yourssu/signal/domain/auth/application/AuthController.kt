package com.yourssu.signal.domain.auth.application

import com.yourssu.signal.domain.auth.application.dto.RefreshTokenRequest
import com.yourssu.signal.domain.auth.application.dto.TokenResponse
import com.yourssu.signal.domain.auth.business.AuthService
import com.yourssu.signal.domain.common.business.dto.Response
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {
    @PostMapping("/register")
    fun register(): ResponseEntity<Response<TokenResponse>> {
        val response = authService.register()
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Response(result = response))
    }
    
    @PostMapping("/refresh")
    fun refreshToken(
        @Valid @RequestBody request: RefreshTokenRequest
    ): ResponseEntity<Response<TokenResponse>> {
        val response = authService.refreshToken(request)
        return ResponseEntity.ok(Response(result = response))
    }
}
