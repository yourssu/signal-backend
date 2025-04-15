package com.yourssu.ssugaeting.infrastructure

import com.yourssu.ssugaeting.config.properties.OpenAIConfigurationProperties
import com.yourssu.ssugaeting.infrastructure.dto.ContentRequest
import com.yourssu.ssugaeting.infrastructure.dto.NicknameSuggestedRequest
import com.yourssu.ssugaeting.infrastructure.dto.NicknameSuggestedResponse
import com.yourssu.ssugaeting.infrastructure.exception.FailedOpenAIModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("prod", "dev")
class OpenAIModel(
    private val okHttpClient: OkHttpClient,
    private val properties: OpenAIConfigurationProperties
) : ChatModel {
    @Cacheable("nicknameCache", key = "T(java.lang.String).join(',', #statements)")
    override fun suggestNickname(statements: List<String>): NicknameSuggestedResponse {
        val response = okHttpClient.newCall(request(statements)).execute().body
        return NicknameSuggestedResponse(parse(response))
    }

    private fun request(statements: List<String>): Request {
        val contents = listOf(
            ContentRequest.of("developer", properties.prompt),
            ContentRequest.of("user", statements.joinToString()
                .take(properties.userInput)),
        )
        val requestBody = Json.encodeToString(NicknameSuggestedRequest.from(properties.model, contents))
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        return Request.Builder()
            .url(properties.url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer ${properties.apiKey}")
            .post(requestBody)
            .build()
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
            throw FailedOpenAIModel()
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
