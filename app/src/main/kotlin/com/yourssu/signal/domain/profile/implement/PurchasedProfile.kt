package com.yourssu.signal.domain.profile.implement

import java.time.ZonedDateTime

class PurchasedProfile(
    val id: Long? = null,
    val profileId: Long,
    val viewerId: Long,
    val createdTime: ZonedDateTime? = null,
) {
}
