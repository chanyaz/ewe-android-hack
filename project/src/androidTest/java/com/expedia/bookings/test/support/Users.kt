package com.expedia.bookings.test.support

import android.support.test.InstrumentationRegistry.getInstrumentation
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mobiata.android.util.IoUtils
import java.io.IOException

class Users {
    private var userList: ArrayList<User>
    private val resourceFile = "users.json"

    init {
        val gson = Gson()
        val type = object : TypeToken<Map<String, @kotlin.jvm.JvmSuppressWildcards ArrayList<User>>>() {}.type
        val map = gson.fromJson<Map<String, ArrayList<User>>>(readUsersFromAssets(), type)
        userList = map["users"]!!
    }

    private fun readUsersFromAssets(): String {
        var usersResource = "{}"

        try {
            usersResource = IoUtils.convertStreamToString(
                    getInstrumentation().context.assets.open(resourceFile))
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return usersResource
    }

    fun findUser(field: String, value: String): User? {
        return findUser(mapOf<String, String>(field to value))
    }

    fun findUser(fields: Map<String, String>): User? {
        val fieldList = fields.keys.toList()

        for (user in userList) {
            val userFragment = User()

            //Set up a user fragment to compare against, using provided fields.
            for (field in fieldList) {
                userFragment.javaClass
                        .getDeclaredMethod("set" + field.capitalize(), String::class.java)
                        .invoke(userFragment, fields.get(field))
            }

            // return a real user if a fragment matches up to the user
            if (user.equalsToFragment(userFragment)) {
                return user
            }
        }

        return null
    }
}
