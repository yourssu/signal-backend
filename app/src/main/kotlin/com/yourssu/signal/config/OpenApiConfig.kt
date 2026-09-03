package com.yourssu.signal.config

import com.yourssu.signal.domain.meeting.business.MeetingService
import com.yourssu.signal.domain.meeting.implement.MeetingSlot
import com.yourssu.signal.domain.profile.implement.Animal
import com.yourssu.signal.domain.profile.implement.EgenTeto
import com.yourssu.signal.domain.profile.implement.Gender
import com.yourssu.signal.domain.profile.implement.MbtiCompatibilityTable
import com.yourssu.signal.domain.profile.implement.ProfileValidationPolicy
import com.yourssu.signal.infrastructure.sms.SMSParser
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springdoc.core.customizers.OpenApiCustomizer
import java.math.BigDecimal

@Configuration
class OpenApiConfig {
    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Signal API")
                    .version("20250825")
                    .description("Signal Application API Documentation")
            )
            .servers(
                listOf(
                    Server().url("http://localhost:8080").description("Local Development Server"),
                    Server().url("https://signal-dev-api.yourssu.com").description("Development Server"),
                )
            )
            .components(
                Components()
                    .addSecuritySchemes(
                        "bearerAuth",
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("Bearer Token authentication")
                    )
            )
    }

    @Bean
    fun profileValidationOpenApiCustomizer(): OpenApiCustomizer = OpenApiCustomizer { openApi ->
        val schemas = openApi.components?.schemas ?: return@OpenApiCustomizer
        val genders = Gender.entries.map { it.name }
        val animals = Animal.entries.map { it.name }
        val mbtiTypes = MbtiCompatibilityTable.validTypes().toList()
        val egenTetoTypes = EgenTeto.entries.map { it.name }
        val smsTypes = SMSParser.supportedTypes()
        schemas["ProfileCreatedRequest"]?.let { schema ->
            schema.property("birthYear")?.maximum = BigDecimal.valueOf(ProfileValidationPolicy.maximumBirthYear().toLong())
            schema.property("introSentences")?.apply {
                minItems = ProfileValidationPolicy.MIN_INTRO_SENTENCES_SIZE
                items?.maxLength = ProfileValidationPolicy.MAX_INTRO_SENTENCE_LENGTH
            }
            schema.stringProperty("gender")?.setEnum(genders)
            schema.stringProperty("animal")?.setEnum(animals)
            schema.stringProperty("mbti")?.setEnum(mbtiTypes)
            schema.stringProperty("egenTeto")?.setEnum(egenTetoTypes)
            schema.oneOf = listOf(
                genderAnimalSchema(Gender.MALE, ProfileValidationPolicy.maleAnimals),
                genderAnimalSchema(Gender.FEMALE, ProfileValidationPolicy.femaleAnimals),
            )
        }
        schemas["ProfileUpdateRequest"]?.let { schema ->
            schema.property("introSentences")?.apply {
                minItems = ProfileValidationPolicy.MIN_INTRO_SENTENCES_SIZE
                items?.maxLength = ProfileValidationPolicy.MAX_INTRO_SENTENCE_LENGTH
            }
            schema.stringProperty("egenTeto")?.setEnum(egenTetoTypes)
        }
        listOf("DeckRequest", "RandomProfileRequest").forEach { schemaName ->
            schemas[schemaName]?.stringProperty("gender")?.setEnum(genders)
        }
        listOf("ProfileResponse", "MyProfileResponse", "ProfileContactResponse", "ProfileRankingResponse")
            .forEach { schemaName ->
                schemas[schemaName]?.let { schema ->
                    schema.stringProperty("gender")?.setEnum(genders)
                    schema.stringProperty("animal")?.setEnum(animals)
                    schema.stringProperty("mbti")?.setEnum(mbtiTypes)
                    schema.stringProperty("egenTeto")?.setEnum(egenTetoTypes)
                }
            }
        schemas["MeetingRoomCreateRequest"]?.let { schema ->
            schema.stringProperty("slot")?.apply {
                setEnum(MeetingSlot.selectableEntries.map { it.name })
                description = "미팅 슬롯"
                example = "SLOT_1"
            }
            schema.stringProperty("invitation")?.apply {
                minLength = 1
                pattern = ".*\\S.*"
                description = "초대 문구"
                example = "같이 축제 즐겨요"
            }
            schema.property("companions")?.description = "본인을 제외한 동행자 1~3명"
        }
        schemas["MeetingMemberRequest"]?.let { schema ->
            schema.property("birthYear")?.apply {
                maximum = BigDecimal.valueOf(ProfileValidationPolicy.maximumBirthYear().toLong())
                description = "출생연도"
                example = 2001
            }
            schema.stringProperty("department")?.apply {
                description = "학과"
                example = "경영학부"
            }
            schema.stringProperty("gender")?.apply {
                setEnum(genders)
                description = "성별"
                example = "MALE"
            }
        }
        schemas["MeetingMatchRequest"]?.let { schema ->
            schema.property("representative")?.description = "JWT 사용자인 신청자 대표 정보"
            schema.stringProperty("contact")?.apply {
                pattern = ProfileValidationPolicy.CONTACT_PATTERN
                description = "신청자 대표 연락처: 휴대폰 번호 또는 인스타그램 아이디"
                example = "@signal_user"
            }
            schema.property("companions")?.description =
                "대표를 제외한 동행자 1~3명. 대표와 합친 인원은 기존 방의 인원수와 같아야 합니다."
        }
        schemas["ReportCreatedRequest"]?.property("profileId")?.apply {
            minimum = BigDecimal.ONE
            description = "신고할 프로필 ID. 연락처를 열람한 프로필만 신고할 수 있습니다."
            example = 1L
        }
        schemas["ReportApprovedRequest"]?.stringProperty("secretKey")?.apply {
            minLength = 1
            pattern = ".*\\S.*"
            description = "신고 승인용 관리자 비밀 키"
            example = "admin-secret-key"
        }
        schemas.values.forEach { schema ->
            schema.stringProperty("timestamp")?.apply {
                format = "date-time"
                example = "2026-09-02T12:00:00+09:00"
            }
        }
        listOf("MeetingRoomResponse", "MeetingRoomSummaryResponse").forEach { schemaName ->
            schemas[schemaName]?.let { schema ->
                schema.positiveExample("id", 1L)
                schema.stringProperty("creatorAnimal")?.apply {
                    setEnum(animals)
                    description = "방 생성 당시 생성자 프로필의 동물상"
                    example = "DOG"
                }
                schema.property("partySize")?.apply {
                    minimum = BigDecimal.valueOf(2)
                    maximum = BigDecimal.valueOf(4)
                    example = 2
                }
                schema.stringProperty("invitation")?.example = "같이 축제 즐겨요"
                schema.stringProperty("expiresAt")?.example = "2026-09-02T13:00:00+09:00"
            }
        }
        schemas["MeetingRoomResponse"]?.stringProperty("slot")
            ?.setEnum(MeetingSlot.selectableEntries.map { it.name })
        schemas["MeetingLatestMatchResponse"]?.let { schema ->
            schema.positiveExample("roomId", 1L)
            schema.stringProperty("creatorNickname")?.example = "시그널"
            schema.stringProperty("creatorAnimal")?.apply {
                setEnum(animals)
                description = "매칭된 방 생성 당시 생성자 프로필의 동물상"
                example = "DOG"
            }
            schema.stringProperty("matchedAt")?.example = "2026-09-02T12:00:00+09:00"
            schema.stringProperty("visibleUntil")?.example = "2026-09-02T12:00:30+09:00"
        }
        schemas["MeetingSlotResponse"]?.stringProperty("slot")
            ?.setEnum(MeetingSlot.selectableEntries.map { it.name })
        schemas["MeetingMemberResponse"]?.let { schema ->
            schema.property("memberOrder")?.apply {
                minimum = BigDecimal.ZERO
                example = 0
            }
            schema.property("birthYear")?.example = 2001
            schema.stringProperty("department")?.example = "경영학부"
        }
        listOf("MeetingMatchResponse", "MeetingResultResponse").forEach { schemaName ->
            schemas[schemaName]?.let { schema ->
                schema.positiveExample("roomId", 1L)
                schema.stringProperty("counterpartContact")?.example = "@signal_user"
            }
        }
        schemas["MeetingCreationEligibilityResponse"]?.property("canCreate")?.example = true
        schemas["ReportResponse"]?.let { schema ->
            schema.positiveExample("reportId", 1L)
            schema.positiveExample("reportedProfileId", 1L)
        }
        schemas["ErrorResponse"]?.let { schema ->
            schema.property("status")?.apply {
                minimum = BigDecimal.valueOf(400)
                maximum = BigDecimal.valueOf(599)
                example = 400
            }
            schema.stringProperty("message")?.example = "요청값 검증 실패"
            schema.stringProperty("code")?.example = "INVALID_INPUT"
        }
        schemas["MeetingCreationEligibilityResponse"]?.stringProperty("reason")?.setEnum(
            listOf(
                MeetingService.PROFILE_REQUIRED,
                MeetingService.DAILY_CREATION_LIMIT_EXCEEDED,
                MeetingService.DAILY_MEETING_LIMIT_EXCEEDED,
                MeetingService.ACTIVE_ROOM_EXISTS,
            )
        )
        schemas["BankDepositSmsRequest"]?.stringProperty("type")?.setEnum(smsTypes)

        openApi.paths?.let { paths ->
            paths["/api/profiles/genders/{gender}/count"]?.get?.parameters
                ?.firstOrNull { it.name == "gender" }?.stringSchema()?.setEnum(genders)
            paths["/api/viewers/sms/{type}/{secretKey}"]?.post?.parameters
                ?.firstOrNull { it.name == "type" }?.stringSchema()?.setEnum(smsTypes)
            paths.forEach { (path, pathItem) ->
                pathItem.readOperationsMap().forEach { (method, operation) ->
                    operation.responses?.forEach response@{ (responseCode, response) ->
                        val status = responseCode.toIntOrNull() ?: return@response
                        val errorExample = errorExample(path, method.name, status)
                        response.content?.get("application/json")
                            ?.takeIf { it.schema?.`$ref` == "#/components/schemas/ErrorResponse" }
                            ?.example = linkedMapOf(
                                "timestamp" to "2026-09-02T12:00:00+09:00",
                                "status" to status,
                                "message" to errorExample.message,
                            ).apply {
                                errorExample.code?.let { put("code", it) }
                            }
                    }
                }
            }
        }
    }

    private fun io.swagger.v3.oas.models.parameters.Parameter.stringSchema(): Schema<String>? =
        @Suppress("UNCHECKED_CAST")
        (schema as? Schema<String>)

    private fun Schema<*>.property(name: String): Schema<*>? = properties?.get(name)

    private fun Schema<*>.stringProperty(name: String): Schema<String>? =
        @Suppress("UNCHECKED_CAST")
        (property(name) as? Schema<String>)

    private fun Schema<*>.positiveExample(name: String, value: Long) {
        property(name)?.apply {
            minimum = BigDecimal.ONE
            example = value
        }
    }

    private fun errorExample(path: String, method: String, status: Int): ErrorExample = when {
        status == 401 -> ErrorExample("Authentication required to access this resource")
        status == 400 && path == "/api/meetings/rooms" ->
            ErrorExample("올바르지 않은 미팅 슬롯입니다.", "INVALID_MEETING_SLOT")
        status == 400 -> ErrorExample("Invalid Input: [must be greater than 0]")
        path == "/api/reports" && status == 403 ->
            ErrorExample("연락처를 열람한 프로필만 신고할 수 있습니다.")
        path == "/api/reports" && status == 404 ->
            ErrorExample("해당하는 프로필을 찾을 수 없습니다.")
        path == "/api/reports" && status == 409 -> ErrorExample("이미 신고한 프로필입니다.")
        path.endsWith("/approve") && status == 403 ->
            ErrorExample("올바르지 않은 관리자 기능 접근 토큰입니다.")
        path.endsWith("/approve") && status == 404 -> ErrorExample("신고를 찾을 수 없습니다.")
        path.endsWith("/approve") && status == 409 -> ErrorExample("이미 승인된 신고입니다.")
        status == 404 -> ErrorExample("미팅 방을 찾을 수 없습니다.", "MEETING_ROOM_NOT_FOUND")
        status == 403 && path.endsWith("/cancel") ->
            ErrorExample("방 생성자만 취소할 수 있습니다.", "MEETING_ROOM_CANCEL_FORBIDDEN")
        status == 403 ->
            ErrorExample("매칭 결과를 조회할 권한이 없습니다.", "MEETING_RESULT_FORBIDDEN")
        status == 409 && path == "/api/meetings/rooms" && method == "POST" ->
            ErrorExample("이미 사용 중인 미팅 슬롯입니다.", "SLOT_ALREADY_OCCUPIED")
        status == 409 && path.endsWith("/matches") ->
            ErrorExample("자신이 생성한 미팅 방에는 신청할 수 없습니다.", "SELF_MATCH_NOT_ALLOWED")
        status == 409 && path.endsWith("/result") ->
            ErrorExample("취소된 미팅 방입니다.", "ROOM_CANCELLED")
        status == 409 -> ErrorExample("이미 매칭된 미팅 방입니다.", "ROOM_ALREADY_MATCHED")
        else -> ErrorExample("요청 처리 실패")
    }

    private data class ErrorExample(
        val message: String,
        val code: String? = null,
    )

    private fun genderAnimalSchema(gender: Gender, animals: Set<Animal>): Schema<Any> = Schema<Any>()
        .type("object")
        .addProperty("gender", StringSchema().apply { setEnum(listOf(gender.name)) })
        .addProperty("animal", StringSchema().apply { setEnum(animals.map { it.name }) })
        .required(listOf("gender", "animal"))
}
