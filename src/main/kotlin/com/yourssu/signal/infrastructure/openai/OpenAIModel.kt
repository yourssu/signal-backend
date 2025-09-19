package com.yourssu.signal.infrastructure.openai

import com.yourssu.signal.config.properties.OpenAIConfigurationProperties
import com.yourssu.signal.domain.profile.implement.ChatModel
import com.yourssu.signal.infrastructure.openai.dto.NicknameSuggestedResponse
import com.yourssu.signal.infrastructure.openai.exception.FailedOpenAIModelException
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
class OpenAIModel(
    private val okHttpClient: OkHttpClient,
    private val properties: OpenAIConfigurationProperties
) : ChatModel {
    override fun suggestNickname(statements: List<String>): NicknameSuggestedResponse {
        val responseBody = okHttpClient.newCall(request(statements)).execute().body
        return NicknameSuggestedResponse(parse(responseBody))
    }

    private fun request(statements: List<String>): Request {
        val inputItems = toInput(statements)

        val modelName = properties.model.ifBlank { "gpt-5-mini" }
        val bodyJson = buildJsonObject {
            put("model", JsonPrimitive(modelName))
            put("reasoning", buildJsonObject {
                put("effort", JsonPrimitive("low"))
            })
            put("input", JsonArray(inputItems))
        }

        val requestBody = Json.encodeToString(bodyJson)
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val baseUrl = properties.url.ifBlank { "https://api.openai.com/v1/responses" }

        return Request.Builder()
            .url(baseUrl)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer ${properties.apiKey}")
            .post(requestBody)
            .build()
    }

    private fun toInput(statements: List<String>): List<JsonObject> {
        val userJoined = if (statements.size >= DEFAULT_STATEMENTS_SIZE) {
            statements.subList(0, DEFAULT_STATEMENTS_SIZE - 1).joinToString()
        } else {
            statements.joinToString()
        }.take(properties.userInput)

        return listOf(
            buildJsonObject {
                put("role", JsonPrimitive("developer"))
                put("content", JsonPrimitive(properties.prompt))
            },
            buildJsonObject {
                put("role", JsonPrimitive("user"))
                put("content", JsonPrimitive(userJoined))
            }
        )
    }

    private fun parse(responseBody: ResponseBody): String {
        val root = Json.parseToJsonElement(responseBody.string()).jsonObject
        validateError(root)
        val output = root["output"]?.jsonArray ?: throw FailedOpenAIModelException()
        val assistantMsg = output.find { it.jsonObject["type"]?.jsonPrimitive?.content == "message" }
            ?.jsonObject ?: throw FailedOpenAIModelException()
        val content = assistantMsg["content"]?.jsonArray ?: throw FailedOpenAIModelException()
        val textPart = content.find { it.jsonObject["type"]?.jsonPrimitive?.content == "output_text" }
            ?.jsonObject ?: throw FailedOpenAIModelException()
        return textPart["text"]?.jsonPrimitive?.content ?: throw FailedOpenAIModelException()
    }

    private fun validateError(root: JsonObject) {
        val error = root["error"]
        if (error != null && error !is JsonNull) {
            val errorMessage = error.jsonObject["message"]?.jsonPrimitive?.content ?: "Unknown OpenAI error"
            throw FailedOpenAIModelException("OpenAI API Error: $errorMessage")
        }
    }
}

