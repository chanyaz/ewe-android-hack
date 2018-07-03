package com.expedia.layouttestandroid.helper

import android.annotation.SuppressLint
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object StorageHelper {

    private val COMPRESSION_QUALITY = 100
    private val DEFAULT_SDCARD_DIRECTORY = "layoutTests"

    @Throws(IOException::class)
    fun storeBitmap(bitmap: Bitmap, fileName: String, packageName: String, testClass: String): File {
        val file = File(getDirectory(packageName, testClass), "$fileName.png")
        val outputStream = FileOutputStream(file)
        outputStream.flush()
        bitmap.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY, outputStream)
        outputStream.flush()
        outputStream.close()
        setWorldReadable(file)
        return file
    }

    @Throws(IOException::class)
    fun storeMetaData(metaData: String, fileName: String, packageName: String, testClass: String): File {
        val file = File(getDirectory(packageName, testClass), fileName)
        val outputStream = FileOutputStream(file)
        outputStream.flush()
        outputStream.write(metaData.toByteArray())
        outputStream.flush()
        outputStream.close()
        setWorldReadable(file)
        return file
    }

    fun cleanup(packageName: String, testClass: String) {
        val directory = getDirectory(packageName, testClass)
        if (!directory.exists()) {
            // We probably failed to even create it, so nothing to clean up
            return
        }
        for (s in directory.list()) {
            File(directory, s).delete()
        }
    }

    private fun getDirectory(packageName: String, testClass: String): File {
        val externalStorage = System.getenv("EXTERNAL_STORAGE") ?: throw RuntimeException("No \$EXTERNAL_STORAGE has been set on the device, please report this bug!")

        File("$externalStorage/$DEFAULT_SDCARD_DIRECTORY").mkdir()
        val parent = "$externalStorage/$DEFAULT_SDCARD_DIRECTORY/$packageName"
        val child = "$parent/$testClass"

        File(parent).mkdirs()

        val dir = File(child)
        dir.mkdir()

        if (!dir.exists()) {
            throw RuntimeException(
                    "Failed to create the directory for screenshots. Is your sdcard directory read-only?")
        }

        setWorldWriteable(dir)
        return dir
    }

    @SuppressLint("SetWorldWritable")
    private fun setWorldWriteable(dir: File) {
        // Context.MODE_WORLD_WRITEABLE has been deprecated, so let's
        // manually set this
        dir.setWritable(/* writeable = */true, /* ownerOnly = */ false)
    }

    @SuppressLint("setWorldReadable")
    private fun setWorldReadable(dir: File) {
        // Context.MODE_WORLD_WRITEABLE has been deprecated, so let's
        // manually set this
        dir.setReadable(/* readable = */true, /* ownerOnly = */ false)
    }
}
