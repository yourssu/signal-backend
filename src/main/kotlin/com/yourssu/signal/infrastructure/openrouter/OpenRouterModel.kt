package com.yourssu.signal.infrastructure.openrouter

import com.yourssu.signal.config.properties.OpenRouterConfigurationProperties
import com.yourssu.signal.domain.profile.implement.ChatModel
import com.yourssu.signal.infrastructure.openai.dto.NicknameSuggestedResponse
import com.yourssu.signal.infrastructure.openrouter.exception.FailedOpenRouterModelException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

private const val DEFAULT_STATEMENTS_SIZE = 3

@Component
@Profile("prod", "dev")
class OpenRouterModel(
    private val okHttpClient: OkHttpClient,
    private val properties: OpenRouterConfigurationProperties
) : ChatModel {
    override fun suggestNickname(statements: List<String>): NicknameSuggestedResponse {
        val responseBody = okHttpClient.newCall(request(statements)).execute().body
        return NicknameSuggestedResponse(parse(responseBody))
    }

    private fun request(statements: List<String>): Request {
        val messages = toMessages(statements)

        val modelName = properties.model.ifBlank { "openrouter/sonoma-dusk-alpha" }
        val bodyJson = buildJsonObject {
            put("model", JsonPrimitive(modelName))
            put("messages", JsonArray(messages))
        }

        val requestBody = Json.encodeToString(bodyJson)
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        return Request.Builder()
            .url(properties.url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer ${properties.apiKey}")
            .post(requestBody)
            .build()
    }

    private fun toMessages(statements: List<String>): List<JsonObject> {
        val userJoined = if (statements.size >= DEFAULT_STATEMENTS_SIZE) {
            statements.subList(0, DEFAULT_STATEMENTS_SIZE - 1).joinToString()
        } else {
            statements.joinToString()
        }.take(properties.userInput)

        return listOf(
            buildJsonObject {
                put("role", JsonPrimitive("developer"))
                put("content", JsonArray(listOf(
                    buildJsonObject {
                        put("type", JsonPrimitive("text"))
                        put("text", JsonPrimitive(properties.prompt))
                    }
                )))
            },
            buildJsonObject {
                put("role", JsonPrimitive("user"))
                put("content", JsonArray(listOf(
                    buildJsonObject {
                        put("type", JsonPrimitive("text"))
                        put("text", JsonPrimitive(userJoined))
                    }
                )))
            }
        )
    }

    private fun parse(responseBody: ResponseBody): String {
        val root = Json.parseToJsonElement(responseBody.string()).jsonObject
        validateError(root)

        val choices = root["choices"]?.jsonArray ?: throw FailedOpenRouterModelException()
        if (choices.isEmpty()) throw FailedOpenRouterModelException("No choices in response")

        val firstChoice = choices[0].jsonObject
        val message = firstChoice["message"]?.jsonObject ?: throw FailedOpenRouterModelException()
        return message["content"]?.jsonPrimitive?.content ?: throw FailedOpenRouterModelException()
    }

    private fun validateError(root: JsonObject) {
        val error = root["error"]
        if (error != null && error !is JsonNull) {
            val errorMessage = error.jsonObject["message"]?.jsonPrimitive?.content ?: "Unknown OpenRouter error"
            throw FailedOpenRouterModelException("OpenRouter API Error: $errorMessage")
        }
    }
}
