package com.expedia.bookings.test.ui.phone.tests.hotels

import android.view.View
import android.widget.Button
import android.widget.ToggleButton
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.SuggestionV4
import com.expedia.bookings.presenter.hotel.HotelPresenter
import com.expedia.bookings.presenter.hotel.HotelSearchPresenter
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.ui.HotelActivity
import com.expedia.bookings.utils.DateUtils
import com.mobiata.android.time.widget.CalendarPicker
import org.joda.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric


/**
 * Created by tharman on 6/22/15.
 */
@RunWith(RobolectricRunner::class)
public class MaterialHotelSearchTest() {

    val activity : HotelActivity = Robolectric.buildActivity(javaClass<HotelActivity>()).create().get()
    val hotelPresenter : HotelPresenter = activity.findViewById(R.id.hotel_presenter) as HotelPresenter
    val hotelSearchPresenter : HotelSearchPresenter = activity.findViewById(R.id.widget_hotel_params) as HotelSearchPresenter
    val checkInDate = LocalDate.now().plusDays(2)
    val checkOutDate = checkInDate.plusDays(3)

    @Test
    public fun searchWithInvalidParams() {
        hotelSearchPresenter.findViewById(R.id.search_btn).performClick()
        assertEquals(View.GONE, hotelPresenter.findViewById(R.id.widget_hotel_results).getVisibility())
    }

    @Test
    public fun searchWithNoDates() {
        val searchBtn : Button = hotelSearchPresenter.findViewById(R.id.search_btn) as Button
        val city = SuggestionV4()
        city.regionNames = SuggestionV4.RegionNames()
        city.regionNames.shortName = "San Francisco"

        hotelSearchPresenter.hotelSearchParamsBuilder
                .city(city)
                .adults(1)
        searchBtn.performClick()
        assertEquals(View.GONE, hotelPresenter.findViewById(R.id.widget_hotel_results).getVisibility())
    }

    @Test
    public fun searchWithValidParams() {
        val searchBtn : Button = hotelSearchPresenter.findViewById(R.id.search_btn) as Button
        val city = SuggestionV4()
        city.regionNames = SuggestionV4.RegionNames()
        city.regionNames.shortName = "San Francisco"

        hotelSearchPresenter.hotelSearchParamsBuilder
                .city(city)
                .adults(1)
                .checkIn(checkInDate)
                .checkOut(checkOutDate)
        searchBtn.performClick()
        assertEquals(View.VISIBLE, hotelPresenter.findViewById(R.id.widget_hotel_results).getVisibility())
    }

    @Test
    public fun testTravelersFieldText() {
        val travelerToggleButton : ToggleButton = hotelSearchPresenter.findViewById(R.id.select_traveler) as ToggleButton
        val adultPlusButton = hotelSearchPresenter.findViewById(R.id.adults_plus)
        val adultMinusButton = hotelSearchPresenter.findViewById(R.id.adults_minus)
        val childPlusButton = hotelSearchPresenter.findViewById(R.id.children_plus)
        val childMinusButton = hotelSearchPresenter.findViewById(R.id.children_minus)

        // test: default:- 1 traveler
        var expectedString = "1 Adult"
        assertEquals(expectedString, travelerToggleButton.getText())

        // test: add 1 adult
        expectedString = "2 Adults"
        travelerToggleButton.performClick()
        adultPlusButton.performClick()
        assertEquals(expectedString, travelerToggleButton.getText())

        // test: add 2 children
        expectedString = "2 Adults, 2 Children"
        childPlusButton.performClick()
        childPlusButton.performClick()
        assertEquals(expectedString, travelerToggleButton.getText())

        // test: remove 1 adult and 1 child
        expectedString = "1 Adult, 1 Child"
        adultMinusButton.performClick()
        childMinusButton.performClick()
        assertEquals(expectedString, travelerToggleButton.getText())
    }

    @Test
    public fun testCheckInOutFieldText() {
        val calendarPicker: CalendarPicker = hotelSearchPresenter.findViewById(R.id.calendar) as CalendarPicker
        val checkInDate = LocalDate.now().plusDays(2)
        val checkOutDate = checkInDate.plusDays(3)
        val selectDateButton : ToggleButton = hotelSearchPresenter.findViewById(R.id.select_date) as ToggleButton
        var expectedDateString : String

        // test: neither checkIn or checkOut date selected
        expectedDateString = "Select dates"
        calendarPicker.setSelectedDates(null, null)
        assertEquals(expectedDateString, selectDateButton.getText())

        // test: checkIn date only selected
        expectedDateString = DateUtils.localDateToMMMd(checkInDate)
        calendarPicker.setSelectedDates(checkInDate, null)
        assertEquals(expectedDateString, selectDateButton.getText())

        // test: Both dates selected
        expectedDateString = DateUtils.localDateToMMMd(checkInDate) + " - " + DateUtils.localDateToMMMd(checkOutDate)
        calendarPicker.setSelectedDates(checkInDate, checkOutDate)
        assertEquals(expectedDateString, selectDateButton.getText())
    }
}