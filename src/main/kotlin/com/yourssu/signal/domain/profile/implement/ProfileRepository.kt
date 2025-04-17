package com.yourssu.signal.domain.profile.implement

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.profile.implement.domain.Gender
import com.yourssu.signal.domain.profile.implement.domain.Profile

interface ProfileRepository {
    fun save(profile: Profile): Profile
    fun getByUuid(uuid: Uuid): Profile
    fun existsByUuid(uuid: Uuid): Boolean
    fun findAll(): List<Profile>
    fun findAllOppositeGenderIds(gender: Gender): List<Long>
    fun updateCacheOppositeGenderIds(gender: Gender): List<Long>
    fun getById(id: Long): Profile
}
