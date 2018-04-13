package com.expedia.bookings.packages.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.packages.PackageOffersResponse
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.data.packages.PackageSearchResponse
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.CurrencyUtils
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.vm.BaseHotelDetailViewModel
import com.mobiata.android.util.SettingUtils
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.math.BigDecimal
import java.util.ArrayList
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
@Config(shadows = [ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class])
class PackageHotelDetailViewModelTest {
    private var testViewModel: PackageHotelDetailViewModel by Delegates.notNull()

    private var context: Context by Delegates.notNull()
    private var offer1: HotelOffersResponse by Delegates.notNull()
    private val expectedTotalPriceWithMandatoryFees = 42f

    @Before
    fun before() {
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

    @Test
    fun packageSearchInfoShouldShow() {
        val searchParams = createSearchParams()
        searchParams.forPackage = true
        val response = PackageSearchResponse()
        response.packageInfo = PackageSearchResponse.PackageInfo()
        response.packageInfo.hotelCheckinDate = PackageSearchResponse.HotelCheckinDate()
        response.packageInfo.hotelCheckinDate.isoDate = "2016-09-07"
        response.packageInfo.hotelCheckoutDate = PackageSearchResponse.HotelCheckoutDate()
        response.packageInfo.hotelCheckoutDate.isoDate = "2016-09-08"
        Db.setPackageResponse(response)
        testViewModel.paramsSubject.onNext(searchParams)
        val dtf = DateTimeFormat.forPattern("yyyy-MM-dd")

        val dates = LocaleBasedDateFormatUtils.localDateToMMMd(dtf.parseLocalDate(Db.getPackageResponse().getHotelCheckInDate())) + " - " +
                LocaleBasedDateFormatUtils.localDateToMMMd(dtf.parseLocalDate(Db.getPackageResponse().getHotelCheckOutDate()))
        assertEquals(dates, testViewModel.searchDatesObservable.value)
        assertEquals(dates, testViewModel.searchInfoObservable.value)
        assertEquals("${searchParams.guests} guests", testViewModel.searchInfoGuestsObservable.value)
    }

    @Test
    fun testOfferReturnedNonNullHotelRoomResponse() {
        testViewModel.hotelOffersSubject.onNext(offer1)

        assertEquals(Money(250, "USD"), testViewModel.bundlePricePerPersonObservable.value)
        assertEquals(Money(500, "USD"), testViewModel.bundleTotalPriceObservable.value)
        assertEquals(Money(100, "USD"), testViewModel.bundleSavingsObservable.value)
    }

    @Test
    fun testOfferReturnedNullHotelRoomResponse() {
        offer1.hotelRoomResponse = null
        testViewModel.hotelOffersSubject.onNext(offer1)

        assertEquals(null, testViewModel.bundlePricePerPersonObservable.value)
        assertEquals(null, testViewModel.bundleTotalPriceObservable.value)
        assertEquals(null, testViewModel.bundleSavingsObservable.value)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testGetFeeTypeTextUSPOS() {
        val currentPOS = PointOfSale.getPointOfSale().pointOfSaleId
        setPOS(PointOfSaleId.UNITED_STATES)
        val feeTypeText = testViewModel.getFeeTypeText()
        assertEquals(R.string.rate_per_night, feeTypeText)
        setPOS(currentPOS)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testGetFeeTypeTextNonUSPOS() {
        val currentPOS = PointOfSale.getPointOfSale().pointOfSaleId
        setPOS(PointOfSaleId.UNITED_KINGDOM)
        val feeTypeText = testViewModel.getFeeTypeText()
        assertEquals(R.string.total_fee, feeTypeText)
        setPOS(currentPOS)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testShouldShowBookByPhoneNonUSPOS() {
        val currentPOS = PointOfSale.getPointOfSale().pointOfSaleId
        setPOS(PointOfSaleId.UNITED_KINGDOM)
        val showBookByPhone = testViewModel.shouldShowBookByPhone()
        assertEquals(false, showBookByPhone)
        setPOS(currentPOS)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testShouldShowBookByPhoneUSPOSWithNoTelesalesNumber() {
        val currentPOS = PointOfSale.getPointOfSale().pointOfSaleId
        testViewModel.hotelOffersSubject.onNext(offer1)
        setPOS(PointOfSaleId.UNITED_STATES)

        val showBookByPhone = testViewModel.shouldShowBookByPhone()
        assertEquals(false, showBookByPhone)
        setPOS(currentPOS)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testShouldShowBookByPhoneUSPOSWithTelesalesNumber() {
        val currentPOS = PointOfSale.getPointOfSale().pointOfSaleId
        offer1.packageTelesalesNumber = "1111111111"
        testViewModel.hotelOffersSubject.onNext(offer1)
        setPOS(PointOfSaleId.UNITED_STATES)

        val showBookByPhone = testViewModel.shouldShowBookByPhone()
        assertEquals(true, showBookByPhone)
        setPOS(currentPOS)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun resortFeeShowsForPackages() {
        CurrencyUtils.initMap(RuntimeEnvironment.application)
        val vm = PackageHotelDetailViewModel(RuntimeEnvironment.application)
        val testSubscriber = TestObserver<String>()
        vm.hotelResortFeeObservable.subscribe(testSubscriber)
        vm.paramsSubject.onNext(createSearchParams())

        makeResortFeeResponse(vm)

        assertEquals("$20", testSubscriber.values()[1])
        assertEquals("per night", context.getString(vm.getFeeTypeText()))
    }

    private fun setPOS(pos: PointOfSaleId) {
        SettingUtils.save(RuntimeEnvironment.application, R.string.PointOfSaleKey, pos.id.toString())
        PointOfSale.onPointOfSaleChanged(RuntimeEnvironment.application)
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
        val rooms = ArrayList<HotelOffersResponse.HotelRoomResponse>()

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

        val lowRateInfo = HotelRate()
        lowRateInfo.discountPercent = -20f
        lowRateInfo.currencyCode = "USD"
        lowRateInfo.packagePricePerPerson = Money(250, "USD")
        lowRateInfo.packageTotalPrice = Money(500, "USD")
        lowRateInfo.packageSavings = Money(100, "USD")

        val rateInfo = HotelOffersResponse.RateInfo()
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
        val checkIn = LocalDate.now().plusDays(2)
        val checkOut = LocalDate.now().plusDays(5)
        val numAdults = 2
        return HotelSearchParams.Builder(0, 0)
                .destination(suggestionV4)
                .startDate(checkIn)
                .endDate(checkOut)
                .adults(numAdults)
                .children(childList).build() as HotelSearchParams
    }
}
