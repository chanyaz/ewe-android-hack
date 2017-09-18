package com.expedia.bookings.data.user

import android.content.Context
import com.expedia.bookings.data.Db

open class UserSource(val context: Context) {
    open var user: User?
        get() = Db.getUser()
        set(value) = Db.setUser(value)

    open fun loadUser() {
        Db.loadUser(context)
    }
}
