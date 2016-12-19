package com.expedia.bookings.utils

import android.content.Context
import okio.Okio
import org.bouncycastle.jce.X509Principal
import org.bouncycastle.x509.X509V3CertificateGenerator
import org.joda.time.LocalDate
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.cert.X509Certificate
import java.security.spec.RSAKeyGenParameterSpec
import javax.crypto.Cipher


class TestEncryptionUtil(context: Context, secretKeyFile: File, alias: String, useRSA: Boolean) : EncryptionUtil(context, secretKeyFile, alias) {
    private val password = "password".toCharArray()
    private val filename = "keyStoreName"
    override val AAD_TAG = "Android".toByteArray()

    override val keyStore: KeyStore by lazy {
        val keystore = KeyStore.getInstance("PKCS12", "SunJSSE")
        var fis: FileInputStream? = null
        try {
            val file = context.getFileStreamPath(filename)
            if (file.exists()) {
                fis = FileInputStream(file)
                keystore.load(fis, password)
            } else {
                keystore.load(null)
            }
        } finally {
            fis?.close()
        }
        keystore
    }

    override val AES_KEY: ByteArray by lazy {
        val key: ByteArray
        if (useRSA) {
            key = super.AES_KEY
        } else {
            if (!secretKeyFile.exists()) {
                key = generateAESKey(AES_KEY_LENGTH).encoded
                writeBytes(key, secretKeyFile)
            } else {
                key = readBytes(secretKeyFile)
            }
        }
        key
    }

    override val RSA_KEY: KeyPair by lazy {
        val key = if (!keyStore.containsAlias(alias)) {
            val keyPair = generateRSAKey()
            keyStore.setKeyEntry(alias, keyPair.private, password, arrayOf(generateCertificate(keyPair)))
            var fos: FileOutputStream? = null
            try {
                val file = context.getFileStreamPath(filename)
                file.parentFile.mkdir()
                file.createNewFile()
                fos = FileOutputStream(file)
                keyStore.store(fos, password)
            } finally {
                fos?.close()
            }
            keyPair
        } else {
            KeyPair(getPublicRSAKey(alias), getPrivateRSAKey(alias))
        }
        key
    }

    private fun generateCertificate(keyPair: KeyPair): X509Certificate {
        val cert = X509V3CertificateGenerator()
        cert.setSerialNumber(BigInteger.valueOf(1))
        cert.setSubjectDN(X509Principal("CN=localhost"))
        cert.setIssuerDN(X509Principal("CN=localhost"))
        cert.setPublicKey(keyPair.public)
        cert.setNotBefore(LocalDate.now().toDate())
        cert.setNotAfter(LocalDate.now().plusYears(25).toDate())
        cert.setSignatureAlgorithm("SHA1WithRSAEncryption")
        return cert.generate(keyPair.private)
    }

    override fun getPublicRSAKey(alias: String): PublicKey {
        val cert = keyStore.getCertificate(alias)
        val publicKey = cert.publicKey
        return publicKey
    }

    override fun getPrivateRSAKey(alias: String): PrivateKey {
        val privateKeyEntry = keyStore.getKey(alias, password) as PrivateKey
        return privateKeyEntry
    }

    override fun generateRSAKey(): KeyPair {
        val spec = RSAKeyGenParameterSpec(4096, RSAKeyGenParameterSpec.F4)
        val generator = KeyPairGenerator.getInstance(RSA_ALGORITHM)
        generator.initialize(spec)
        return generator.generateKeyPair()
    }

    override fun getAESCipher(): Cipher {
        return Cipher.getInstance(TRANSFORMATION_AES)
    }

    private fun writeBytes(byteArray: ByteArray, file: File) {
        file.parentFile.mkdirs()
        file.createNewFile()
        val inputStream = ByteArrayInputStream(byteArray)
        val from = Okio.source(inputStream)
        val to = Okio.buffer(Okio.sink(file))
        to.writeAll(from)
        to.close()
    }

    private fun readBytes(file: File): ByteArray {
        val source = Okio.buffer(Okio.source(file))
        val byteArray = source.readByteArray()
        source.close()
        return byteArray
    }

}