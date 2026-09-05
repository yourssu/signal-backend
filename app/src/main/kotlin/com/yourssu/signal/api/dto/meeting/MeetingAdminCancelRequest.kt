package com.yourssu.signal.api.dto.meeting

import jakarta.validation.constraints.NotBlank

data class MeetingAdminCancelRequest(@field:NotBlank val secretKey: String)
