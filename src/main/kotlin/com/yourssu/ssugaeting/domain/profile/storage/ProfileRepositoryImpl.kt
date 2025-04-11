package com.yourssu.ssugaeting.domain.profile.storage

import com.yourssu.ssugaeting.domain.profile.implement.Profile
import com.yourssu.ssugaeting.domain.profile.implement.ProfileRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
class ProfileRepositoryImpl(
    private val profileJpaRepository: ProfileJpaRepository,
) : ProfileRepository {
    override fun save(profile: Profile): Profile {
        return profileJpaRepository.save(ProfileEntity.from(profile))
            .toDomain()
    }
}

interface ProfileJpaRepository: JpaRepository<ProfileEntity, Long> {
}
