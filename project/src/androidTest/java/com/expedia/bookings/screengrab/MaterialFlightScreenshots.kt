package com.expedia.bookings.screengrab

import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.typeText
import android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import android.support.test.espresso.matcher.RootMatchers
import android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.v7.widget.RecyclerView
import com.expedia.bookings.R
import com.expedia.bookings.data.LobInfo
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.CustomMatchers
import com.expedia.bookings.test.pagemodels.common.SearchScreen
import com.expedia.bookings.test.pagemodels.common.SearchScreenActions
import org.hamcrest.Matchers
import org.joda.time.LocalDate
import org.junit.Assume
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.locale.LocaleUtil
import java.util.Locale

@RunWith(JUnit4::class)
class MaterialFlightScreenshots : BaseScreenshots() {

    private val flightSearchCriteriaForLocale = HashMap<Locale, FlightSearchCriteria>()
    private lateinit var searchCriteria: FlightSearchCriteria

    private val AMS = AirportSearchCriteria("AMS")
    private val ARN = AirportSearchCriteria("ARN")
    private val BER = AirportSearchCriteria("BER")
    private val BKK = AirportSearchCriteria("BKK")
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
        flightSearchCriteriaForLocale[AUSTRALIA] = FlightSearchCriteria(NSW, BKK)
        flightSearchCriteriaForLocale[AUSTRIA] = FlightSearchCriteria(VIE, LON)
        flightSearchCriteriaForLocale[BELGIUM_DUTCH] = FlightSearchCriteria(BRU, LON)
        flightSearchCriteriaForLocale[BELGIUM_FRENCH] = FlightSearchCriteria(BRU, LON)
        flightSearchCriteriaForLocale[BRAZIL] = FlightSearchCriteria(GIG, LAS)
        flightSearchCriteriaForLocale[CANADA_FRENCH] = FlightSearchCriteria(YUL, LAS)
        flightSearchCriteriaForLocale[CANADA_ENGLISH] = FlightSearchCriteria(YUL, LAS)
        flightSearchCriteriaForLocale[SWITZERLAND_FRENCH] = FlightSearchCriteria(VIE, LON)
        flightSearchCriteriaForLocale[SWITZERLAND_GERMAN] = FlightSearchCriteria(VIE, LON)
        flightSearchCriteriaForLocale[DENMARK] = FlightSearchCriteria(CPH, LON)
        flightSearchCriteriaForLocale[FINLAND] = FlightSearchCriteria(HEL, LON)
        flightSearchCriteriaForLocale[FRANCE] = FlightSearchCriteria(PAR, LON)
        flightSearchCriteriaForLocale[GERMANY] = FlightSearchCriteria(BER, LON)
        flightSearchCriteriaForLocale[HONG_KONG_CHINESE_TRADITIONAL] = FlightSearchCriteria(HKG, BKK)
        flightSearchCriteriaForLocale[HONG_KONG_ENGLISH] = FlightSearchCriteria(HKG, BKK)
        flightSearchCriteriaForLocale[HONG_KONG_CHINESE_SIMPLIFIED] = FlightSearchCriteria(HKG, BKK)
        flightSearchCriteriaForLocale[INDONESIA] = FlightSearchCriteria(HKG, DMK, true)
        flightSearchCriteriaForLocale[IRELAND] = FlightSearchCriteria(PAR, LON)
        flightSearchCriteriaForLocale[ITALY] = FlightSearchCriteria(ROM, LON)
        flightSearchCriteriaForLocale[JAPAN_ENGLISH] = FlightSearchCriteria(HND, BKK)
        flightSearchCriteriaForLocale[JAPAN_JAPANESE] = FlightSearchCriteria(HND, BKK)
        flightSearchCriteriaForLocale[MALAYSYA] = FlightSearchCriteria(HND, BKK)
        flightSearchCriteriaForLocale[MEXICO] = FlightSearchCriteria(MEX, LAS)
        flightSearchCriteriaForLocale[NETHERLANDS] = FlightSearchCriteria(AMS, LON)
        flightSearchCriteriaForLocale[NEW_ZELAND] = FlightSearchCriteria(AKL, BKK)
        flightSearchCriteriaForLocale[NORWAY] = FlightSearchCriteria(OSL, LON)
        flightSearchCriteriaForLocale[PHILIPPINES] = FlightSearchCriteria(MNL, BKK, true)
        flightSearchCriteriaForLocale[SINGAPORE] = FlightSearchCriteria(HND, BKK)
        flightSearchCriteriaForLocale[KOREA_ENGLISH] = FlightSearchCriteria(TPE, DMK)
        flightSearchCriteriaForLocale[KOREA_KOREAN] = FlightSearchCriteria(TPE, DMK)
        flightSearchCriteriaForLocale[SPAIN] = FlightSearchCriteria(MAD, LON)
        flightSearchCriteriaForLocale[SWEDEN] = FlightSearchCriteria(ARN, LON)
        flightSearchCriteriaForLocale[THAILAND_THAI] = FlightSearchCriteria(HKT, BKK)
        flightSearchCriteriaForLocale[THAILAND_ENGLISH] = FlightSearchCriteria(HKT, BKK)
        flightSearchCriteriaForLocale[TAIWAN_CHINESE] = FlightSearchCriteria(TPE, BKK)
        flightSearchCriteriaForLocale[TAIWAN_ENGLISH] = FlightSearchCriteria(TPE, BKK)
        flightSearchCriteriaForLocale[UK] = FlightSearchCriteria(EDI, LON)
        flightSearchCriteriaForLocale[USA_ENGLISH] = FlightSearchCriteria(SFO, LAS)
        flightSearchCriteriaForLocale[USA_SPANISH] = FlightSearchCriteria(SFO, LAS)
        flightSearchCriteriaForLocale[USA_CHINESE] = FlightSearchCriteria(SFO, LAS)
    }

    @Before
    override fun before() {
        super.before()
        Assume.assumeNotNull(flightSearchCriteriaForLocale[LocaleUtil.getTestLocale()])
        searchCriteria = flightSearchCriteriaForLocale[LocaleUtil.getTestLocale()]!!
    }

    @Test
    fun takeMaterialFlightScreenshots() {
        onView(Matchers.allOf(withText(LobInfo.FLIGHTS.labelRes), isCompletelyDisplayed())).perform(click())

        if (searchCriteria.isDropdownSearch) {
            SearchScreen.origin().perform(click())
            Common.delay(2)
            Espresso.onData(CustomMatchers.airportDropDownEntryWithAirportCode(searchCriteria.departureAirport.code)).inRoot(RootMatchers.isPlatformPopup()).perform(click())
            SearchScreen.destination().perform(ViewActions.click())
            Espresso.onData(CustomMatchers.airportDropDownEntryWithAirportCode(searchCriteria.arrivalAirport.code)).inRoot(RootMatchers.isPlatformPopup()).perform(click())
        } else {
            SearchScreen.origin().perform(click())
            SearchScreen.waitForSearchEditText().perform(typeText("1"), typeTextViaReplace(searchCriteria.departureAirport.code))
            Common.delay(2)
            SearchScreen.suggestionList().perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
            Common.delay(1)
            SearchScreen.waitForSearchEditText().perform(typeText("1"), typeTextViaReplace(searchCriteria.arrivalAirport.code))
            Common.delay(2)
            SearchScreen.suggestionList().perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
        }

        Common.delay(1)

        val startDate = LocalDate.now().plusDays(35)
        val endDate = startDate.plusDays(3)
        SearchScreenActions.chooseDatesWithDialog(startDate, endDate)
        SearchScreen.searchButton().perform(ViewActions.click())

        waitForViewNotYetInLayout(R.id.sort_filter_button, 60)
        Common.delay(1)

        Screengrab.screenshot("MaterialFlight_FlightResults")
    }

    private class AirportSearchCriteria(val code: String)
    private class FlightSearchCriteria(val departureAirport: AirportSearchCriteria,
                                       val arrivalAirport: AirportSearchCriteria,
                                       val isDropdownSearch: Boolean = false)
}
