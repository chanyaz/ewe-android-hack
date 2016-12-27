package com.expedia.bookings.utils

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.security.KeyPairGeneratorSpec
import android.support.annotation.VisibleForTesting
import android.util.Base64
import okio.Okio
import java.io.ByteArrayInputStream
import java.io.File
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.util.Calendar
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.security.auth.x500.X500Principal

open class EncryptionUtil(private val context: Context, private val secretKeyFile: File, private val alias: String) {

    protected val TRANSFORMATION_AES = "AES/GCM/NoPadding"
    protected val RSA_ALGORITHM = "RSA"
    private val AES_ALGORITHM = "AES"
    private val KEYSTORE_NAME = "AndroidKeyStore"
    private val CIPHER_PROVIDER = "SC"
    protected open val AAD_TAG = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)?.toByteArray()
    protected val AES_KEY_LENGTH: Int = 128
    private val GCM_NONCE_LENGTH_BYTES = 12
    private val GCM_TAG_LENGTH_BITS = 128
    private val DELIMITER = "]"
    private val random = SecureRandom()

    protected open val keyStore: KeyStore by lazy {
        val keystore = KeyStore.getInstance(KEYSTORE_NAME)
        keystore.load(null)
        keystore
    }

    private fun getTransformationRSA(apiLevel: Int): String {
        when (apiLevel) {
            in Build.VERSION_CODES.BASE..Build.VERSION_CODES.LOLLIPOP_MR1 -> return "RSA/ECB/PKCS1Padding"
            else -> return "RSA/ECB/OAEPWithSHA-512AndMGF1Padding"
        }
    }

    protected open val AES_KEY: ByteArray by lazy {
        val key: ByteArray
        if (!keyStore.containsAlias(alias) || !secretKeyFile.exists()) {
            key = generateAESKey(AES_KEY_LENGTH).encoded
            val algorithm = getTransformationRSA(Build.VERSION.SDK_INT)
            encryptBytesIntoFileRSA(key, secretKeyFile, algorithm, RSA_KEY.public)
        } else {
            val source = Okio.buffer(Okio.source(secretKeyFile))
            val encryptedText = source.readUtf8()
            val fields = encryptedText.split(DELIMITER)

            if (fields.size != 2) {
                throw RuntimeException("RSA Format Incorrect")
            }

            val binary = Base64.decode(fields[0], Base64.DEFAULT)
            val algorithm = String(Base64.decode(fields[1], Base64.DEFAULT))
            key = decryptFileIntoBytesRSA(binary, algorithm, RSA_KEY.private)

            performRSAAlgorithmUpgrade(key, algorithm)
        }
        key
    }

    protected open val RSA_KEY: KeyPair by lazy {
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
        val iv = generateIv(GCM_NONCE_LENGTH_BYTES)
        val ivParams = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)

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
        val ivParams = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParams)
        cipher.updateAAD(AAD_TAG)

        val plainText = cipher.doFinal(binary)
        return String(plainText)
    }

    @Throws(Exception::class)
    private fun encryptBytesIntoFileRSA(data: ByteArray, file: File, algorithm: String, publicKey: PublicKey) {
        file.parentFile.mkdirs()
        file.createNewFile()
        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)

        val plainText = cipher.doFinal(data)
        val base64Text = Base64.encodeToString(plainText, Base64.DEFAULT)
        val base64algorithm = Base64.encodeToString(algorithm.toByteArray(), Base64.DEFAULT)
        val finalString = "$base64Text$DELIMITER$base64algorithm"

        val inputStream = ByteArrayInputStream(finalString.toByteArray())
        val from = Okio.source(inputStream)
        val to = Okio.buffer(Okio.sink(file))
        to.writeAll(from)
        to.close()
    }

    @Throws(Exception::class)
    private fun decryptFileIntoBytesRSA(byteArray: ByteArray, algorithm: String, privateKey: PrivateKey): ByteArray {
        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val decrypted = cipher.doFinal(byteArray)

        return decrypted
    }

    @VisibleForTesting
    protected fun generateAESKey(outputKeyLength: Int): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM)
        keyGenerator.init(outputKeyLength)
        return keyGenerator.generateKey()
    }

    protected open fun generateRSAKey(): KeyPair {
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

    protected open fun getPublicRSAKey(alias: String) : PublicKey {
        val privateKeyEntry = keyStore.getEntry(alias, null) as KeyStore.PrivateKeyEntry
        return privateKeyEntry.certificate.publicKey
    }

    protected open fun getPrivateRSAKey(alias: String) : PrivateKey {
        val privateKeyEntry = keyStore.getEntry(alias, null) as KeyStore.PrivateKeyEntry
        return privateKeyEntry.privateKey
    }

    @VisibleForTesting
    protected fun generateIv(length: Int): ByteArray {
        val b = ByteArray(length)
        random.nextBytes(b)
        return b
    }

    protected open fun getAESCipher() : Cipher {
        return Cipher.getInstance(TRANSFORMATION_AES, CIPHER_PROVIDER)
    }

    /** Only call clear if a fatal error occurs **/
    fun clear() {
        secretKeyFile.delete()
        if (keyStore.containsAlias(alias)) {
            keyStore.deleteEntry(alias)
        }
    }

    private fun performRSAAlgorithmUpgrade(aesKey: ByteArray, algorithm: String) {
        if (algorithm != getTransformationRSA(Build.VERSION.SDK_INT)) {
            val algorithm = getTransformationRSA(Build.VERSION.SDK_INT)
            encryptBytesIntoFileRSA(aesKey, secretKeyFile, algorithm, RSA_KEY.public)
        }
    }
}