package com.expedia.bookings.test.support

import android.support.test.InstrumentationRegistry.getInstrumentation
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mobiata.android.util.IoUtils
import java.io.IOException

class Users {

	inner class User {
		var email = ""
		var password = ""
		var tier = ""

		constructor() {}
		constructor(email: String, password: String) {
			this.email = email
			this.password = password
		}

		constructor(email: String, password: String, tier: String) : this(email, password) {
			this.tier = tier
		}

		val user: User
			get() = this
	}

	companion object {
		private lateinit var userList: ArrayList<User>
		private val resourceFile = "users.json"

		fun findUser(field: String, value: String): User? {
			parseJSONtoUsersList()

			for (user in userList) {
				//We should ideally use reflection here. Something like user.getClass().getMethod("get"+field).invoke(null) == value
				//For now, since reflection isn't working, use a switch workaround
				when (field) {
					"email" -> if (user.email == value) {
						return user
					}
					"password" -> if (user.password == value) {
						return user
					}
					"tier" -> if (user.tier == value) {
						return user
					}
				}
			}

			return null
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

		private fun parseJSONtoUsersList() {
			val gson = Gson()
			val type = object : TypeToken<Map<String, @kotlin.jvm.JvmSuppressWildcards ArrayList<User>>>() {}.type
			val map = gson.fromJson<Map<String, ArrayList<User>>>(readUsersFromAssets(), type)
			userList = map["users"]!!
		}
	}
}
