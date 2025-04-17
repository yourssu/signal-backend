package com.yourssu.signal.domain.viewer.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.yourssu.signal.domain.common.business.dto.Response
import com.yourssu.signal.domain.viewer.application.dto.*
import com.yourssu.signal.domain.viewer.business.ViewerService
import com.yourssu.signal.domain.viewer.business.dto.VerificationResponse
import com.yourssu.signal.domain.viewer.business.dto.ViewerDetailResponse
import com.yourssu.signal.domain.viewer.business.dto.ViewerResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

private val logger = KotlinLogging.logger {}
private val mapper = ObjectMapper()

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
        logger.info { "POST /api/viewers response: ${mapper.writeValueAsString(
            mapOf(
                "인증 코드" to request.verificationCode,
                "발급한 이용권 개수" to request.ticket,
                "보유한 이용권 개수" to response.ticket - response.usedTicket,
            )
        )}" }
        return ResponseEntity.ok(Response(result = response))
    }

    @GetMapping("/uuid")
    fun getViewer(@Valid @ModelAttribute request: ViewFoundRequest): ResponseEntity<Response<ViewerDetailResponse>> {
        val response = viewerService.getViewer(request.toCommand())
        return ResponseEntity.ok(Response(result = response))
    }

    @GetMapping
    fun findAllViewers(@Valid @ModelAttribute request: ViewersFoundRequest) : ResponseEntity<Response<List<ViewerResponse>>> {
        val response = viewerService.findAllViewers(request.toCommand())
        return ResponseEntity.ok(Response(result = response))
    }
}
