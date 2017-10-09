package com.expedia.bookings.data.user

import android.content.Context
import com.expedia.bookings.activity.ExpediaBookingApp
import com.mobiata.android.FileCipher
import com.mobiata.android.Log
import com.mobiata.android.util.IoUtils
import org.json.JSONException
import org.json.JSONObject
import java.io.FileNotFoundException

open class UserSource(val context: Context,
                      private val fileCipher: FileCipher = FileCipher(PASSWORD)) {
    private companion object {
        val PASSWORD = "M2MBDdEjbFTXTgNynBY2uvMPcUd8g3k9"
        val SAVED_INFO_FILENAME = "user.dat"
    }

    open var user: User? = null
        get() {
            if (field == null) {
                try {
                    loadUser()
                } catch (e: Exception) {
                    return null
                }
            }

            return field
        }
        set(value) {
            field = value
            saveUser()
        }

    @Throws
    open fun loadUser() {
        Log.d("Loading saved user.")

        val file = context.getFileStreamPath(SAVED_INFO_FILENAME)

        if (!file.exists()) {
            throw FileNotFoundException("The file user.dat doesn't exist.")
        }

        if (!fileCipher.isInitialized) {
            throw Exception("FileCipher unable to initialize to decrypt user.dat")
        }

        val results = fileCipher.loadSecureData(file)

        if (results.isNullOrEmpty()) {
            throw Exception("Contents of decrypted user.dat file are null or empty.")
        }

        try {
            user = User(JSONObject(results))
        }
        catch (e: JSONException) {
            Log.e("Could not restore saved user info.", e)
            throw e
        }
    }

    @Throws
    open fun saveUser() {
        Log.d("Saving user.")

        val data = user?.toJson()?.toString()
        val pathToSave = context.getFileStreamPath(SAVED_INFO_FILENAME)

        if (data == null) {
            pathToSave.delete()
        }
        else {
            if (ExpediaBookingApp.isRobolectric()) {
                try {
                    IoUtils.writeStringToFile(SAVED_INFO_FILENAME, data, context)
                } catch (e: Exception) {
                    throw IllegalStateException("Unable to save temp user.dat file.")
                }
            } else if (fileCipher.isInitialized) {
                fileCipher.saveSecureData(pathToSave, data)
            }
        }
    }
}
