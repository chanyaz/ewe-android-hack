package com.expedia.account.test

import com.expedia.account.data.PartialUser
import org.hamcrest.BaseMatcher
import org.hamcrest.Description

class RecaptchaMatchers {

    companion object {
        @JvmStatic
        fun hasRecaptchaToken(expectedToken: String) = PartialUserRecaptchaTokenMatcher(expectedToken)
    }

    class PartialUserRecaptchaTokenMatcher(private val expectedToken: String) : BaseMatcher<PartialUser>() {
        override fun describeTo(description: Description) {
            description.appendText("has recaptcha token '$expectedToken'")
        }

        override fun matches(o: Any): Boolean {
            return (o as PartialUser).recaptchaResponseToken == expectedToken
        }
    }
}
