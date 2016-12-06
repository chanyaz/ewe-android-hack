package com.expedia.bookings.utils

import android.content.Context
import org.bouncycastle.jce.X509Principal
import org.bouncycastle.x509.X509V3CertificateGenerator
import org.joda.time.LocalDate
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


class TestEncryptionUtil(context: Context, secretKeyFileOld: File, secretKeyFile: File, alias: String) : EncryptionUtil(context, secretKeyFileOld, secretKeyFile, alias) {
    private val password = "password".toCharArray()
    private val filename = "keyStoreName"

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

    private fun generateCertificate(keyPair: KeyPair) : X509Certificate {
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

    override fun getPublicRSAKey(alias: String) : PublicKey {
        val cert = keyStore.getCertificate(alias)
        val publicKey = cert.publicKey
        return publicKey
    }

    override fun getPrivateRSAKey(alias: String) : PrivateKey {
        val privateKeyEntry = keyStore.getKey(alias, password) as PrivateKey
        return privateKeyEntry
    }

    override fun generateRSAKey(): KeyPair {
        val spec = RSAKeyGenParameterSpec(4096, RSAKeyGenParameterSpec.F4)
        val generator = KeyPairGenerator.getInstance(RSA_ALGORITHM)
        generator.initialize(spec)
        return generator.generateKeyPair()
    }

    override fun getAESCipher() : Cipher {
        return Cipher.getInstance(TRANSFORMATION_AES)
    }
}