package com.yourssu.signal.domain.viewer.application

import com.yourssu.signal.domain.common.business.dto.Response
import com.yourssu.signal.domain.viewer.application.dto.FoundSelfRequest
import com.yourssu.signal.domain.viewer.application.dto.TicketIssuedRequest
import com.yourssu.signal.domain.viewer.application.dto.VerificationRequest
import com.yourssu.signal.domain.viewer.application.dto.ViewersFoundRequest
import com.yourssu.signal.domain.viewer.business.ViewerService
import com.yourssu.signal.domain.viewer.business.dto.VerificationResponse
import com.yourssu.signal.domain.viewer.business.dto.ViewerDetailResponse
import com.yourssu.signal.domain.viewer.business.dto.ViewerResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/viewers")
class ViewerController(
    private val viewerService: ViewerService,
) {
    @PostMapping("/verification")
    fun issueVerification(@Valid @RequestBody request: VerificationRequest) : ResponseEntity<Response<VerificationResponse>> {
        val response = viewerService.issueVerificationCode(request.toCommand())
        return ResponseEntity.ok(Response(result = response))
    }

    @PostMapping
    fun issueTicket(@Valid @RequestBody request: TicketIssuedRequest) : ResponseEntity<Response<ViewerResponse>> {
        val response = viewerService.issueTicket(request.toCommand())
        return ResponseEntity.ok(Response(result = response))
    }

    @GetMapping("/uuid")
    fun getViewer(@Valid @ModelAttribute request: FoundSelfRequest): ResponseEntity<Response<ViewerDetailResponse>> {
        val response = viewerService.getViewer(request.toCommand())
        return ResponseEntity.ok(Response(result = response))
    }

    @GetMapping
    fun findAllViewers(@Valid @ModelAttribute request: ViewersFoundRequest) : ResponseEntity<Response<List<ViewerResponse>>> {
        val response = viewerService.findAllViewers(request.toCommand())
        return ResponseEntity.ok(Response(result = response))
    }
}
