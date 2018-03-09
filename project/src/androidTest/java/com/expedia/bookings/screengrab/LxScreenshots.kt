package com.expedia.bookings.screengrab

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.v7.widget.RecyclerView
import com.expedia.bookings.R
import com.expedia.bookings.data.LobInfo
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.pagemodels.common.SearchScreen
import com.expedia.bookings.test.pagemodels.common.SearchScreenActions
import com.expedia.bookings.test.pagemodels.lx.LXScreen
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
class LxScreenshots : BaseScreenshots() {

    private val lxSearchCriteriaForLocale = HashMap<Locale, LocationSearchCriteria>()
    private lateinit var searchCriteria: LocationSearchCriteria

    init {
        lxSearchCriteriaForLocale[USA_ENGLISH] = LocationSearchCriteria("San Francisco", "San Francisco, CA")
        lxSearchCriteriaForLocale[CANADA_FRENCH] = LocationSearchCriteria("Montréal", "Montréal, QC")
        lxSearchCriteriaForLocale[GERMANY] = LocationSearchCriteria("Berlin", "Berlin, Deutschland")
        lxSearchCriteriaForLocale[FRANCE] = LocationSearchCriteria("Paris", "Paris, France")
        lxSearchCriteriaForLocale[TAIWAN_CHINESE] = LocationSearchCriteria("Hong Kong", "Hong Kong (all), Hong Kong")
        lxSearchCriteriaForLocale[ITALY] = LocationSearchCriteria("Roma", "Roma, Italia")
        lxSearchCriteriaForLocale[JAPAN_JAPANESE] = LocationSearchCriteria("ホノルル", "ホノルル, ハワイ州")
        lxSearchCriteriaForLocale[SWEDEN] = LocationSearchCriteria("Stockholm", "Stockholm, Sverige")
        lxSearchCriteriaForLocale[UK] = LocationSearchCriteria("London", "London, England")
    }

    @Before
    override fun before() {
        super.before()
        Assume.assumeNotNull(lxSearchCriteriaForLocale[LocaleUtil.getTestLocale()])
        searchCriteria = lxSearchCriteriaForLocale[LocaleUtil.getTestLocale()]!!
    }

    @Test
    fun takeLxScreenshots() {
        onView(Matchers.allOf(withText(LobInfo.ACTIVITIES.labelRes), isCompletelyDisplayed())).perform(click())

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

        Screengrab.screenshot("LX_LXDetails")
    }

    private class LocationSearchCriteria(val searchString: String,
                                         val suggestString: String)
}
