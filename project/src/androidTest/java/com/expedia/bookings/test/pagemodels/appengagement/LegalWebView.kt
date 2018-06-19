package com.expedia.bookings.test.pagemodels.appengagement

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withContentDescription
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.uiautomator.By
import android.support.test.uiautomator.Until
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.Common.device
import junit.framework.TestCase.assertTrue
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf

object LegalWebView {
    private val byGdprBanner = By.res( "gdpr-consent-banner")
    private val byGdprBannerClose = By.res( "gdpr-banner-close")
    private val toolbar = withId(R.id.toolbar)
    private val toolbarCloseButton = allOf(isDescendantOfA(toolbar), withContentDescription(R.string.acct__Toolbar_nav_close_icon_cont_desc))

    fun waitUntilLoaded() {
        //First we wait for the webview to begin loading
        device.wait(Until.findObject(By.res("header-logo")), 30000)

        //once the webview HTML has loaded, a progress indicator (loading spinner) appears until
        // JS and all API calls have been resolved. We wait for the indicator to disappear.
        device.wait(Until.gone(By.clazz("android.widget.ProgressBar")), 30000)
    }

    fun verifyToolbarTitle(toolbarTitle: ToolbarTitle) {
        onView(toolbarTitle.matcher).check(matches(isDisplayed()))
    }

    fun verifyGDPRBannerPresence(expectedPresence: Boolean) {
        var actualPresence: Boolean

        try {
            //If this errors out, then the banner is not present.
            actualPresence = device.findObject(byGdprBanner).isEnabled &&
                    device.findObject(byGdprBannerClose).isClickable &&
                    device.findObject(byGdprBannerClose).isEnabled &&
                    device.findObject(byGdprBannerClose).isFocusable
        } catch (ignored: Exception) {
            actualPresence = false
        }

        assertTrue(expectedPresence.equals(actualPresence))
    }

    fun closeTheView() {
        onView(toolbarCloseButton).perform(click())
    }

    enum class ToolbarTitle(val matcher: Matcher<View>) {
        TERMS_OF_USE(allOf(isDescendantOfA(toolbar), withText(R.string.info_label_terms_of_use))),
        PRIVACY_POLICY(allOf(isDescendantOfA(toolbar), withText(R.string.privacy_policy))),
        TERMS_AND_CONDITIONS(allOf(isDescendantOfA(toolbar), withText(R.string.terms_and_conditions)))
    }
}
