package com.snapcompany.snapsafe.utilities

import android.util.Log
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.util.Base64 // For encoding ciphertext for transmission

class AESEncryption {

    fun generateIv(): ByteArray {
        // Recommended IV length for AES/GCM is 12 bytes (96 bits)
        val iv = ByteArray(12)
        val secureRandom = SecureRandom()
        secureRandom.nextBytes(iv)
        return iv
    }

    fun byteArrayToString(byteArray: ByteArray): String {
        return Base64.getEncoder().encodeToString(byteArray)
    }

    fun stringToByteArray(base64String: String): ByteArray {
        return Base64.getDecoder().decode(base64String)
    }

    fun encryptStringWithSharedKey(plainText: String, secretKeyBytes: ByteArray, ivBytes: ByteArray): String? {
        try {
            val secretKey = SecretKeySpec(secretKeyBytes, "AES")
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            // GCMParameterSpec: IV length (12 bytes is common for GCM), and authentication tag length (128 bits is common)
            val parameterSpec = GCMParameterSpec(128, ivBytes)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec)
            val cipherTextBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            return Base64.getEncoder().encodeToString(cipherTextBytes) // Encode to send as string
        } catch (e: Exception) {
            Log.e("EncryptionError", "Encryption failed", e)
            return null
        }
    }



    fun decryptStringWithSharedKey(cipherTextBase64: String, secretKeyBytes: ByteArray, ivBytes: ByteArray): String? {
        try {
            val secretKey = SecretKeySpec(secretKeyBytes, "AES")
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val parameterSpec = GCMParameterSpec(128, ivBytes) // Same IV and tag length as encryption
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec)
            val decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(cipherTextBase64))
            return String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e("DecryptionError", "Decryption failed", e)
            return null
        }
    }

}