package com.yourssu.signal.config.security

import com.yourssu.signal.config.properties.AdminConfigurationProperties
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets.UTF_8
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*
import java.util.Base64.getEncoder
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

private const val ENCRYPTED_ALGORITHM = "AES"
private const val CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding"
private const val IV_SIZE = 16

private const val DELIMITER = "||"

private const val HASH_ALGORITHM = "SHA-256"

@Component
class DataCipher(
    properties: AdminConfigurationProperties,
) {
    private val secretKey: SecretKeySpec = SecretKeySpec(
        MessageDigest.getInstance(HASH_ALGORITHM)
            .digest(properties.contactSecretKey.toByteArray(UTF_8)),
        ENCRYPTED_ALGORITHM
    )

    fun encrypt(data: String): String {
        val ivBytes = ByteArray(IV_SIZE)
        SecureRandom().nextBytes(ivBytes)

        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(ivBytes))

        val iv = getEncoder().encodeToString(ivBytes)
        val encryptedData = getEncoder().encodeToString(cipher.doFinal(data.toByteArray(UTF_8)))
        return "$iv$DELIMITER$encryptedData"
    }

    fun decrypt(encrypted: String): String {
        val parts = encrypted.split(DELIMITER)
        val iv = Base64.getDecoder().decode(parts[0])
        val encryptedData = Base64.getDecoder().decode(parts[1])

        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
        return cipher.doFinal(encryptedData).toString(UTF_8)
    }
}
