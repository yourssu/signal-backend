package com.yourssu.signal.api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class ReportCreatedRequest(@field:Positive val profileId: Long)
data class ReportApprovedRequest(@field:NotBlank val secretKey: String)
