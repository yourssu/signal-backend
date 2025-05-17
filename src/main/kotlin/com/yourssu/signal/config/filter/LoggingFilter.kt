package com.yourssu.signal.config.filter

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import java.nio.charset.StandardCharsets

private val log = KotlinLogging.logger {}

//@Profile("!prod")
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

        val method = requestWrapper.method
        val requestUri = requestWrapper.requestURI
        val headers = requestWrapper.headerNames.toList()
            .associateWith { requestWrapper.getHeader(it) }
            .entries.joinToString(", ") { "\"${it.key.replace("\"", "\\\"")}\": \"${it.value.replace("\"", "\\\"")}\"" }
        val requestPayload = String(requestWrapper.contentAsByteArray, StandardCharsets.UTF_8)
        val responseStatus = responseWrapper.status
        val responsePayload = String(responseWrapper.contentAsByteArray, StandardCharsets.UTF_8)
        log.info {
            """{"Request":{"Method":"$method $requestUri - ${duration}ms","Headers": {$headers},"Payload":$requestPayload},"Reply":{"Payload":$responsePayload}}"""
                .replace("\n", "")
        }
        log.info {
            """{"Reply":{"Method":"$method $requestUri - ${duration}ms","Status":$responseStatus}}"""
                .replace("\n", "")
        }
        responseWrapper.copyBodyToResponse()
    }
}
