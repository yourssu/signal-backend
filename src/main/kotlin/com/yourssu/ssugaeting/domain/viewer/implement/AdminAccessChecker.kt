package com.yourssu.ssugaeting.domain.viewer.implement

import com.yourssu.ssugaeting.config.AdminConfigurationProperties
import com.yourssu.ssugaeting.domain.viewer.implement.exception.AdminPermissionDeniedException
import org.springframework.stereotype.Component

@Component
class AdminAccessChecker(
    private val adminConfigurationProperties: AdminConfigurationProperties,
) {
    fun validateAdminAccess(secretKey: String) {
        if (!adminConfigurationProperties.isValidSecretKey(secretKey)) {
            throw AdminPermissionDeniedException()
        }
    }
}
