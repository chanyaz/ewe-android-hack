package com.expedia.account.test

import com.expedia.account.data.PartialUser
import org.mockito.ArgumentMatcher

class RecaptchaMatchers {

    companion object {
        @JvmStatic
        fun hasRecaptchaToken(expectedToken: String) = PartialUserRecaptchaTokenMatcher(expectedToken)
    }

    class PartialUserRecaptchaTokenMatcher(private val expectedToken: String) : ArgumentMatcher<PartialUser> {
        override fun matches(argument: PartialUser?): Boolean {
            return argument?.recaptchaResponseToken == expectedToken
        }
    }
}
