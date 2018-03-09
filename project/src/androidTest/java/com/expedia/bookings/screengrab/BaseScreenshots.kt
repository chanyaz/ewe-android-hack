package com.expedia.bookings.screengrab

import android.content.Intent
import android.support.annotation.IdRes
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.UiController
import android.support.test.espresso.ViewAction
import android.support.test.espresso.action.CoordinatesProvider
import android.support.test.espresso.action.GeneralLocation
import android.support.test.espresso.action.GeneralSwipeAction
import android.support.test.espresso.action.Press
import android.support.test.espresso.action.Swipe
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.rule.ActivityTestRule
import android.support.test.uiautomator.UiDevice
import android.view.View
import android.view.ViewConfiguration
import android.widget.EditText
import com.expedia.bookings.R
import com.expedia.bookings.activity.RouterActivity
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.otto.Events
import com.expedia.bookings.test.BuildConfig
import com.expedia.bookings.test.Settings
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.EspressoUtils
import com.expedia.bookings.test.pagemodels.common.LaunchScreen
import com.expedia.bookings.test.pagemodels.hotels.HotelResultsScreen
import com.expedia.bookings.utils.Ui
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.junit.Assume
import org.junit.ClassRule
import org.junit.Rule
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy
import tools.fastlane.screengrab.locale.LocaleTestRule
import tools.fastlane.screengrab.locale.LocaleUtil
import java.util.Locale
import java.util.concurrent.TimeUnit

abstract class BaseScreenshots {
    var device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @Rule @JvmField
    var activityRule = ActivityTestRule(RouterActivity::class.java)

    protected val lasVegasCity = "Las Vegas, NV"
    protected val bangkokCity = "Bangkok, Thailand"
    protected val londonCity = "London, England"

    protected val ARGENTINA = Locale("es", "AR")
    protected val AUSTRALIA = Locale("en", "AU")
    protected val AUSTRIA = Locale("de", "AT")
    protected val BELGIUM_DUTCH = Locale("nl", "BE")
    protected val BELGIUM_FRENCH = Locale("fr", "BE")
    protected val BRAZIL = Locale("pt", "BR")
    protected val CANADA_ENGLISH = Locale("en", "CA")
    protected val CANADA_FRENCH = Locale("fr", "CA")
    protected val DENMARK = Locale("da", "DK")
    protected val FINLAND = Locale("fi", "FI")
    protected val FRANCE = Locale("fr", "FR")
    protected val GERMANY = Locale("de", "DE")
    protected val HONG_KONG_ENGLISH = Locale("en", "HK")
    protected val HONG_KONG_CHINESE_TRADITIONAL = Locale("zh", "HK")
    protected val HONG_KONG_CHINESE_SIMPLIFIED = Locale("zh", "CN")
    protected val INDIA = Locale("en", "IN")
    protected val INDONESIA = Locale("id", "ID")
    protected val IRELAND = Locale("en", "IE")
    protected val ITALY = Locale("it", "IT")
    protected val JAPAN_ENGLISH = Locale("en", "JP")
    protected val JAPAN_JAPANESE = Locale("ja", "JP")
    protected val KOREA_ENGLISH = Locale("en", "KR")
    protected val KOREA_KOREAN = Locale("ko", "KR")
    protected val MALAYSYA = Locale("en", "MY")
    protected val MEXICO = Locale("es", "MX")
    protected val NETHERLANDS = Locale("nl", "NL")
    protected val NEW_ZELAND = Locale("en", "NZ")
    protected val NORWAY = Locale("nb", "NO")
    protected val PHILIPPINES = Locale("en", "PH")
    protected val SINGAPORE = Locale("en", "SG")
    protected val SPAIN = Locale("es", "ES")
    protected val SWEDEN = Locale("sv", "SE")
    protected val SWITZERLAND_FRENCH = Locale("fr", "CH")
    protected val SWITZERLAND_GERMAN = Locale("de", "CH")
    protected val THAILAND_ENGLISH = Locale("en", "TH")
    protected val THAILAND_THAI = Locale("th", "TH")
    protected val TAIWAN_ENGLISH = Locale("en", "TW")
    protected val TAIWAN_CHINESE = Locale("zh", "TW")
    protected val UK = Locale("en", "GB")
    protected val USA_ENGLISH = Locale("en", "US")
    protected val USA_SPANISH = Locale("es", "US")
    protected val USA_CHINESE = Locale("zh", "US")
    protected val VIETNAM = Locale("vi", "VN")

    private val pointOfSaleForLocale = mapOf(
            ARGENTINA to PointOfSaleId.ARGENTINA,
            AUSTRALIA to PointOfSaleId.AUSTRALIA,
            AUSTRIA to PointOfSaleId.AUSTRIA,
            BELGIUM_DUTCH to PointOfSaleId.BELGIUM,
            BELGIUM_FRENCH to PointOfSaleId.BELGIUM,
            BRAZIL to PointOfSaleId.BRAZIL,
            CANADA_ENGLISH to PointOfSaleId.CANADA,
            CANADA_FRENCH to PointOfSaleId.CANADA,
            DENMARK to PointOfSaleId.DENMARK,
            FINLAND to PointOfSaleId.FINLAND,
            FRANCE to PointOfSaleId.FRANCE,
            GERMANY to PointOfSaleId.GERMANY,
            HONG_KONG_ENGLISH to PointOfSaleId.HONG_KONG,
            HONG_KONG_CHINESE_TRADITIONAL to PointOfSaleId.HONG_KONG,
            HONG_KONG_CHINESE_SIMPLIFIED to PointOfSaleId.HONG_KONG,
            INDIA to PointOfSaleId.INDIA,
            INDONESIA to PointOfSaleId.INDONESIA,
            IRELAND to PointOfSaleId.IRELAND,
            ITALY to PointOfSaleId.ITALY,
            JAPAN_ENGLISH to PointOfSaleId.JAPAN,
            JAPAN_JAPANESE to PointOfSaleId.JAPAN,
            KOREA_ENGLISH to PointOfSaleId.SOUTH_KOREA,
            KOREA_KOREAN to PointOfSaleId.SOUTH_KOREA,
            MALAYSYA to PointOfSaleId.MALAYSIA,
            MEXICO to PointOfSaleId.MEXICO,
            NETHERLANDS to PointOfSaleId.NETHERLANDS,
            NEW_ZELAND to PointOfSaleId.NEW_ZEALND,
            NORWAY to PointOfSaleId.NORWAY,
            PHILIPPINES to PointOfSaleId.PHILIPPINES,
            SINGAPORE to PointOfSaleId.SINGAPORE,
            SPAIN to PointOfSaleId.SPAIN,
            SWEDEN to PointOfSaleId.SWEDEN,
            SWITZERLAND_FRENCH to PointOfSaleId.SWITZERLAND,
            SWITZERLAND_GERMAN to PointOfSaleId.SWITZERLAND,
            THAILAND_ENGLISH to PointOfSaleId.THAILAND,
            THAILAND_THAI to PointOfSaleId.THAILAND,
            TAIWAN_ENGLISH to PointOfSaleId.TAIWAN,
            TAIWAN_CHINESE to PointOfSaleId.TAIWAN,
            UK to PointOfSaleId.UNITED_KINGDOM,
            USA_ENGLISH to PointOfSaleId.UNITED_STATES,
            USA_SPANISH to PointOfSaleId.UNITED_STATES,
            USA_CHINESE to PointOfSaleId.UNITED_STATES,
            VIETNAM to PointOfSaleId.VIETNAM
    )

    companion object {
        @ClassRule @JvmField
        val localeTestRule = LocaleTestRule()
    }

    open fun before() {
        Assume.assumeTrue(BuildConfig.IS_SCREENSHOT_BUILD)
        setupDemoMode()
        Common.setPOS(pointOfSaleForLocale[LocaleUtil.getTestLocale()])
        Settings.setServer("Production")

        waitForLaunchScreenReady()
        Events.post(Events.PhoneLaunchOnPOSChange())
        Common.delay(1)
        Ui.hideKeyboard(activityRule.activity)
        Common.delay(1)

        Screengrab.setDefaultScreenshotStrategy(UiAutomatorScreenshotStrategy())
    }

    protected fun waitForHotelResultsToLoad() {
        HotelResultsScreen.waitForResultsLoaded(30)
        Common.delay(3)
    }

    protected fun typeTextViaReplace(str: String): ViewAction {
        return EmulateTypeTextAction(str)
    }

    protected fun tinySwipeDown(): ViewAction {
        return GeneralSwipeAction(Swipe.FAST, GeneralLocation.CENTER,
                CoordinatesProvider { view ->
                    val xy = GeneralLocation.CENTER.calculateCoordinates(view)
                    xy[1] = xy[1] + (3 * ViewConfiguration.get(view.context).scaledTouchSlop)
                    xy
                },
                Press.FINGER)
    }

    protected fun waitForViewNotYetInLayout(@IdRes viewId: Int, seconds: Int = 10) {
        EspressoUtils.waitForViewNotYetInLayoutToDisplay(ViewMatchers.withId(viewId), seconds.toLong(), TimeUnit.SECONDS)
    }

    private fun setupDemoMode() {
        sendDemoModeCommand("enter", emptyArray())
        sendDemoModeCommand("battery", arrayOf(Pair("plugged", "false")))
        sendDemoModeCommand("battery", arrayOf(Pair("level", "100")))
        sendDemoModeCommand("network", arrayOf(Pair("wifi", "show"), Pair("level", "4")))
        sendDemoModeCommand("network", arrayOf(Pair("mobile", "show"), Pair("datatype", "none"), Pair("level", "4")))
        sendDemoModeCommand("notifications", arrayOf(Pair("visible", "false")))
        sendDemoModeCommand("clock", arrayOf(Pair("hhmm", "0900")))
    }

    private fun sendDemoModeCommand(command: String, extras: Array<Pair<String, String>>) {
        val intent = Intent("com.android.systemui.demo")
        intent.putExtra("command", command)
        for (pair in extras) {
            intent.putExtra(pair.first, pair.second)
        }
        activityRule.activity.sendBroadcast(intent)
    }

    private fun waitForLaunchScreenReady() {
        waitForViewNotYetInLayout(R.id.launch_toolbar)
        LaunchScreen.waitForLOBHeaderToBeDisplayed()
        device.waitForIdle(10000)
        Common.delay(1)
    }

    private class EmulateTypeTextAction(private val stringToType: String) : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return Matchers.allOf(ViewMatchers.isDisplayed(), ViewMatchers.isAssignableFrom(EditText::class.java))
        }

        override fun getDescription(): String {
            return "emulate type text"
        }

        override fun perform(uiController: UiController?, view: View?) {
            for (i in 1..stringToType.length) {
                (view as EditText).setText(stringToType.substring(0, i))
                uiController?.loopMainThreadUntilIdle()
            }
        }
    }
}
