package com.yourssu.ssugaeting.domain.profile.implement

import com.yourssu.ssugaeting.domain.common.implement.Uuid

interface ProfileRepository {
    fun save(profile: Profile): Profile
    fun getByUuid(uuid: Uuid): Profile
}
