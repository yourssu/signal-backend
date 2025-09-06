package com.yourssu.signal.domain.viewer.business.dto

import VerificationCode

data class VerificationResponse(
   val verificationCode: Int,
) {
   companion object {
        fun from(verificationCode: VerificationCode): VerificationResponse {
             return VerificationResponse(verificationCode.value)
        }
   }
}
