package com.expedia.bookings.test.widget

import android.app.Activity
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.HotelDetailView
import com.expedia.util.endlessObserver
import com.expedia.vm.packages.PackageHotelDetailViewModel
import org.joda.time.LocalDate
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowResourcesEB
import java.math.BigDecimal
import java.util.ArrayList
import kotlin.properties.Delegates

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowResourcesEB::class))
class PackageHotelDetailsTest {

    private var vm: PackageHotelDetailViewModel by Delegates.notNull()
    private var hotelDetailView: HotelDetailView by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()
    private var offers: HotelOffersResponse by Delegates.notNull()

    lateinit private var checkIn: LocalDate
    lateinit private var checkOut: LocalDate
    lateinit private var searchParams: HotelSearchParams

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        hotelDetailView = android.view.LayoutInflater.from(activity).inflate(R.layout.test_hotel_details_widget, null) as HotelDetailView
        vm = PackageHotelDetailViewModel(activity.applicationContext, endlessObserver { /*ignore*/ })
        hotelDetailView.viewmodel = vm

        offers = HotelOffersResponse()
        offers.hotelId = "happyPath"
        offers.hotelName = "Hotel"
        offers.hotelCity = "San Francisco"
        offers.hotelStateProvince = "CA"
        offers.hotelCountry = "US of A"
        offers.checkInDate = LocalDate.now().toString()
        offers.checkOutDate = LocalDate.now().plusDays(2).toString()
        offers.hotelGuestRating = 5.0
        offers.hotelStarRating = 5.0
        offers.deskTopOverrideNumber = false
        offers.telesalesNumber = "1-800-766-6658"
        offers.isPackage = true
    }

    @Test
    fun testNoPayByPhoneView() {
        val hotel = makeHotel()

        val lowRateInfo = HotelRate()
        lowRateInfo.strikethroughPriceToShowUsers = 100f
        lowRateInfo.currencyCode = "USD"
        lowRateInfo.packagePricePerPerson = Money()
        lowRateInfo.packagePricePerPerson.amount = BigDecimal(23.67)
        lowRateInfo.packagePricePerPerson.currencyCode = "USD"

        val rateInfo = HotelOffersResponse.RateInfo()
        rateInfo.chargeableRateInfo = lowRateInfo
        hotel.rateInfo = rateInfo

        val rooms = ArrayList<HotelOffersResponse.HotelRoomResponse>()
        rooms.add(hotel)

        offers.hotelRoomResponse = rooms

        givenHotelSearchParams()

        vm.hotelOffersSubject.onNext(offers)
        vm.addViewsAfterTransition()

        Assert.assertEquals(View.GONE, hotelDetailView.payByPhoneContainer.visibility)
    }


    private fun givenHotelSearchParams() {
        checkIn = LocalDate.now();
        checkOut = checkIn.plusDays(1)
        val suggestion = SuggestionV4()
        suggestion.gaiaId = ""
        searchParams = HotelSearchParams.Builder(activity.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                activity.resources.getInteger(R.integer.calendar_max_selectable_date_range))
                .destination(suggestion)
                .adults(2)
                .children(listOf(10,10,10))
                .startDate(checkIn)
                .endDate(checkOut).build() as HotelSearchParams
        vm.paramsSubject.onNext(searchParams)
    }

    private fun makeHotel() : HotelOffersResponse.HotelRoomResponse {
        val hotel = HotelOffersResponse.HotelRoomResponse()
        val valueAdds = ArrayList<HotelOffersResponse.ValueAdds>()
        val valueAdd = HotelOffersResponse.ValueAdds()
        valueAdd.description = "Value Add"
        valueAdds.add(valueAdd)
        hotel.valueAdds = valueAdds

        val bedTypes = ArrayList<HotelOffersResponse.BedTypes>()
        val bedType = HotelOffersResponse.BedTypes()
        bedType.id = "1"
        bedType.description = "King Bed"
        bedTypes.add(bedType)
        hotel.bedTypes = bedTypes

        hotel.currentAllotment = "1"
        hotel.payLaterOffer = hotel

        return hotel
    }
}
