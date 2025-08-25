package com.yourssu.signal.api.dto

import jakarta.validation.constraints.NotBlank

data class DevTokenRequest(
    @field:NotBlank(message = "UUID is required")
    val uuid: String,
    @field:NotBlank(message = "Access key is required")
    val accessKey: String
)
