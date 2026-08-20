package com.yourssu.signal.domain.profile.storage

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.profile.implement.Animal
import com.yourssu.signal.domain.profile.implement.Gender
import com.yourssu.signal.domain.profile.implement.Profile
import com.yourssu.signal.domain.profile.implement.ProfileRepository
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProfileRepositoryImplTest {
    @Autowired
    private lateinit var profileRepository: ProfileRepository

    @Autowired
    private lateinit var profileJpaRepository: ProfileJpaRepository

    @BeforeEach
    fun setUp() {
        profileJpaRepository.deleteAll()
    }

    @Test
    fun `암호화 저장된 프로필 중 같은 연락처만 잠금 조회한다`() {
        profileRepository.save(profile("same-1", "@same_contact"))
        profileRepository.save(profile("same-2", "@same_contact"))
        profileRepository.save(profile("different", "@different_contact"))

        val result = profileRepository.findByContact("@same_contact")

        result.map { it.uuid.value } shouldContainExactlyInAnyOrder listOf("same-1", "same-2")
    }

    private fun profile(uuid: String, contact: String) = Profile(
        uuid = Uuid(uuid),
        gender = Gender.MALE,
        department = "컴퓨터학부",
        birthYear = 2000,
        animal = Animal.DOG,
        contact = contact,
        mbti = "ENFP",
        nickname = uuid,
        introSentences = emptyList(),
        school = "숭실대학교",
    )
}
