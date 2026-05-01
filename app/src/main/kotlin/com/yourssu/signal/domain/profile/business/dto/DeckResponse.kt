package com.yourssu.signal.domain.profile.business.dto

import com.yourssu.signal.domain.profile.implement.Profile

class DeckResponse(
    val profiles: List<ProfileResponse>,
) {
    companion object {
        fun from(profiles: List<Profile>) = DeckResponse(
            profiles = profiles.map { ProfileResponse.from(it) }
        )
    }
}
