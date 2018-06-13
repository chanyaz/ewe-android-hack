package com.expedia.bookings.utils

import android.content.Context
import java.io.IOException

class FileReadUtils {

    companion object {
        fun getJsonStringFromFile(context: Context, fileName: String = "api/trips/tns_notification_payload.json"): String {
            var json: String?
            val assertManager = context.resources.assets
            try {
                val openedFile = assertManager.open(fileName)
                val size = openedFile.available()
                val buffer = ByteArray(size)
                openedFile.read(buffer)
                openedFile.close()
                json = String(buffer)
                return json
            } catch (ex: IOException) {
                ex.printStackTrace()
                return ""
            }
        }
    }
}
