package com.yourssu.ssugaeting.domain.viewer.business.dto

import com.yourssu.ssugaeting.domain.verification.implement.domain.VerificationCode

class VerificationResponse(
   val verificationCode: Int,
) {
   companion object {
        fun from(verificationCode: VerificationCode): VerificationResponse {
             return VerificationResponse(verificationCode.value)
        }
   }
}
