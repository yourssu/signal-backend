package com.yourssu.signal.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.yourssu.signal.domain.profile.implement.ProfileValidationPolicy
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProfileOpenApiContractTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `api-docs는 프로필 생성과 수정의 실제 제약조건을 제공한다`() {
        val response = mockMvc.get("/api-docs").andExpect { status { isOk() } }.andReturn().response.contentAsString
        val schemas = objectMapper.readTree(response).path("components").path("schemas")
        val created = schemas.path("ProfileCreatedRequest")
        val updated = schemas.path("ProfileUpdateRequest")

        created.path("properties").path("department").path("minLength").asInt() shouldBe ProfileValidationPolicy.MIN_DEPARTMENT_LENGTH
        created.path("properties").path("department").path("maxLength").asInt() shouldBe ProfileValidationPolicy.MAX_DEPARTMENT_LENGTH
        created.path("properties").path("birthYear").path("minimum").asLong() shouldBe ProfileValidationPolicy.MIN_BIRTH_YEAR
        created.path("properties").path("birthYear").path("maximum").asInt() shouldBe ProfileValidationPolicy.maximumBirthYear()
        created.path("properties").path("nickname").path("maxLength").asInt() shouldBe ProfileValidationPolicy.MAX_NICKNAME_LENGTH
        created.path("properties").path("introSentences").path("maxItems").asInt() shouldBe ProfileValidationPolicy.MAX_INTRO_SENTENCES_SIZE
        created.path("properties").path("introSentences").path("items").path("maxLength").asInt() shouldBe ProfileValidationPolicy.MAX_INTRO_SENTENCE_LENGTH
        created.path("properties").path("contact").path("pattern").asText() shouldBe ProfileValidationPolicy.CONTACT_PATTERN
        updated.path("properties").path("contact").path("pattern").asText() shouldBe ProfileValidationPolicy.CONTACT_PATTERN

        created.path("properties").path("gender").path("enum").map { it.asText() }
            .shouldContainExactlyInAnyOrder("MALE", "FEMALE")
        created.path("properties").path("animal").path("enum").size() shouldBe 9
        created.path("properties").path("mbti").path("enum").size() shouldBe 16
        created.path("properties").path("egenTeto").path("enum").map { it.asText() }
            .shouldContainExactlyInAnyOrder("EGEN", "TETO", "NOT_SELECTED")
    }

    @Test
    fun `api-docs는 성별과 동물상 조합을 oneOf로 제공한다`() {
        val response = mockMvc.get("/api-docs").andExpect { status { isOk() } }.andReturn().response.contentAsString
        val combinations = objectMapper.readTree(response)
            .path("components").path("schemas").path("ProfileCreatedRequest").path("oneOf")

        combinations.size() shouldBe 2
        combinations[0].path("required").map { it.asText() }
            .shouldContainExactlyInAnyOrder("gender", "animal")
        val animalsByGender = combinations.associate { combination ->
            val properties = combination.path("properties")
            properties.path("gender").path("enum")[0].asText() to
                properties.path("animal").path("enum").map { it.asText() }
        }
        animalsByGender.getValue("MALE").shouldContainExactlyInAnyOrder(
            "BEAR", "DEER", "DINOSAUR", "DOG", "CAT", "HAMSTER"
        )
        animalsByGender.getValue("FEMALE").shouldContainExactlyInAnyOrder(
            "FOX", "RABBIT", "TURTLE", "DOG", "CAT", "HAMSTER"
        )
    }
}
