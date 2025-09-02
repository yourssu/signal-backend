package com.yourssu.signal.infra.google

import com.fasterxml.jackson.databind.ObjectMapper
import com.yourssu.signal.domain.auth.implement.OAuthOutputPort
import com.yourssu.signal.config.properties.GoogleOAuthConfigurationProperties
import okhttp3.*
import org.springframework.stereotype.Component
import java.io.IOException
import java.net.URLDecoder

@Component
class GoogleClient(
    private val okHttpClient: OkHttpClient,
    private val objectMapper: ObjectMapper,
    private val googleOAuthProperties: GoogleOAuthConfigurationProperties
) : OAuthOutputPort {
    override fun exchangeCodeForIdToken(code: String): String? {
        val url = "https://oauth2.googleapis.com/token"
        val requestBody = FormBody.Builder()
            .add("code", code.replace("%2F", "/"))
            .add("client_id", googleOAuthProperties.clientId)
            .add("client_secret", googleOAuthProperties.clientSecret)
            .add("redirect_uri", googleOAuthProperties.redirectUri)
            .add("grant_type", "authorization_code")
            .build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        return try {
            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body.string()
                    val tokenResponse = objectMapper.readTree(responseBody)
                    tokenResponse.get("id_token")?.asText()
                } else {
                    null
                }
            }
        } catch (e: IOException) {
            null
        }
    }
}
