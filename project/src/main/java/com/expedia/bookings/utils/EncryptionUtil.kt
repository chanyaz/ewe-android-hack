package com.expedia.bookings.utils

import android.content.Context
import android.security.KeyPairGeneratorSpec
import android.support.annotation.VisibleForTesting
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.util.Calendar
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.security.auth.x500.X500Principal

open class EncryptionUtil(val context: Context, val secretKeyFile: File, val alias: String) {
    private val TRANSFORMATION_RSA = "RSA/ECB/PKCS1Padding"
    protected val TRANSFORMATION_AES = "AES/GCM/NoPadding"
    private val RSA_ALGORITHM = "RSA"
    private val AES_ALGORITHM = "AES"
    private val KEYSTORE_NAME = "AndroidKeyStore"
    private val CIPHER_PROVIDER = "SC"
    private val AAD_TAG = "AAD_EXPEDIA".toByteArray()
    protected val AES_KEY_LENGTH: Int = 128
    private val GCM_NONCE_LENGTH = 12 // in bytes
    private val GCM_TAG_LENGTH = 16 // in bytes
    private val DELIMITER = "]"
    private val random = SecureRandom()

    private val keyStore: KeyStore by lazy {
        val keystore = KeyStore.getInstance(KEYSTORE_NAME)
        keystore.load(null)
        keystore
    }

    open val AES_KEY: ByteArray by lazy {
        val key: ByteArray
        if (!keyStore.containsAlias(alias) || !secretKeyFile.exists()) {
            key = generateAESKey(AES_KEY_LENGTH).encoded
            encryptBytesIntoFileRSA(key, secretKeyFile, RSA_KEY.public)
        } else {
            key = decryptFileIntoBytesRSA(secretKeyFile, RSA_KEY.private)
        }
        key
    }

    private val RSA_KEY: KeyPair by lazy {
        val key = if (!keyStore.containsAlias(alias)) {
            generateRSAKey()
        } else {
            KeyPair(getPublicRSAKey(alias), getPrivateRSAKey(alias))
        }
        key
    }

    @Throws(Exception::class)
    fun encryptStringToBase64CipherText(plainText: String): String {
        val keySpec = SecretKeySpec(AES_KEY, AES_ALGORITHM)

        val cipher = getAESCipher()
        val iv = generateIv(GCM_NONCE_LENGTH)
        val ivParams = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)

        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParams)
        cipher.updateAAD(AAD_TAG)

        val original = plainText.toByteArray()
        val binary = cipher.doFinal(original)

        val ivText = Base64.encodeToString(iv, Base64.DEFAULT)
        val cipherText = Base64.encodeToString(binary, Base64.DEFAULT)

        val finalString = "$ivText$DELIMITER$cipherText"
        return finalString
    }

    @Throws(Exception::class)
    fun decryptStringFromBase64CipherText(cipherText: String): String {
        val fields = cipherText.split(DELIMITER)

        if (fields.size != 2) {
            return String()
        }

        val iv = Base64.decode(fields[0], Base64.DEFAULT)
        val binary = Base64.decode(fields[1], Base64.DEFAULT)

        val keySpec = SecretKeySpec(AES_KEY, AES_ALGORITHM)
        val cipher = getAESCipher()
        val ivParams = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParams)
        cipher.updateAAD(AAD_TAG)

        val plainText = cipher.doFinal(binary)
        return String(plainText)
    }

    @Throws(Exception::class)
    private fun encryptBytesIntoFileRSA(data: ByteArray, file: File, publicKey: PublicKey) {
        file.parentFile.mkdirs()
        file.createNewFile()
        val input = Cipher.getInstance(TRANSFORMATION_RSA)
        input.init(Cipher.ENCRYPT_MODE, publicKey)

        val outputStream = FileOutputStream(file)
        val cipherOutputStream = CipherOutputStream(outputStream, input)
        cipherOutputStream.write(data)
        cipherOutputStream.close()
        outputStream.close()
    }

    @Throws(Exception::class)
    private fun decryptFileIntoBytesRSA(secretKeyFile: File, privateKey: PrivateKey): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION_RSA)

        cipher.init(Cipher.DECRYPT_MODE, privateKey)

        val os = ByteArrayOutputStream()
        val inputStream = FileInputStream(secretKeyFile)
        val cipherInputStream = CipherInputStream(inputStream, cipher)

        var byte: Int = 0
        while ({ byte = cipherInputStream.read(); byte }() >= 0) {
            os.write(byte)
        }
        return os.toByteArray()
    }

    @VisibleForTesting
    fun generateAESKey(outputKeyLength: Int): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM)
        keyGenerator.init(outputKeyLength)
        return keyGenerator.generateKey()
    }

    private fun generateRSAKey(): KeyPair {
        val start = Calendar.getInstance()
        val end = Calendar.getInstance()
        end.add(Calendar.YEAR, 25)
        val spec = KeyPairGeneratorSpec.Builder(context)
                .setAlias(alias)
                .setSubject(X500Principal(String.format("CN=%s", alias)))
                .setSerialNumber(BigInteger.ONE)
                .setStartDate(start.time)
                .setKeySize(4096)
                .setEndDate(end.time).build()

        val generator = KeyPairGenerator.getInstance(RSA_ALGORITHM, KEYSTORE_NAME)
        generator.initialize(spec)

        return generator.generateKeyPair()
    }

    private fun getPublicRSAKey(alias: String) : PublicKey {
        val privateKeyEntry = keyStore.getEntry(alias, null) as KeyStore.PrivateKeyEntry
        return privateKeyEntry.certificate.publicKey
    }

    private fun getPrivateRSAKey(alias: String) : PrivateKey {
        val privateKeyEntry = keyStore.getEntry(alias, null) as KeyStore.PrivateKeyEntry
        return privateKeyEntry.privateKey
    }

    fun generateIv(length: Int): ByteArray {
        val b = ByteArray(length)
        random.nextBytes(b)
        return b
    }

    open fun getAESCipher() : Cipher {
        return Cipher.getInstance(TRANSFORMATION_AES, CIPHER_PROVIDER)
    }

    open fun clear() {
        secretKeyFile.delete()
        keyStore.deleteEntry(alias)
    }
}