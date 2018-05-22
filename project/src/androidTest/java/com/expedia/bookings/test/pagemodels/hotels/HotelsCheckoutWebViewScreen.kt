package com.expedia.bookings.test.pagemodels.hotels

import android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.isJavascriptEnabled
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.web.sugar.Web.onWebView
import android.support.test.espresso.web.webdriver.DriverAtoms.findElement
import android.support.test.espresso.web.webdriver.DriverAtoms.webScrollIntoView
import android.support.test.espresso.web.webdriver.Locator
import android.support.test.uiautomator.By
import android.support.test.uiautomator.UiObject2
import android.support.test.uiautomator.Until
import com.expedia.bookings.R
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.Common.device
import com.expedia.bookings.test.espresso.Common.isOneOfUiObjectsPresent
import org.hamcrest.Matchers.allOf
import java.util.concurrent.TimeUnit

object HotelsCheckoutWebViewScreen {

    private val webView = allOf(
            isDescendantOfA(withId(R.id.web_checkout_view)),
            withId(R.id.web_view),
            isJavascriptEnabled(),
            isDisplayed()
    )

    fun waitForViewToLoad() {
        //First we wait for the webview to begin loading
        Common.waitForOneOfViewsToDisplay(
                arrayListOf(By.res("page-header"), By.res("trip-summary")),
                30,
                TimeUnit.SECONDS
        )

        //once the webview HTML has loaded, a progress indicator (loading spinner) appears until
        // JS and all API calls have been resolved. We wait for the indicator to disappear.
        val loadingIndicators = arrayListOf(
                By.res("webview_progress_view"),
                By.res("webview_loading_screen")
        )
        val timer = System.currentTimeMillis()
        while (isOneOfUiObjectsPresent(loadingIndicators) && System.currentTimeMillis() - timer < 30000) {
            Common.delay(1)
        }
    }

    object Preferences {
        private val byRoomPreferencesContainer = By.res("preferences")

        fun roomPreferencesContainer() = device.findObject(byRoomPreferencesContainer)

        fun contactNameField(): UiObject2 {
            if (roomPreferencesContainer().findObject(By.desc("Contact name")) != null) {
                return roomPreferencesContainer()
                        .findObject(By.desc("Contact name")).parent
                        .findObject(By.clazz("android.widget.EditText"))
            } else {
                return roomPreferencesContainer()
                        .findObject(By.text("Contact name")).parent
                        .findObject(By.clazz("android.widget.EditText"))
            }
        }

        fun scrollToRoomPreferences() {
            onWebView(webView)
                    .withElement(findElement(Locator.ID, "cko-form"))
                    .withContextualElement(findElement(Locator.ID, "preferences"))
                    .perform(webScrollIntoView())
            device.wait(Until.findObject(byRoomPreferencesContainer), 5000)
        }

        fun enterContactName(contactName: String) {
            Common.delay(1)
            contactNameField().text = contactName
            Common.genericCloseKeyboard()
        }

        fun verifyContactNameFieldValue(expected: String) {
            assert(contactNameField().text == expected)
        }
    }

    object Insurance {
        val byInsuranceContainer = By.res("insurance")
        val byInsuranceDecline = By.res("no_insurance")
        val byTripNotProtected = By.desc("Your trip is not protected.")

        fun insuranceContainer() = device.findObject(byInsuranceContainer)

        fun scrollToInsuranceContainer() {
            onWebView(webView)
                    .withNoTimeout()
                    .withElement(findElement(Locator.CLASS_NAME, "ins-conditions"))
                    .perform(webScrollIntoView())
            device.wait(Until.findObject(byInsuranceContainer), 5000)
        }

        fun clickDeclineInsurance() {
            insuranceContainer()
                    .wait(Until.findObject(byInsuranceDecline), 5000)
                    .click()
            device.wait(Until.findObject(byTripNotProtected), 5000)
        }

        fun verifyTripNotProtected() {
            assert(device.findObject(byTripNotProtected) != null)
        }
    }
}
