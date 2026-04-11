package com.yourssu.signal.infrastructure.openai

import com.yourssu.signal.config.properties.OpenAIConfigurationProperties
import com.yourssu.signal.infrastructure.openai.ChatModel
import com.yourssu.signal.infrastructure.openai.dto.ContentRequest
import com.yourssu.signal.infrastructure.openai.dto.NicknameSuggestedRequest
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
//    @Cacheable("nicknameCache", key = "T(java.lang.String).join(',', #statements)")
    override fun suggestNickname(statements: List<String>): NicknameSuggestedResponse {
        val response = okHttpClient.newCall(request(statements)).execute().body
        return NicknameSuggestedResponse(parse(response))
    }

    private fun request(statements: List<String>): Request {
        val contents = toRequestBody(statements)
        val requestBody = Json.encodeToString(NicknameSuggestedRequest.from(properties.model, contents))
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        return Request.Builder()
            .url(properties.url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer ${properties.apiKey}")
            .post(requestBody)
            .build()
    }

    private fun toRequestBody(statements: List<String>): List<ContentRequest> {
        if (statements.size >= DEFAULT_STATEMENTS_SIZE) {
            return listOf(
                ContentRequest.of("developer", properties.prompt),
                ContentRequest.of(
                    "user", statements.subList(0, DEFAULT_STATEMENTS_SIZE - 1)
                        .joinToString()
                        .take(properties.userInput)
                ),
            )
        }
        return listOf(
            ContentRequest.of("developer", properties.prompt),
            ContentRequest.of(
                "user", statements.joinToString()
                    .take(properties.userInput)
            ),
        )
    }

    private fun parse(responseBody: ResponseBody): String {
        val json = Json.parseToJsonElement(responseBody.string()).jsonObject
        validateError(json)
        return parseResult(json)
    }

    private fun validateError(root: JsonObject) {
        val error = root["error"]
        if (error != null && error !is JsonNull) {
            val errorMessage = error.jsonObject["message"]!!.jsonPrimitive.content
            throw FailedOpenAIModelException()
        }
    }

    private fun parseResult(root: JsonObject): String {
        val outputArray = root["output"]!!.jsonArray
        val firstOutput = outputArray[0].jsonObject
        val contentArray = firstOutput["content"]!!.jsonArray
        val firstContent = contentArray[0].jsonObject
        return firstContent["text"]!!.jsonPrimitive.content
    }
}
