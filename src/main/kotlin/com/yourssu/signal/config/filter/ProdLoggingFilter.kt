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

private val log = KotlinLogging.logger {}

@Profile("prod")
@Component
class ProdLoggingFilter : OncePerRequestFilter() {
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
        val responseStatus = responseWrapper.status
        log.info {
            """{"Reply":{"Method":"$method $requestUri - ${duration}ms","Status":$responseStatus}}"""
                .replace("\n", "")
        }
        responseWrapper.copyBodyToResponse()
    }
}
