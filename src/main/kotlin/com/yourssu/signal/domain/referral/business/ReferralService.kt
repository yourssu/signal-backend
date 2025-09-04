package com.yourssu.signal.domain.referral.business

import com.yourssu.signal.domain.referral.business.command.ReferralCodeGenerateCommand
import com.yourssu.signal.domain.referral.business.dto.ReferralCodeResponse
import com.yourssu.signal.domain.referral.implement.ReferralWriter
import com.yourssu.signal.domain.referral.implement.domain.Referral
import com.yourssu.signal.domain.user.implement.UserReader
import org.springframework.stereotype.Service

@Service
class ReferralService(
    private val userReader: UserReader,
    private val referralWriter: ReferralWriter,
) {
    fun generateReferralCode(command: ReferralCodeGenerateCommand): ReferralCodeResponse {
        val user = userReader.getByUuid(command.toUuid())
        val referral = referralWriter.save(
            Referral(
                origin = user.uuid.value,
                referralCode = command.referralCode()
            )
        )
        return ReferralCodeResponse(referral.referralCode)
    }
}
