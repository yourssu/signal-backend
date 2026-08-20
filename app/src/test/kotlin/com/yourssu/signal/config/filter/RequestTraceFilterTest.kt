package com.yourssu.signal.config.filter

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import org.slf4j.MDC
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class RequestTraceFilterTest : StringSpec({
    val filter = RequestTraceFilter()

    "X-Request-ID가 없으면 traceId를 생성하여 응답한다" {
        val response = MockHttpServletResponse()

        filter.doFilter(MockHttpServletRequest(), response, MockFilterChain())

        response.getHeader(RequestTraceFilter.TRACE_HEADER) shouldMatch
            Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
        MDC.get(RequestTraceFilter.TRACE_KEY) shouldBe null
    }

    "유효한 X-Request-ID는 그대로 사용한다" {
        val request = MockHttpServletRequest().apply {
            addHeader(RequestTraceFilter.TRACE_HEADER, "client-request-123")
        }
        val response = MockHttpServletResponse()

        filter.doFilter(request, response, MockFilterChain())

        response.getHeader(RequestTraceFilter.TRACE_HEADER) shouldBe "client-request-123"
    }
})
