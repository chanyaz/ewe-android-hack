package com.mobiata.mocke3

import com.expedia.bookings.services.DateTimeTypeAdapter
import com.google.gson.GsonBuilder
import org.joda.time.DateTime
import java.io.File

fun <T> mockObject(clazz: Class<T>, mockName: String, params: Map<String, String>? = null): T? {
    return try {
        val jsonString = getJsonStringFromMock(mockName, params)
        val gson = GsonBuilder()
                .registerTypeAdapter(DateTime::class.java, DateTimeTypeAdapter())
                .create()
        gson.fromJson(jsonString, clazz)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun getJsonStringFromMock(mockName: String, params: Map<String, String>?): String {
    var file = File("lib/mocked/templates")
    while (!file.exists()) {
        file = File("../" + file.path)
    }
    val opener = FileSystemOpener(file.canonicalPath)
    val jsonString = loadMockResponseAndReplaceTemplateParams(mockName, opener, params)
    return jsonString
}

object Mocker {
    @JvmStatic
    fun <T> loadMock(clazz: Class<T>, mockName: String, params: Map<String, String>): T? {
        return mockObject(clazz, mockName, params)
    }
}
