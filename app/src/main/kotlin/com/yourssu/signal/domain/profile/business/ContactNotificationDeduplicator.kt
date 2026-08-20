package com.yourssu.signal.domain.profile.business

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.common.base.Ticker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets.UTF_8
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

private const val DEDUP_TTL_HOURS = 1L

enum class ContactNotificationType {
    WARNING,
    FAILURE,
}

@Component
class ContactNotificationDeduplicator internal constructor(
    private val notifiedContacts: Cache<String, Boolean>,
) {
    @Autowired
    constructor() : this(buildCache(Ticker.systemTicker()))

    internal constructor(ticker: Ticker) : this(buildCache(ticker))

    fun shouldNotify(contact: String, type: ContactNotificationType): Boolean {
        val key = digest("${type.name}:$contact")
        return notifiedContacts.asMap().putIfAbsent(key, true) == null
    }

    private fun digest(value: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(UTF_8))
            .joinToString("") { "%02x".format(it) }
    }

    companion object {
        private fun buildCache(ticker: Ticker): Cache<String, Boolean> {
            return CacheBuilder.newBuilder()
                .expireAfterWrite(DEDUP_TTL_HOURS, TimeUnit.HOURS)
                .ticker(ticker)
                .build()
        }
    }
}
