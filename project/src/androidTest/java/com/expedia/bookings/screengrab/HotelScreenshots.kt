package com.expedia.bookings.screengrab

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.typeText
import android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.v7.widget.RecyclerView
import com.expedia.bookings.R
import com.expedia.bookings.data.LobInfo
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.pagemodels.common.SearchScreen
import com.expedia.bookings.test.pagemodels.common.SearchScreenActions
import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSiteScreen
import com.expedia.bookings.test.pagemodels.hotels.HotelResultsScreen
import com.expedia.bookings.test.pagemodels.hotels.HotelSortAndFilterScreen
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
class HotelScreenshots : BaseScreenshots() {

    private val hotelSearchCriteriaForLocale = HashMap<Locale, HotelSearchCriteria>()
    private lateinit var searchCriteria: HotelSearchCriteria

    init {
        val lasVegasHotel = "The Venetian"
        val londonHotel = "The Ampersand Hotel"
        val bangkokHotel = "Siam@Siam Design Hotel Bangkok"

        val searchLasVegas = HotelSearchCriteria(lasVegasCity, lasVegasHotel)
        val searchBangkok = HotelSearchCriteria(bangkokCity, bangkokHotel)
        val searchLondonEngland = HotelSearchCriteria(londonCity, londonHotel)
        val searchLondon = HotelSearchCriteria("London", londonHotel)

        hotelSearchCriteriaForLocale[ARGENTINA] = searchLasVegas
        hotelSearchCriteriaForLocale[AUSTRALIA] = searchBangkok
        hotelSearchCriteriaForLocale[AUSTRIA] = searchLondonEngland
        hotelSearchCriteriaForLocale[BELGIUM_DUTCH] = searchLondonEngland
        hotelSearchCriteriaForLocale[BELGIUM_FRENCH] = searchLondonEngland
        hotelSearchCriteriaForLocale[BRAZIL] = searchLasVegas
        hotelSearchCriteriaForLocale[CANADA_ENGLISH] = searchLasVegas
        hotelSearchCriteriaForLocale[CANADA_FRENCH] = searchLasVegas
        hotelSearchCriteriaForLocale[DENMARK] = searchLondon
        hotelSearchCriteriaForLocale[FINLAND] = searchLondon
        hotelSearchCriteriaForLocale[FRANCE] = searchLondonEngland
        hotelSearchCriteriaForLocale[GERMANY] = searchLondon
        hotelSearchCriteriaForLocale[HONG_KONG_ENGLISH] = searchBangkok
        hotelSearchCriteriaForLocale[HONG_KONG_CHINESE_TRADITIONAL] = searchBangkok
        hotelSearchCriteriaForLocale[HONG_KONG_CHINESE_SIMPLIFIED] = searchBangkok
        hotelSearchCriteriaForLocale[INDIA] = searchBangkok
        hotelSearchCriteriaForLocale[INDONESIA] = searchBangkok
        hotelSearchCriteriaForLocale[IRELAND] = searchLondonEngland
        hotelSearchCriteriaForLocale[ITALY] = searchLondon
        hotelSearchCriteriaForLocale[JAPAN_ENGLISH] = searchBangkok
        hotelSearchCriteriaForLocale[JAPAN_JAPANESE] = searchBangkok
        hotelSearchCriteriaForLocale[KOREA_ENGLISH] = searchBangkok
        hotelSearchCriteriaForLocale[KOREA_KOREAN] = searchBangkok
        hotelSearchCriteriaForLocale[MALAYSYA] = searchBangkok
        hotelSearchCriteriaForLocale[MEXICO] = searchLasVegas
        hotelSearchCriteriaForLocale[NETHERLANDS] = searchLondonEngland
        hotelSearchCriteriaForLocale[NEW_ZELAND] = searchBangkok
        hotelSearchCriteriaForLocale[NORWAY] = searchLondonEngland
        hotelSearchCriteriaForLocale[PHILIPPINES] = searchBangkok
        hotelSearchCriteriaForLocale[SINGAPORE] = searchBangkok
        hotelSearchCriteriaForLocale[SPAIN] = searchLondon
        hotelSearchCriteriaForLocale[SWEDEN] = searchLondon
        hotelSearchCriteriaForLocale[SWITZERLAND_FRENCH] = searchLondonEngland
        hotelSearchCriteriaForLocale[SWITZERLAND_GERMAN] = searchLondonEngland
        hotelSearchCriteriaForLocale[THAILAND_ENGLISH] = searchBangkok
        hotelSearchCriteriaForLocale[THAILAND_THAI] = searchBangkok
        hotelSearchCriteriaForLocale[TAIWAN_ENGLISH] = searchBangkok
        hotelSearchCriteriaForLocale[TAIWAN_CHINESE] = searchBangkok
        hotelSearchCriteriaForLocale[UK] = searchLondon
        hotelSearchCriteriaForLocale[USA_ENGLISH] = searchLasVegas
        hotelSearchCriteriaForLocale[USA_SPANISH] = searchLasVegas
        hotelSearchCriteriaForLocale[USA_CHINESE] = searchLasVegas
        hotelSearchCriteriaForLocale[VIETNAM] = searchBangkok
    }

    @Before
    override fun before() {
        super.before()
        Assume.assumeNotNull(hotelSearchCriteriaForLocale[LocaleUtil.getTestLocale()])
        searchCriteria = hotelSearchCriteriaForLocale[LocaleUtil.getTestLocale()]!!
    }

    @Test
    fun takeHotelScreenshots() {
        onView(Matchers.allOf(withText(LobInfo.HOTELS.labelRes), isCompletelyDisplayed())).perform(click())

        waitForViewNotYetInLayout(R.id.search_src_text)

        SearchScreen.waitForSearchEditText().perform(typeTextViaReplace(searchCriteria.city))
        Common.delay(1)
        SearchScreen.suggestionList().perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))
        Common.delay(1)

        val startDate = LocalDate.now().plusDays(90)
        val endDate = startDate.plusDays(5)
        SearchScreenActions.chooseDatesWithDialog(startDate, endDate)
        SearchScreen.searchButton().perform(click())
        Common.delay(1)

        waitForHotelResultsToLoad()

        HotelResultsScreen.hotelResultsList().perform(tinySwipeDown())

        Screengrab.screenshot("Hotel_HotelSearchResults")
        HotelSortAndFilterScreen.clickSortFilter()
        HotelSortAndFilterScreen.filterHotelName().perform(click(), typeText(searchCriteria.hotelName))
        HotelSortAndFilterScreen.clickSortFilterDoneButton()
        waitForHotelResultsToLoad()

        HotelResultsScreen.hotelResultsList().perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(2, click()))
        HotelInfoSiteScreen.waitForDetailsLoaded()
        Screengrab.screenshot("Hotel_HotelInfosite")
    }

    private class HotelSearchCriteria(val city: String, val hotelName: String)
}
