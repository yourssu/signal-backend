package com.yourssu.signal.config.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class RequestTraceFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val traceId = request.getHeader(TRACE_HEADER)
            ?.takeIf { TRACE_ID_PATTERN.matches(it) }
            ?: UUID.randomUUID().toString()

        MDC.put(TRACE_KEY, traceId)
        response.setHeader(TRACE_HEADER, traceId)
        try {
            filterChain.doFilter(request, response)
        } finally {
            MDC.remove(TRACE_KEY)
        }
    }

    companion object {
        const val TRACE_HEADER = "X-Request-ID"
        const val TRACE_KEY = "traceId"
        private val TRACE_ID_PATTERN = Regex("[A-Za-z0-9._-]{1,100}")
    }
}
