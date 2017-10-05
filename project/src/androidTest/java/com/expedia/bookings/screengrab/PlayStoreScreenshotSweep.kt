package com.expedia.bookings.screengrab

import android.content.Intent
import android.support.annotation.IdRes
import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onData
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.UiController
import android.support.test.espresso.ViewAction
import android.support.test.espresso.action.CoordinatesProvider
import android.support.test.espresso.action.GeneralLocation
import android.support.test.espresso.action.GeneralSwipeAction
import android.support.test.espresso.action.Press
import android.support.test.espresso.action.Swipe
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.typeText
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import android.support.test.espresso.matcher.RootMatchers
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom
import android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.rule.ActivityTestRule
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewConfiguration
import android.widget.EditText
import com.expedia.bookings.R
import com.expedia.bookings.activity.RouterActivity
import com.expedia.bookings.data.LobInfo
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.otto.Events
import com.expedia.bookings.test.BuildConfig
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.CustomMatchers.airportDropDownEntryWithAirportCode
import com.expedia.bookings.test.espresso.EspressoUtils.waitForViewNotYetInLayoutToDisplay
import com.expedia.bookings.test.espresso.ViewActions
import com.expedia.bookings.test.pagemodels.hotels.HotelScreen
import com.expedia.bookings.test.pagemodels.lx.LXScreen
import com.expedia.bookings.test.pagemodels.common.LogInScreen
import com.expedia.bookings.test.pagemodels.common.LaunchScreen
import com.expedia.bookings.test.pagemodels.common.SearchScreen
import com.expedia.bookings.test.pagemodels.common.TripsScreen
import com.expedia.bookings.test.Settings
import com.expedia.bookings.utils.Ui
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.joda.time.LocalDate
import org.junit.Assume
import org.junit.Before
import org.junit.ClassRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy
import tools.fastlane.screengrab.locale.LocaleTestRule
import tools.fastlane.screengrab.locale.LocaleUtil
import java.util.HashMap
import java.util.Locale
import java.util.concurrent.TimeUnit

@RunWith(JUnit4::class)
class PlayStoreScreenshotSweep {
    @Rule @JvmField
    var activityRule = ActivityTestRule(RouterActivity::class.java)

    @Before
    fun before() {
        Assume.assumeTrue(BuildConfig.IS_SCREENSHOT_BUILD)
        setupDemoMode()
        Common.setPOS(pointOfSaleForLocale[LocaleUtil.getTestLocale()])

        if (BuildConfig.ITIN_SCREENSHOT_BUILD) {
            Settings.setMockModeEndPoint()
        } else {
            Settings.setServer("Production")
        }
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

    @Test
    @Throws(Throwable::class)
    fun takeScreenshots() {
        Screengrab.setDefaultScreenshotStrategy(UiAutomatorScreenshotStrategy())

        val hotelSearchCriteria = hotelSearchCriteriaForLocale[LocaleUtil.getTestLocale()]
        val flightSearchCriteria = flightSearchCriteriaForLocale[LocaleUtil.getTestLocale()]
        val lxSearchCriteria = lxSearchCriteriaForLocale[LocaleUtil.getTestLocale()]

        try {
            waitForLaunchScreenReady()
            Events.post(Events.PhoneLaunchOnPOSChange())
            Common.delay(1)
            Ui.hideKeyboard(activityRule.activity)
            Common.delay(1)

            if (BuildConfig.ITIN_SCREENSHOT_BUILD) {
                signInUser()
            }

            if (hotelSearchCriteria != null) {
                takeHotelScreenshotAndReturnToLaunchScreen(hotelSearchCriteria)
            }

            if (flightSearchCriteria != null) {
                takeMaterialFlightScreenshotAndReturnToLaunchScreen(flightSearchCriteria)
            }

            if (lxSearchCriteria != null) {
                takeLxScreenshotAndReturnToLaunchScreen(lxSearchCriteria)
            }

            //Use this for mock itins
            if (BuildConfig.ITIN_SCREENSHOT_BUILD) {
                takeItinScreens()
            }
        }
        catch (e: Throwable) {
             //shoot, something failed, but ignore so that the rest of the screens finish
        }
    }

    private fun signInUser() {
        LaunchScreen.tripsButton().perform(click())

        TripsScreen.clickOnLogInButton()

        LogInScreen.typeTextEmailEditText("deepanshu11madan@gmail.com")
        LogInScreen.typeTextPasswordEditText("fwefw")
        LogInScreen.clickOnLoginButton()
        Common.delay(1)

        LaunchScreen.shopButton().perform(click())
        LaunchScreen.waitForLOBHeaderToBeDisplayed()

        Screengrab.screenshot("launch")
        Common.delay(1)
    }

    //this runs on mock mode
    @Throws(Throwable::class)
    private fun takeItinScreens() {
        LaunchScreen.tripsButton().perform(click())

        TripsScreen.clickOnLogInButton()

        LogInScreen.typeTextEmailEditText("qa-ehcc@mobiata.com")
        LogInScreen.typeTextPasswordEditText("e3trefwfw")
        LogInScreen.clickOnLoginButton()
        onView(allOf(withId(R.id.summary_layout), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))).perform(ViewActions.waitForViewToDisplay())
        onView(allOf(withId(R.id.summary_layout), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))).perform(click())
        Common.delay(1)
        Screengrab.screenshot("itin")
    }

    @Throws(Throwable::class)
    private fun takeHotelScreenshotAndReturnToLaunchScreen(hotelSearchInfo: LocationSearchCriteria) {
        onView(allOf(withText(LobInfo.HOTELS.labelRes), isCompletelyDisplayed())).perform(click())

        waitForViewNotYetInLayout(R.id.search_src_text)

        SearchScreen.searchEditText().perform(typeTextViaReplace(hotelSearchInfo.searchString))
        Common.delay(1)
        SearchScreen.suggestionList().perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))
        Common.delay(1)

        val startDate = LocalDate.now().plusDays(90)
        val endDate = startDate.plusDays(5)
        SearchScreen.selectDates(startDate, endDate)

        SearchScreen.searchButton().perform(click())
        Common.delay(1)

        waitForHotelResultsToLoad()

        HotelScreen.hotelResultsList().perform(tinySwipeDown())

        Screengrab.screenshot("hotel_results")
        HotelScreen.hotelResultsList().perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(3, click()))
        HotelScreen.waitForDetailsLoaded()

        onView(withId(R.id.sticky_bottom_button)).perform(click())
        Common.delay(2)
        HotelScreen.addRoom().perform(click())
        Common.delay(3)
        Screengrab.screenshot("hotel_checkout")
        Common.delay(1)

        Espresso.pressBack()
        Espresso.pressBack()
        Espresso.pressBack()
        Espresso.pressBack()

        Common.delay(1)
    }

    private fun takeMaterialFlightScreenshotAndReturnToLaunchScreen(searchCriteria: FlightSearchCriteria) {
        onView(allOf(withText(LobInfo.FLIGHTS.labelRes), isCompletelyDisplayed())).perform(click())

        if (searchCriteria.isDropdownSearch) {
            SearchScreen.origin().perform(click())
            Common.delay(2)
            onData(airportDropDownEntryWithAirportCode(searchCriteria.departureAirport.code)).inRoot(RootMatchers.isPlatformPopup()).perform(click())
            SearchScreen.destination().perform(click())
            onData(airportDropDownEntryWithAirportCode(searchCriteria.arrivalAirport.code)).inRoot(RootMatchers.isPlatformPopup()).perform(click())

        } else {
            SearchScreen.origin().perform(click())
            SearchScreen.searchEditText().perform(typeText("1"), typeTextViaReplace(searchCriteria.departureAirport.code))
            Common.delay(2)
            SearchScreen.suggestionList().perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
            Common.delay(1)
            SearchScreen.searchEditText().perform(ViewActions.waitForViewToDisplay())
            SearchScreen.searchEditText().perform(typeText("1"), typeTextViaReplace(searchCriteria.arrivalAirport.code))
            Common.delay(2)
            SearchScreen.suggestionList().perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
        }

        Common.delay(1)

        val startDate = LocalDate.now().plusDays(35)
        val endDate = startDate.plusDays(3)
        SearchScreen.selectDates(startDate, endDate)
        SearchScreen.searchButton().perform(click())

        waitForViewNotYetInLayout(R.id.sort_filter_button, 60)
        Common.delay(1)

        Screengrab.screenshot("flight_results")

        Espresso.pressBack()
        Espresso.pressBack()
        Common.delay(1)
    }

    private fun takeLxScreenshotAndReturnToLaunchScreen(searchCriteria: LocationSearchCriteria) {
        onView(allOf(withText(LobInfo.ACTIVITIES.labelRes), isCompletelyDisplayed())).perform(click())

        SearchScreen.searchEditText().perform(typeTextViaReplace(searchCriteria.searchString))
        Common.delay(1)
        SearchScreen.selectLocation(searchCriteria.suggestString)

        val startDate = LocalDate.now().plusDays(90)
        val endDate = startDate.plusDays(1)
        SearchScreen.selectDates(startDate, endDate)

        SearchScreen.searchButton().perform(click())
        waitForViewNotYetInLayout(R.id.sort_filter_button, 30)

        LXScreen.searchList().perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
        waitForViewNotYetInLayout(R.id.offer_dates_container)
        Common.delay(1)

        Screengrab.screenshot("lx_details")

        Espresso.pressBack()
        Common.delay(1)
        Espresso.pressBack()
        Common.delay(1)
        Espresso.pressBack()
        Common.delay(1)
    }

    private fun waitForHotelResultsToLoad() {
        HotelScreen.waitForResultsLoaded(30)
        Common.delay(3)
    }

    private fun waitForLaunchScreenReady() {
        waitForViewNotYetInLayout(R.id.launch_toolbar)
        Common.delay(1)
    }

    private fun waitForViewNotYetInLayout(@IdRes viewId: Int, seconds: Int = 10) {
        waitForViewNotYetInLayoutToDisplay(withId(viewId), seconds.toLong(), TimeUnit.SECONDS)
    }

    private fun tinySwipeDown(): ViewAction {
        return GeneralSwipeAction(Swipe.FAST, GeneralLocation.CENTER,
                CoordinatesProvider { view ->
                    val xy = GeneralLocation.CENTER.calculateCoordinates(view)
                    xy[1] = xy[1] + (3 * ViewConfiguration.get(view.context).scaledTouchSlop)
                    xy
                },
                Press.FINGER)
    }

    private fun typeTextViaReplace(str: String): ViewAction {
        return EmulateTypeTextAction(str)
    }

    class EmulateTypeTextAction(private val stringToType: String): ViewAction {
        override fun getConstraints(): Matcher<View> {
            return allOf(isDisplayed(), isAssignableFrom(EditText::class.java))
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

    companion object {
        @ClassRule @JvmField
        val localeTestRule = LocaleTestRule()

        private val hotelSearchCriteriaForLocale = HashMap<Locale, LocationSearchCriteria>()
        private val lxSearchCriteriaForLocale = HashMap<Locale, LocationSearchCriteria>()
        private val flightSearchCriteriaForLocale = HashMap<Locale, FlightSearchCriteria>()
        private val pointOfSaleForLocale = HashMap<Locale, PointOfSaleId>()

        private val ARGENTINA = Locale("es", "AR")
        private val AUSTRALIA = Locale("en", "AU")
        private val BRAZIL = Locale("pt", "BR")
        private val CANADA_ENGLISH = Locale("en", "CA")
        private val DENMARK = Locale("da", "DK")
        private val FINLAND = Locale("fi", "FI")
        private val HONG_KONG_HK = Locale("zh", "HK")
        private val HONG_KONG_SIMPLIFIED = Locale.SIMPLIFIED_CHINESE
        private val HONG_KONG_TRADITIONAL = Locale.TRADITIONAL_CHINESE
        private val INDONESIA = Locale("id", "ID")
        private val MEXICO = Locale("es", "MX")
        private val NETHERLANDS = Locale("nl", "NL")
        private val NORWAY = Locale("nb", "NO")
        private val SPAIN = Locale("es", "ES")
        private val SWEDEN = Locale("sv", "SE")
        private val THAILAND = Locale("th", "TH")
        private val VIETNAM = Locale("vi", "VN")

        private val AMS = AirportSearchCriteria("AMS")
        private val ARN = AirportSearchCriteria("ARN")
        private val BER = AirportSearchCriteria("BER")
        private val BKK = AirportSearchCriteria("BKK")
        private val CGK = AirportSearchCriteria("CGK")
        private val CPH = AirportSearchCriteria("CPH")
        private val DMK = AirportSearchCriteria("DMK")
        private val EDI = AirportSearchCriteria("EDI")
        private val GIG = AirportSearchCriteria("GIG")
        private val HEL = AirportSearchCriteria("HEL")
        private val HKG = AirportSearchCriteria("HKG")
        private val HND = AirportSearchCriteria("HND")
        private val ICN = AirportSearchCriteria("ICN")
        private val LAS = AirportSearchCriteria("LAS")
        private val LON = AirportSearchCriteria("LON")
        private val MAD = AirportSearchCriteria("MAD")
        private val MEX = AirportSearchCriteria("MEX")
        private val NSW = AirportSearchCriteria("NSW")
        private val OSL = AirportSearchCriteria("OSL")
        private val PAR = AirportSearchCriteria("PAR")
        private val PYC = AirportSearchCriteria("PYC")
        private val ROM = AirportSearchCriteria("ROM")
        private val SFO = AirportSearchCriteria("SFO")
        private val TPE = AirportSearchCriteria("TPE")
        private val YUL = AirportSearchCriteria("YUL")

        init {
            hotelSearchCriteriaForLocale.put(Locale.US, LocationSearchCriteria("Las Vegas, NV"))
            hotelSearchCriteriaForLocale.put(ARGENTINA, LocationSearchCriteria("Buenos Aires, Argentina"))
            hotelSearchCriteriaForLocale.put(BRAZIL, LocationSearchCriteria("Las Vegas, NV"))
            hotelSearchCriteriaForLocale.put(Locale.CANADA_FRENCH, LocationSearchCriteria("Las Vegas, NV"))
            hotelSearchCriteriaForLocale.put(CANADA_ENGLISH, LocationSearchCriteria("Las Vegas, NV"))
            hotelSearchCriteriaForLocale.put(AUSTRALIA, LocationSearchCriteria("Bangkok, Thailand"))
            hotelSearchCriteriaForLocale.put(Locale.GERMANY, LocationSearchCriteria("London, England, UK"))
            hotelSearchCriteriaForLocale.put(DENMARK, LocationSearchCriteria("London, England, UK"))
            hotelSearchCriteriaForLocale.put(SPAIN, LocationSearchCriteria("London, England, UK"))
            hotelSearchCriteriaForLocale.put(FINLAND, LocationSearchCriteria("London, England, UK"))
            hotelSearchCriteriaForLocale.put(Locale.FRANCE, LocationSearchCriteria("London, England, UK"))
            hotelSearchCriteriaForLocale.put(INDONESIA, LocationSearchCriteria("Bangkok, Thailand"))
            hotelSearchCriteriaForLocale.put(Locale.ITALY, LocationSearchCriteria("London", "London, England, UK"))
            hotelSearchCriteriaForLocale.put(Locale.JAPAN, LocationSearchCriteria("Bangkok, Thailand"))
            hotelSearchCriteriaForLocale.put(Locale.KOREA, LocationSearchCriteria("홍콩(전체), 홍콩"))
            hotelSearchCriteriaForLocale.put(MEXICO, LocationSearchCriteria("Ciudad de México, Distrito Federal, México"))
            hotelSearchCriteriaForLocale.put(NETHERLANDS, LocationSearchCriteria("Amsterdam, Nederland"))
            hotelSearchCriteriaForLocale.put(NORWAY, LocationSearchCriteria("Oslo, Norge"))
            hotelSearchCriteriaForLocale.put(SWEDEN, LocationSearchCriteria("Stockholm, Sverige"))
            hotelSearchCriteriaForLocale.put(THAILAND, LocationSearchCriteria("โตเกียว, ญี่ปุ่น"))
            // ESS typeahead currently forced to English for Simplified Chinese o_O
            // hotelSearchCriteriaForLocale.put(HONG_KONG_TRADITIONAL, HotelSearchCriteria("汉城百济", "汉城百济博物馆, 首尔, 韩国"))
            hotelSearchCriteriaForLocale.put(HONG_KONG_TRADITIONAL, LocationSearchCriteria("Bangkok, Thailand"))
            hotelSearchCriteriaForLocale.put(HONG_KONG_HK, LocationSearchCriteria("Bangkok, Thailand"))
            hotelSearchCriteriaForLocale.put(HONG_KONG_SIMPLIFIED, LocationSearchCriteria("Bangkok, Thailand"))
            hotelSearchCriteriaForLocale.put(Locale.TAIWAN, LocationSearchCriteria("東京,", "東京 (及鄰近地區), 日本"))
            hotelSearchCriteriaForLocale.put(Locale.UK, LocationSearchCriteria("London", "London, England, UK"))
            hotelSearchCriteriaForLocale.put(VIETNAM, LocationSearchCriteria("Bangkok, Thailand"))

            flightSearchCriteriaForLocale.put(Locale.US, FlightSearchCriteria(SFO, LAS))
            flightSearchCriteriaForLocale.put(BRAZIL, FlightSearchCriteria(GIG, LAS))
            flightSearchCriteriaForLocale.put(Locale.CANADA_FRENCH, FlightSearchCriteria(YUL, LAS))
            flightSearchCriteriaForLocale.put(CANADA_ENGLISH, FlightSearchCriteria(YUL, LAS))
            flightSearchCriteriaForLocale.put(AUSTRALIA, FlightSearchCriteria(NSW, BKK))
            flightSearchCriteriaForLocale.put(Locale.GERMANY, FlightSearchCriteria(BER, LON))
            flightSearchCriteriaForLocale.put(DENMARK, FlightSearchCriteria(CPH, LON))
            flightSearchCriteriaForLocale.put(SPAIN, FlightSearchCriteria(MAD, LON))
            flightSearchCriteriaForLocale.put(FINLAND, FlightSearchCriteria(HEL, LON))
            flightSearchCriteriaForLocale.put(Locale.FRANCE, FlightSearchCriteria(PAR, LON))
            flightSearchCriteriaForLocale.put(INDONESIA, FlightSearchCriteria(CGK, DMK, true))
            flightSearchCriteriaForLocale.put(Locale.ITALY, FlightSearchCriteria(ROM, LON))
            flightSearchCriteriaForLocale.put(Locale.JAPAN, FlightSearchCriteria(HND, BKK))
            flightSearchCriteriaForLocale.put(Locale.KOREA, FlightSearchCriteria(ICN, DMK, true))
            flightSearchCriteriaForLocale.put(MEXICO, FlightSearchCriteria(MEX, LAS))
            flightSearchCriteriaForLocale.put(NETHERLANDS, FlightSearchCriteria(AMS, LON))
            flightSearchCriteriaForLocale.put(NORWAY, FlightSearchCriteria(OSL, LON))
            flightSearchCriteriaForLocale.put(SWEDEN, FlightSearchCriteria(ARN, LON))
            flightSearchCriteriaForLocale.put(THAILAND, FlightSearchCriteria(PYC, BKK))
            flightSearchCriteriaForLocale.put(HONG_KONG_TRADITIONAL, FlightSearchCriteria(HKG, BKK))
            flightSearchCriteriaForLocale.put(HONG_KONG_HK, FlightSearchCriteria(HKG, BKK))
            flightSearchCriteriaForLocale.put(HONG_KONG_SIMPLIFIED, FlightSearchCriteria(HKG, BKK))
            flightSearchCriteriaForLocale.put(Locale.TAIWAN, FlightSearchCriteria(TPE, BKK))
            flightSearchCriteriaForLocale.put(Locale.UK, FlightSearchCriteria(EDI, LON))

            lxSearchCriteriaForLocale.put(Locale.US, LocationSearchCriteria("San Francisco", "San Francisco, CA"))
            lxSearchCriteriaForLocale.put(Locale.CANADA_FRENCH, LocationSearchCriteria("Montréal", "Montréal, QC"))
            lxSearchCriteriaForLocale.put(Locale.GERMANY, LocationSearchCriteria("Berlin", "Berlin, Deutschland"))
            lxSearchCriteriaForLocale.put(Locale.FRANCE, LocationSearchCriteria("Paris", "Paris, France"))
            lxSearchCriteriaForLocale.put(HONG_KONG_TRADITIONAL, LocationSearchCriteria("Hong Kong", "Hong Kong (all), Hong Kong"))
            lxSearchCriteriaForLocale.put(Locale.ITALY, LocationSearchCriteria("Roma", "Roma, Italia"))
            lxSearchCriteriaForLocale.put(Locale.JAPAN, LocationSearchCriteria("ホノルル", "ホノルル, ハワイ州"))
            lxSearchCriteriaForLocale.put(SWEDEN, LocationSearchCriteria("Stockholm", "Stockholm, Sverige"))
            lxSearchCriteriaForLocale.put(Locale.UK, LocationSearchCriteria("London", "London, England"))

            pointOfSaleForLocale.put(Locale.US, PointOfSaleId.UNITED_STATES)
            pointOfSaleForLocale.put(ARGENTINA, PointOfSaleId.ARGENTINA)
            pointOfSaleForLocale.put(AUSTRALIA, PointOfSaleId.AUSTRALIA)
            pointOfSaleForLocale.put(BRAZIL, PointOfSaleId.BRAZIL)
            pointOfSaleForLocale.put(Locale.CANADA_FRENCH, PointOfSaleId.CANADA)
            pointOfSaleForLocale.put(CANADA_ENGLISH, PointOfSaleId.CANADA)
            pointOfSaleForLocale.put(Locale.GERMANY, PointOfSaleId.GERMANY)
            pointOfSaleForLocale.put(DENMARK, PointOfSaleId.DENMARK)
            pointOfSaleForLocale.put(SPAIN, PointOfSaleId.SPAIN)
            pointOfSaleForLocale.put(FINLAND, PointOfSaleId.FINLAND)
            pointOfSaleForLocale.put(Locale.FRANCE, PointOfSaleId.FRANCE)
            pointOfSaleForLocale.put(INDONESIA, PointOfSaleId.INDONESIA)
            pointOfSaleForLocale.put(Locale.ITALY, PointOfSaleId.ITALY)
            pointOfSaleForLocale.put(Locale.JAPAN, PointOfSaleId.JAPAN)
            pointOfSaleForLocale.put(Locale.KOREA, PointOfSaleId.SOUTH_KOREA)
            pointOfSaleForLocale.put(MEXICO, PointOfSaleId.MEXICO)
            pointOfSaleForLocale.put(NETHERLANDS, PointOfSaleId.NETHERLANDS)
            pointOfSaleForLocale.put(NORWAY, PointOfSaleId.NORWAY)
            pointOfSaleForLocale.put(SWEDEN, PointOfSaleId.SWEDEN)
            pointOfSaleForLocale.put(THAILAND, PointOfSaleId.THAILAND)
            pointOfSaleForLocale.put(HONG_KONG_TRADITIONAL, PointOfSaleId.HONG_KONG)
            pointOfSaleForLocale.put(HONG_KONG_HK, PointOfSaleId.HONG_KONG)
            pointOfSaleForLocale.put(HONG_KONG_SIMPLIFIED, PointOfSaleId.HONG_KONG)
            pointOfSaleForLocale.put(Locale.TAIWAN, PointOfSaleId.TAIWAN)
            pointOfSaleForLocale.put(Locale.UK, PointOfSaleId.UNITED_KINGDOM)
            pointOfSaleForLocale.put(VIETNAM, PointOfSaleId.VIETNAM)
        }

        class LocationSearchCriteria(val searchString: String,
                                     private val alternateSuggestString: String? = null) {
            val suggestString: String
                get() = alternateSuggestString ?: searchString
        }

        class FlightSearchCriteria(val departureAirport: AirportSearchCriteria,
                                   val arrivalAirport: AirportSearchCriteria,
                                   val isDropdownSearch: Boolean = false)

        class AirportSearchCriteria(val code: String)
    }


}
