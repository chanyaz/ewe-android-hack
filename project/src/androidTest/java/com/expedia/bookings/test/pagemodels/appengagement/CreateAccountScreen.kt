package com.expedia.bookings.test.pagemodels.appengagement

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.ViewActions.clickClickableSpan
import com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString

object CreateAccountScreen {
    val pageScrollView = withId(R.id.single_page_scrollview)
    val firstName = allOf(isDescendantOfA(withId(R.id.single_page_first_name)), withId(R.id.single_page_text_field))
    val lastName = allOf(isDescendantOfA(withId(R.id.single_page_last_name)), withId(R.id.single_page_text_field))
    val emailAddress = allOf(isDescendantOfA(withId(R.id.single_page_email_address)), withId(R.id.single_page_text_field))
    val createPassword = allOf(isDescendantOfA(withId(R.id.single_page_password)), withId(R.id.single_page_text_field))

    val termsOfUseCheckbox = allOf(isDescendantOfA(pageScrollView), withId(R.id.terms_of_use_checkbox))
    val termsOfUseLink = allOf(isDescendantOfA(pageScrollView), withId(R.id.terms_of_use_text),
            withText(containsString("Terms of Use")))
    val privacyPolicyLink = allOf(isDescendantOfA(pageScrollView), withId(R.id.terms_of_use_text),
            withText(containsString("Privacy Policy")))

    val expediaRewardsCheckbox = allOf(isDescendantOfA(pageScrollView), withId(R.id.enroll_in_loyalty_checkbox))
    val termsAndConditionsLink = allOf(isDescendantOfA(pageScrollView), withId(R.id.enroll_in_loyalty_text),
            withText(containsString("Terms and Conditions")))

    val travelDealsCheckbox = allOf(isDescendantOfA(pageScrollView), withId(R.id.agree_to_spam_checkbox))

    fun clickTermsOfUseCheckbox() {
        onView(termsOfUseCheckbox).perform(waitForViewToDisplay(), click())
    }
    fun clickTermsOfUseLink() {
        onView(termsOfUseLink).perform(clickClickableSpan("Terms of Use"))
    }
    fun clickPrivacyPolicyLink() {
        onView(privacyPolicyLink).perform(clickClickableSpan("Privacy Policy"))
    }
    fun clickTermsAndConditionsLink() {
        onView(termsAndConditionsLink).perform(clickClickableSpan("Terms and Conditions"))
    }
}
