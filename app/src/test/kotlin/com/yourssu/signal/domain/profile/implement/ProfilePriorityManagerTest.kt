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

        context("내가 구매한 프로필 ID가 전달되면") {
            it("해당 프로필은 덱에 포함되지 않는다") {
                whenever(profileReader.findIdsByGender(Gender.FEMALE)).thenReturn(listOf(1L, 2L, 3L, 4L))
                whenever(purchasedProfileReader.findProfileIdsOrderByPurchasedAsc()).thenReturn(listOf(3L, 4L))
                whenever(blacklistReader.getAllBlacklistIds()).thenReturn(emptySet())

                val deck = manager.buildDeck(myProfileId = null, gender = Gender.FEMALE, myPurchasedProfileIds = setOf(3L))

                deck shouldNotContain 3L
                deck shouldContainAll listOf(1L, 2L, 4L)
            }
        }

        context("내가 해당 성별의 모든 프로필을 구매했으면") {
            it("빈 덱을 반환한다") {
                whenever(profileReader.findIdsByGender(Gender.FEMALE)).thenReturn(listOf(1L, 2L))
                whenever(purchasedProfileReader.findProfileIdsOrderByPurchasedAsc()).thenReturn(emptyList())
                whenever(blacklistReader.getAllBlacklistIds()).thenReturn(emptySet())

                val deck = manager.buildDeck(myProfileId = null, gender = Gender.FEMALE, myPurchasedProfileIds = setOf(1L, 2L))

                deck shouldBe emptyList()
            }
        }

        context("내가 프로필 1만 구매했고 프로필이 1, 2 있으면") {
            it("프로필 2만 덱에 반환된다") {
                whenever(profileReader.findIdsByGender(Gender.FEMALE)).thenReturn(listOf(1L, 2L))
                whenever(purchasedProfileReader.findProfileIdsOrderByPurchasedAsc()).thenReturn(emptyList())
                whenever(blacklistReader.getAllBlacklistIds()).thenReturn(emptySet())

                val deck = manager.buildDeck(myProfileId = null, gender = Gender.FEMALE, myPurchasedProfileIds = setOf(1L))

                deck shouldBe listOf(2L)
            }
        }

        context("profiles/random과 동일하게 전역 구매순 정렬이 유지되면서 내 구매 프로필만 제외되면") {
            it("미구매(전역) 프로필이 앞에, 전역 구매 프로필이 뒤에 오고 내가 구매한 프로필은 없다") {
                // 전체 프로필: 1,2,3,4,5 / 전역 구매된 것: 4,5 / 내가 구매한 것: 3
                whenever(profileReader.findIdsByGender(Gender.MALE)).thenReturn(listOf(1L, 2L, 3L, 4L, 5L))
                whenever(purchasedProfileReader.findProfileIdsOrderByPurchasedAsc()).thenReturn(listOf(4L, 5L))
                whenever(blacklistReader.getAllBlacklistIds()).thenReturn(emptySet())

                val deck = manager.buildDeck(myProfileId = null, gender = Gender.MALE, myPurchasedProfileIds = setOf(3L))

                // 3은 제외
                deck shouldNotContain 3L
                // 전역 미구매(1,2)가 전역 구매(4,5)보다 앞에
                val globalPurchasedInDeck = setOf(4L, 5L)
                val unpurchasedPart = deck.takeWhile { it !in globalPurchasedInDeck }
                val purchasedPart = deck.dropWhile { it !in globalPurchasedInDeck }
                unpurchasedPart.toSet() shouldBe setOf(1L, 2L)
                purchasedPart shouldBe listOf(4L, 5L)
            }
        }

        context("여자 프로필 100개가 있고 구매 이력이 없으면") {
            it("중복 없이 정확히 100개를 반환한다") {
                val allIds = (1L..100L).toList()
                whenever(profileReader.findIdsByGender(Gender.FEMALE)).thenReturn(allIds)
                whenever(purchasedProfileReader.findProfileIdsOrderByPurchasedAsc()).thenReturn(emptyList())
                whenever(blacklistReader.getAllBlacklistIds()).thenReturn(emptySet())

                val deck = manager.buildDeck(myProfileId = null, gender = Gender.FEMALE)

                deck.size shouldBe 100
                deck.toSet().size shouldBe 100  // 중복 없음
                deck.toSet() shouldBe allIds.toSet()  // 전체 포함
            }
        }
    }
})
