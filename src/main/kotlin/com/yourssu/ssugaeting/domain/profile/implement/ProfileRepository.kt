package com.yourssu.ssugaeting.domain.profile.implement

interface ProfileRepository {
    fun save(profile: Profile): Profile
}
