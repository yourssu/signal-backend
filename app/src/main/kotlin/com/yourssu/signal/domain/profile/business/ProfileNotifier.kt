package com.yourssu.signal.domain.profile.business

import com.yourssu.signal.domain.profile.implement.Profile
import com.yourssu.signal.infrastructure.logging.Notification
import org.springframework.stereotype.Component

@Component
class ProfileNotifier {
    fun notifyCreatedProfile(profile: Profile) = Notification.notifyCreatedProfile(profile)

    fun notifyContactExceedsLimitWarning(contactLimitPolicy: Int) =
        Notification.notifyContactExceedsLimitWarning(contactLimitPolicy)

    fun notifyDetailedContactExceedsLimitWarning(
        contact: String,
        newProfileId: Long,
        existingProfileIds: List<Long>,
        currentCount: Int,
        contactLimitPolicy: Int,
    ) = Notification.notifyContactExceedsLimitWarning(
        contact, newProfileId, existingProfileIds, currentCount, contactLimitPolicy
    )

    fun notifyFailedProfileContactExceedsLimit(contactLimitPolicy: Int) =
        Notification.notifyFailedProfileContactExceedsLimit(contactLimitPolicy)

    fun notifyDetailedFailedProfileContactExceedsLimit(
        contact: String,
        existingProfileIds: List<Long>,
        attemptedCount: Int,
        contactLimitPolicy: Int,
    ) = Notification.notifyFailedProfileContactExceedsLimit(
        contact, existingProfileIds, attemptedCount, contactLimitPolicy
    )
}
