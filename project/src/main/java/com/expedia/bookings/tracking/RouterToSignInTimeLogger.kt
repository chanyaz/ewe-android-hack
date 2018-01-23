package com.expedia.bookings.tracking

class RouterToSignInTimeLogger : TimeLogger(pageName = "Router.To.SignIn.Time") {

    var shouldGoToSignIn = false

    override fun clear() {
        super.clear()
        shouldGoToSignIn = false
    }
}
