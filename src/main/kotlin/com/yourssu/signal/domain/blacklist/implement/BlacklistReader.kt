package com.yourssu.signal.domain.blacklist.implement

import com.yourssu.signal.domain.blacklist.implement.Blacklist
import org.springframework.stereotype.Component

@Component
class BlacklistReader(
    private val blacklistRepository: BlacklistRepository,
) {
    fun existsByProfileId(profileId: Long): Boolean {
        return blacklistRepository.existsByProfileId(profileId)
    }

    fun getByProfileId(profileId: Long): Blacklist {
        return blacklistRepository.getByProfileId(profileId)
    }

    fun getAllBlacklistIds(): Set<Long> {
        return blacklistRepository.findAll()
    }

    fun isAddedByAdmin(profileId: Long): Boolean {
        return blacklistRepository.isAddedByAdmin(profileId)
    }
}
