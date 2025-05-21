package com.yourssu.signal.domain.blacklist.application

import com.yourssu.signal.domain.blacklist.application.dto.BlacklistAddedRequest
import com.yourssu.signal.domain.blacklist.application.dto.BlacklistDeletedRequest
import com.yourssu.signal.domain.blacklist.business.BlacklistService
import com.yourssu.signal.domain.blacklist.business.dto.BlacklistResponse
import com.yourssu.signal.domain.common.business.dto.Response
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/profiles/blacklists")
class BlacklistController(
    private val blacklistService: BlacklistService,
) {
    @PostMapping
    fun addBlacklist(@Valid @RequestBody request: BlacklistAddedRequest): ResponseEntity<Response<BlacklistResponse>> {
        val response = blacklistService.addBlacklist(request.toCommand())
        return ResponseEntity.ok(Response(result = response))
    }

    @DeleteMapping("/{profileId}")
    fun removeBlacklist(@ModelAttribute @Valid request: BlacklistDeletedRequest, @PathVariable profileId: Long): ResponseEntity<Void> {
        blacklistService.removeBlacklist(request.toCommand(profileId))
        return ResponseEntity.noContent().build()
    }
}
