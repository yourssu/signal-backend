package com.yourssu.ssugaeting.domain.viewer.application.dto

import jakarta.validation.constraints.NotBlank

class ViewersFoundRequest(
    @field:NotBlank
    val secretKey: String,
) {
}
