package com.yourssu.signal.domain.viewer.application

import com.yourssu.signal.domain.common.business.dto.Response
import com.yourssu.signal.domain.viewer.application.dto.FoundSelfRequest
import com.yourssu.signal.domain.viewer.application.dto.SMSTicketIssuedRequest
import com.yourssu.signal.domain.viewer.application.dto.TicketIssuedRequest
import com.yourssu.signal.domain.viewer.application.dto.VerificationRequest
import com.yourssu.signal.domain.viewer.application.dto.ViewersFoundRequest
import com.yourssu.signal.domain.viewer.business.ViewerService
import com.yourssu.signal.domain.viewer.business.dto.VerificationResponse
import com.yourssu.signal.domain.viewer.business.dto.ViewerDetailResponse
import com.yourssu.signal.domain.viewer.business.dto.ViewerResponse
import com.yourssu.signal.infrastructure.TicketSseManager
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/api/viewers")
class ViewerController(
    private val viewerService: ViewerService,
    private val ticketSseManager: TicketSseManager,
) {
    @PostMapping("/verification")
    fun issueVerification(@Valid @RequestBody request: VerificationRequest): ResponseEntity<Response<VerificationResponse>> {
        val response = viewerService.issueVerificationCode(request.toCommand())
        return ResponseEntity.ok(Response(result = response))
    }

    @PostMapping
    fun issueTicket(@Valid @RequestBody request: TicketIssuedRequest): ResponseEntity<Response<ViewerResponse>> {
        val response = viewerService.issueTicket(request.toCommand())
        ticketSseManager.notifyTicketIssued(response)
        return ResponseEntity.ok(Response(result = response))
    }

    @PostMapping("/sms")
    fun issueTicket(@Valid @RequestBody request: SMSTicketIssuedRequest): ResponseEntity<Response<ViewerResponse>> {
        val response = viewerService.issueTicket(request.toCommand())
        ticketSseManager.notifyTicketIssued(response)
        return ResponseEntity.ok(Response(result = response))
    }

    @GetMapping("/tickets/events", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun subscribeToTicketEvents(@Valid @ModelAttribute request: FoundSelfRequest): SseEmitter {
        return ticketSseManager.streamTicketEvents(request.toCommand())
    }

    @GetMapping("/uuid")
    fun getViewer(@Valid @ModelAttribute request: FoundSelfRequest): ResponseEntity<Response<ViewerDetailResponse>> {
        val response = viewerService.getViewer(request.toCommand())
        return ResponseEntity.ok(Response(result = response))
    }

    @GetMapping
    fun findAllViewers(@Valid @ModelAttribute request: ViewersFoundRequest): ResponseEntity<Response<List<ViewerResponse>>> {
        val response = viewerService.findAllViewers(request.toCommand())
        return ResponseEntity.ok(Response(result = response))
    }
}
