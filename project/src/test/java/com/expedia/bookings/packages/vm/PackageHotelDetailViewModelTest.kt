package com.expedia.bookings.packages.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.packages.PackageSearchParams

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
import com.expedia.bookings.test.MockPackageServiceTestRule
import com.expedia.bookings.test.robolectric.PackageTestUtil.Companion.dummyMIDItemRoomOffer
import com.expedia.bookings.test.robolectric.PackageTestUtil.Companion.dummyMidHotelRoomOffer
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.util.PackageCalendarRules
import org.junit.Rule
import java.math.BigDecimal
import java.util.ArrayList
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@Config(shadows = [ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class])
class PackageHotelDetailViewModelTest {
    private var testViewModel: PackageHotelDetailViewModel by Delegates.notNull()

    private var context: Context by Delegates.notNull()
    private var hotelOffersResponse: HotelOffersResponse by Delegates.notNull()
    private val expectedTotalPriceWithMandatoryFees = 42f
    private val perNightTestSubscriber = TestObserver.create<Boolean>()

    val mockPackageServiceRule: MockPackageServiceTestRule = MockPackageServiceTestRule()
        @Rule get

    private lateinit var rules: PackageCalendarRules

    @Before fun before() {
        context = RuntimeEnvironment.application
        rules = PackageCalendarRules(context)
        testViewModel = PackageHotelDetailViewModel(context)

        hotelOffersResponse = HotelOffersResponse()
        hotelOffersResponse.hotelId = "hotel1"
        hotelOffersResponse.hotelName = "hotel1"
        hotelOffersResponse.hotelCity = "hotel1"
        hotelOffersResponse.hotelStateProvince = "hotel1"
        hotelOffersResponse.hotelCountry = "USA"
        hotelOffersResponse.latitude = 1.0
        hotelOffersResponse.longitude = 2.0
        hotelOffersResponse.hotelRoomResponse = makeHotel()
    }

    @Test
    fun packageSearchInfoShouldShow() {
        val params = mockPackageServiceRule.getPackageParams().convertToHotelSearchParams(rules.getMaxSearchDurationDays(), rules.getMaxDateRange())
        val response = mockPackageServiceRule.getMIDHotelResponse()
        Db.setPackageResponse(response)

        testViewModel.paramsSubject.onNext(params)
        val dtf = DateTimeFormat.forPattern("yyyy-MM-dd")

        val dates = LocaleBasedDateFormatUtils.localDateToMMMd(dtf.parseLocalDate(Db.getPackageResponse().getHotelCheckInDate())) + " - " +
                LocaleBasedDateFormatUtils.localDateToMMMd(dtf.parseLocalDate(Db.getPackageResponse().getHotelCheckOutDate()))
        assertEquals(dates, testViewModel.searchDatesObservable.value)
        assertEquals(dates, testViewModel.searchInfoObservable.value)
        assertEquals("${params.guests} guests", testViewModel.searchInfoGuestsObservable.value)
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
        testViewModel.hotelOffersSubject.onNext(hotelOffersResponse)
        setPOS(PointOfSaleId.UNITED_STATES)

        val showBookByPhone = testViewModel.shouldShowBookByPhone()
        assertEquals(false, showBookByPhone)
        setPOS(currentPOS)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testShouldShowBookByPhoneUSPOSWithTelesalesNumber() {
        val currentPOS = PointOfSale.getPointOfSale().pointOfSaleId
        val expectedNumber = "1111111111"
        hotelOffersResponse.packageTelesalesNumber = expectedNumber
        testViewModel.hotelOffersSubject.onNext(hotelOffersResponse)
        setPOS(PointOfSaleId.UNITED_STATES)

        val showBookByPhone = testViewModel.shouldShowBookByPhone()
        assertEquals(true, showBookByPhone)
        assertEquals(expectedNumber, testViewModel.getTelesalesNumber())
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

        assertEquals("$221.10", testSubscriber.values()[1])
        assertEquals("per night", context.getString(vm.getFeeTypeText()))
    }

    @Test
    fun testOfferReturnedWithNullRoomResponse_noPricesAreChanged() {
        hotelOffersResponse.hotelRoomResponse = null
        testViewModel.hotelOffersSubject.onNext(hotelOffersResponse)
        assertNoEmissionsToObservables()
    }

    @Test
    fun testOfferReturnedWithEmptyRoomResponse_noPricesAreChanged() {
        hotelOffersResponse.hotelRoomResponse = emptyList()
        testViewModel.hotelOffersSubject.onNext(hotelOffersResponse)
        assertNoEmissionsToObservables()
    }

    @Test
    fun testOfferReturnedNonNullHotelRoomResponse() {
        val hotelRoomResponse = getMockHotelRoomResponse()
        hotelOffersResponse.hotelRoomResponse = listOf(hotelRoomResponse)

        testViewModel.hotelOffersSubject.onNext(hotelOffersResponse)

        val pricePerPerson = hotelRoomResponse.rateInfo.chargeableRateInfo.packagePricePerPerson
        assertEquals(Money(BigDecimal(pricePerPerson.amount.toDouble()), pricePerPerson.currencyCode), testViewModel.bundlePricePerPersonObservable.value)
        assertEquals(hotelRoomResponse.rateInfo.chargeableRateInfo.packageTotalPrice, testViewModel.bundleTotalPriceObservable.value)
        assertEquals(hotelRoomResponse.rateInfo.chargeableRateInfo.packageSavings, testViewModel.bundleSavingsObservable.value)
    }

    @Test
    fun testOfferReturnedWithNoPricing_noPricesAreChanged() {
        val hotelRoomResponse = getMockHotelRoomResponse()
        hotelRoomResponse.rateInfo.chargeableRateInfo.packageTotalPrice = null
        hotelRoomResponse.rateInfo.chargeableRateInfo.packageSavings = null
        hotelRoomResponse.rateInfo.chargeableRateInfo.packagePricePerPerson = null
        checkNoEmissionsForMissingPricesInRoomsResponse(hotelRoomResponse)
    }

    @Test
    fun testOfferReturnedWithOnlyPricePerPerson_noPricesAreChanged() {
        val money = Money()
        val hotelRoomResponse = getMockHotelRoomResponse()
        hotelRoomResponse.rateInfo.chargeableRateInfo.packageTotalPrice = null
        hotelRoomResponse.rateInfo.chargeableRateInfo.packageSavings = null
        hotelRoomResponse.rateInfo.chargeableRateInfo.packagePricePerPerson = money
        checkNoEmissionsForMissingPricesInRoomsResponse(hotelRoomResponse)
    }

    @Test
    fun testOfferReturnedWithOnlySavings_noPricesAreChanged() {
        val money = Money()
        val hotelRoomResponse = getMockHotelRoomResponse()
        hotelRoomResponse.rateInfo.chargeableRateInfo.packageTotalPrice = null
        hotelRoomResponse.rateInfo.chargeableRateInfo.packagePricePerPerson = null
        hotelRoomResponse.rateInfo.chargeableRateInfo.packageSavings = money
        checkNoEmissionsForMissingPricesInRoomsResponse(hotelRoomResponse)
    }

    @Test
    fun testOfferReturnedWithSavingsAndPerPersonPrice_noPricesAreChanged() {
        val money = Money()
        val hotelRoomResponse = getMockHotelRoomResponse()
        hotelRoomResponse.rateInfo.chargeableRateInfo.packageTotalPrice = null
        hotelRoomResponse.rateInfo.chargeableRateInfo.packageSavings = money
        hotelRoomResponse.rateInfo.chargeableRateInfo.packagePricePerPerson = money
        checkNoEmissionsForMissingPricesInRoomsResponse(hotelRoomResponse)
    }

    @Test
    fun testOfferReturnedWithOnlyTotalPrice_noPricesAreChanged() {
        val money = Money()
        val hotelRoomResponse = getMockHotelRoomResponse()
        hotelRoomResponse.rateInfo.chargeableRateInfo.packageTotalPrice = money
        hotelRoomResponse.rateInfo.chargeableRateInfo.packageSavings = null
        hotelRoomResponse.rateInfo.chargeableRateInfo.packagePricePerPerson = null
        checkNoEmissionsForMissingPricesInRoomsResponse(hotelRoomResponse)
    }

    @Test
    fun testOfferReturnedWithTotalAndPerPersonPrice_noPricesAreChanged() {
        val money = Money()
        val hotelRoomResponse = getMockHotelRoomResponse()
        hotelRoomResponse.rateInfo.chargeableRateInfo.packageTotalPrice = money
        hotelRoomResponse.rateInfo.chargeableRateInfo.packageSavings = null
        hotelRoomResponse.rateInfo.chargeableRateInfo.packagePricePerPerson = money
        checkNoEmissionsForMissingPricesInRoomsResponse(hotelRoomResponse)
    }

    @Test
    fun testOfferReturnedWithTotalAndSavings_noPricesAreChanged() {
        val money = Money()
        val hotelRoomResponse = getMockHotelRoomResponse()
        hotelRoomResponse.rateInfo.chargeableRateInfo.packageTotalPrice = money
        hotelRoomResponse.rateInfo.chargeableRateInfo.packageSavings = money
        hotelRoomResponse.rateInfo.chargeableRateInfo.packagePricePerPerson = null
        checkNoEmissionsForMissingPricesInRoomsResponse(hotelRoomResponse)
    }

    private fun checkNoEmissionsForMissingPricesInRoomsResponse(hotelRoomResponse: HotelOffersResponse.HotelRoomResponse) {
        hotelOffersResponse.hotelRoomResponse = listOf(hotelRoomResponse)

        testViewModel.hotelOffersSubject.onNext(hotelOffersResponse)
        assertNoEmissionsToObservables()
    }

    private fun getMockHotelRoomResponse(): HotelOffersResponse.HotelRoomResponse {
        val hotelOffer = dummyMidHotelRoomOffer()
        val multiItemOffer = dummyMIDItemRoomOffer()
        return HotelOffersResponse.convertMidHotelRoomResponse(hotelOffer, multiItemOffer)
    }

    fun testDetailedPriceShouldBeShownOrNot() {
        assertFalse(testViewModel.shouldDisplayDetailedPricePerDescription())
        AbacusTestUtils.bucketTestsAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppPackagesHSRPriceDisplay)
        assertTrue(testViewModel.shouldDisplayDetailedPricePerDescription())
    }

    @Test
    fun testPerNightMessageShouldBeShownOrNot() {
        testViewModel.perNightVisibility.subscribe(perNightTestSubscriber)

        assertPerNightVisibility(true)

        AbacusTestUtils.bucketTestsAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppPackagesHSRPriceDisplay)
        testViewModel.onlyShowTotalPrice.onNext(false)
        assertPerNightVisibility(false)

        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppPackagesHSRPriceDisplay)
        testViewModel.onlyShowTotalPrice.onNext(false)
        assertPerNightVisibility(true)

        testViewModel.hotelSoldOut.onNext(true)
        assertPerNightVisibility(false)

        testViewModel.onlyShowTotalPrice.onNext(true)
        assertPerNightVisibility(false)

        AbacusTestUtils.bucketTestsAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppPackagesHSRPriceDisplay)
        testViewModel.hotelSoldOut.onNext(false)
        testViewModel.onlyShowTotalPrice.onNext(true)
        assertPerNightVisibility(false)

        testViewModel.onlyShowTotalPrice.onNext(false)
        testViewModel.hotelSoldOut.onNext(true)
        assertPerNightVisibility(false)

        testViewModel.onlyShowTotalPrice.onNext(true)
        assertPerNightVisibility(false)
    }

    @Test
    fun testShouldShowFavoriteIcon() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.HotelShortlist)
        UserLoginTestUtil.setupUserAndMockLogin(UserLoginTestUtil.mockUser())
        testViewModel.isDatelessObservable.onNext(false)
        assertFalse(testViewModel.showHotelFavoriteIcon())
    }

    private fun assertNoEmissionsToObservables() {
        assertEquals(null, testViewModel.bundlePricePerPersonObservable.value)
        assertEquals(null, testViewModel.bundleTotalPriceObservable.value)
        assertEquals(null, testViewModel.bundleSavingsObservable.value)
    }

    private fun assertPerNightVisibility(expectedVisibility: Boolean) {
        perNightTestSubscriber.assertValueAt(perNightTestSubscriber.valueCount() - 1, expectedVisibility)
    }

    private fun setPOS(pos: PointOfSaleId) {
        SettingUtils.save(RuntimeEnvironment.application, R.string.PointOfSaleKey, pos.id.toString())
        PointOfSale.onPointOfSaleChanged(RuntimeEnvironment.application)
    }

    private fun makeResortFeeResponse(vm: BaseHotelDetailViewModel) {
        hotelOffersResponse.hotelRoomResponse.clear()
        val packageSearchParams = PackageSearchParams.Builder(30, 330)
                .adults(1)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .destination(SuggestionV4())
                .origin(SuggestionV4())
                .build() as PackageSearchParams

        val offer = HotelOffersResponse.convertToHotelOffersResponse(hotelOffersResponse, mockPackageServiceRule.getMIDRoomsResponse().getBundleRoomResponse(), packageSearchParams.startDate.toString(), packageSearchParams.endDate.toString())

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
