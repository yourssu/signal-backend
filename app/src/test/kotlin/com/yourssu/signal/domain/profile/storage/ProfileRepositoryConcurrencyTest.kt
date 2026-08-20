package com.yourssu.signal.domain.profile.storage

import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.profile.implement.Animal
import com.yourssu.signal.domain.profile.implement.Gender
import com.yourssu.signal.domain.profile.implement.Profile
import com.yourssu.signal.domain.profile.implement.ProfileRepository
import com.yourssu.signal.domain.profile.business.ProfileService
import com.yourssu.signal.domain.profile.business.command.ProfileCreatedCommand
import com.yourssu.signal.domain.blacklist.storage.BlacklistJpaRepository
import com.yourssu.signal.domain.user.implement.UserWriter
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.support.TransactionTemplate
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.test.assertFailsWith

@SpringBootTest
@ActiveProfiles("test")
class ProfileRepositoryConcurrencyTest {
    @Autowired
    private lateinit var profileRepository: ProfileRepository

    @Autowired
    private lateinit var profileJpaRepository: ProfileJpaRepository

    @Autowired
    private lateinit var transactionTemplate: TransactionTemplate

    @Autowired
    private lateinit var profileService: ProfileService

    @Autowired
    private lateinit var blacklistJpaRepository: BlacklistJpaRepository

    @Autowired
    private lateinit var userWriter: UserWriter

    private val executor = Executors.newFixedThreadPool(3)

    @BeforeEach
    fun setUp() {
        blacklistJpaRepository.deleteAll()
        profileJpaRepository.deleteAll()
    }

    @AfterEach
    fun tearDown() {
        executor.shutdownNow()
    }

    @Test
    fun `동일 기존 프로필을 처리하는 동시 생성 요청은 blacklist unique 충돌 없이 완료된다`() {
        val contact = "@concurrent_contact"
        val existingProfile = profileRepository.save(profile("existing-uuid", contact))
        userWriter.generateUser("concurrent-user-1")
        userWriter.generateUser("concurrent-user-2")
        val lockAcquired = CountDownLatch(1)
        val releaseLock = CountDownLatch(1)

        val lockHolder = executor.submit {
            transactionTemplate.executeWithoutResult {
                profileRepository.findByContact(contact)
                lockAcquired.countDown()
                releaseLock.await(5, TimeUnit.SECONDS) shouldBe true
            }
        }
        lockAcquired.await(5, TimeUnit.SECONDS) shouldBe true

        val firstRequest = executor.submit {
            profileService.createProfile(command("concurrent-user-1", contact, "first"))
        }
        val secondRequest = executor.submit {
            profileService.createProfile(command("concurrent-user-2", contact, "second"))
        }
        assertFailsWith<TimeoutException> { firstRequest.get(200, TimeUnit.MILLISECONDS) }
        assertFailsWith<TimeoutException> { secondRequest.get(200, TimeUnit.MILLISECONDS) }

        releaseLock.countDown()
        lockHolder.get(5, TimeUnit.SECONDS)
        firstRequest.get(5, TimeUnit.SECONDS)
        secondRequest.get(5, TimeUnit.SECONDS)

        blacklistJpaRepository.findAll().count { it.profileId == existingProfile.id } shouldBe 1
        blacklistJpaRepository.findAll().single { it.profileId == existingProfile.id }.createdByAdmin shouldBe true
    }

    private fun command(uuid: String, contact: String, nickname: String) = ProfileCreatedCommand(
        uuid = uuid,
        gender = "MALE",
        department = "컴퓨터학부",
        birthYear = 2000,
        animal = "DOG",
        contact = contact,
        mbti = "ENFP",
        nickname = nickname,
        introSentences = emptyList(),
        school = "숭실대학교",
    )

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
