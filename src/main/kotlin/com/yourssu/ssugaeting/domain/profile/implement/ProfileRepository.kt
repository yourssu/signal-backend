package com.yourssu.ssugaeting.domain.profile.implement

import com.yourssu.ssugaeting.domain.common.implement.Uuid

interface ProfileRepository {
    fun save(profile: Profile): Profile
    fun getByUuid(uuid: Uuid): Profile
    fun existsByUuid(uuid: Uuid): Boolean
    fun findAll(): List<Profile>
    fun updateCacheProfiles(): List<Profile>
    fun getById(id: Long): Profile
}
