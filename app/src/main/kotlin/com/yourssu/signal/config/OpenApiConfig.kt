package com.yourssu.signal.config

import com.yourssu.signal.domain.profile.implement.Animal
import com.yourssu.signal.domain.profile.implement.EgenTeto
import com.yourssu.signal.domain.profile.implement.Gender
import com.yourssu.signal.domain.profile.implement.MbtiCompatibilityTable
import com.yourssu.signal.domain.profile.implement.ProfileValidationPolicy
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
        schemas["ProfileCreatedRequest"]?.let { schema ->
            schema.property("birthYear")?.maximum = BigDecimal.valueOf(ProfileValidationPolicy.maximumBirthYear().toLong())
            schema.property("introSentences")?.apply {
                minItems = ProfileValidationPolicy.MIN_INTRO_SENTENCES_SIZE
                items?.maxLength = ProfileValidationPolicy.MAX_INTRO_SENTENCE_LENGTH
            }
            schema.stringProperty("gender")?.setEnum(Gender.entries.map { it.name })
            schema.stringProperty("animal")?.setEnum(Animal.entries.map { it.name })
            schema.stringProperty("mbti")?.setEnum(MbtiCompatibilityTable.validTypes().toList())
            schema.stringProperty("egenTeto")?.setEnum(EgenTeto.entries.map { it.name })
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
            schema.stringProperty("egenTeto")?.setEnum(EgenTeto.entries.map { it.name })
        }
    }

    private fun Schema<*>.property(name: String): Schema<*>? = properties?.get(name)

    private fun Schema<*>.stringProperty(name: String): Schema<String>? =
        @Suppress("UNCHECKED_CAST")
        (property(name) as? Schema<String>)

    private fun genderAnimalSchema(gender: Gender, animals: Set<Animal>): Schema<Any> = Schema<Any>()
        .type("object")
        .addProperty("gender", StringSchema().apply { setEnum(listOf(gender.name)) })
        .addProperty("animal", StringSchema().apply { setEnum(animals.map { it.name }) })
        .required(listOf("gender", "animal"))
}
