package com.yourssu.signal.config.security.exception

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtUtils: JwtUtils
) : OncePerRequestFilter() {
    companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
        const val X_AUTH_TOKEN_HEADER = "X-Auth-Token"
        const val BEARER_PREFIX = "Bearer "
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = extractTokenFromRequest(request)
        if (token != null && jwtUtils.validateToken(token)) {
            val userUuid = jwtUtils.getUserUuidFromToken(token)
            val authentication = UsernamePasswordAuthenticationToken(
                userUuid,
                null,
                emptyList()
            )
            SecurityContextHolder.getContext().authentication = authentication
        }

        filterChain.doFilter(request, response)
    }

    private fun extractTokenFromRequest(request: HttpServletRequest): String? {
        val authHeader = request.getHeader(AUTHORIZATION_HEADER)
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length)
        }
        val xAuthToken = request.getHeader(X_AUTH_TOKEN_HEADER)
        if (xAuthToken != null && xAuthToken.startsWith(BEARER_PREFIX)) {
            return xAuthToken.substring(BEARER_PREFIX.length)
        }
        return null
    }
}
