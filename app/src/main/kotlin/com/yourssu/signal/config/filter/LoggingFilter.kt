package com.yourssu.signal.config.filter

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
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
class LoggingFilter(
    private val objectMapper: ObjectMapper,
) : OncePerRequestFilter() {
    override fun shouldNotFilter(request: HttpServletRequest): Boolean = request.method == "OPTIONS"

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
        val headers = LOGGED_HEADERS.mapNotNull { headerName ->
            requestWrapper.getHeader(headerName)?.let { headerName to it }
        }.joinToString(", ") { (name, value) -> "\"$name\": \"${value.escapeJson()}\"" }
        val redactAuthCode = requestUri.startsWith(AUTH_URI_PREFIX)
        val requestPayload = requestWrapper.contentAsByteArray.toLogPayload(
            maxBytes = MAX_DIAGNOSTIC_PAYLOAD_BYTES,
            redactAuthCode = redactAuthCode,
        )
        val responseStatus = responseWrapper.status
        val responsePayloadLimit = if (method == "GET" && responseStatus in 200..299) {
            MAX_SUCCESS_GET_PAYLOAD_BYTES
        } else {
            MAX_DIAGNOSTIC_PAYLOAD_BYTES
        }
        val responsePayload = responseWrapper.contentAsByteArray.toLogPayload(
            maxBytes = responsePayloadLimit,
            forceSummary = responseStatus in 200..299 && requestUri in SUMMARY_RESPONSE_URIS,
            redactAuthCode = redactAuthCode,
        )
        log.info {
            """{"Request":{"Method":"$method $requestUri - ${duration}ms","Payload":$requestPayload,"Headers": {$headers}},"Reply":{"Payload":$responsePayload}}"""
                .replace("\n", "")
        }
        log.info {
            """{"Reply":{"Method":"$method $requestUri - ${duration}ms","Status":$responseStatus}}"""
                .replace("\n", "")
        }
        responseWrapper.copyBodyToResponse()
    }

    private fun ByteArray.toLogPayload(
        maxBytes: Int,
        forceSummary: Boolean = false,
        redactAuthCode: Boolean = false,
    ): String {
        if (isEmpty()) return "{}"
        if (forceSummary || size > maxBytes) return "{\"Truncated\":true,\"Bytes\":$size}"
        val payload = String(this, StandardCharsets.UTF_8)
        return redactSensitiveFields(payload, redactAuthCode)
    }

    private fun redactSensitiveFields(payload: String, redactAuthCode: Boolean): String = try {
        objectMapper.readTree(payload).also { node -> redactSensitiveFields(node, redactAuthCode) }.toString()
    } catch (_: Exception) {
        payload
    }

    private fun redactSensitiveFields(node: JsonNode, redactAuthCode: Boolean) {
        when {
            node.isObject -> (node as ObjectNode).fields().forEachRemaining { (name, value) ->
                if (name.lowercase() in SENSITIVE_PAYLOAD_FIELDS || redactAuthCode && name.equals("code", ignoreCase = true)) {
                    node.put(name, "***")
                } else {
                    redactSensitiveFields(value, redactAuthCode)
                }
            }
            node.isArray -> node.forEach { redactSensitiveFields(it, redactAuthCode) }
        }
    }

    private fun String.escapeJson(): String = buildString(length) {
        this@escapeJson.forEach { character ->
            append(
                when (character) {
                    '\\' -> "\\\\"
                    '"' -> "\\\""
                    '\n' -> "\\n"
                    '\r' -> "\\r"
                    '\t' -> "\\t"
                    else -> character
                },
            )
        }
    }

    companion object {
        private const val MAX_SUCCESS_GET_PAYLOAD_BYTES = 2 * 1024
        private const val MAX_DIAGNOSTIC_PAYLOAD_BYTES = 8 * 1024
        private const val AUTH_URI_PREFIX = "/api/auth"
        private val LOGGED_HEADERS = listOf("x-real-ip", "user-agent", "content-type")
        private val SENSITIVE_PAYLOAD_FIELDS = setOf(
            "accesstoken",
            "refreshtoken",
            "secretkey",
            "authorization",
            "cookie",
        )
        private val SUMMARY_RESPONSE_URIS = setOf(
            "/api/profiles/deck",
            "/api/profiles/me/purchased",
        )
    }
}
