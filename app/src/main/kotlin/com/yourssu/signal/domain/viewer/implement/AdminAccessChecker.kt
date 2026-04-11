package com.yourssu.signal.domain.viewer.implement

import com.yourssu.signal.config.properties.AdminConfigurationProperties
import com.yourssu.signal.domain.viewer.implement.exception.AdminPermissionDeniedException
import org.springframework.stereotype.Component

@Component
class AdminAccessChecker(
    private val adminConfigurationProperties: AdminConfigurationProperties,
) {
    fun validateAdminAccess(secretKey: String) {
        if (!adminConfigurationProperties.isValidAccessKey(secretKey)) {
            throw AdminPermissionDeniedException()
        }
    }
}
