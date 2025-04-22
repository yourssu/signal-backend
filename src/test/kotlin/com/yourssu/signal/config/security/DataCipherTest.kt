package com.yourssu.signal.config.security

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class DataCipherTest {
    @Autowired
    private lateinit var dataCipher: DataCipher

    @Test
    fun testEncryptAndDecrypt() {
        val originalData = "@Hello"
        val encryptedData = dataCipher.encrypt(originalData)
        val decryptedData = dataCipher.decrypt(encryptedData)

        assertNotEquals(originalData, encryptedData)
        assertEquals(originalData, decryptedData)
    }
}

