package com.yourssu.ssugaeting.domain.profile.storage

import com.querydsl.jpa.impl.JPAQueryFactory
import com.yourssu.ssugaeting.domain.common.implement.Uuid
import com.yourssu.ssugaeting.domain.profile.implement.Profile
import com.yourssu.ssugaeting.domain.profile.implement.ProfileRepository
import com.yourssu.ssugaeting.domain.profile.storage.execption.ProfileNotFoundException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
class ProfileRepositoryImpl(
    private val profileJpaRepository: ProfileJpaRepository,
    private val jpaQueryFactory: JPAQueryFactory,
) : ProfileRepository {
    override fun save(profile: Profile): Profile {
        return profileJpaRepository.save(ProfileEntity.from(profile))
            .toDomain()
    }

    override fun getByUuid(uuid: Uuid): Profile {
        return jpaQueryFactory.selectFrom(QProfileEntity.profileEntity)
            .where(QProfileEntity.profileEntity.uuid.eq(uuid.value))
            .fetchFirst()
            ?.toDomain()
            ?: throw ProfileNotFoundException()
    }
}

interface ProfileJpaRepository: JpaRepository<ProfileEntity, Long> {
}
