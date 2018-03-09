package com.expedia.bookings.screengrab

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.typeText
import android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.v7.widget.RecyclerView
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.pagemodels.common.SearchScreen
import com.expedia.bookings.test.pagemodels.common.SearchScreenActions
import com.expedia.bookings.test.pagemodels.hotels.HotelResultsScreen
import com.expedia.util.PackageUtil
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
class PackagesScreenshots : BaseScreenshots() {

    private val packageSearchCriteriaForLocal = HashMap<Locale, PackageSearchCriteria>()
    private lateinit var searchCriteria: PackageSearchCriteria

    init {
        packageSearchCriteriaForLocal[GERMANY] = PackageSearchCriteria(destination = londonCity)
        packageSearchCriteriaForLocal[AUSTRALIA] = PackageSearchCriteria(destination = bangkokCity)
        packageSearchCriteriaForLocal[CANADA_ENGLISH] = PackageSearchCriteria(destination = lasVegasCity)
        packageSearchCriteriaForLocal[HONG_KONG_ENGLISH] = PackageSearchCriteria(destination = bangkokCity)
        packageSearchCriteriaForLocal[HONG_KONG_CHINESE_TRADITIONAL] = PackageSearchCriteria(destination = bangkokCity)
        packageSearchCriteriaForLocal[JAPAN_JAPANESE] = PackageSearchCriteria(destination = bangkokCity)
        packageSearchCriteriaForLocal[JAPAN_ENGLISH] = PackageSearchCriteria(destination = bangkokCity)
        packageSearchCriteriaForLocal[THAILAND_ENGLISH] = PackageSearchCriteria(destination = bangkokCity)
        packageSearchCriteriaForLocal[THAILAND_THAI] = PackageSearchCriteria(destination = bangkokCity)
        packageSearchCriteriaForLocal[MALAYSYA] = PackageSearchCriteria(destination = bangkokCity)
        packageSearchCriteriaForLocal[NEW_ZELAND] = PackageSearchCriteria(destination = bangkokCity)
        packageSearchCriteriaForLocal[SINGAPORE] = PackageSearchCriteria(destination = bangkokCity)
        packageSearchCriteriaForLocal[UK] = PackageSearchCriteria(destination = londonCity)
        packageSearchCriteriaForLocal[USA_ENGLISH] = PackageSearchCriteria(destination = lasVegasCity)
        packageSearchCriteriaForLocal[USA_SPANISH] = PackageSearchCriteria(destination = lasVegasCity)
        packageSearchCriteriaForLocal[USA_CHINESE] = PackageSearchCriteria(destination = lasVegasCity)
    }

    @Before
    override fun before() {
        super.before()
        Assume.assumeNotNull(packageSearchCriteriaForLocal[LocaleUtil.getTestLocale()])
        searchCriteria = packageSearchCriteriaForLocal[LocaleUtil.getTestLocale()]!!
    }

    @Test
    fun takePackagesScreenshots() {
        val packagesTitle = PackageUtil.packageTitle(activityRule.activity)
        onView(Matchers.allOf(withText(packagesTitle), isCompletelyDisplayed())).perform(click())

        SearchScreen.origin().perform(click())
        SearchScreen.waitForSearchEditText().perform(typeText(searchCriteria.origin))
        Common.delay(2)
        SearchScreen.suggestionList().perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        Common.delay(1)
        SearchScreen.waitForSearchEditText().perform(typeText(searchCriteria.destination))
        Common.delay(2)
        SearchScreen.suggestionList().perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
        Common.delay(1)

        val startDate = LocalDate.now().plusDays(90)
        val endDate = startDate.plusDays(5)
        SearchScreenActions.chooseDatesWithDialog(startDate, endDate)
        SearchScreen.searchButton().perform(click())
        Common.delay(3)

        waitForHotelResultsToLoad()
        HotelResultsScreen.hotelResultsList().perform(tinySwipeDown())

        Screengrab.screenshot("Packages_HotelSearchResults")
    }

    private class PackageSearchCriteria(val destination: String, val origin: String = "San Francisco")
}
