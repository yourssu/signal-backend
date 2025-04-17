package com.yourssu.signal.config.filter

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import java.nio.charset.StandardCharsets

private val log = KotlinLogging.logger {}

@Component
class LoggingFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val requestWrapper = ContentCachingRequestWrapper(request)
        val responseWrapper = ContentCachingResponseWrapper(response)

        val startTime = System.currentTimeMillis()
        filterChain.doFilter(requestWrapper, responseWrapper)
        val duration = System.currentTimeMillis() - startTime

        val requestUri = requestWrapper.requestURI
        val method = requestWrapper.method
        val requestPayload = String(requestWrapper.contentAsByteArray, StandardCharsets.UTF_8)
        val responseStatus = responseWrapper.status
        val responsePayload = String(responseWrapper.contentAsByteArray, StandardCharsets.UTF_8)
        log.info {
            """{"Request":{"Method":"$method $requestUri - ${duration}ms","Payload":$requestPayload}}"""
                .replace("\n", "")
                .trimIndent()
        }
        log.info {
            """{"Reply":{"Method":"$method $requestUri - ${duration}ms","Status":$responseStatus,"Payload": $responsePayload}}"""
                .replace("\n", "")
                .trimIndent()
        }
        responseWrapper.copyBodyToResponse()
    }
}
