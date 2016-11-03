package com.expedia.bookings.utils

import android.content.Context
import okio.Okio
import java.io.ByteArrayInputStream
import java.io.File
import javax.crypto.Cipher

class TestEncryptionUtil(context: Context, secretKeyFile: File, alias: String) : EncryptionUtil(context, secretKeyFile, alias) {

    override val AES_KEY: ByteArray by lazy {
        val key: ByteArray
        if (secretKeyFile.exists()) {
            key = readBytes(secretKeyFile)
        } else {
            key = generateAESKey(AES_KEY_LENGTH).encoded
            writeBytes(key, secretKeyFile)
        }
        key
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

    override fun getAESCipher() : Cipher {
        return Cipher.getInstance(TRANSFORMATION_AES)
    }

    override fun clear() {
        secretKeyFile.delete()
    }
}