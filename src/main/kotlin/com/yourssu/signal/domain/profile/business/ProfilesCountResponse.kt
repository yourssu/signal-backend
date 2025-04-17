package com.yourssu.signal.domain.profile.business

data class ProfilesCountResponse(
    val count: Int,
) {
    companion object {
        fun of(count: Int): ProfilesCountResponse {
            return ProfilesCountResponse(count)
        }
    }
}
