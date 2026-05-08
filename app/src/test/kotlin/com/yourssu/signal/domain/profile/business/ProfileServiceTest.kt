package com.yourssu.signal.domain.profile.business

import com.yourssu.signal.config.properties.PolicyConfigurationProperties
import com.yourssu.signal.domain.blacklist.implement.BlacklistWriter
import com.yourssu.signal.domain.common.implement.Uuid
import com.yourssu.signal.domain.profile.business.command.*
import com.yourssu.signal.domain.profile.business.dto.DeckResponse
import com.yourssu.signal.domain.profile.business.ProfilesCountResponse
import com.yourssu.signal.domain.profile.implement.*
import com.yourssu.signal.domain.profile.implement.EgenTeto
import com.yourssu.signal.domain.profile.implement.exception.BannedWordException
import com.yourssu.signal.domain.profile.implement.exception.ContactLimitExceededException
import com.yourssu.signal.domain.user.implement.User
import com.yourssu.signal.domain.user.implement.UserReader
import com.yourssu.signal.domain.viewer.implement.AdminAccessChecker
import com.yourssu.signal.domain.viewer.implement.Viewer
import com.yourssu.signal.domain.viewer.implement.ViewerReader
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.mockito.kotlin.*
import java.time.ZonedDateTime

class ProfileServiceTest : DescribeSpec({
    
    describe("ProfileService") {
        
        // Data - 테스트 데이터와 Mock 객체들
        val profileWriter = mock<ProfileWriter>()
        val profileReader = mock<ProfileReader>()
        val userReader = mock<UserReader>()
        val viewerReader = mock<ViewerReader>()
        val usedTicketManager = mock<UsedTicketManager>()
        val profilePriorityManager = mock<ProfilePriorityManager>()
        val purchasedProfileReader = mock<PurchasedProfileReader>()
        val policy = mock<PolicyConfigurationProperties>()
        val adminAccessChecker = mock<AdminAccessChecker>()
        val blacklistWriter = mock<BlacklistWriter>()
        
        val profileService = ProfileService(
            profileWriter = profileWriter,
            profileReader = profileReader,
            userReader = userReader,
            viewerReader = viewerReader,
            usedTicketManager = usedTicketManager,
            profilePriorityManager = profilePriorityManager,
            purchasedProfileReader = purchasedProfileReader,
            policy = policy,
            adminAccessChecker = adminAccessChecker,
            blacklistWriter = blacklistWriter
        )
        
        fun createTestUser(uuid: String = "test-uuid") = User(
            id = 1L,
            uuid = Uuid(uuid)
        )
        
        fun createTestProfile(
            id: Long? = 1L,
            uuid: String = "test-uuid",
            nickname: String = "테스트닉네임",
            contact: String = "@test_contact",
            introSentences: List<String> = listOf("안녕하세요"),
            gender: Gender = Gender.MALE,
            egenTeto: EgenTeto? = null
        ) = Profile(
            id = id,
            uuid = Uuid(uuid),
            gender = gender,
            department = "컴퓨터학부",
            birthYear = 2000,
            animal = Animal.DOG,
            contact = contact,
            mbti = "ENFP",
            nickname = nickname,
            introSentences = introSentences,
            school = "숭실대학교",
            egenTeto = egenTeto
        )
        
        fun createTestViewer(
            uuid: String = "viewer-uuid",
            usedTicket: Int = 0
        ) = Viewer(
            id = 1L,
            uuid = Uuid(uuid),
            ticket = 10,
            usedTicket = usedTicket,
            updatedTime = ZonedDateTime.now()
        )
        
        beforeEach {
            reset(profileWriter, profileReader, userReader, viewerReader, usedTicketManager,
                  profilePriorityManager, purchasedProfileReader, policy, adminAccessChecker, blacklistWriter)
            whenever(policy.bannedWords).thenReturn(emptyList())
        }
        
        context("createProfile 메서드를 호출할 때") {
            
            context("유효한 프로필 생성 명령을 받으면") {
                it("프로필을 성공적으로 생성한다") {
                    // given
                    val command = ProfileCreatedCommand(
                        uuid = "test-uuid",
                        gender = "MALE",
                        department = "컴퓨터학부",
                        birthYear = 2000,
                        animal = "DOG",
                        contact = "@new_contact",
                        mbti = "ENFP",
                        nickname = "새닉네임",
                        introSentences = listOf("안녕하세요"),
                        school = "숭실대학교"
                    )
                    
                    val user = createTestUser("test-uuid")
                    val createdProfile = createTestProfile(
                        uuid = "test-uuid",
                        nickname = "새닉네임",
                        contact = "@new_contact"
                    )
                    
                    whenever(userReader.getByUuid(Uuid("test-uuid"))).thenReturn(user)
                    whenever(profileReader.countContact("@new_contact")).thenReturn(2)
                    whenever(policy.contactLimit).thenReturn(5)
                    whenever(policy.contactLimitWarning).thenReturn(3)
                    whenever(profileWriter.createProfile(any())).thenReturn(createdProfile)
                    whenever(policy.whitelist).thenReturn(false)
                    
                    // when
                    val result = profileService.createProfile(command)
                    
                    // then
                    result.uuid shouldBe "test-uuid"
                    result.nickname shouldBe "새닉네임"
                    result.contact shouldBe "@new_contact"
                    verify(userReader).getByUuid(Uuid("test-uuid"))
                    verify(profileReader).countContact("@new_contact")
                    verify(profileWriter).createProfile(any())
                }
            }
            
            context("연락처 제한을 초과하면") {
                it("ContactLimitExceededException을 발생시킨다") {
                    // given
                    val command = ProfileCreatedCommand(
                        uuid = "test-uuid",
                        gender = "MALE",
                        department = "컴퓨터학부",
                        birthYear = 2000,
                        animal = "DOG",
                        contact = "@popular_contact",
                        mbti = "ENFP",
                        nickname = "닉네임",
                        introSentences = listOf("안녕하세요"),
                        school = "숭실대학교"
                    )
                    
                    val user = createTestUser("test-uuid")
                    
                    whenever(userReader.getByUuid(Uuid("test-uuid"))).thenReturn(user)
                    whenever(profileReader.countContact("@popular_contact")).thenReturn(10)
                    whenever(policy.contactLimit).thenReturn(5)
                    
                    // when & then
                    shouldThrow<ContactLimitExceededException> {
                        profileService.createProfile(command)
                    }
                }
            }
            
            context("화이트리스트 정책이 활성화되면") {
                it("블랙리스트에 프로필을 추가한다") {
                    // given
                    val command = ProfileCreatedCommand(
                        uuid = "test-uuid",
                        gender = "FEMALE",
                        department = "경영학부",
                        birthYear = 1999,
                        animal = "CAT",
                        contact = "@whitelist_contact",
                        mbti = "ISTJ",
                        nickname = "화이트리스트",
                        introSentences = listOf("화이트리스트 테스트"),
                        school = "숭실대학교"
                    )
                    
                    val user = createTestUser("test-uuid")
                    val createdProfile = createTestProfile(
                        id = 100L,
                        uuid = "test-uuid"
                    )
                    
                    whenever(userReader.getByUuid(any())).thenReturn(user)
                    whenever(profileReader.countContact(any())).thenReturn(1)
                    whenever(policy.contactLimit).thenReturn(10)
                    whenever(policy.contactLimitWarning).thenReturn(5)
                    whenever(profileWriter.createProfile(any())).thenReturn(createdProfile)
                    whenever(policy.whitelist).thenReturn(true)
                    whenever(blacklistWriter.save(any())).thenReturn(mock())
                    
                    // when
                    profileService.createProfile(command)
                    
                    // then
                    verify(blacklistWriter).save(any())
                }
            }
        }
        
        context("getProfile 메서드를 호출할 때") {
            
            context("존재하는 UUID로 조회하면") {
                it("해당 프로필을 반환한다") {
                    // given
                    val uuid = "existing-uuid"
                    val profile = createTestProfile(uuid = uuid, nickname = "조회테스트")
                    
                    whenever(profileReader.getByUuid(Uuid(uuid))).thenReturn(profile)
                    
                    // when
                    val result = profileService.getProfile(uuid)
                    
                    // then
                    result.uuid shouldBe uuid
                    result.nickname shouldBe "조회테스트"
                    verify(profileReader).getByUuid(Uuid(uuid))
                }
            }
        }
        
        context("updateProfile 메서드를 호출할 때") {
            
            context("유효한 업데이트 명령을 받으면") {
                it("프로필을 성공적으로 업데이트한다") {
                    // given
                    val command = ProfileUpdateCommand(
                        uuid = "update-uuid",
                        nickname = "업데이트닉네임",
                        introSentences = listOf("업데이트된 소개"),
                        contact = "@updated_contact"
                    )
                    
                    val existingProfile = createTestProfile(
                        uuid = "update-uuid",
                        nickname = "기존닉네임",
                        contact = "@old_contact"
                    )
                    
                    val updatedProfile = existingProfile.copy(
                        nickname = "업데이트닉네임",
                        introSentences = listOf("업데이트된 소개"),
                        contact = "@updated_contact"
                    )
                    
                    whenever(profileReader.getByUuid(Uuid("update-uuid"))).thenReturn(existingProfile)
                    whenever(profileReader.countContact("@updated_contact")).thenReturn(2)
                    whenever(policy.contactLimit).thenReturn(5)
                    whenever(profileWriter.updateProfile(any())).thenReturn(updatedProfile)
                    
                    // when
                    val result = profileService.updateProfile(command)
                    
                    // then
                    result.nickname shouldBe "업데이트닉네임"
                    result.contact shouldBe "@updated_contact"
                    verify(profileReader).getByUuid(Uuid("update-uuid"))
                    verify(profileWriter).updateProfile(any())
                }
            }
            
            context("연락처 제한을 초과하는 업데이트를 시도하면") {
                it("ContactLimitExceededException을 발생시킨다") {
                    // given
                    val command = ProfileUpdateCommand(
                        uuid = "update-uuid",
                        nickname = "닉네임",
                        introSentences = listOf("소개"),
                        contact = "@over_limit_contact"
                    )
                    
                    val existingProfile = createTestProfile(uuid = "update-uuid")
                    
                    whenever(profileReader.getByUuid(Uuid("update-uuid"))).thenReturn(existingProfile)
                    whenever(profileReader.countContact("@over_limit_contact")).thenReturn(10)
                    whenever(policy.contactLimit).thenReturn(5)
                    
                    // when & then
                    shouldThrow<ContactLimitExceededException> {
                        profileService.updateProfile(command)
                    }
                }
            }
        }
        
        context("getRandomProfile 메서드를 호출할 때") {
            
            context("내 프로필이 존재하는 상태에서 랜덤 프로필을 조회하면") {
                it("내 프로필을 제외한 랜덤 프로필을 반환한다") {
                    // given
                    val command = RandomProfileFoundCommand(
                        uuid = "my-uuid",
                        excludeProfiles = emptyList(),
                        gender = "FEMALE"
                    )
                    
                    val myProfile = createTestProfile(
                        uuid = "my-uuid",
                        nickname = "내프로필"
                    )
                    
                    val randomProfile = createTestProfile(
                        id = 2L,
                        uuid = "random-uuid",
                        nickname = "랜덤프로필"
                    )
                    
                    whenever(profileReader.existsByUuid(Uuid("my-uuid"))).thenReturn(true)
                    whenever(profileReader.getByUuid(Uuid("my-uuid"))).thenReturn(myProfile)
                    whenever(profilePriorityManager.pickRandomProfile(any(), any())).thenReturn(randomProfile)
                    
                    // when
                    val result = profileService.getRandomProfile(command)
                    
                    // then
                    result.profileId shouldBe 2L
                    result.nickname shouldBe "랜덤프로필"
                    verify(profileReader).existsByUuid(Uuid("my-uuid"))
                    verify(profilePriorityManager).pickRandomProfile(any(), any())
                }
            }
            
            context("내 프로필이 존재하지 않는 상태에서 랜덤 프로필을 조회하면") {
                it("모든 프로필 중에서 랜덤 프로필을 반환한다") {
                    // given
                    val command = RandomProfileFoundCommand(
                        uuid = "non-existing-uuid",
                        excludeProfiles = emptyList(),
                        gender = "MALE"
                    )
                    
                    val randomProfile = createTestProfile(
                        id = 3L,
                        nickname = "전체랜덤프로필"
                    )
                    
                    whenever(profileReader.existsByUuid(Uuid("non-existing-uuid"))).thenReturn(false)
                    whenever(profilePriorityManager.pickRandomProfile(any(), any())).thenReturn(randomProfile)
                    
                    // when
                    val result = profileService.getRandomProfile(command)
                    
                    // then
                    result.profileId shouldBe 3L
                    result.nickname shouldBe "전체랜덤프로필"
                    verify(profileReader).existsByUuid(Uuid("non-existing-uuid"))
                    verify(profilePriorityManager).pickRandomProfile(any(), any())
                }
            }
        }
        
        context("consumeTicket 메서드를 호출할 때") {
            
            context("유효한 티켓 소모 명령을 받으면") {
                it("티켓을 소모하고 프로필 연락처를 반환한다") {
                    // given
                    val command = TicketConsumedCommand(
                        uuid = "viewer-uuid",
                        profileId = 1L
                    )
                    
                    val viewer = createTestViewer("viewer-uuid", 5)
                    val targetProfile = createTestProfile(id = 1L, nickname = "대상프로필")
                    val updatedViewer = viewer
                    
                    whenever(viewerReader.get(Uuid("viewer-uuid"))).thenReturn(viewer)
                    whenever(profileReader.getById(1L)).thenReturn(targetProfile)
                    whenever(policy.contactPrice).thenReturn(2)
                    whenever(usedTicketManager.consumeTicket(viewer, targetProfile, 2)).thenReturn(updatedViewer)
                    
                    // when
                    val result = profileService.consumeTicket(command)
                    
                    // then
                    result.contact shouldBe "@test_contact"
                    verify(viewerReader).get(Uuid("viewer-uuid"))
                    verify(profileReader).getById(1L)
                    verify(usedTicketManager).consumeTicket(viewer, targetProfile, 2)
                }
            }
        }
        
        context("getAllProfiles 메서드를 호출할 때") {
            
            context("유효한 관리자 시크릿 키로 조회하면") {
                it("모든 프로필 목록을 반환한다") {
                    // given
                    val command = AllProfilesFoundCommand(secretKey = "valid-secret")
                    val profiles = listOf(
                        createTestProfile(id = 1L, nickname = "프로필1"),
                        createTestProfile(id = 2L, nickname = "프로필2")
                    )
                    
                    whenever(adminAccessChecker.validateAdminAccess("valid-secret")).then { }
                    whenever(profileReader.getAll()).thenReturn(profiles)
                    
                    // when
                    val result = profileService.getAllProfiles(command)
                    
                    // then
                    result.size shouldBe 2
                    result[0].nickname shouldBe "프로필1"
                    result[1].nickname shouldBe "프로필2"
                    verify(adminAccessChecker).validateAdminAccess("valid-secret")
                    verify(profileReader).getAll()
                }
            }
        }
        
        context("countAllProfiles 메서드를 호출할 때") {
            
            context("전체 프로필 수를 조회하면") {
                it("전체 프로필 수를 반환한다") {
                    // given
                    whenever(profileReader.countAll()).thenReturn(42)
                    
                    // when
                    val result = profileService.countAllProfiles()
                    
                    // then
                    result.count shouldBe 42
                    verify(profileReader).countAll()
                }
            }
        }
        
        context("countByGender 메서드를 호출할 때") {
            
            context("특정 성별의 프로필 수를 조회하면") {
                it("해당 성별의 프로필 수를 반환한다") {
                    // given
                    whenever(profileReader.count(Gender.FEMALE)).thenReturn(15)
                    
                    // when
                    val result = profileService.countByGender("FEMALE")
                    
                    // then
                    result.count shouldBe 15
                    verify(profileReader).count(Gender.FEMALE)
                }
            }
        }
        
        context("getProfile (by command) 메서드를 호출할 때") {
            
            context("구매한 프로필을 조회하면") {
                it("프로필 연락처를 반환한다") {
                    // given
                    val command = ProfileFoundCommand(
                        uuid = "viewer-uuid",
                        profileId = 1L
                    )
                    
                    val viewer = createTestViewer("viewer-uuid")
                    val targetProfile = createTestProfile(id = 1L)
                    
                    whenever(viewerReader.get(Uuid("viewer-uuid"))).thenReturn(viewer)
                    whenever(profileReader.getById(1L)).thenReturn(targetProfile)
                    whenever(usedTicketManager.validatePurchasedProfile(viewer, targetProfile)).then { }
                    
                    // when
                    val result = profileService.getProfile(command)
                    
                    // then
                    result.contact shouldBe "@test_contact"
                    verify(viewerReader).get(Uuid("viewer-uuid"))
                    verify(profileReader).getById(1L)
                    verify(usedTicketManager).validatePurchasedProfile(viewer, targetProfile)
                }
            }
        }
        
        context("getProfileRanking 메서드를 호출할 때") {

            context("프로필 랭킹을 조회하면") {
                it("프로필의 랭킹 정보를 반환한다") {
                    // given
                    val uuid = "ranking-uuid"
                    val profile = createTestProfile(
                        id = 10L,
                        uuid = uuid,
                        nickname = "랭킹프로필"
                    )

                    whenever(profileReader.getByUuid(Uuid(uuid))).thenReturn(profile)
                    whenever(purchasedProfileReader.getProfileRanking(10L, Gender.MALE)).thenReturn(mock())
                    whenever(profileReader.count(Gender.MALE)).thenReturn(50)

                    // when
                    val result = profileService.getProfileRanking(uuid)

                    // then
                    result shouldNotBe null
                    verify(profileReader).getByUuid(Uuid(uuid))
                    verify(purchasedProfileReader).getProfileRanking(10L, Gender.MALE)
                    verify(profileReader).count(Gender.MALE)
                }
            }
        }

        context("getDeck 메서드를 호출할 때") {

            context("프로필이 있는 유저가 덱을 요청하면") {
                it("본인 프로필 ID를 제외하고 buildDeck을 호출한다") {
                    val command = DeckCommand(uuid = "my-uuid", gender = "FEMALE")
                    val myProfile = createTestProfile(id = 1L, uuid = "my-uuid")
                    val deckProfiles = listOf(
                        createTestProfile(id = 2L, uuid = "uuid-2", nickname = "프로필2"),
                        createTestProfile(id = 3L, uuid = "uuid-3", nickname = "프로필3"),
                    )

                    whenever(profileReader.existsByUuid(Uuid("my-uuid"))).thenReturn(true)
                    whenever(profileReader.getByUuid(Uuid("my-uuid"))).thenReturn(myProfile)
                    whenever(profilePriorityManager.buildDeck(1L, Gender.FEMALE)).thenReturn(listOf(2L, 3L))
                    whenever(profileReader.getOrderedByIds(listOf(2L, 3L))).thenReturn(deckProfiles)

                    val result = profileService.getDeck(command)

                    result.profiles.size shouldBe 2
                    result.profiles[0].nickname shouldBe "프로필2"
                    result.profiles[1].nickname shouldBe "프로필3"
                    verify(profilePriorityManager).buildDeck(1L, Gender.FEMALE)
                }
            }

            context("프로필이 없는 유저가 덱을 요청하면") {
                it("myProfileId null로 buildDeck을 호출한다") {
                    val command = DeckCommand(uuid = "no-profile-uuid", gender = "MALE")
                    val deckProfiles = listOf(
                        createTestProfile(id = 5L, uuid = "uuid-5", nickname = "프로필5"),
                    )

                    whenever(profileReader.existsByUuid(Uuid("no-profile-uuid"))).thenReturn(false)
                    whenever(profilePriorityManager.buildDeck(null, Gender.MALE)).thenReturn(listOf(5L))
                    whenever(profileReader.getOrderedByIds(listOf(5L))).thenReturn(deckProfiles)

                    val result = profileService.getDeck(command)

                    result.profiles.size shouldBe 1
                    result.profiles[0].nickname shouldBe "프로필5"
                    verify(profilePriorityManager).buildDeck(null, Gender.MALE)
                    verify(profileReader, never()).getByUuid(any())
                }
            }

            context("덱이 비어있으면") {
                it("빈 profiles 목록을 반환한다") {
                    val command = DeckCommand(uuid = "my-uuid", gender = "FEMALE")
                    val myProfile = createTestProfile(id = 1L, uuid = "my-uuid")

                    whenever(profileReader.existsByUuid(Uuid("my-uuid"))).thenReturn(true)
                    whenever(profileReader.getByUuid(Uuid("my-uuid"))).thenReturn(myProfile)
                    whenever(profilePriorityManager.buildDeck(1L, Gender.FEMALE)).thenReturn(emptyList())
                    whenever(profileReader.getOrderedByIds(emptyList())).thenReturn(emptyList())

                    val result = profileService.getDeck(command)

                    result.profiles shouldBe emptyList()
                }
            }

            context("이미 구매한 프로필이 있는 유저가 덱을 요청하면") {
                it("구매한 프로필을 제외하고 buildDeck을 호출한다") {
                    val command = DeckCommand(uuid = "buyer-uuid", gender = "FEMALE")
                    val myProfile = createTestProfile(id = 1L, uuid = "buyer-uuid")
                    val viewer = createTestViewer("buyer-uuid")
                    val deckProfiles = listOf(
                        createTestProfile(id = 3L, uuid = "uuid-3", nickname = "프로필3"),
                    )

                    whenever(profileReader.existsByUuid(Uuid("buyer-uuid"))).thenReturn(true)
                    whenever(profileReader.getByUuid(Uuid("buyer-uuid"))).thenReturn(myProfile)
                    whenever(viewerReader.existsByUuid(Uuid("buyer-uuid"))).thenReturn(true)
                    whenever(viewerReader.get(Uuid("buyer-uuid"))).thenReturn(viewer)
                    whenever(purchasedProfileReader.findProfileIdsByViewerId(1L)).thenReturn(setOf(2L))
                    whenever(profilePriorityManager.buildDeck(eq(1L), eq(Gender.FEMALE), eq(setOf(2L)))).thenReturn(listOf(3L))
                    whenever(profileReader.getOrderedByIds(listOf(3L))).thenReturn(deckProfiles)

                    val result = profileService.getDeck(command)

                    result.profiles.size shouldBe 1
                    result.profiles[0].nickname shouldBe "프로필3"
                    verify(purchasedProfileReader).findProfileIdsByViewerId(1L)
                    verify(profilePriorityManager).buildDeck(eq(1L), eq(Gender.FEMALE), eq(setOf(2L)))
                }
            }
        }

        context("금지어 검사") {

            context("프로필 생성 시 닉네임에 금지어가 포함되면") {
                it("BannedWordException을 던진다") {
                    val command = ProfileCreatedCommand(
                        uuid = "test-uuid",
                        gender = "MALE",
                        department = "컴퓨터학부",
                        birthYear = 2000,
                        animal = "DOG",
                        contact = "@test_contact",
                        mbti = "ENFP",
                        nickname = "나쁜말닉네임",
                        introSentences = listOf("안녕하세요"),
                        school = "숭실대학교"
                    )
                    whenever(userReader.getByUuid(any())).thenReturn(createTestUser())
                    whenever(policy.bannedWords).thenReturn(listOf("나쁜말"))

                    shouldThrow<BannedWordException> {
                        profileService.createProfile(command)
                    }
                }
            }

            context("프로필 생성 시 소개글에 금지어가 포함되면") {
                it("BannedWordException을 던진다") {
                    val command = ProfileCreatedCommand(
                        uuid = "test-uuid",
                        gender = "MALE",
                        department = "컴퓨터학부",
                        birthYear = 2000,
                        animal = "DOG",
                        contact = "@test_contact",
                        mbti = "ENFP",
                        nickname = "정상닉네임",
                        introSentences = listOf("badword 포함 소개"),
                        school = "숭실대학교"
                    )
                    whenever(userReader.getByUuid(any())).thenReturn(createTestUser())
                    whenever(policy.bannedWords).thenReturn(listOf("badword"))

                    shouldThrow<BannedWordException> {
                        profileService.createProfile(command)
                    }
                }
            }

            context("프로필 수정 시 닉네임에 금지어가 포함되면") {
                it("BannedWordException을 던진다") {
                    val command = ProfileUpdateCommand(
                        uuid = "test-uuid",
                        nickname = "나쁜말닉네임",
                        introSentences = listOf("안녕하세요"),
                        contact = "@test_contact"
                    )
                    whenever(policy.bannedWords).thenReturn(listOf("나쁜말"))

                    shouldThrow<BannedWordException> {
                        profileService.updateProfile(command)
                    }
                }
            }

            context("프로필 수정 시 소개글에 금지어가 포함되면") {
                it("BannedWordException을 던진다") {
                    val command = ProfileUpdateCommand(
                        uuid = "test-uuid",
                        nickname = "정상닉네임",
                        introSentences = listOf("안녕", "badword 섞인 소개"),
                        contact = "@test_contact"
                    )
                    whenever(policy.bannedWords).thenReturn(listOf("badword"))

                    shouldThrow<BannedWordException> {
                        profileService.updateProfile(command)
                    }
                }
            }
        }

        context("egenTeto 필드 동작") {

            context("프로필 생성 시 egenTeto 없이 요청하면") {
                it("응답의 egenTeto가 null이다") {
                    val command = ProfileCreatedCommand(
                        uuid = "test-uuid",
                        gender = "MALE",
                        department = "컴퓨터학부",
                        birthYear = 2000,
                        animal = "DOG",
                        contact = "@test_contact",
                        mbti = "ENFP",
                        nickname = "닉네임",
                        introSentences = listOf("안녕하세요"),
                        school = "숭실대학교",
                        egenTeto = null
                    )
                    val createdProfile = createTestProfile(egenTeto = null)

                    whenever(userReader.getByUuid(any())).thenReturn(createTestUser())
                    whenever(profileReader.countContact(any())).thenReturn(0)
                    whenever(policy.contactLimit).thenReturn(5)
                    whenever(policy.contactLimitWarning).thenReturn(3)
                    whenever(profileWriter.createProfile(any())).thenReturn(createdProfile)
                    whenever(policy.whitelist).thenReturn(false)

                    val result = profileService.createProfile(command)

                    result.egenTeto shouldBe null
                }
            }

            context("프로필 생성 시 egenTeto: EGEN으로 요청하면") {
                it("응답에 EGEN이 반환된다") {
                    val command = ProfileCreatedCommand(
                        uuid = "test-uuid",
                        gender = "MALE",
                        department = "컴퓨터학부",
                        birthYear = 2000,
                        animal = "DOG",
                        contact = "@test_contact",
                        mbti = "ENFP",
                        nickname = "닉네임",
                        introSentences = listOf("안녕하세요"),
                        school = "숭실대학교",
                        egenTeto = "EGEN"
                    )
                    val createdProfile = createTestProfile(egenTeto = EgenTeto.EGEN)

                    whenever(userReader.getByUuid(any())).thenReturn(createTestUser())
                    whenever(profileReader.countContact(any())).thenReturn(0)
                    whenever(policy.contactLimit).thenReturn(5)
                    whenever(policy.contactLimitWarning).thenReturn(3)
                    whenever(profileWriter.createProfile(any())).thenReturn(createdProfile)
                    whenever(policy.whitelist).thenReturn(false)

                    val result = profileService.createProfile(command)

                    result.egenTeto shouldBe "EGEN"
                }
            }

            context("프로필 수정 시 egenTeto: TETO로 요청하면") {
                it("변경된 TETO가 반영된다") {
                    val command = ProfileUpdateCommand(
                        uuid = "test-uuid",
                        nickname = "닉네임",
                        introSentences = listOf("안녕하세요"),
                        contact = "@test_contact",
                        egenTeto = "TETO"
                    )
                    val existingProfile = createTestProfile(egenTeto = null)
                    val updatedProfile = createTestProfile(egenTeto = EgenTeto.TETO)

                    whenever(profileReader.getByUuid(any())).thenReturn(existingProfile)
                    whenever(profileReader.countContact(any())).thenReturn(0)
                    whenever(policy.contactLimit).thenReturn(5)
                    whenever(profileWriter.updateProfile(any())).thenReturn(updatedProfile)

                    val result = profileService.updateProfile(command)

                    result.egenTeto shouldBe "TETO"
                }
            }

            context("egenTeto가 null인 기존 프로필을 조회하면") {
                it("응답의 egenTeto가 null이다") {
                    val profile = createTestProfile(egenTeto = null)

                    whenever(profileReader.getByUuid(any())).thenReturn(profile)

                    val result = profileService.getProfile("test-uuid")

                    result.egenTeto shouldBe null
                }
            }
        }

        context("gender-specific operations") {

            context("createProfile with different genders") {
                it("FEMALE 성별로 프로필을 생성한다") {
                    // given
                    val command = ProfileCreatedCommand(
                        uuid = "female-uuid",
                        gender = "FEMALE",
                        department = "경영학부",
                        birthYear = 1999,
                        animal = "CAT",
                        contact = "@female_contact",
                        mbti = "ISFJ",
                        nickname = "여성닉네임",
                        introSentences = listOf("안녕하세요, 여성입니다"),
                        school = "숭실대학교"
                    )

                    val user = createTestUser("female-uuid")
                    val createdProfile = createTestProfile(
                        uuid = "female-uuid",
                        nickname = "여성닉네임",
                        contact = "@female_contact",
                        gender = Gender.FEMALE
                    )

                    whenever(userReader.getByUuid(Uuid("female-uuid"))).thenReturn(user)
                    whenever(profileReader.countContact("@female_contact")).thenReturn(1)
                    whenever(policy.contactLimit).thenReturn(5)
                    whenever(policy.contactLimitWarning).thenReturn(3)
                    whenever(profileWriter.createProfile(any())).thenReturn(createdProfile)
                    whenever(policy.whitelist).thenReturn(false)

                    // when
                    val result = profileService.createProfile(command)

                    // then
                    result.uuid shouldBe "female-uuid"
                    result.nickname shouldBe "여성닉네임"
                    verify(profileWriter).createProfile(any())
                }

                it("MALE 성별로 프로필을 생성한다") {
                    // given
                    val command = ProfileCreatedCommand(
                        uuid = "male-uuid",
                        gender = "MALE",
                        department = "컴퓨터학부",
                        birthYear = 2001,
                        animal = "DOG",
                        contact = "@male_contact",
                        mbti = "ENTP",
                        nickname = "남성닉네임",
                        introSentences = listOf("안녕하세요, 남성입니다"),
                        school = "숭실대학교"
                    )

                    val user = createTestUser("male-uuid")
                    val createdProfile = createTestProfile(
                        uuid = "male-uuid",
                        nickname = "남성닉네임",
                        contact = "@male_contact",
                        gender = Gender.MALE
                    )

                    whenever(userReader.getByUuid(Uuid("male-uuid"))).thenReturn(user)
                    whenever(profileReader.countContact("@male_contact")).thenReturn(1)
                    whenever(policy.contactLimit).thenReturn(5)
                    whenever(policy.contactLimitWarning).thenReturn(3)
                    whenever(profileWriter.createProfile(any())).thenReturn(createdProfile)
                    whenever(policy.whitelist).thenReturn(false)

                    // when
                    val result = profileService.createProfile(command)

                    // then
                    result.uuid shouldBe "male-uuid"
                    result.nickname shouldBe "남성닉네임"
                    verify(profileWriter).createProfile(any())
                }
            }

            context("getRandomProfile with gender filtering") {
                it("FEMALE 성별만 필터링하여 랜덤 프로필을 조회한다") {
                    // given
                    val command = RandomProfileFoundCommand(
                        uuid = "viewer-uuid",
                        excludeProfiles = emptyList(),
                        gender = "FEMALE"
                    )

                    val myProfile = createTestProfile(
                        uuid = "viewer-uuid",
                        nickname = "내프로필",
                        gender = Gender.MALE
                    )

                    val femaleProfile = createTestProfile(
                        id = 2L,
                        uuid = "female-profile-uuid",
                        nickname = "여성프로필",
                        gender = Gender.FEMALE
                    )

                    whenever(profileReader.existsByUuid(Uuid("viewer-uuid"))).thenReturn(true)
                    whenever(profileReader.getByUuid(Uuid("viewer-uuid"))).thenReturn(myProfile)
                    whenever(profilePriorityManager.pickRandomProfile(any(), any())).thenReturn(femaleProfile)

                    // when
                    val result = profileService.getRandomProfile(command)

                    // then
                    result.profileId shouldBe 2L
                    result.nickname shouldBe "여성프로필"
                    verify(profilePriorityManager).pickRandomProfile(any(), any())
                }

                it("MALE 성별만 필터링하여 랜덤 프로필을 조회한다") {
                    // given
                    val command = RandomProfileFoundCommand(
                        uuid = "viewer-uuid",
                        excludeProfiles = emptyList(),
                        gender = "MALE"
                    )

                    val myProfile = createTestProfile(
                        uuid = "viewer-uuid",
                        nickname = "내프로필",
                        gender = Gender.FEMALE
                    )

                    val maleProfile = createTestProfile(
                        id = 3L,
                        uuid = "male-profile-uuid",
                        nickname = "남성프로필",
                        gender = Gender.MALE
                    )

                    whenever(profileReader.existsByUuid(Uuid("viewer-uuid"))).thenReturn(true)
                    whenever(profileReader.getByUuid(Uuid("viewer-uuid"))).thenReturn(myProfile)
                    whenever(profilePriorityManager.pickRandomProfile(any(), any())).thenReturn(maleProfile)

                    // when
                    val result = profileService.getRandomProfile(command)

                    // then
                    result.profileId shouldBe 3L
                    result.nickname shouldBe "남성프로필"
                    verify(profilePriorityManager).pickRandomProfile(any(), any())
                }
            }

            context("countByGender operations") {
                it("FEMALE 성별 프로필 수를 정확히 조회한다") {
                    // given
                    whenever(profileReader.count(Gender.FEMALE)).thenReturn(25)

                    // when
                    val result = profileService.countByGender("FEMALE")

                    // then
                    result.count shouldBe 25
                    verify(profileReader).count(Gender.FEMALE)
                }

                it("MALE 성별 프로필 수를 정확히 조회한다") {
                    // given
                    whenever(profileReader.count(Gender.MALE)).thenReturn(35)

                    // when
                    val result = profileService.countByGender("MALE")

                    // then
                    result.count shouldBe 35
                    verify(profileReader).count(Gender.MALE)
                }

                it("잘못된 성별 문자열로 조회시 예외를 발생시킨다") {
                    // when & then
                    shouldThrow<Exception> {
                        profileService.countByGender("INVALID_GENDER")
                    }
                }
            }

            context("getProfileRanking with gender consideration") {
                it("FEMALE 프로필의 랭킹을 성별별로 조회한다") {
                    // given
                    val uuid = "female-ranking-uuid"
                    val femaleProfile = createTestProfile(
                        id = 5L,
                        uuid = uuid,
                        nickname = "여성랭킹프로필",
                        gender = Gender.FEMALE
                    )

                    whenever(profileReader.getByUuid(Uuid(uuid))).thenReturn(femaleProfile)
                    whenever(purchasedProfileReader.getProfileRanking(5L, Gender.FEMALE)).thenReturn(mock())
                    whenever(profileReader.count(Gender.FEMALE)).thenReturn(30)

                    // when
                    val result = profileService.getProfileRanking(uuid)

                    // then
                    result shouldNotBe null
                    verify(profileReader).getByUuid(Uuid(uuid))
                    verify(purchasedProfileReader).getProfileRanking(5L, Gender.FEMALE)
                    verify(profileReader).count(Gender.FEMALE)
                }

                it("MALE 프로필의 랭킹을 성별별로 조회한다") {
                    // given
                    val uuid = "male-ranking-uuid"
                    val maleProfile = createTestProfile(
                        id = 6L,
                        uuid = uuid,
                        nickname = "남성랭킹프로필",
                        gender = Gender.MALE
                    )

                    whenever(profileReader.getByUuid(Uuid(uuid))).thenReturn(maleProfile)
                    whenever(purchasedProfileReader.getProfileRanking(6L, Gender.MALE)).thenReturn(mock())
                    whenever(profileReader.count(Gender.MALE)).thenReturn(40)

                    // when
                    val result = profileService.getProfileRanking(uuid)

                    // then
                    result shouldNotBe null
                    verify(profileReader).getByUuid(Uuid(uuid))
                    verify(purchasedProfileReader).getProfileRanking(6L, Gender.MALE)
                    verify(profileReader).count(Gender.MALE)
                }
            }
        }

        describe("countDistinctPurchasedProfiles 메서드를 호출할 때") {

            context("구매된 고유 프로필이 있으면") {
                it("고유 프로필 수를 반환한다") {
                    // given
                    whenever(purchasedProfileReader.countDistinctPurchasedProfiles()).thenReturn(4)

                    // when
                    val result = profileService.countDistinctPurchasedProfiles()

                    // then
                    result.count shouldBe 4
                    verify(purchasedProfileReader).countDistinctPurchasedProfiles()
                }
            }

            context("구매된 프로필이 없으면") {
                it("0을 반환한다") {
                    // given
                    whenever(purchasedProfileReader.countDistinctPurchasedProfiles()).thenReturn(0)

                    // when
                    val result = profileService.countDistinctPurchasedProfiles()

                    // then
                    result.count shouldBe 0
                    verify(purchasedProfileReader).countDistinctPurchasedProfiles()
                }
            }
        }

        describe("getConnectionsCount 메서드를 호출할 때") {

            context("남성/여성 구매 기록이 있으면") {
                it("성별별 카운트와 totalCount를 반환한다") {
                    // given
                    whenever(purchasedProfileReader.countByGender(Gender.MALE)).thenReturn(3)
                    whenever(purchasedProfileReader.countByGender(Gender.FEMALE)).thenReturn(2)

                    // when
                    val result = profileService.getConnectionsCount()

                    // then
                    result.maleCount shouldBe 3
                    result.femaleCount shouldBe 2
                    result.totalCount shouldBe 5
                    verify(purchasedProfileReader).countByGender(Gender.MALE)
                    verify(purchasedProfileReader).countByGender(Gender.FEMALE)
                }
            }

            context("구매 기록이 없으면") {
                it("모든 카운트가 0을 반환한다") {
                    // given
                    whenever(purchasedProfileReader.countByGender(Gender.MALE)).thenReturn(0)
                    whenever(purchasedProfileReader.countByGender(Gender.FEMALE)).thenReturn(0)

                    // when
                    val result = profileService.getConnectionsCount()

                    // then
                    result.maleCount shouldBe 0
                    result.femaleCount shouldBe 0
                    result.totalCount shouldBe 0
                    verify(purchasedProfileReader).countByGender(Gender.MALE)
                    verify(purchasedProfileReader).countByGender(Gender.FEMALE)
                }
            }
        }

        context("getPurchasedProfiles 메서드를 호출할 때") {

            context("구매한 프로필이 있으면") {
                it("introSentences를 포함한 프로필 목록을 반환한다") {
                    val viewer = createTestViewer("viewer-uuid")
                    val purchased = PurchasedProfile(profileId = 10L, viewerId = 1L)
                    val profile = createTestProfile(
                        id = 10L,
                        uuid = "profile-uuid",
                        introSentences = listOf("소개문장1", "소개문장2")
                    )

                    whenever(viewerReader.get(Uuid("viewer-uuid"))).thenReturn(viewer)
                    whenever(purchasedProfileReader.findByViewerId(1L)).thenReturn(listOf(purchased))
                    whenever(profileReader.getByIds(listOf(10L))).thenReturn(listOf(profile))

                    val result = profileService.getPurchasedProfiles("viewer-uuid")

                    result.size shouldBe 1
                    result[0].profileId shouldBe 10L
                    result[0].introSentences shouldBe listOf("소개문장1", "소개문장2")
                    verify(profileReader).getByIds(listOf(10L))
                }
            }

            context("구매한 프로필이 없으면") {
                it("빈 목록을 반환한다") {
                    val viewer = createTestViewer("viewer-uuid")

                    whenever(viewerReader.get(Uuid("viewer-uuid"))).thenReturn(viewer)
                    whenever(purchasedProfileReader.findByViewerId(1L)).thenReturn(emptyList())
                    whenever(profileReader.getByIds(emptyList())).thenReturn(emptyList())

                    val result = profileService.getPurchasedProfiles("viewer-uuid")

                    result shouldBe emptyList()
                }
            }
        }
    }
})
