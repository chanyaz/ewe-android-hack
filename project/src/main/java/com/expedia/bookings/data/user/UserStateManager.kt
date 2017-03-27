package com.expedia.bookings.data.user

import android.content.Context

class UserStateManager(val context: Context) {

    fun isUserAuthenticated(): Boolean {
        return User.isLoggedIn(context)
    }
}