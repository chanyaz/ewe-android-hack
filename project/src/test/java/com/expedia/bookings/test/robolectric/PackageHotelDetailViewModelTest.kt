package com.expedia.bookings.test.robolectric

import android.content.Context
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.packages.PackageOffersResponse
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.data.packages.PackageSearchResponse
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.CurrencyUtils
import com.expedia.bookings.utils.DateUtils
import com.expedia.vm.BaseHotelDetailViewModel
import com.expedia.vm.packages.PackageHotelDetailViewModel
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import rx.observers.TestSubscriber
import java.math.BigDecimal
import java.util.ArrayList
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class PackageHotelDetailViewModelTest {
    private var testViewModel: PackageHotelDetailViewModel by Delegates.notNull()

    private var context: Context by Delegates.notNull()
    private var offer1: HotelOffersResponse by Delegates.notNull()
    private val expectedTotalPriceWithMandatoryFees = 42f

    @Before fun before() {
        context = RuntimeEnvironment.application
        testViewModel = PackageHotelDetailViewModel(context)

        offer1 = HotelOffersResponse()
        offer1.hotelId = "hotel1"
        offer1.hotelName = "hotel1"
        offer1.hotelCity = "hotel1"
        offer1.hotelStateProvince = "hotel1"
        offer1.hotelCountry = "USA"
        offer1.latitude = 1.0
        offer1.longitude = 2.0
        offer1.hotelRoomResponse = makeHotel()
    }

    @Test fun packageSearchInfoShouldShow() {
        var searchParams = createSearchParams()
        searchParams.forPackage = true
        val response = PackageSearchResponse()
        response.packageInfo = PackageSearchResponse.PackageInfo()
        response.packageInfo.hotelCheckinDate = PackageSearchResponse.HotelCheckinDate()
        response.packageInfo.hotelCheckinDate.isoDate = "2016-09-07"
        response.packageInfo.hotelCheckoutDate = PackageSearchResponse.HotelCheckoutDate()
        response.packageInfo.hotelCheckoutDate.isoDate = "2016-09-08"
        Db.setPackageResponse(response)
        testViewModel.paramsSubject.onNext(searchParams)
        val dtf = DateTimeFormat.forPattern("yyyy-MM-dd");

        val dates = DateUtils.localDateToMMMd(dtf.parseLocalDate(Db.getPackageResponse().getHotelCheckInDate())) + " - " +
                DateUtils.localDateToMMMd(dtf.parseLocalDate(Db.getPackageResponse().getHotelCheckOutDate()))
        assertEquals(dates, testViewModel.searchDatesObservable.value)
        assertEquals("$dates, ${searchParams.guests} guests", testViewModel.searchInfoObservable.value)
    }

    @Test @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun resortFeeShowsForPackages() {
        CurrencyUtils.initMap(RuntimeEnvironment.application)
        val vm = PackageHotelDetailViewModel(RuntimeEnvironment.application)
        val testSubscriber = TestSubscriber<String>()
        vm.hotelResortFeeObservable.subscribe(testSubscriber)
        vm.paramsSubject.onNext(createSearchParams())

        makeResortFeeResponse(vm)

        testSubscriber.requestMore(100)
        assertEquals("$20", testSubscriber.onNextEvents[1])
        assertEquals("per night", context.getString(vm.getFeeTypeText()))
    }

    private fun makeResortFeeResponse(vm: BaseHotelDetailViewModel) {
        offer1.hotelRoomResponse.clear()
        val packageSearchParams = PackageSearchParams.Builder(30, 330)
                .adults(1)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .destination(SuggestionV4())
                .origin(SuggestionV4())
                .build() as PackageSearchParams

        val packageOffer = PackageOffersResponse()

        val packageHotelOffer = PackageOffersResponse.PackageHotelOffer()
        packageHotelOffer.hotelOffer = makeHotel().first()
        packageHotelOffer.packagePricing = PackageOffersResponse.PackagePricing()
        packageHotelOffer.packagePricing.hotelPricing = PackageOffersResponse.HotelPricing()
        packageHotelOffer.packagePricing.hotelPricing.mandatoryFees = PackageOffersResponse.MandatoryFees()
        packageHotelOffer.packagePricing.hotelPricing.mandatoryFees.feeTotal = Money(20, "USD")
        packageHotelOffer.cancellationPolicy = PackageOffersResponse.CancellationPolicy()
        packageHotelOffer.cancellationPolicy.hasFreeCancellation = false
        packageHotelOffer.pricePerPerson = Money()
        packageHotelOffer.pricePerPerson.amount = BigDecimal(25.00)
        packageHotelOffer.pricePerPerson.currencyCode = "USD"
        packageOffer.packageHotelOffers = arrayListOf(packageHotelOffer)

        val offer = HotelOffersResponse.convertToHotelOffersResponse(offer1, HotelOffersResponse.convertPSSHotelRoomResponse(packageOffer), packageSearchParams.startDate.toString(), packageSearchParams.endDate.toString())

        vm.hotelOffersSubject.onNext(offer)
        vm.addViewsAfterTransition()
    }

    private fun makeHotel(): ArrayList<HotelOffersResponse.HotelRoomResponse> {
        var rooms = ArrayList<HotelOffersResponse.HotelRoomResponse>();

        var hotel = HotelOffersResponse.HotelRoomResponse()
        var valueAdds = ArrayList<HotelOffersResponse.ValueAdds>()
        var valueAdd = HotelOffersResponse.ValueAdds()
        valueAdd.description = "Value Add"
        valueAdds.add(valueAdd)
        hotel.valueAdds = valueAdds

        var bedTypes = ArrayList<HotelOffersResponse.BedTypes>()
        var bedType = HotelOffersResponse.BedTypes()
        bedType.id = "1"
        bedType.description = "King Bed"
        bedTypes.add(bedType)
        hotel.bedTypes = bedTypes

        hotel.currentAllotment = "1"

        var lowRateInfo = HotelRate()
        lowRateInfo.discountPercent = -20f
        lowRateInfo.currencyCode = "USD"

        var rateInfo = HotelOffersResponse.RateInfo()
        rateInfo.chargeableRateInfo = lowRateInfo
        rateInfo.chargeableRateInfo.totalPriceWithMandatoryFees = expectedTotalPriceWithMandatoryFees
        hotel.rateInfo = rateInfo

        rooms.add(hotel)
        return rooms
    }

    private fun createSearchParams(): HotelSearchParams {
        val suggestionV4 = SuggestionV4()
        suggestionV4.gaiaId = "1234"
        val regionNames = SuggestionV4.RegionNames()
        regionNames.displayName = "San Francisco"
        regionNames.shortName = "SFO"
        suggestionV4.regionNames = regionNames
        val childList = ArrayList<Int>()
        childList.add(1)
        var checkIn = LocalDate.now().plusDays(2)
        var checkOut = LocalDate.now().plusDays(5)
        val numAdults = 2
        return HotelSearchParams.Builder(0, 0)
                .destination(suggestionV4)
                .startDate(checkIn)
                .endDate(checkOut)
                .adults(numAdults)
                .children(childList).build() as HotelSearchParams
    }
}