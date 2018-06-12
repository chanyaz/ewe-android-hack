package com.expedia.bookings.test.robolectric

import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.FlightTripResponse
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.FareFamilyAmenityItemWidget
import com.expedia.bookings.widget.flights.FareFamilyItemWidget
import com.expedia.bookings.flights.vm.FareFamilyItemViewModel
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import io.reactivex.subjects.PublishSubject
import java.math.BigDecimal
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class FareFamilyAmenityDialogTest {
    private lateinit var activity: FragmentActivity
    lateinit var widget: FareFamilyItemWidget

    @Before
    fun before() {
        activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(activity).defaultFlightComponents()
        widget = LayoutInflater.from(activity).inflate(R.layout.flight_fare_family_item_layout, null) as FareFamilyItemWidget
        addFlightSearchParams()
        widget.bindViewModel(getViewModel())
    }

    @Test
    fun testAmenitiesDialogVisibility() {
        widget.showMoreContainer.performClick()
        assertTrue(widget.fareFamilyAmenitiesDialog.isShowing)
    }

    @Test
    fun testFlightDetails() {
        assertEquals(View.VISIBLE, widget.fareFamilyAmenitiesDialogView.fareFamilyNameText.visibility)
        assertEquals(widget.viewModel?.fareFamilyName, widget.fareFamilyAmenitiesDialogView.fareFamilyNameText.text.toString())
        assertEquals(View.VISIBLE, widget.fareFamilyAmenitiesDialogView.airlineNameText.visibility)
        assertEquals(View.VISIBLE, widget.fareFamilyAmenitiesDialogView.fareFamilyCabinClassNameText.visibility)
        assertEquals(widget.viewModel?.cabinClass, widget.fareFamilyAmenitiesDialogView.fareFamilyCabinClassNameText.text.toString())
    }

    @Test
    fun testPrimaryAmenitiesDetails() {
        assertEquals(3, widget.fareFamilyAmenitiesDialogView.fareFamilyPrimaryAmenitiesWidget.primaryAmenitiesContainer.childCount)
        val firstPrimaryAmenity = (widget.fareFamilyAmenitiesDialogView.fareFamilyPrimaryAmenitiesWidget.primaryAmenitiesContainer.getChildAt(0) as FareFamilyAmenityItemWidget).getChildAt(0) as LinearLayout
        assertEquals("Checked Bags", (firstPrimaryAmenity.getChildAt(1) as TextView).text.toString())
        val secondPrimaryAmenity = (widget.fareFamilyAmenitiesDialogView.fareFamilyPrimaryAmenitiesWidget.primaryAmenitiesContainer.getChildAt(1) as FareFamilyAmenityItemWidget).getChildAt(0) as LinearLayout
        assertEquals("Carry on Bag", (secondPrimaryAmenity.getChildAt(1) as TextView).text.toString())
        val thirdPrimaryAmenity = (widget.fareFamilyAmenitiesDialogView.fareFamilyPrimaryAmenitiesWidget.primaryAmenitiesContainer.getChildAt(2) as FareFamilyAmenityItemWidget).getChildAt(0) as LinearLayout
        assertEquals("Seat Choice", (thirdPrimaryAmenity.getChildAt(1) as TextView).text.toString())
    }

    @Test
    fun testPrimaryAmenitiesContDesc() {
        var primaryAmenity = (widget.fareFamilyAmenitiesDialogView.fareFamilyPrimaryAmenitiesWidget.primaryAmenitiesContainer.getChildAt(0))
        assertEquals("Checked Bags available for a fee.  ", primaryAmenity.contentDescription)
        primaryAmenity = (widget.fareFamilyAmenitiesDialogView.fareFamilyPrimaryAmenitiesWidget.primaryAmenitiesContainer.getChildAt(1) as FareFamilyAmenityItemWidget)
        assertEquals("Carry on Bag available.  ", primaryAmenity.contentDescription)
        primaryAmenity = (widget.fareFamilyAmenitiesDialogView.fareFamilyPrimaryAmenitiesWidget.primaryAmenitiesContainer.getChildAt(2) as FareFamilyAmenityItemWidget)
        assertEquals("Seat Choice available.  ", primaryAmenity.contentDescription)
    }

    @Test
    fun testOtherAmenitiesContDesc() {
        assertEquals("Seat Choice available.  ", (widget.fareFamilyAmenitiesDialogView.amenitiesList.getChildAt(2) as FareFamilyAmenityItemWidget).contentDescription)
        assertEquals("Priority Boarding available for a fee.  ", (widget.fareFamilyAmenitiesDialogView.amenitiesList.getChildAt(7) as FareFamilyAmenityItemWidget).contentDescription)
    }

    @Test
    fun testOtherAmenitiesDetails() {
        assertEquals(8, widget.fareFamilyAmenitiesDialogView.amenitiesList.childCount)
        assertEquals("Included", (widget.fareFamilyAmenitiesDialogView.amenitiesList.getChildAt(1) as TextView).text.toString())
        assertEquals("Seat Choice", (((widget.fareFamilyAmenitiesDialogView.amenitiesList.getChildAt(2) as FareFamilyAmenityItemWidget).getChildAt(0) as LinearLayout).getChildAt(1) as TextView).text.toString())
        assertEquals("Fee Applies", (widget.fareFamilyAmenitiesDialogView.amenitiesList.getChildAt(4) as TextView).text.toString())
        assertEquals("Priority Boarding", (((widget.fareFamilyAmenitiesDialogView.amenitiesList.getChildAt(7) as FareFamilyAmenityItemWidget).getChildAt(0) as LinearLayout).getChildAt(1) as TextView).text.toString())
    }

    private fun getViewModel(): FareFamilyItemViewModel {
        val totalPriceMoney = Money()
        totalPriceMoney.amount = BigDecimal("2558.40")
        totalPriceMoney.currencyCode = "USD"
        totalPriceMoney.formattedPrice = "$2,558.40"
        totalPriceMoney.formattedWholePrice = "$2,558"
        totalPriceMoney.roundedAmount = BigDecimal("2558")

        val deltaTotalPriceMoney = Money()
        deltaTotalPriceMoney.amount = BigDecimal("1912.00")
        deltaTotalPriceMoney.currencyCode = "USD"
        deltaTotalPriceMoney.formattedPrice = "+$1,912.00"
        deltaTotalPriceMoney.formattedWholePrice = "+$1,912.00"

        val fareFamilyComponentMap = HashMap<String, HashMap<String, String>>()
        fareFamilyComponentMap.put("notoffered", HashMap<String, String>())
        fareFamilyComponentMap.put("unknown", HashMap<String, String>())
        var amenityMap = HashMap<String, String>()
        amenityMap.put("SeatReservation", "Seat Choice")
        amenityMap.put("CarryOnBag", "Carry on Bag")
        fareFamilyComponentMap.put("included", amenityMap)
        amenityMap = HashMap<String, String>()
        amenityMap.put("Bags", "Checked Bags")
        amenityMap.put("ExtraLegroom", "Premium Seat")
        amenityMap.put("PriorityBoarding", "Priority Boarding")
        fareFamilyComponentMap.put("chargeable", amenityMap)

        val fareFamilyDetails = FlightTripResponse.FareFamilyDetails(
                Strings.capitalize("ECONOMY FLEXIBLE", Locale.US),
                "ECOFLEX",
                "coach",
                totalPriceMoney,
                deltaTotalPriceMoney,
                true,
                fareFamilyComponentMap
        )

        return FareFamilyItemViewModel(activity, fareFamilyDetails, false, PublishSubject.create())
    }

    private fun addFlightSearchParams() {
        val origin = getFakeSuggestion("SFO")
        val destination = getFakeSuggestion("SEA")
        val params = FlightSearchParams.Builder(100, 500)
                .origin(origin)
                .destination(destination)
                .startDate(LocalDate.now().withYear(2019).withMonthOfYear(9).withDayOfMonth(6))
                .endDate(LocalDate.now().withYear(2021).withMonthOfYear(9).withDayOfMonth(6))
                .adults(1).build() as FlightSearchParams
        Db.setFlightSearchParams(params)
    }

    private fun getFakeSuggestion(airportCode: String): SuggestionV4 {
        val suggestion = SuggestionV4()
        val hierarchyInfo = SuggestionV4.HierarchyInfo()
        val airport = SuggestionV4.Airport()
        airport.airportCode = airportCode
        hierarchyInfo.airport = airport
        val country = SuggestionV4.Country()
        country.name = ""
        hierarchyInfo.country = country
        suggestion.hierarchyInfo = hierarchyInfo

        val regionName = SuggestionV4.RegionNames()
        regionName.shortName = "San Francisco, CA (SFO-San Francisco Intl.)"
        regionName.displayName = "San Francisco, CA (<B>SFO</B>-San Francisco Intl.)"
        regionName.fullName = "San Francisco, CA, United States (<B>SFO</B>-San Francisco Intl.)"
        suggestion.regionNames = regionName
        return suggestion
    }
}
