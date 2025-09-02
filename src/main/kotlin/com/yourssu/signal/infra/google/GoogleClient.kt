package com.yourssu.signal.infra.google

import com.fasterxml.jackson.databind.ObjectMapper
import com.yourssu.signal.domain.auth.implement.OAuthOutputPort
import okhttp3.*
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class GoogleClient(
    private val okHttpClient: OkHttpClient,
    private val objectMapper: ObjectMapper
) : OAuthOutputPort {
    override fun verifyOAuthAccessToken(accessToken: String): String? {
        val url = "https://www.googleapis.com/oauth2/v1/tokeninfo?access_token=$accessToken"
        val request = Request.Builder()
            .url(url)
            .build()
            
        return try {
            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body.string()
                    val tokenInfo = objectMapper.readTree(responseBody)
                    tokenInfo.get("email")?.asText()
                } else {
                    null
                }
            }
        } catch (_: IOException) {
            null
        }
    }
}
