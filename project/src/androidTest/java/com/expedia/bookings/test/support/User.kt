package com.expedia.bookings.test.support

class User() {
    var email = ""
    var password = ""
    var tier = ""
    var type = ""

    constructor(email: String, password: String) : this() {
        this.email = email
        this.password = password
    }
    constructor(email: String, password: String, type: String) : this(email, password) {
        this.type = type
    }

    fun equals (otherUser: User): Boolean {
        return this == otherUser
    }

    fun equalsToFragment(userFragment: User): Boolean {
        for (field in this.javaClass.declaredFields) {
            if (field.get(userFragment) != "" &&
                    field.name != "this$0" &&
                    field.get(this) != field.get(userFragment)) {
                //Field Values don't match. Users are not the same.
                return false
            }
        }

        // All fields of userFragment matched the current user.
        return true
    }
}
