package com.yourssu.signal.config.security.annotation

import org.springframework.security.access.prepost.PreAuthorize

/**
 * 인증이 필요한 엔드포인트에 사용
 * JWT 토큰이 유효한 사용자만 접근 가능
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("isAuthenticated()")
annotation class RequireAuth

/**
 * 인증이 필요 없는 공개 엔드포인트에 사용
 * 명시적으로 공개 API임을 표시
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("permitAll()")
annotation class PublicApi

/**
 * 특정 권한이 필요한 엔드포인트에 사용
 * 예: @RequireRole("ADMIN")
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("hasRole(#role)")
annotation class RequireRole(val role: String)