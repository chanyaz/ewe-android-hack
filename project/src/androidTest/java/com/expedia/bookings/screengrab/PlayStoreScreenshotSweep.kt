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
import com.expedia.bookings.test.pagemodels.lx.LXScreen
import com.expedia.bookings.test.pagemodels.common.LogInScreen
import com.expedia.bookings.test.pagemodels.common.LaunchScreen
import com.expedia.bookings.test.pagemodels.common.SearchScreen
import com.expedia.bookings.test.Settings
import com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay
import com.expedia.bookings.test.pagemodels.common.SearchScreenActions
import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSiteScreen
import com.expedia.bookings.test.pagemodels.hotels.HotelResultsScreen
import com.expedia.bookings.test.pagemodels.hotels.HotelSortAndFilterScreen
import com.expedia.bookings.utils.Ui
import com.expedia.util.PackageUtil
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
        val packageSearchCriteria = packageSearchCriteriaForLocal[LocaleUtil.getTestLocale()]

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

            if (packageSearchCriteria != null) {
                takePackagesScreenshotReturnToLaunch(packageSearchCriteria)
            }

            //Use this for mock itins
            if (BuildConfig.ITIN_SCREENSHOT_BUILD) {
                takeItinScreens()
            }
        } catch (e: Throwable) {
            //shoot, something failed, but ignore so that the rest of the screens finish
        }
    }

    private fun signInUser() {
        LaunchScreen.tripsButton().perform(click())

        LogInScreen.signInWithExpediaButton().perform(waitForViewToDisplay(), click())
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

        LogInScreen.signInWithExpediaButton().perform(waitForViewToDisplay(), click())

        LogInScreen.typeTextEmailEditText("qa-ehcc@mobiata.com")
        LogInScreen.typeTextPasswordEditText("e3trefwfw")
        LogInScreen.clickOnLoginButton()
        onView(allOf(withId(R.id.summary_layout), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))).perform(ViewActions.waitForViewToDisplay())
        onView(allOf(withId(R.id.summary_layout), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))).perform(click())
        Common.delay(1)
        Screengrab.screenshot("itin")
    }

    @Throws(Throwable::class)
    private fun takeHotelScreenshotAndReturnToLaunchScreen(hotelSearchInfo: HotelSearchCriteria) {
        onView(allOf(withText(LobInfo.HOTELS.labelRes), isCompletelyDisplayed())).perform(click())

        waitForViewNotYetInLayout(R.id.search_src_text)

        SearchScreen.waitForSearchEditText().perform(typeTextViaReplace(hotelSearchInfo.city))
        Common.delay(1)
        SearchScreen.suggestionList().perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))
        Common.delay(1)

        val startDate = LocalDate.now().plusDays(90)
        val endDate = startDate.plusDays(5)
        SearchScreenActions.chooseDatesWithDialog(startDate, endDate)
        SearchScreen.searchButton().perform(click())
        Common.delay(1)

        waitForHotelResultsToLoad()

        HotelResultsScreen.hotelResultsList().perform(tinySwipeDown())

        Screengrab.screenshot("hotel_results")
        HotelSortAndFilterScreen.clickSortFilter()
        HotelSortAndFilterScreen.filterHotelName().perform(click(), typeText(hotelSearchInfo.hotelName))
        HotelSortAndFilterScreen.clickSortFilterDoneButton()
        waitForHotelResultsToLoad()

        HotelResultsScreen.hotelResultsList().perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(2, click()))
        HotelInfoSiteScreen.waitForDetailsLoaded()
        Screengrab.screenshot("hotel_infosite")

        Common.delay(1)

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
            SearchScreen.waitForSearchEditText().perform(typeText("1"), typeTextViaReplace(searchCriteria.departureAirport.code))
            Common.delay(2)
            SearchScreen.suggestionList().perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
            Common.delay(1)
            SearchScreen.waitForSearchEditText().perform(typeText("1"), typeTextViaReplace(searchCriteria.arrivalAirport.code))
            Common.delay(2)
            SearchScreen.suggestionList().perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
        }

        Common.delay(1)

        val startDate = LocalDate.now().plusDays(35)
        val endDate = startDate.plusDays(3)
        SearchScreenActions.chooseDatesWithDialog(startDate, endDate)
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

        SearchScreen.waitForSearchEditText().perform(typeTextViaReplace(searchCriteria.searchString))
        Common.delay(1)
        SearchScreenActions.selectLocation(searchCriteria.suggestString)

        val startDate = LocalDate.now().plusDays(90)
        val endDate = startDate.plusDays(1)
        SearchScreenActions.chooseDatesWithDialog(startDate, endDate)

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

    private fun takePackagesScreenshotReturnToLaunch(packageSearchCriteria: PackageSearchCriteria) {
        val packagesTitle = PackageUtil.packageTitle(activityRule.activity)
        onView(allOf(withText(packagesTitle), isCompletelyDisplayed())).perform(click())

        SearchScreen.origin().perform(click())
        SearchScreen.waitForSearchEditText().perform(typeText(packageSearchCriteria.origin))
        Common.delay(2)
        SearchScreen.suggestionList().perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        Common.delay(1)
        SearchScreen.waitForSearchEditText().perform(typeText(packageSearchCriteria.destination))
        Common.delay(2)
        SearchScreen.suggestionList().perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
        Common.delay(1)

        val startDate = LocalDate.now().plusDays(90)
        val endDate = startDate.plusDays(5)
        SearchScreenActions.chooseDatesWithDialog(startDate, endDate)
        SearchScreen.searchButton().perform(click())
        Common.delay(3)

        waitForHotelResultsToLoad()
        HotelResultsScreen.hotelResultsList().perform(tinySwipeDown())

        Screengrab.screenshot("hotel_results")

        Espresso.pressBack()
        Espresso.pressBack()
        Espresso.pressBack()

        Common.delay(1)
    }

    private fun waitForHotelResultsToLoad() {
        HotelResultsScreen.waitForResultsLoaded(30)
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

    class EmulateTypeTextAction(private val stringToType: String) : ViewAction {
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

        private val hotelSearchCriteriaForLocale = HashMap<Locale, HotelSearchCriteria>()
        private val lxSearchCriteriaForLocale = HashMap<Locale, LocationSearchCriteria>()
        private val flightSearchCriteriaForLocale = HashMap<Locale, FlightSearchCriteria>()
        private val pointOfSaleForLocale = HashMap<Locale, PointOfSaleId>()
        private val packageSearchCriteriaForLocal = HashMap<Locale, PackageSearchCriteria>()

        private val ARGENTINA = Locale("es", "AR")
        private val AUSTRALIA = Locale("en", "AU")
        private val AUSTRIA = Locale("de", "AT")
        private val BELGIUM_DUTCH = Locale("nl", "BE")
        private val BELGIUM_FRENCH = Locale("fr", "BE")
        private val BRAZIL = Locale("pt", "BR")
        private val CANADA_ENGLISH = Locale("en", "CA")
        private val CANADA_FRENCH = Locale("fr", "CA")
        private val DENMARK = Locale("da", "DK")
        private val FINLAND = Locale("fi", "FI")
        private val FRANCE = Locale("fr", "FR")
        private val GERMANY = Locale("de", "DE")
        private val HONG_KONG_ENGLISH = Locale("en", "HK")
        private val HONG_KONG_CHINESE_TRADITIONAL = Locale("zh", "HK")
        private val HONG_KONG_CHINESE_SIMPLIFIED = Locale("zh", "CN")
        private val INDIA = Locale("en", "IN")
        private val INDONESIA = Locale("id", "ID")
        private val IRELAND = Locale("en", "IE")
        private val ITALY = Locale("it", "IT")
        private val JAPAN_ENGLISH = Locale("en", "JP")
        private val JAPAN_JAPANESE = Locale("ja", "JP")
        private val KOREA_ENGLISH = Locale("en", "KR")
        private val KOREA_KOREAN = Locale("ko", "KR")
        private val MALAYSYA = Locale("en", "MY")
        private val MEXICO = Locale("es", "MX")
        private val NETHERLANDS = Locale("nl", "NL")
        private val NEW_ZELAND = Locale("en", "NZ")
        private val NORWAY = Locale("nb", "NO")
        private val PHILIPPINES = Locale("en", "PH")
        private val SINGAPORE = Locale("en", "SG")
        private val SPAIN = Locale("es", "ES")
        private val SWEDEN = Locale("sv", "SE")
        private val SWITZERLAND_FRENCH = Locale("fr", "CH")
        private val SWITZERLAND_GERMAN = Locale("de", "CH")
        private val THAILAND_ENGLISH = Locale("en", "TH")
        private val THAILAND_THAI = Locale("th", "TH")
        private val TAIWAN_ENGLISH = Locale("en", "TW")
        private val TAIWAN_CHINESE = Locale("zh", "TW")
        private val UK = Locale("en", "GB")
        private val USA_ENGLISH = Locale("en", "US")
        private val USA_SPANISH = Locale("es", "US")
        private val USA_CHINESE = Locale("zh", "US")
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
        private val HKT = AirportSearchCriteria("HKT")
        private val HND = AirportSearchCriteria("HND")
        private val LAS = AirportSearchCriteria("LAS")
        private val LON = AirportSearchCriteria("LHR")
        private val MAD = AirportSearchCriteria("MAD")
        private val MEX = AirportSearchCriteria("MEX")
        private val MNL = AirportSearchCriteria("MNL")
        private val NSW = AirportSearchCriteria("NSW")
        private val OSL = AirportSearchCriteria("OSL")
        private val PAR = AirportSearchCriteria("PAR")
        private val ROM = AirportSearchCriteria("ROM")
        private val SFO = AirportSearchCriteria("SFO")
        private val TPE = AirportSearchCriteria("TPE")
        private val YUL = AirportSearchCriteria("YUL")
        private val VIE = AirportSearchCriteria("VIE")
        private val BRU = AirportSearchCriteria("BRU")
        private val AKL = AirportSearchCriteria("AKL")

        init {
            val LasVegasHotel = "The Venetian"
            val LondonHotel = "The Ampersand Hotel"
            val BangkokHotel = "Siam@Siam Design Hotel Bangkok"
            val lasVegasCity = "Las Vegas, NV"
            val bangkokCity = "Bangkok, Thailand"
            val londonCity = "London, England"
            val searchLasVegas = HotelSearchCriteria(lasVegasCity, LasVegasHotel)
            val searchBangkok = HotelSearchCriteria(bangkokCity, BangkokHotel)
            val searchLondonEngland = HotelSearchCriteria(londonCity, LondonHotel)
            val searchLondon = HotelSearchCriteria("London", LondonHotel)

            hotelSearchCriteriaForLocale.put(ARGENTINA, searchLasVegas)
            hotelSearchCriteriaForLocale.put(AUSTRALIA, searchBangkok)
            hotelSearchCriteriaForLocale.put(AUSTRIA, searchLondonEngland)
            hotelSearchCriteriaForLocale.put(BELGIUM_DUTCH, searchLondonEngland)
            hotelSearchCriteriaForLocale.put(BELGIUM_FRENCH, searchLondonEngland)
            hotelSearchCriteriaForLocale.put(BRAZIL, searchLasVegas)
            hotelSearchCriteriaForLocale.put(CANADA_ENGLISH, searchLasVegas)
            hotelSearchCriteriaForLocale.put(CANADA_FRENCH, searchLasVegas)
            hotelSearchCriteriaForLocale.put(DENMARK, searchLondon)
            hotelSearchCriteriaForLocale.put(FINLAND, searchLondon)
            hotelSearchCriteriaForLocale.put(FRANCE, searchLondonEngland)
            hotelSearchCriteriaForLocale.put(GERMANY, searchLondon)
            hotelSearchCriteriaForLocale.put(HONG_KONG_ENGLISH, searchBangkok)
            hotelSearchCriteriaForLocale.put(HONG_KONG_CHINESE_TRADITIONAL, searchBangkok)
            hotelSearchCriteriaForLocale.put(HONG_KONG_CHINESE_SIMPLIFIED, searchBangkok)
            hotelSearchCriteriaForLocale.put(INDIA, searchBangkok)
            hotelSearchCriteriaForLocale.put(INDONESIA, searchBangkok)
            hotelSearchCriteriaForLocale.put(IRELAND, searchLondonEngland)
            hotelSearchCriteriaForLocale.put(ITALY, searchLondon)
            hotelSearchCriteriaForLocale.put(JAPAN_ENGLISH, searchBangkok)
            hotelSearchCriteriaForLocale.put(JAPAN_JAPANESE, searchBangkok)
            hotelSearchCriteriaForLocale.put(KOREA_ENGLISH, searchBangkok)
            hotelSearchCriteriaForLocale.put(KOREA_KOREAN, searchBangkok)
            hotelSearchCriteriaForLocale.put(MALAYSYA, searchBangkok)
            hotelSearchCriteriaForLocale.put(MEXICO, searchLasVegas)
            hotelSearchCriteriaForLocale.put(NETHERLANDS, searchLondonEngland)
            hotelSearchCriteriaForLocale.put(NEW_ZELAND, searchBangkok)
            hotelSearchCriteriaForLocale.put(NORWAY, searchLondonEngland)
            hotelSearchCriteriaForLocale.put(PHILIPPINES, searchBangkok)
            hotelSearchCriteriaForLocale.put(SINGAPORE, searchBangkok)
            hotelSearchCriteriaForLocale.put(SPAIN, searchLondon)
            hotelSearchCriteriaForLocale.put(SWEDEN, searchLondon)
            hotelSearchCriteriaForLocale.put(SWITZERLAND_FRENCH, searchLondonEngland)
            hotelSearchCriteriaForLocale.put(SWITZERLAND_GERMAN, searchLondonEngland)
            hotelSearchCriteriaForLocale.put(THAILAND_ENGLISH, searchBangkok)
            hotelSearchCriteriaForLocale.put(THAILAND_THAI, searchBangkok)
            hotelSearchCriteriaForLocale.put(TAIWAN_ENGLISH, searchBangkok)
            hotelSearchCriteriaForLocale.put(TAIWAN_CHINESE, searchBangkok)
            hotelSearchCriteriaForLocale.put(UK, searchLondon)
            hotelSearchCriteriaForLocale.put(USA_ENGLISH, searchLasVegas)
            hotelSearchCriteriaForLocale.put(USA_SPANISH, searchLasVegas)
            hotelSearchCriteriaForLocale.put(USA_CHINESE, searchLasVegas)
            hotelSearchCriteriaForLocale.put(VIETNAM, searchBangkok)

            packageSearchCriteriaForLocal.put(GERMANY, PackageSearchCriteria(destination = londonCity))
            packageSearchCriteriaForLocal.put(AUSTRALIA, PackageSearchCriteria(destination = bangkokCity))
            packageSearchCriteriaForLocal.put(CANADA_ENGLISH, PackageSearchCriteria(destination = lasVegasCity))
            packageSearchCriteriaForLocal.put(HONG_KONG_ENGLISH, PackageSearchCriteria(destination = bangkokCity))
            packageSearchCriteriaForLocal.put(HONG_KONG_CHINESE_TRADITIONAL, PackageSearchCriteria(destination = bangkokCity))
            packageSearchCriteriaForLocal.put(JAPAN_JAPANESE, PackageSearchCriteria(destination = bangkokCity))
            packageSearchCriteriaForLocal.put(JAPAN_ENGLISH, PackageSearchCriteria(destination = bangkokCity))
            packageSearchCriteriaForLocal.put(THAILAND_ENGLISH, PackageSearchCriteria(destination = bangkokCity))
            packageSearchCriteriaForLocal.put(THAILAND_THAI, PackageSearchCriteria(destination = bangkokCity))
            packageSearchCriteriaForLocal.put(MALAYSYA, PackageSearchCriteria(destination = bangkokCity))
            packageSearchCriteriaForLocal.put(NEW_ZELAND, PackageSearchCriteria(destination = bangkokCity))
            packageSearchCriteriaForLocal.put(SINGAPORE, PackageSearchCriteria(destination = bangkokCity))
            packageSearchCriteriaForLocal.put(UK, PackageSearchCriteria(destination = londonCity))
            packageSearchCriteriaForLocal.put(USA_ENGLISH, PackageSearchCriteria(destination = lasVegasCity))
            packageSearchCriteriaForLocal.put(USA_SPANISH, PackageSearchCriteria(destination = lasVegasCity))
            packageSearchCriteriaForLocal.put(USA_CHINESE, PackageSearchCriteria(destination = lasVegasCity))

            flightSearchCriteriaForLocale.put(AUSTRALIA, FlightSearchCriteria(NSW, BKK))
            flightSearchCriteriaForLocale.put(AUSTRIA, FlightSearchCriteria(VIE, LON))
            flightSearchCriteriaForLocale.put(BELGIUM_DUTCH, FlightSearchCriteria(BRU, LON))
            flightSearchCriteriaForLocale.put(BELGIUM_FRENCH, FlightSearchCriteria(BRU, LON))
            flightSearchCriteriaForLocale.put(BRAZIL, FlightSearchCriteria(GIG, LAS))
            flightSearchCriteriaForLocale.put(CANADA_FRENCH, FlightSearchCriteria(YUL, LAS))
            flightSearchCriteriaForLocale.put(CANADA_ENGLISH, FlightSearchCriteria(YUL, LAS))
            flightSearchCriteriaForLocale.put(SWITZERLAND_FRENCH, FlightSearchCriteria(VIE, LON))
            flightSearchCriteriaForLocale.put(SWITZERLAND_GERMAN, FlightSearchCriteria(VIE, LON))
            flightSearchCriteriaForLocale.put(DENMARK, FlightSearchCriteria(CPH, LON))
            flightSearchCriteriaForLocale.put(FINLAND, FlightSearchCriteria(HEL, LON))
            flightSearchCriteriaForLocale.put(FRANCE, FlightSearchCriteria(PAR, LON))
            flightSearchCriteriaForLocale.put(GERMANY, FlightSearchCriteria(BER, LON))
            flightSearchCriteriaForLocale.put(HONG_KONG_CHINESE_TRADITIONAL, FlightSearchCriteria(HKG, BKK))
            flightSearchCriteriaForLocale.put(HONG_KONG_ENGLISH, FlightSearchCriteria(HKG, BKK))
            flightSearchCriteriaForLocale.put(HONG_KONG_CHINESE_SIMPLIFIED, FlightSearchCriteria(HKG, BKK))
            flightSearchCriteriaForLocale.put(INDONESIA, FlightSearchCriteria(HKG, DMK, true))
            flightSearchCriteriaForLocale.put(IRELAND, FlightSearchCriteria(PAR, LON))
            flightSearchCriteriaForLocale.put(ITALY, FlightSearchCriteria(ROM, LON))
            flightSearchCriteriaForLocale.put(JAPAN_ENGLISH, FlightSearchCriteria(HND, BKK))
            flightSearchCriteriaForLocale.put(JAPAN_JAPANESE, FlightSearchCriteria(HND, BKK))
            flightSearchCriteriaForLocale.put(MALAYSYA, FlightSearchCriteria(HND, BKK))
            flightSearchCriteriaForLocale.put(MEXICO, FlightSearchCriteria(MEX, LAS))
            flightSearchCriteriaForLocale.put(NETHERLANDS, FlightSearchCriteria(AMS, LON))
            flightSearchCriteriaForLocale.put(NEW_ZELAND, FlightSearchCriteria(AKL, BKK))
            flightSearchCriteriaForLocale.put(NORWAY, FlightSearchCriteria(OSL, LON))
            flightSearchCriteriaForLocale.put(PHILIPPINES, FlightSearchCriteria(MNL, BKK, true))
            flightSearchCriteriaForLocale.put(SINGAPORE, FlightSearchCriteria(HND, BKK))
            flightSearchCriteriaForLocale.put(KOREA_ENGLISH, FlightSearchCriteria(TPE, DMK))
            flightSearchCriteriaForLocale.put(KOREA_KOREAN, FlightSearchCriteria(TPE, DMK))
            flightSearchCriteriaForLocale.put(SPAIN, FlightSearchCriteria(MAD, LON))
            flightSearchCriteriaForLocale.put(SWEDEN, FlightSearchCriteria(ARN, LON))
            flightSearchCriteriaForLocale.put(THAILAND_THAI, FlightSearchCriteria(HKT, BKK))
            flightSearchCriteriaForLocale.put(THAILAND_ENGLISH, FlightSearchCriteria(HKT, BKK))
            flightSearchCriteriaForLocale.put(TAIWAN_CHINESE, FlightSearchCriteria(TPE, BKK))
            flightSearchCriteriaForLocale.put(TAIWAN_ENGLISH, FlightSearchCriteria(TPE, BKK))
            flightSearchCriteriaForLocale.put(UK, FlightSearchCriteria(EDI, LON))
            flightSearchCriteriaForLocale.put(USA_ENGLISH, FlightSearchCriteria(SFO, LAS))
            flightSearchCriteriaForLocale.put(USA_SPANISH, FlightSearchCriteria(SFO, LAS))
            flightSearchCriteriaForLocale.put(USA_CHINESE, FlightSearchCriteria(SFO, LAS))

            lxSearchCriteriaForLocale.put(Locale.US, LocationSearchCriteria("San Francisco", "San Francisco, CA"))
            lxSearchCriteriaForLocale.put(Locale.CANADA_FRENCH, LocationSearchCriteria("Montréal", "Montréal, QC"))
            lxSearchCriteriaForLocale.put(Locale.GERMANY, LocationSearchCriteria("Berlin", "Berlin, Deutschland"))
            lxSearchCriteriaForLocale.put(Locale.FRANCE, LocationSearchCriteria("Paris", "Paris, France"))
            lxSearchCriteriaForLocale.put(TAIWAN_CHINESE, LocationSearchCriteria("Hong Kong", "Hong Kong (all), Hong Kong"))
            lxSearchCriteriaForLocale.put(Locale.ITALY, LocationSearchCriteria("Roma", "Roma, Italia"))
            lxSearchCriteriaForLocale.put(Locale.JAPAN, LocationSearchCriteria("ホノルル", "ホノルル, ハワイ州"))
            lxSearchCriteriaForLocale.put(SWEDEN, LocationSearchCriteria("Stockholm", "Stockholm, Sverige"))
            lxSearchCriteriaForLocale.put(Locale.UK, LocationSearchCriteria("London", "London, England"))

            pointOfSaleForLocale.put(ARGENTINA, PointOfSaleId.ARGENTINA)
            pointOfSaleForLocale.put(AUSTRALIA, PointOfSaleId.AUSTRALIA)
            pointOfSaleForLocale.put(AUSTRIA, PointOfSaleId.AUSTRIA)
            pointOfSaleForLocale.put(BELGIUM_DUTCH, PointOfSaleId.BELGIUM)
            pointOfSaleForLocale.put(BELGIUM_FRENCH, PointOfSaleId.BELGIUM)
            pointOfSaleForLocale.put(BRAZIL, PointOfSaleId.BRAZIL)
            pointOfSaleForLocale.put(CANADA_ENGLISH, PointOfSaleId.CANADA)
            pointOfSaleForLocale.put(CANADA_FRENCH, PointOfSaleId.CANADA)
            pointOfSaleForLocale.put(DENMARK, PointOfSaleId.DENMARK)
            pointOfSaleForLocale.put(FINLAND, PointOfSaleId.FINLAND)
            pointOfSaleForLocale.put(FRANCE, PointOfSaleId.FRANCE)
            pointOfSaleForLocale.put(GERMANY, PointOfSaleId.GERMANY)
            pointOfSaleForLocale.put(HONG_KONG_ENGLISH, PointOfSaleId.HONG_KONG)
            pointOfSaleForLocale.put(HONG_KONG_CHINESE_TRADITIONAL, PointOfSaleId.HONG_KONG)
            pointOfSaleForLocale.put(HONG_KONG_CHINESE_SIMPLIFIED, PointOfSaleId.HONG_KONG)
            pointOfSaleForLocale.put(INDIA, PointOfSaleId.INDIA)
            pointOfSaleForLocale.put(INDONESIA, PointOfSaleId.INDONESIA)
            pointOfSaleForLocale.put(IRELAND, PointOfSaleId.IRELAND)
            pointOfSaleForLocale.put(ITALY, PointOfSaleId.ITALY)
            pointOfSaleForLocale.put(JAPAN_ENGLISH, PointOfSaleId.JAPAN)
            pointOfSaleForLocale.put(JAPAN_JAPANESE, PointOfSaleId.JAPAN)
            pointOfSaleForLocale.put(KOREA_ENGLISH, PointOfSaleId.SOUTH_KOREA)
            pointOfSaleForLocale.put(KOREA_KOREAN, PointOfSaleId.SOUTH_KOREA)
            pointOfSaleForLocale.put(MALAYSYA, PointOfSaleId.MALAYSIA)
            pointOfSaleForLocale.put(MEXICO, PointOfSaleId.MEXICO)
            pointOfSaleForLocale.put(NETHERLANDS, PointOfSaleId.NETHERLANDS)
            pointOfSaleForLocale.put(NEW_ZELAND, PointOfSaleId.NEW_ZEALND)
            pointOfSaleForLocale.put(NORWAY, PointOfSaleId.NORWAY)
            pointOfSaleForLocale.put(PHILIPPINES, PointOfSaleId.PHILIPPINES)
            pointOfSaleForLocale.put(SINGAPORE, PointOfSaleId.SINGAPORE)
            pointOfSaleForLocale.put(SPAIN, PointOfSaleId.SPAIN)
            pointOfSaleForLocale.put(SWEDEN, PointOfSaleId.SWEDEN)
            pointOfSaleForLocale.put(SWITZERLAND_FRENCH, PointOfSaleId.SWITZERLAND)
            pointOfSaleForLocale.put(SWITZERLAND_GERMAN, PointOfSaleId.SWITZERLAND)
            pointOfSaleForLocale.put(THAILAND_ENGLISH, PointOfSaleId.THAILAND)
            pointOfSaleForLocale.put(THAILAND_THAI, PointOfSaleId.THAILAND)
            pointOfSaleForLocale.put(TAIWAN_ENGLISH, PointOfSaleId.TAIWAN)
            pointOfSaleForLocale.put(TAIWAN_CHINESE, PointOfSaleId.TAIWAN)
            pointOfSaleForLocale.put(UK, PointOfSaleId.UNITED_KINGDOM)
            pointOfSaleForLocale.put(USA_ENGLISH, PointOfSaleId.UNITED_STATES)
            pointOfSaleForLocale.put(USA_SPANISH, PointOfSaleId.UNITED_STATES)
            pointOfSaleForLocale.put(USA_CHINESE, PointOfSaleId.UNITED_STATES)
            pointOfSaleForLocale.put(VIETNAM, PointOfSaleId.VIETNAM)
        }

        class LocationSearchCriteria(
            val searchString: String,
            private val alternateSuggestString: String? = null
        ) {
            val suggestString: String
                get() = alternateSuggestString ?: searchString
        }

        class FlightSearchCriteria(
            val departureAirport: AirportSearchCriteria,
            val arrivalAirport: AirportSearchCriteria,
            val isDropdownSearch: Boolean = false
        )

        class AirportSearchCriteria(val code: String)

        class HotelSearchCriteria(val city: String, val hotelName: String)

        class PackageSearchCriteria(val destination: String, val origin: String = "San Francisco")
    }
}
