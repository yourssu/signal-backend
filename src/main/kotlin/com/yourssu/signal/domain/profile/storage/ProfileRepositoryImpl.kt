package com.yourssu.signal.domain.profile.storage

import com.querydsl.jpa.impl.JPAQueryFactory
import com.yourssu.signal.config.security.DataCipher
import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.profile.implement.ProfileRepository
import com.yourssu.signal.domain.profile.implement.domain.Gender
import com.yourssu.signal.domain.profile.implement.domain.Profile
import com.yourssu.signal.domain.profile.storage.domain.ProfileEntity
import com.yourssu.signal.domain.profile.storage.domain.QProfileEntity.profileEntity
import com.yourssu.signal.domain.profile.storage.execption.ProfileNotFoundException
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
class ProfileRepositoryImpl(
    private val profileJpaRepository: ProfileJpaRepository,
    private val jpaQueryFactory: JPAQueryFactory,
    private val dataCipher: DataCipher,
) : ProfileRepository {
    override fun save(profile: Profile): Profile {
        val encryptedContact = dataCipher.encrypt(profile.contact)
        val saveProfile = profileJpaRepository.save(ProfileEntity.from(profile, encryptedContact))
        return decryptContact(saveProfile.toDomain(introSentences = profile.introSentences))
    }

    override fun getByUuid(uuid: Uuid): Profile {
        return decryptContact(jpaQueryFactory.selectFrom(profileEntity)
            .where(profileEntity.uuid.eq(uuid.value))
            .fetchFirst()
            ?.toDomain()
            ?: throw ProfileNotFoundException())
    }

    override fun getById(id: Long): Profile {
        return decryptContact(profileJpaRepository.findById(id)
            .orElseThrow { ProfileNotFoundException() }
            .toDomain())
    }

    override fun existsByUuid(uuid: Uuid): Boolean {
        return jpaQueryFactory.selectFrom(profileEntity)
            .where(profileEntity.uuid.eq(uuid.value))
            .fetchFirst() != null
    }

    override fun findAll(): List<Profile> {
        return profileJpaRepository.findAll().map { decryptContact(it.toDomain()) }
    }

    @Cacheable(cacheNames = ["profileCache"], key = "#gender.name")
    override fun findIdsByGender(gender: Gender): List<Long> {
        return jpaQueryFactory.select(profileEntity.id)
            .from(profileEntity)
            .where(profileEntity.gender.eq(gender))
            .fetch()
    }

    @CachePut(cacheNames = ["profileCache"], key = "#gender.name")
    override fun updateCacheIdsByGender(gender: Gender): List<Long> {
        return jpaQueryFactory.select(profileEntity.id)
            .from(profileEntity)
            .where(profileEntity.gender.eq(gender))
            .fetch()
    }

    private fun decryptContact(profile: Profile): Profile {
        return profile.copy(
            contact = dataCipher.decrypt(profile.contact)
        )
    }
}

interface ProfileJpaRepository: JpaRepository<ProfileEntity, Long> {
}
