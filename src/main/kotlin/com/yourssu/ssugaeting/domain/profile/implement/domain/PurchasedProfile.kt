package com.yourssu.ssugaeting.domain.profile.implement.domain

import java.time.ZonedDateTime

class PurchasedProfile(
    val id: Long? = null,
    val profileId: Long,
    val viewerId: Long,
    val createdTime: ZonedDateTime = ZonedDateTime.now(),
) {
}
