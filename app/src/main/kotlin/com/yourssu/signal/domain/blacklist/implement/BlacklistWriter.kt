package com.yourssu.signal.domain.blacklist.implement

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

@Component
class BlacklistWriter(
    private val blacklistRepository: BlacklistRepository,
) {
    @Transactional
    fun save(blacklist: Blacklist): Blacklist {
        val savedBlacklist = blacklistRepository.save(blacklist)
        updateCacheAfterCompletion()
        return savedBlacklist
    }

    @Transactional
    fun updateToAdminBlacklist(profileId: Long) {
        blacklistRepository.updateToAdminBlacklist(profileId)
        updateCacheAfterCompletion()
    }

    @Transactional
    fun deleteByProfileId(profileId: Long) {
        blacklistRepository.deleteByProfileId(profileId)
        updateCacheAfterCompletion()
    }

    private fun updateCacheAfterCompletion() {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            blacklistRepository.updateCache()
            return
        }
        TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
            override fun afterCompletion(status: Int) {
                blacklistRepository.updateCache()
            }
        })
    }
}
