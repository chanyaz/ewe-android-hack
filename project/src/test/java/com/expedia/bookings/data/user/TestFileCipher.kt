package com.expedia.bookings.data.user

import com.mobiata.android.FileCipher
import com.mobiata.android.util.IoUtils
import org.robolectric.RuntimeEnvironment
import java.io.File
import kotlin.test.fail

class TestFileCipher(val password: String?, val results: String? = null) : FileCipher() {
    override fun isInitialized(): Boolean = !password.isNullOrEmpty()
    override fun loadSecureData(file: File?): String {
        if (results != null) {
            return results
        }

        try {
            return IoUtils.readStringFromFile("user.dat", RuntimeEnvironment.application)
        } catch (e: Exception) {
            fail(e.message)
        }
    }
}
