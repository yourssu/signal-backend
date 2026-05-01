package com.yourssu.signal.domain.profile.implement

import com.yourssu.signal.domain.blacklist.implement.BlacklistReader
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.*

class ProfilePriorityManagerTest : DescribeSpec({

    val profileReader = mock<ProfileReader>()
    val purchasedProfileReader = mock<PurchasedProfileReader>()
    val blacklistReader = mock<BlacklistReader>()

    val manager = ProfilePriorityManager(
        profileReader = profileReader,
        purchasedProfileReader = purchasedProfileReader,
        blacklistReader = blacklistReader,
    )

    beforeEach {
        reset(profileReader, purchasedProfileReader, blacklistReader)
    }

    describe("buildDeck") {

        context("미구매 프로필과 구매된 프로필이 모두 있으면") {
            it("미구매 프로필이 앞에, 구매된 프로필이 뒤에 위치한다") {
                whenever(profileReader.findIdsByGender(Gender.FEMALE)).thenReturn(listOf(1L, 2L, 3L, 4L))
                whenever(purchasedProfileReader.findProfileIdsOrderByPurchasedAsc()).thenReturn(listOf(3L, 4L))
                whenever(blacklistReader.getAllBlacklistIds()).thenReturn(emptySet())

                val deck = manager.buildDeck(myProfileId = null, gender = Gender.FEMALE)

                val purchasedIds = setOf(3L, 4L)
                val unpurchasedIds = setOf(1L, 2L)
                val unpurchasedInDeck = deck.takeWhile { it !in purchasedIds }
                val purchasedInDeck = deck.dropWhile { it !in purchasedIds }

                deck shouldContainAll listOf(1L, 2L, 3L, 4L)
                unpurchasedInDeck.toSet() shouldBe unpurchasedIds
                purchasedInDeck shouldBe listOf(3L, 4L)
            }
        }

        context("본인 프로필 ID가 있으면") {
            it("본인 프로필은 덱에 포함되지 않는다") {
                whenever(profileReader.findIdsByGender(Gender.MALE)).thenReturn(listOf(1L, 2L, 3L))
                whenever(purchasedProfileReader.findProfileIdsOrderByPurchasedAsc()).thenReturn(emptyList())
                whenever(blacklistReader.getAllBlacklistIds()).thenReturn(emptySet())

                val deck = manager.buildDeck(myProfileId = 2L, gender = Gender.MALE)

                deck shouldNotContain 2L
                deck shouldContainAll listOf(1L, 3L)
            }
        }

        context("블랙리스트 프로필이 있으면") {
            it("블랙리스트 프로필은 덱에 포함되지 않는다") {
                whenever(profileReader.findIdsByGender(Gender.FEMALE)).thenReturn(listOf(1L, 2L, 3L))
                whenever(purchasedProfileReader.findProfileIdsOrderByPurchasedAsc()).thenReturn(emptyList())
                whenever(blacklistReader.getAllBlacklistIds()).thenReturn(setOf(2L))

                val deck = manager.buildDeck(myProfileId = null, gender = Gender.FEMALE)

                deck shouldNotContain 2L
                deck shouldContainAll listOf(1L, 3L)
            }
        }

        context("블랙리스트이면서 구매된 프로필이 있으면") {
            it("해당 프로필은 덱에 포함되지 않는다") {
                whenever(profileReader.findIdsByGender(Gender.MALE)).thenReturn(listOf(1L, 2L, 3L))
                whenever(purchasedProfileReader.findProfileIdsOrderByPurchasedAsc()).thenReturn(listOf(2L))
                whenever(blacklistReader.getAllBlacklistIds()).thenReturn(setOf(2L))

                val deck = manager.buildDeck(myProfileId = null, gender = Gender.MALE)

                deck shouldNotContain 2L
                deck shouldContainAll listOf(1L, 3L)
            }
        }

        context("성별에 해당하는 프로필이 없으면") {
            it("빈 덱을 반환한다") {
                whenever(profileReader.findIdsByGender(Gender.FEMALE)).thenReturn(emptyList())
                whenever(purchasedProfileReader.findProfileIdsOrderByPurchasedAsc()).thenReturn(emptyList())
                whenever(blacklistReader.getAllBlacklistIds()).thenReturn(emptySet())

                val deck = manager.buildDeck(myProfileId = null, gender = Gender.FEMALE)

                deck shouldBe emptyList()
            }
        }

        context("구매된 프로필만 있으면") {
            it("구매된 프로필만 덱에 포함된다") {
                whenever(profileReader.findIdsByGender(Gender.MALE)).thenReturn(listOf(1L, 2L))
                whenever(purchasedProfileReader.findProfileIdsOrderByPurchasedAsc()).thenReturn(listOf(2L, 1L))
                whenever(blacklistReader.getAllBlacklistIds()).thenReturn(emptySet())

                val deck = manager.buildDeck(myProfileId = null, gender = Gender.MALE)

                deck shouldBe listOf(2L, 1L)
            }
        }
    }
})
