package com.yourssu.signal.domain.profile.storage

import org.springframework.cache.annotation.CacheEvict
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

private const val TEN_MINUTE = 60 * 1000L

@Component
class ProfileRankingCacheScheduler {
    @Scheduled(fixedRate = TEN_MINUTE)
    @CacheEvict(value = ["profileRankingCache"], allEntries = true)
    fun evictProfileRankingCache() {
    }
}
