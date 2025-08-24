package com.yourssu.signal.domain.user.application

import com.yourssu.signal.config.resolver.UserUuid
import com.yourssu.signal.config.security.annotation.RequireAuth
import com.yourssu.signal.domain.common.business.dto.Response
import com.yourssu.signal.domain.user.business.UserService
import com.yourssu.signal.domain.user.business.dto.UserInfoResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {
    @GetMapping("/me")
    @RequireAuth
    fun getMyInfo(@UserUuid uuid: String): ResponseEntity<Response<UserInfoResponse>> {
        val response = userService.getMyInfo(uuid)
        return ResponseEntity.ok(Response(result = response))
    }
}