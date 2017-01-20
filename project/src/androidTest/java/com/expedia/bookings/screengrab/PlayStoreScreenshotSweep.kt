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
import android.support.test.espresso.matcher.RootMatchers
import android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom
import android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
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
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.otto.Events
import com.expedia.bookings.test.BuildConfig
import com.expedia.bookings.test.espresso.AbacusTestUtils
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.CustomMatchers.airportDropDownEntryWithAirportCode
import com.expedia.bookings.test.espresso.EspressoUtils.waitForViewNotYetInLayoutToDisplay
import com.expedia.bookings.test.espresso.ViewActions
import com.expedia.bookings.test.phone.hotels.HotelScreen
import com.expedia.bookings.test.phone.lx.LXScreen
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen
import com.expedia.bookings.test.phone.pagemodels.flights.FlightsSearchScreen
import com.expedia.bookings.test.tablet.pagemodels.Settings
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
        Settings.setServer("Production")
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
        val carSearchCriteria = carSearchCriteriaForLocale[LocaleUtil.getTestLocale()]
        val lxSearchCriteria = lxSearchCriteriaForLocale[LocaleUtil.getTestLocale()]

        try {
            waitForLaunchScreenReady()
            Events.post(Events.PhoneLaunchOnPOSChange())

            if (hotelSearchCriteria != null) {
                takeHotelScreenshotAndReturnToLaunchScreen(hotelSearchCriteria)
            }

            if (flightSearchCriteria != null) {
                takeFlightScreenshotAndReturnToLaunchScreen(flightSearchCriteria)
                takeMaterialFlightScreenshotAndReturnToLaunchScreen(flightSearchCriteria)
            }

            if (carSearchCriteria != null) {
                takeCarScreenshotAndReturnToLaunchScreen(carSearchCriteria)
            }

            if (lxSearchCriteria != null) {
                takeLxScreenshotAndReturnToLaunchScreen(lxSearchCriteria)
            }
        }
        catch (e: Throwable) {
             //shoot, something failed, but ignore so that the rest of the screens finish
        }
    }

    @Throws(Throwable::class)
    private fun takeHotelScreenshotAndReturnToLaunchScreen(hotelSearchInfo: LocationSearchCriteria) {
        onView(allOf(withText(LobInfo.HOTELS.labelRes), isCompletelyDisplayed())).perform(click())

        waitForViewNotYetInLayout(R.id.search_src_text)

        SearchScreen.searchEditText().perform(typeTextViaReplace(hotelSearchInfo.searchString))
        Common.delay(1)
        SearchScreen.selectUnambiguousSuggestion(hotelSearchInfo.suggestString)

        val startDate = LocalDate.now().plusDays(90)
        val endDate = startDate.plusDays(5)
        SearchScreen.selectDates(startDate, endDate)

        SearchScreen.searchButton().perform(click())
        waitForHotelResultsToLoad()
        HotelScreen.hotelResultsList().perform(tinySwipeDown())

        Screengrab.screenshot("hotel_results")

        Espresso.pressBack()
        Espresso.pressBack()
        Common.delay(1)
    }

    private fun takeFlightScreenshotAndReturnToLaunchScreen(searchCriteria: FlightSearchCriteria) {
        AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppFlightTest, AbacusUtils.DefaultVariate.CONTROL.ordinal)
        onView(allOf(withText(LobInfo.FLIGHTS.labelRes), isCompletelyDisplayed())).perform(click())

        if (searchCriteria.isDropdownSearch) {
            onView(withId(R.id.departure_airport_spinner)).perform(click())
            onData(airportDropDownEntryWithAirportCode(searchCriteria.departureAirport.code)).perform(click())
            onView(withId(R.id.arrival_airport_spinner)).perform(click())
            onData(airportDropDownEntryWithAirportCode(searchCriteria.arrivalAirport.code)).perform(click())
        } else {
            FlightsSearchScreen.departureEditText().perform(typeText("1"), typeTextViaReplace(searchCriteria.departureAirport.code))
            Espresso.closeSoftKeyboard()
            Common.delay(1)
            FlightsSearchScreen.arrivalEditText().perform(typeText("1"), typeTextViaReplace(searchCriteria.arrivalAirport.code))
            Espresso.closeSoftKeyboard()
        }
        FlightsSearchScreen.clickSelectDepartureButton()
        val startDate = LocalDate.now().plusDays(35)
        val endDate = startDate.plusDays(3)
        FlightsSearchScreen.clickDate(startDate, endDate)
        FlightsSearchScreen.clickSearchButton()

        waitForViewNotYetInLayout(R.id.flight_price_label_text_view, 20)

        Screengrab.screenshot("flight_results")

        Espresso.pressBack()
        Espresso.pressBack()
        Common.delay(1)
    }

    private fun takeMaterialFlightScreenshotAndReturnToLaunchScreen(searchCriteria: FlightSearchCriteria) {
        AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppFlightTest, AbacusUtils.DefaultVariate.BUCKETED.ordinal)
        onView(allOf(withText(LobInfo.FLIGHTS.labelRes), isCompletelyDisplayed())).perform(click())

        if (searchCriteria.isDropdownSearch) {
            SearchScreen.origin().perform(click())
            onData(airportDropDownEntryWithAirportCode(searchCriteria.departureAirport.code)).inRoot(RootMatchers.isPlatformPopup()).perform(click())
            SearchScreen.destination().perform(click())
            onData(airportDropDownEntryWithAirportCode(searchCriteria.arrivalAirport.code)).inRoot(RootMatchers.isPlatformPopup()).perform(click())
        } else {
            SearchScreen.origin().perform(click())
            SearchScreen.searchEditText().perform(typeText("1"), typeTextViaReplace(searchCriteria.departureAirport.code))
            Common.delay(1)
            SearchScreen.selectLocation(searchCriteria.departureAirport.suggestString)
            Common.delay(1)
            SearchScreen.searchEditText().perform(ViewActions.waitForViewToDisplay())
            SearchScreen.searchEditText().perform(typeText("1"), typeTextViaReplace(searchCriteria.arrivalAirport.code))
            Common.delay(1)
            SearchScreen.selectLocation(searchCriteria.arrivalAirport.suggestString)
        }
        val startDate = LocalDate.now().plusDays(35)
        val endDate = startDate.plusDays(3)
        SearchScreen.selectDates(startDate, endDate)
        SearchScreen.searchButton().perform(click())

        waitForViewNotYetInLayout(R.id.sort_filter_button, 60)

        Screengrab.screenshot("material_flight_results")

        Espresso.pressBack()
        Espresso.pressBack()
        Common.delay(1)
    }

    private fun takeCarScreenshotAndReturnToLaunchScreen(searchCriteria: LocationSearchCriteria) {
        onView(allOf(withText(LobInfo.CARS.labelRes), isCompletelyDisplayed())).perform(click())

        SearchScreen.searchEditText().perform(typeTextViaReplace(searchCriteria.searchString))
        Common.delay(1)
        SearchScreen.selectLocation(searchCriteria.suggestString)

        val startDate = LocalDate.now().plusDays(90)
        val endDate = startDate.plusDays(1)
        SearchScreen.selectDates(startDate, endDate)

        SearchScreen.searchButton().perform(click())
        waitForViewNotYetInLayout(R.id.category_sort_toolbar, 30)
        Common.delay(2)

        Screengrab.screenshot("car_results")

        Espresso.pressBack()
        Common.delay(1)
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

        LXScreen.searchList().perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
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
        private val carSearchCriteriaForLocale = HashMap<Locale, LocationSearchCriteria>()
        private val lxSearchCriteriaForLocale = HashMap<Locale, LocationSearchCriteria>()
        private val flightSearchCriteriaForLocale = HashMap<Locale, FlightSearchCriteria>()
        private val pointOfSaleForLocale = HashMap<Locale, PointOfSaleId>()

        private val ARGENTINA = Locale("es", "AR")
        private val BRAZIL = Locale("pt", "BR")
        private val DENMARK = Locale("da", "DK")
        private val FINLAND = Locale("fi", "FI")
        private val HONG_KONG = Locale.SIMPLIFIED_CHINESE
        private val INDONESIA = Locale("id", "ID")
        private val MEXICO = Locale("es", "MX")
        private val NETHERLANDS = Locale("nl", "NL")
        private val NORWAY = Locale("nb", "NO")
        private val SPAIN = Locale("es", "ES")
        private val SWEDEN = Locale("sv", "SE")
        private val THAILAND = Locale("th", "TH")

        private val AMS_en = AirportSearchCriteria("AMS", "Amsterdam, Netherlands (AMS - Schiphol)")
        private val AMS_nl = AirportSearchCriteria("AMS", "Amsterdam, Nederland (AMS - Schiphol)")
        private val ARN_se = AirportSearchCriteria("ARN", "Stockholm, Sverige (ARN - Arlanda)")
        private val BER_de = AirportSearchCriteria("BER", "Berlin, Deutschland (BER - Alle Flughäfen)")
        private val BKK_tl = AirportSearchCriteria("BKK", "กรุงเทพ, ไทย (BKK - ทุกสนามบิน)")
        private val CGK = AirportSearchCriteria("CGK")
        private val CNX = AirportSearchCriteria("CNX")
        private val CPH_da = AirportSearchCriteria("CPH", "København, Danmark (CPH - Alle lufthavne)")
        private val GIG_pt = AirportSearchCriteria("GIG", "Rio de Janeiro, Brasil (GIG - Galeão - Aeroporto Internacional Antonio Carlos Jobim)")
        private val HEL_fi = AirportSearchCriteria("HEL", "Helsinki, Suomi (HEL - Helsinki-Vantaa)")
        private val HKG_en = AirportSearchCriteria("HKG", "Hong Kong, Hong Kong (HKG - Hong Kong Intl.)")
        private val HND_jp = AirportSearchCriteria("HND", "東京, 日本 (HND - 羽田空港)")
        private val HND_tl = AirportSearchCriteria("HND", "โตเกียว, ญี่ปุ่น (HND - สนามบินฮาเนดะ)")
        private val HNL_jp = AirportSearchCriteria("HNL", "ホノルル - オアフ, オアフ, ハワイ, アメリカ合衆国 (HNL-ホノルル国際空港)")
        private val ICN = AirportSearchCriteria("ICN")
        private val KUL = AirportSearchCriteria("KUL")
        private val LAS_en = AirportSearchCriteria("LAS", "Las Vegas, NV (LAS - All Airports)")
        private val LAS_es = AirportSearchCriteria("LAS", "Las Vegas, Nevada, Estados Unidos (LAS - Todos los aeropuertos)")
        private val LAS_pt = AirportSearchCriteria("LAS", "Las Vegas, NV, Estados Unidos (LAS - Todos os aeroportos)")
        private val LAS_fr = AirportSearchCriteria("LAS", "Las Vegas, Nevada, États - Unis (LAS-Tous les aéroports)")
        private val LAS_cn = AirportSearchCriteria("LAS", "拉斯維加斯, 內華達, 美國 (LAS - 所有機場)")
        private val LON_en = AirportSearchCriteria("LON", "London, England, UK (LON - All Airports)")
        private val LON_de = AirportSearchCriteria("LON", "London, England, Großbritannien (LON - Alle Flughäfen)")
        private val LON_da = AirportSearchCriteria("LON", "London, England, Storbritannien (LON - Alle lufthavne)")
        private val LON_fi = AirportSearchCriteria("LON", "Lontoo, Englanti, Yhdistynyt kuningaskunta (LON - Kaikki lentokentät)")
        private val LON_no = AirportSearchCriteria("LON", "London, England, Storbritannia (LON - alle flyplasser)")
        private val LON_se = AirportSearchCriteria("LON", "London, England, Storbritannien (LON - Alla flygplatser)")
        private val MAD_es = AirportSearchCriteria("MAD", "Madrid, España (MAD - Todos los aeropuertos)")
        private val MEX_es = AirportSearchCriteria("MEX", "Ciudad de México, Distrito Federal, México (MEX - A. Internacional de la Ciudad de México)")
        private val NYC_fr = AirportSearchCriteria("NYC", "New York, New York, États - Unis (NYC-Tous les aéroports)")
        private val NYC_nl = AirportSearchCriteria("NYC", "New York, NY, Verenigde Staten (NYC - Alle luchthavens)")
        private val OSL_no = AirportSearchCriteria("OSL", "Oslo, Norge (OSL - alle flyplasser)")
        private val PAR_fr = AirportSearchCriteria("PAR", "Paris, France (PAR - Tous les aéroports)")
        private val PAR_it = AirportSearchCriteria("PAR", "Parigi, Francia (PAR - tutti gli aeroporti)")
        private val ROM_es = AirportSearchCriteria("ROM", "Roma, Italia (ROM - Todos los aeropuertos)")
        private val ROM_it = AirportSearchCriteria("ROM", "Roma, Italia (ROM - tutti gli aeroporti)")
        private val SFO_en = AirportSearchCriteria("SFO", "San Francisco, CA (SFO - San Francisco Intl.)")
        private val TPE_en = AirportSearchCriteria("TPE", "Taipei, Taiwan (TPE - All Airports)")
        private val TPE_cn = AirportSearchCriteria("TPE", "台北, 台灣 (TPE - 所有機場)")
        private val YUL_fr = AirportSearchCriteria("YUL", "Montréal, QC, Canada (YUL - Aéroport international Pierre Elliott Trudeau)")

        init {
            hotelSearchCriteriaForLocale.put(Locale.US, LocationSearchCriteria("San Francisco, CA"))
            hotelSearchCriteriaForLocale.put(ARGENTINA, LocationSearchCriteria("Buenos Aires, Argentina"))
            hotelSearchCriteriaForLocale.put(BRAZIL, LocationSearchCriteria("Rio de Janeiro, Brasil"))
            hotelSearchCriteriaForLocale.put(Locale.CANADA_FRENCH, LocationSearchCriteria("Montréal", "Montréal, QC, Canada"))
            hotelSearchCriteriaForLocale.put(Locale.GERMANY, LocationSearchCriteria("Berlin, Deutschland"))
            hotelSearchCriteriaForLocale.put(DENMARK, LocationSearchCriteria("København, Danmark"))
            hotelSearchCriteriaForLocale.put(SPAIN, LocationSearchCriteria("Madrid, España"))
            hotelSearchCriteriaForLocale.put(FINLAND, LocationSearchCriteria("Helsinki, Suomi"))
            hotelSearchCriteriaForLocale.put(Locale.FRANCE, LocationSearchCriteria("Paris, France"))
            hotelSearchCriteriaForLocale.put(INDONESIA, LocationSearchCriteria("Denpasar, Indonesia"))
            hotelSearchCriteriaForLocale.put(Locale.ITALY, LocationSearchCriteria("Roma, Italia"))
            hotelSearchCriteriaForLocale.put(Locale.JAPAN, LocationSearchCriteria("ホノルル, ハワイ州, アメリカ合衆国"))
            hotelSearchCriteriaForLocale.put(Locale.KOREA, LocationSearchCriteria("홍콩(전체), 홍콩"))
            hotelSearchCriteriaForLocale.put(MEXICO, LocationSearchCriteria("Ciudad de México, Distrito Federal, México"))
            hotelSearchCriteriaForLocale.put(NETHERLANDS, LocationSearchCriteria("Amsterdam, Nederland"))
            hotelSearchCriteriaForLocale.put(NORWAY, LocationSearchCriteria("Oslo, Norge"))
            hotelSearchCriteriaForLocale.put(SWEDEN, LocationSearchCriteria("Stockholm, Sverige"))
            hotelSearchCriteriaForLocale.put(THAILAND, LocationSearchCriteria("โตเกียว, ญี่ปุ่น"))
            // ESS typeahead currently forced to English for Simplified Chinese o_O
            // hotelSearchCriteriaForLocale.put(HONG_KONG, HotelSearchCriteria("汉城百济", "汉城百济博物馆, 首尔, 韩国"))
            hotelSearchCriteriaForLocale.put(HONG_KONG, LocationSearchCriteria("Seoul", "Seoul, South Korea"))
            hotelSearchCriteriaForLocale.put(Locale.TAIWAN, LocationSearchCriteria("東京,", "東京 (及鄰近地區), 日本"))
            hotelSearchCriteriaForLocale.put(Locale.UK, LocationSearchCriteria("London", "London, England, UK"))

            flightSearchCriteriaForLocale.put(Locale.US, FlightSearchCriteria(SFO_en, LAS_en))
            flightSearchCriteriaForLocale.put(BRAZIL, FlightSearchCriteria(GIG_pt, LAS_pt))
            flightSearchCriteriaForLocale.put(Locale.CANADA_FRENCH, FlightSearchCriteria(YUL_fr, LAS_fr))
            flightSearchCriteriaForLocale.put(Locale.GERMANY, FlightSearchCriteria(BER_de, LON_de))
            flightSearchCriteriaForLocale.put(DENMARK, FlightSearchCriteria(CPH_da, LON_da))
            flightSearchCriteriaForLocale.put(SPAIN, FlightSearchCriteria(MAD_es, ROM_es))
            flightSearchCriteriaForLocale.put(FINLAND, FlightSearchCriteria(HEL_fi, LON_fi))
            flightSearchCriteriaForLocale.put(Locale.FRANCE, FlightSearchCriteria(PAR_fr, NYC_fr))
            flightSearchCriteriaForLocale.put(INDONESIA, FlightSearchCriteria(CGK, KUL, true))
            flightSearchCriteriaForLocale.put(Locale.ITALY, FlightSearchCriteria(ROM_it, PAR_it))
            flightSearchCriteriaForLocale.put(Locale.JAPAN, FlightSearchCriteria(HND_jp, HNL_jp))
            flightSearchCriteriaForLocale.put(Locale.KOREA, FlightSearchCriteria(ICN, CNX, true))
            flightSearchCriteriaForLocale.put(MEXICO, FlightSearchCriteria(MEX_es, LAS_es))
            flightSearchCriteriaForLocale.put(NETHERLANDS, FlightSearchCriteria(AMS_nl, NYC_nl))
            flightSearchCriteriaForLocale.put(NORWAY, FlightSearchCriteria(OSL_no, LON_no))
            flightSearchCriteriaForLocale.put(SWEDEN, FlightSearchCriteria(ARN_se, LON_se))
            flightSearchCriteriaForLocale.put(THAILAND, FlightSearchCriteria(BKK_tl, HND_tl))
            flightSearchCriteriaForLocale.put(HONG_KONG, FlightSearchCriteria(HKG_en, TPE_en))
            flightSearchCriteriaForLocale.put(Locale.TAIWAN, FlightSearchCriteria(TPE_cn, LAS_cn))
            flightSearchCriteriaForLocale.put(Locale.UK, FlightSearchCriteria(LON_en, AMS_en))

            carSearchCriteriaForLocale.put(Locale.US, LocationSearchCriteria("San Francisco", "San Francisco (and vicinity)"))
            carSearchCriteriaForLocale.put(Locale.CANADA_FRENCH, LocationSearchCriteria("Montréal", "Montréal (et environs)"))
            carSearchCriteriaForLocale.put(Locale.GERMANY, LocationSearchCriteria("Berlin", "Berlin (und Umgebung)"))
            carSearchCriteriaForLocale.put(FINLAND, LocationSearchCriteria("Helsinki", "Helsinki (ja lähialueet)"))
            carSearchCriteriaForLocale.put(Locale.FRANCE, LocationSearchCriteria("Paris", "Paris (et environs)"))
            carSearchCriteriaForLocale.put(Locale.ITALY, LocationSearchCriteria("Roma", "Roma (e dintorni)"))
            carSearchCriteriaForLocale.put(SWEDEN, LocationSearchCriteria("Stockholm", "Stockholm (med omnejd)"))
            carSearchCriteriaForLocale.put(Locale.UK, LocationSearchCriteria("London", "London (and vicinity)"))

            lxSearchCriteriaForLocale.put(Locale.US, LocationSearchCriteria("San Francisco", "San Francisco, CA"))
            lxSearchCriteriaForLocale.put(Locale.CANADA_FRENCH, LocationSearchCriteria("Montréal", "Montréal, QC"))
            lxSearchCriteriaForLocale.put(Locale.GERMANY, LocationSearchCriteria("Berlin", "Berlin, Deutschland"))
            lxSearchCriteriaForLocale.put(Locale.FRANCE, LocationSearchCriteria("Paris", "Paris, France"))
            lxSearchCriteriaForLocale.put(HONG_KONG, LocationSearchCriteria("Hong Kong", "Hong Kong (all), Hong Kong"))
            lxSearchCriteriaForLocale.put(Locale.ITALY, LocationSearchCriteria("Roma", "Roma, Italia"))
            lxSearchCriteriaForLocale.put(Locale.JAPAN, LocationSearchCriteria("ホノルル", "ホノルル, ハワイ州"))
            lxSearchCriteriaForLocale.put(SWEDEN, LocationSearchCriteria("Stockholm", "Stockholm, Sverige"))
            lxSearchCriteriaForLocale.put(Locale.UK, LocationSearchCriteria("London", "London, England"))

            pointOfSaleForLocale.put(Locale.US, PointOfSaleId.UNITED_STATES)
            pointOfSaleForLocale.put(ARGENTINA, PointOfSaleId.ARGENTINA)
            pointOfSaleForLocale.put(BRAZIL, PointOfSaleId.BRAZIL)
            pointOfSaleForLocale.put(Locale.CANADA_FRENCH, PointOfSaleId.CANADA)
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
            pointOfSaleForLocale.put(HONG_KONG, PointOfSaleId.HONG_KONG)
            pointOfSaleForLocale.put(Locale.TAIWAN, PointOfSaleId.TAIWAN)
            pointOfSaleForLocale.put(Locale.UK, PointOfSaleId.UNITED_KINGDOM)
        }

        class LocationSearchCriteria(val searchString: String,
                                     private val alternateSuggestString: String? = null) {
            val suggestString: String
                get() = alternateSuggestString ?: searchString
        }

        class FlightSearchCriteria(val departureAirport: AirportSearchCriteria,
                                   val arrivalAirport: AirportSearchCriteria,
                                   val isDropdownSearch: Boolean = false)

        class AirportSearchCriteria(val code: String, val suggestString: String = "")
    }


}
