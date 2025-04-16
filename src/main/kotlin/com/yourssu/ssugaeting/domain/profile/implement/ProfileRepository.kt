package com.yourssu.ssugaeting.domain.profile.implement

import com.yourssu.ssugaeting.domain.common.implement.Uuid
import com.yourssu.ssugaeting.domain.profile.implement.domain.Profile

interface ProfileRepository {
    fun save(profile: Profile): Profile
    fun getByUuid(uuid: Uuid): Profile
    fun existsByUuid(uuid: Uuid): Boolean
    fun findAll(): List<Profile>
    fun findAllIds(): List<Long>
    fun updateCacheProfiles(): List<Long>
    fun getById(id: Long): Profile
}
