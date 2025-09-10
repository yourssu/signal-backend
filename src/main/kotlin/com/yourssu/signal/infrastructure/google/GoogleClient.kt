package com.yourssu.signal.infrastructure.google

import com.fasterxml.jackson.databind.ObjectMapper
import com.yourssu.signal.config.properties.GoogleOAuthConfigurationProperties
import com.yourssu.signal.domain.auth.implement.OAuthOutputPort
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.stereotype.Component
import java.io.IOException
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Component
class GoogleClient(
    private val okHttpClient: OkHttpClient,
    private val objectMapper: ObjectMapper,
    private val googleOAuthProperties: GoogleOAuthConfigurationProperties
) : OAuthOutputPort {
    override fun exchangeCodeForIdToken(code: String): String? {
        val url = "https://oauth2.googleapis.com/token"
        val decodedCode = URLDecoder.decode(code, StandardCharsets.UTF_8)
        val requestBody = FormBody.Builder()
            .add("client_id", googleOAuthProperties.clientId)
            .add("client_secret", googleOAuthProperties.clientSecret)
            .add("code", decodedCode)
            .add("grant_type", "authorization_code")
            .add("redirect_uri", googleOAuthProperties.redirectUri)
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
        } catch (_: IOException) {
            null
        }
    }
}
