package com.yourssu.signal.domain.blacklist.business

import com.yourssu.signal.domain.blacklist.implement.BlacklistReader
import com.yourssu.signal.domain.blacklist.implement.BlacklistWriter
import com.yourssu.signal.domain.blacklist.implement.exception.AdminBlacklistCannotBeRemovedException
import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.profile.implement.Animal
import com.yourssu.signal.domain.profile.implement.Gender
import com.yourssu.signal.domain.profile.implement.Profile
import com.yourssu.signal.domain.profile.implement.ProfileReader
import com.yourssu.signal.domain.viewer.implement.AdminAccessChecker
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.*

class BlacklistServiceTest : DescribeSpec({
    val blacklistWriter = mock<BlacklistWriter>()
    val blacklistReader = mock<BlacklistReader>()
    val adminAccessChecker = mock<AdminAccessChecker>()
    val profileReader = mock<ProfileReader>()
    val service = BlacklistService(blacklistWriter, blacklistReader, adminAccessChecker, profileReader)

    fun profile(id: Long, uuid: String, contact: String = "@same") = Profile(
        id = id,
        uuid = Uuid(uuid),
        gender = Gender.MALE,
        department = "컴퓨터학부",
        birthYear = 2000,
        animal = Animal.DOG,
        contact = contact,
        mbti = "ENFP",
        nickname = "닉네임$id",
        introSentences = emptyList(),
        school = "숭실대학교",
    )

    beforeEach { reset(blacklistWriter, blacklistReader, adminAccessChecker, profileReader) }

    describe("내 비공개 해제") {
        it("해제하면 같은 연락처의 다른 공개 프로필을 개인 비공개로 숨긴다") {
            val me = profile(1L, "me")
            whenever(profileReader.getByUuid(Uuid("me"))).thenReturn(me)
            whenever(blacklistReader.existsByProfileId(1L)).thenReturn(true)
            whenever(blacklistReader.isAddedByAdmin(1L)).thenReturn(false)
            whenever(profileReader.findByContact("@same")).thenReturn(listOf(me, profile(2L, "orphan"), profile(3L, "admin-banned")))
            whenever(blacklistReader.existsByProfileId(2L)).thenReturn(false)
            whenever(blacklistReader.existsByProfileId(3L)).thenReturn(true)

            service.removeMyBlacklist("me")

            verify(blacklistWriter).deleteByProfileId(1L)
            verify(blacklistWriter).save(check {
                it.profileId shouldBe 2L
                it.createdByAdmin shouldBe false
            })
            verify(blacklistWriter, times(1)).save(any())
        }

        it("관리자 블랙리스트는 해제할 수 없다") {
            whenever(profileReader.getByUuid(Uuid("me"))).thenReturn(profile(1L, "me"))
            whenever(blacklistReader.existsByProfileId(1L)).thenReturn(true)
            whenever(blacklistReader.isAddedByAdmin(1L)).thenReturn(true)

            shouldThrow<AdminBlacklistCannotBeRemovedException> { service.removeMyBlacklist("me") }
            verify(profileReader, never()).findByContact(any())
        }

        it("비공개가 아니면 아무것도 하지 않는다") {
            whenever(profileReader.getByUuid(Uuid("me"))).thenReturn(profile(1L, "me"))
            whenever(blacklistReader.existsByProfileId(1L)).thenReturn(false)

            service.removeMyBlacklist("me")

            verify(blacklistWriter, never()).deleteByProfileId(any())
            verify(profileReader, never()).findByContact(any())
        }
    }
})
