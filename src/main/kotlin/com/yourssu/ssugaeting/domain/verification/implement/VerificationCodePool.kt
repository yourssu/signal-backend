package com.yourssu.ssugaeting.domain.verification.implement

import com.yourssu.ssugaeting.domain.verification.implement.domain.VerificationCode
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

private const val MAXIMUM_VERIFICATION_CODE = 9999

@Component
class VerificationCodePool(
    private val codeQueue: Queue<Int> = ConcurrentLinkedQueue(),
    private val verificationRepository: VerificationRepository,
) {
    @PostConstruct
    fun initialize() {
        codeQueue.addAll(shuffledCandidates())
    }

    private fun shuffledCandidates(): List<Int> {
        val excludeCodes = verificationRepository.findAll()
            .toHashSet()
        return (0 until MAXIMUM_VERIFICATION_CODE)
            .filter { it !in excludeCodes }
            .shuffled()
    }

    fun pop(): VerificationCode {
        if (codeQueue.isEmpty()) {
            verificationRepository.clear()
            codeQueue.addAll(shuffledCandidates())
        }
        return VerificationCode(codeQueue.poll())
    }
}
