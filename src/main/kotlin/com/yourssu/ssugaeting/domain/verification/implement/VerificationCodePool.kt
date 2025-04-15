package com.yourssu.ssugaeting.domain.verification.implement

import com.yourssu.ssugaeting.domain.verification.implement.domain.VerificationCode
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

private const val MAXIMUM_VERIFICATION_CODE = 9999

@Component
class VerificationCodePool(
    private val codeQueue: Queue<Int> = ConcurrentLinkedQueue(shuffledCandidates()),
    private val verificationRepository: VerificationRepository,
) {
    companion object {
        fun shuffledCandidates(): List<Int> {
            return (0..MAXIMUM_VERIFICATION_CODE).shuffled()
        }
    }
    fun pop(): VerificationCode {
        if (codeQueue.isEmpty()) {
            verificationRepository.clear()
            codeQueue.addAll(shuffledCandidates())
        }
        return VerificationCode(codeQueue.poll())
    }
}
