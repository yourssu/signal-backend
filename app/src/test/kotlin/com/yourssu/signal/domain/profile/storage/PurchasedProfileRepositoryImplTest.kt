package com.yourssu.signal.domain.profile.storage

import com.yourssu.signal.domain.profile.implement.Gender
import com.yourssu.signal.domain.profile.implement.PurchasedProfileRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PurchasedProfileRepositoryImplIntegrationTest {

    @Autowired
    private lateinit var purchasedProfileRepository: PurchasedProfileRepository

    @Autowired
    private lateinit var profileJpaRepository: ProfileJpaRepository

    @Autowired
    private lateinit var purchasedProfileJpaRepository: PurchasedProfileJpaRepository

    @BeforeEach
    fun setup() {
        purchasedProfileJpaRepository.deleteAll()
        profileJpaRepository.deleteAll()
    }

    @Test
    fun `MALE 프로필 3개 구매 기록 저장 후 countByGender(MALE)은 3을 반환한다`() {
        // given
        val maleProfile1 = createAndSaveProfile(gender = Gender.MALE, uuid = "male-uuid-1")
        val maleProfile2 = createAndSaveProfile(gender = Gender.MALE, uuid = "male-uuid-2")
        val maleProfile3 = createAndSaveProfile(gender = Gender.MALE, uuid = "male-uuid-3")

        val viewerId = 1L
        purchasedProfileJpaRepository.save(
            PurchasedProfileEntity(profileId = maleProfile1.id!!, viewerId = viewerId)
        )
        purchasedProfileJpaRepository.save(
            PurchasedProfileEntity(profileId = maleProfile2.id!!, viewerId = viewerId)
        )
        purchasedProfileJpaRepository.save(
            PurchasedProfileEntity(profileId = maleProfile3.id!!, viewerId = viewerId)
        )

        // when
        val result = purchasedProfileRepository.countByGender(Gender.MALE)

        // then
        assertEquals(3, result)
    }

    @Test
    fun `FEMALE 프로필 2개, MALE 1개 저장 후 각각의 카운트가 정확하다`() {
        // given
        val femaleProfile1 = createAndSaveProfile(gender = Gender.FEMALE, uuid = "female-uuid-1")
        val femaleProfile2 = createAndSaveProfile(gender = Gender.FEMALE, uuid = "female-uuid-2")
        val maleProfile1 = createAndSaveProfile(gender = Gender.MALE, uuid = "male-uuid-1")

        val viewerId = 1L
        purchasedProfileJpaRepository.save(
            PurchasedProfileEntity(profileId = femaleProfile1.id!!, viewerId = viewerId)
        )
        purchasedProfileJpaRepository.save(
            PurchasedProfileEntity(profileId = femaleProfile2.id!!, viewerId = viewerId)
        )
        purchasedProfileJpaRepository.save(
            PurchasedProfileEntity(profileId = maleProfile1.id!!, viewerId = viewerId)
        )

        // when
        val femaleCount = purchasedProfileRepository.countByGender(Gender.FEMALE)
        val maleCount = purchasedProfileRepository.countByGender(Gender.MALE)

        // then
        assertEquals(2, femaleCount)
        assertEquals(1, maleCount)
    }

    @Test
    fun `같은 MALE 프로필을 3명의 viewer가 구매하면 countByGender(MALE)은 3을 반환한다`() {
        // given
        val maleProfile = createAndSaveProfile(gender = Gender.MALE, uuid = "male-uuid-1")

        purchasedProfileJpaRepository.save(
            PurchasedProfileEntity(profileId = maleProfile.id!!, viewerId = 1L)
        )
        purchasedProfileJpaRepository.save(
            PurchasedProfileEntity(profileId = maleProfile.id!!, viewerId = 2L)
        )
        purchasedProfileJpaRepository.save(
            PurchasedProfileEntity(profileId = maleProfile.id!!, viewerId = 3L)
        )

        // when
        val result = purchasedProfileRepository.countByGender(Gender.MALE)

        // then
        assertEquals(3, result)
    }

    @Test
    fun `구매 기록이 없으면 countByGender는 0을 반환한다`() {
        // given - 아무 데이터도 저장하지 않음

        // when
        val maleCount = purchasedProfileRepository.countByGender(Gender.MALE)
        val femaleCount = purchasedProfileRepository.countByGender(Gender.FEMALE)

        // then
        assertEquals(0, maleCount)
        assertEquals(0, femaleCount)
    }

    private fun createAndSaveProfile(
        gender: Gender,
        uuid: String,
        nickname: String = "테스트닉네임",
        contact: String = "@test_contact"
    ): ProfileEntity {
        return profileJpaRepository.save(
            ProfileEntity(
                uuid = uuid,
                gender = gender,
                department = "컴퓨터학부",
                birthYear = 2000,
                animal = "강아지",
                contact = contact,
                mbti = "ENFP",
                nickname = nickname,
                school = "숭실대학교"
            )
        )
    }
}

class PurchasedProfileRepositoryImplTest {

    @Test
    fun `ranking calculation logic should work correctly with different purchase counts`() {
        val purchaseCounts = listOf(3, 2, 1)
        
        val countToRankMap = purchaseCounts
            .withIndex()
            .groupBy { it.value }
            .mapValues { it.value.minOf { indexedValue -> indexedValue.index } + 1 }
        
        assertEquals(1, countToRankMap[3])
        assertEquals(2, countToRankMap[2])
        assertEquals(3, countToRankMap[1])
    }

    @Test
    fun `ranking calculation logic should handle ties correctly`() {
        val purchaseCounts = listOf(2, 2, 1)
        
        val countToRankMap = purchaseCounts
            .withIndex()
            .groupBy { it.value }
            .mapValues { it.value.minOf { indexedValue -> indexedValue.index } + 1 }
        
        assertEquals(1, countToRankMap[2])
        assertEquals(3, countToRankMap[1])
    }

    @Test
    fun `ranking calculation logic should handle multiple ties correctly`() {
        val purchaseCounts = listOf(3, 2, 2, 1, 1, 1)
        
        val countToRankMap = purchaseCounts
            .withIndex()
            .groupBy { it.value }
            .mapValues { it.value.minOf { indexedValue -> indexedValue.index } + 1 }
        
        assertEquals(1, countToRankMap[3])
        assertEquals(2, countToRankMap[2])
        assertEquals(4, countToRankMap[1])
    }

    @Test
    fun `ranking calculation logic should work with single item`() {
        val purchaseCounts = listOf(5)
        
        val countToRankMap = purchaseCounts
            .withIndex()
            .groupBy { it.value }
            .mapValues { it.value.minOf { indexedValue -> indexedValue.index } + 1 }
        
        assertEquals(1, countToRankMap[5])
    }

    @Test
    fun `ranking calculation logic should handle empty list`() {
        val purchaseCounts = emptyList<Int>()
        
        val countToRankMap = purchaseCounts
            .withIndex()
            .groupBy { it.value }
            .mapValues { it.value.minOf { indexedValue -> indexedValue.index } + 1 }
        
        assertEquals(0, countToRankMap.size)
    }
}