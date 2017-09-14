package com.expedia.bookings.hotel.vm

import android.app.Activity
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.multiitem.BundleSearchResponse
import com.expedia.bookings.data.packages.PackageApiError
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.data.payment.LoyaltyBurnInfo
import com.expedia.bookings.data.payment.LoyaltyEarnInfo
import com.expedia.bookings.data.payment.LoyaltyInformation
import com.expedia.bookings.data.payment.LoyaltyType
import com.expedia.bookings.data.payment.PointsEarnInfo
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.test.MockHotelServiceTestRule
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.CurrencyUtils
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.widget.priceFormatter
import com.expedia.util.LoyaltyUtil
import com.expedia.vm.PackageHotelDetailPriceViewModel
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import java.math.BigDecimal
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
class PackageHotelDetailPriceViewModelTest {

    var mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    private var activity: Activity by Delegates.notNull()
    private var testVM: PackageHotelDetailPriceViewModel by Delegates.notNull()
    private var offerResponse: HotelOffersResponse by Delegates.notNull()
    private var hotelSearchParams: HotelSearchParams by Delegates.notNull()

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
        CurrencyUtils.initMap(activity)
        offerResponse = mockHotelServiceTestRule.getHappyOfferResponse()

        offerResponse.isPackage = true

        offerResponse.hotelRoomResponse[0].rateInfo.chargeableRateInfo.currencyCode = "USD"
        offerResponse.hotelRoomResponse[0].rateInfo.chargeableRateInfo.currencySymbol = "$"
        offerResponse.hotelRoomResponse[0].rateInfo.chargeableRateInfo.strikethroughPriceToShowUsers = 200f
        offerResponse.hotelRoomResponse[0].rateInfo.chargeableRateInfo.priceToShowUsers = 100f
        offerResponse.hotelRoomResponse[0].rateInfo.chargeableRateInfo.averageRate = 100f
        offerResponse.hotelRoomResponse[0].rateInfo.chargeableRateInfo.totalPriceWithMandatoryFees = 150f
        offerResponse.hotelRoomResponse[0].rateInfo.chargeableRateInfo.discountPercent = 50f

        offerResponse.hotelRoomResponse[0].rateInfo.chargeableRateInfo.packagePricePerPerson = Money("250", "USD")
        offerResponse.hotelRoomResponse[0].packageLoyaltyInformation = createLoyaltyInformation()

        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppHotelHideStrikethroughPrice)
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppHotelPriceDescriptorProminence)

        Db.setPackageResponse(PackageBundleSearchResponse())

        val suggestion = SuggestionV4()
        suggestion.coordinates = SuggestionV4.LatLng()
        hotelSearchParams = HotelSearchParams.Builder(0, 0)
                .destination(suggestion)
                .startDate(LocalDate(2016, 12, 31))
                .endDate(LocalDate(2017, 1, 1))
                .adults(1)
                .children(emptyList())
                .build() as HotelSearchParams
        testVM = PackageHotelDetailPriceViewModel(activity)
        testVM.bind(offerResponse = offerResponse, hotelSearchParams = hotelSearchParams)
    }

    @Test
    fun testSoldOutString() {
        testVM.isSoldOut.onNext(true)

        val startEndDateString = getSearchInfoStartEndDateString()

        assertNull(testVM.getStrikeThroughPriceString())
        assertNull(testVM.getPriceString())
        assertNull(testVM.getPerDescriptorString())
        assertNull(testVM.getTaxFeeDescriptorString())
        assertNull(testVM.getEarnMessageString())
        assertEquals("", testVM.getPriceContainerContentDescriptionString())

        assertEquals(startEndDateString + ", 1 guest", testVM.getSearchInfoString())
        assertEquals(ContextCompat.getColor(activity, R.color.gray3), testVM.getSearchInfoTextColor())
    }

    @Test
    fun testNotSoldOutString() {
        testVM.isSoldOut.onNext(false)

        val strikeThroughPriceString = getStrikeThroughPriceString()
        val priceString = getPriceString()
        val startEndDateString = getSearchInfoStartEndDateString()

        assertEquals(getStrikeThroughPriceString(), testVM.getStrikeThroughPriceString())
        assertEquals(getPriceString(), testVM.getPriceString())
        // note that the space below is non breaking space, typing in normal space from keyboard would fail
        assertEquals(" /person", testVM.getPerDescriptorString())
        assertEquals("Excluding taxes and fees", testVM.getTaxFeeDescriptorString())
        if (LoyaltyUtil.shouldShowEarnMessage("any string", true)) {
            assertEquals("Earn 15 points", testVM.getEarnMessageString())
        } else {
            assertNull(testVM.getEarnMessageString())
        }

        assertEquals(priceString + " /person", testVM.getPriceContainerContentDescriptionString())

        assertEquals(startEndDateString + ", 1 guest", testVM.getSearchInfoString())
        assertEquals(ContextCompat.getColor(activity, R.color.gray6), testVM.getSearchInfoTextColor())
    }

    @Test
    fun testStrikeThroughPriceShopWithPointsOverrideAirAttachAndBuckToHideStrikeThrough() {
        val loyaltyEarnInfo = createLoyaltyInformation().earn
        val loyaltyBurn = LoyaltyBurnInfo(LoyaltyType.REGULAR, Money("50", "USD"))
        val newLoyaltyInfo = LoyaltyInformation(loyaltyBurn, loyaltyEarnInfo, true)
        offerResponse.hotelRoomResponse[0].rateInfo.chargeableRateInfo.loyaltyInfo = newLoyaltyInfo

        offerResponse.hotelRoomResponse[0].rateInfo.chargeableRateInfo.airAttached = true

        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppHotelHideStrikethroughPrice)

        testVM.bind(offerResponse, hotelSearchParams)

        val strikeThroughPriceString = getStrikeThroughPriceString()

        assertEquals(strikeThroughPriceString, testVM.getStrikeThroughPriceString())
    }

    @Test
    fun testStrikeThroughPriceAirAttached() {
        offerResponse.hotelRoomResponse[0].rateInfo.chargeableRateInfo.airAttached = true

        testVM.bind(offerResponse, hotelSearchParams)

        if (PointOfSale.getPointOfSale().showHotelCrossSell() && ProductFlavorFeatureConfiguration.getInstance().shouldShowAirAttach()) {
            assertNull(testVM.getStrikeThroughPriceString())
        } else {
            val strikeThroughPriceString = getStrikeThroughPriceString()

            assertEquals(strikeThroughPriceString, testVM.getStrikeThroughPriceString())
        }
    }

    @Test
    fun testStrikeThroughPriceAirAttachedZeroPercentDiscount() {
        offerResponse.hotelRoomResponse[0].rateInfo.chargeableRateInfo.airAttached = true
        offerResponse.hotelRoomResponse[0].rateInfo.chargeableRateInfo.discountPercent = 0f

        testVM.bind(offerResponse, hotelSearchParams)

        val strikeThroughPriceString = getStrikeThroughPriceString()

        assertEquals(strikeThroughPriceString, testVM.getStrikeThroughPriceString())
    }

    @Test
    fun testStrikeThroughPriceBucketToHide() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppHotelHideStrikethroughPrice)

        testVM = PackageHotelDetailPriceViewModel(activity)
        testVM.bind(offerResponse, hotelSearchParams)

        assertNull(testVM.getStrikeThroughPriceString())
    }

    @Test
    fun testNegativeStrikeThroughPrice() {
        offerResponse.hotelRoomResponse[0].rateInfo.chargeableRateInfo.strikethroughPriceToShowUsers = -200f

        testVM.bind(offerResponse, hotelSearchParams)

        assertNull(testVM.getStrikeThroughPriceString())
    }

    @Test
    fun testStrikeThroughPriceLesserThanPrice() {
        offerResponse.hotelRoomResponse[0].rateInfo.chargeableRateInfo.strikethroughPriceToShowUsers = 100f
        offerResponse.hotelRoomResponse[0].rateInfo.chargeableRateInfo.priceToShowUsers = 200f

        testVM.bind(offerResponse, hotelSearchParams)

        assertNull(testVM.getStrikeThroughPriceString())
    }

    @Test
    fun testStrikeThroughPriceEqualPrice() {
        offerResponse.hotelRoomResponse[0].rateInfo.chargeableRateInfo.strikethroughPriceToShowUsers = 100f

        testVM.bind(offerResponse, hotelSearchParams)

        assertNull(testVM.getStrikeThroughPriceString())
    }

    @Test
    fun testPackageStrikeThroughDontShowNegative() {
        offerResponse.hotelRoomResponse[0].rateInfo.chargeableRateInfo.strikethroughPriceToShowUsers = -200f

        testVM.bind(offerResponse, hotelSearchParams)

        assertNull(testVM.getStrikeThroughPriceString())
    }

    @Test
    fun testPackagePrice() {
        val priceString = getPriceString()

        assertEquals(priceString, testVM.getPriceString())
    }

    @Test
    fun testTotalPrice() {
        offerResponse.hotelRoomResponse[0].rateInfo.chargeableRateInfo.userPriceType = "RateForWholeStayWithTaxes"

        testVM.bind(offerResponse, hotelSearchParams)

        val priceString = getPriceString()

        assertEquals(priceString, testVM.getPriceString())
    }

    @Test
    fun testPackageTotalPrice() {
        offerResponse.hotelRoomResponse[0].rateInfo.chargeableRateInfo.userPriceType = "RateForWholeStayWithTaxes"

        testVM.bind(offerResponse, hotelSearchParams)

        val priceString = getPriceString()

        assertEquals(priceString, testVM.getPriceString())
    }

    @Test
    fun testPerDescriptor() {
        assertEquals(" /person", testVM.getPerDescriptorString())
    }

    @Test
    fun testPerDescriptorNotBucketedPriceProminence() {
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppHotelPriceDescriptorProminence)

        assertEquals(" /person", testVM.getPerDescriptorString())
    }

    @Test
    fun testPerDescriptorTotalPrice() {
        offerResponse.hotelRoomResponse[0].rateInfo.chargeableRateInfo.userPriceType = "RateForWholeStayWithTaxes"
        testVM.bind(offerResponse, hotelSearchParams)

        assertEquals(" /person", testVM.getPerDescriptorString())
    }

    @Test
    fun testPerDescriptorTotalPriceNotBucketedPriceProminence() {
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppHotelPriceDescriptorProminence)

        offerResponse.hotelRoomResponse[0].rateInfo.chargeableRateInfo.userPriceType = "RateForWholeStayWithTaxes"
        testVM.bind(offerResponse, hotelSearchParams)

        assertEquals(" /person", testVM.getPerDescriptorString())
    }

    @Test
    fun testPackagePerDescriptor() {
        assertEquals(" /person", testVM.getPerDescriptorString())
    }

    @Test
    fun testTaxFeeDescriptor() {
        assertEquals("Excluding taxes and fees", testVM.getTaxFeeDescriptorString())
    }

    @Test
    fun testTaxFeeDescriptorTotalPrice() {
        offerResponse.hotelRoomResponse[0].rateInfo.chargeableRateInfo.userPriceType = "RateForWholeStayWithTaxes"
        testVM.bind(offerResponse, hotelSearchParams)

        assertEquals("Total including taxes and fees", testVM.getTaxFeeDescriptorString())
    }

    @Test
    fun testTaxFeeDescriptorPackage() {
        offerResponse.hotelRoomResponse[0].rateInfo.chargeableRateInfo.userPriceType = "per night all travelers"
        testVM.bind(offerResponse, hotelSearchParams)

        assertNull(testVM.getTaxFeeDescriptorString())
    }

    @Test
    fun testTaxFeeDescriptorNotBucketedPriceProminence() {
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppHotelPriceDescriptorProminence)

        assertNull(testVM.getTaxFeeDescriptorString())
    }

    @Test
    fun testPackageEarnMessage() {
        val shouldShowEarnMessage = LoyaltyUtil.shouldShowEarnMessage("any string", true)
        if (shouldShowEarnMessage) {
            val packageEarnInfo = offerResponse.hotelRoomResponse[0].packageLoyaltyInformation.earn
            val earnMessage = LoyaltyUtil.getEarnMessagingString(activity, offerResponse.isPackage, null, packageEarnInfo)
            assertEquals(earnMessage, testVM.getEarnMessageString())
        } else {
            assertNull(testVM.getEarnMessageString())
        }
    }


    @Test
    fun testPackageNoEarnMessage() {
        offerResponse.hotelRoomResponse[0].packageLoyaltyInformation = null

        testVM.bind(offerResponse, hotelSearchParams)

        assertNull(testVM.getEarnMessageString())
    }

    @Test
    fun testPackageSearchInfoString() {
        testVM.bind(offerResponse, hotelSearchParams)

        val startEndDateString = getSearchInfoStartEndDateString()

        assertEquals(startEndDateString + ", 1 guest", testVM.getSearchInfoString())
    }

    @Test
    fun testNoStrikeThroughPriceContainerContentDescription() {
        offerResponse.hotelRoomResponse[0].rateInfo.chargeableRateInfo.strikethroughPriceToShowUsers = 0f

        testVM.bind(offerResponse, hotelSearchParams)

        val priceString = getPriceString()

        assertEquals(priceString + " /person", testVM.getPriceContainerContentDescriptionString())
    }

    @Test
    fun testPackagePriceContainerContentDescription() {
        val priceString = getPriceString()

        assertEquals(priceString + " /person", testVM.getPriceContainerContentDescriptionString())
    }

    @Test
    fun testShopWithPointPriceContainerContentDescription() {
        offerResponse.hotelRoomResponse[0].rateInfo.chargeableRateInfo?.loyaltyInfo = createSWPLoyaltyInformation()

        testVM.bind(offerResponse, hotelSearchParams)

        val priceString = getPriceString()

        assertEquals(priceString + " /person", testVM.getPriceContainerContentDescriptionString())
    }

    @Test
    fun testNoDiscountPriceContainerContentDescription() {
        offerResponse.hotelRoomResponse[0].rateInfo.chargeableRateInfo?.discountPercent = 0.0.toFloat()

        testVM.bind(offerResponse, hotelSearchParams)

        val priceString = getPriceString()

        assertEquals(priceString + " /person", testVM.getPriceContainerContentDescriptionString())
    }

    private fun getStrikeThroughPriceString(): String {
        val chargeableRateInfo = offerResponse.hotelRoomResponse[0].rateInfo.chargeableRateInfo
        return priceFormatter(activity.resources, chargeableRateInfo, true, !offerResponse!!.isPackage).toString()
    }

    private fun getPriceString(): String {
        val chargeableRateInfo = offerResponse.hotelRoomResponse[0].rateInfo.chargeableRateInfo
        val showTotalPrice = chargeableRateInfo.getUserPriceType() == HotelRate.UserPriceType.RATE_FOR_WHOLE_STAY_WITH_TAXES
        val currencyCode = chargeableRateInfo.currencyCode
        if (showTotalPrice) {
            val totalPriceWithMandatoryFees = BigDecimal(chargeableRateInfo.totalPriceWithMandatoryFees.toDouble())
            return Money(totalPriceWithMandatoryFees, currencyCode).getFormattedMoney(Money.F_NO_DECIMAL)
        } else {
            return chargeableRateInfo.packagePricePerPerson.getFormattedMoney(Money.F_NO_DECIMAL)
        }
    }

    private fun getSearchInfoStartEndDateString(): String {
        val startEndDateString: String
        val dtf = DateTimeFormat.forPattern("yyyy-MM-dd")
        val startDate = LocaleBasedDateFormatUtils.localDateToMMMd(dtf.parseLocalDate(Db.getPackageResponse().getHotelCheckInDate()))
        val endDate = LocaleBasedDateFormatUtils.localDateToMMMd(dtf.parseLocalDate(Db.getPackageResponse().getHotelCheckOutDate()))
        startEndDateString = startDate + " - " + endDate
        return startEndDateString
    }

    private fun createLoyaltyInformation(): LoyaltyInformation {
        val earnInfo = PointsEarnInfo(10, 5, 15)
        val loyaltyEarnInfo = LoyaltyEarnInfo(earnInfo, null)
        return LoyaltyInformation(null, loyaltyEarnInfo, false)
    }

//    private fun createPriceLoyaltyInformation(): LoyaltyInformation {
//        val priceEarnInfo = PriceEarnInfo(Money("1", "USD"), Money("0.5", "USD"), Money("1.5", "USD"))
//        val loyaltyEarnInfo = LoyaltyEarnInfo(null, priceEarnInfo)
//        return LoyaltyInformation(null, loyaltyEarnInfo, false)
//    }

    private fun createSWPLoyaltyInformation(): LoyaltyInformation {
        val earnInfo = PointsEarnInfo(10, 5, 15)
        val loyaltyEarnInfo = LoyaltyEarnInfo(earnInfo, null)
        return LoyaltyInformation(null, loyaltyEarnInfo, true)
    }

    class PackageBundleSearchResponse : BundleSearchResponse {
        override fun getHotelCheckInDate(): String {
            return "2016-12-31"
        }
        override fun getHotelCheckOutDate(): String {
            return "2017-01-01"
        }
        override fun getHotelResultsCount(): Int {
            return 1
        }
        override fun getHotels(): List<Hotel> {
            return ArrayList()
        }
        override fun getFlightLegs(): List<FlightLeg> {
            return ArrayList()
        }
        override fun hasSponsoredHotelListing(): Boolean {
            return false
        }
        override fun getCurrencyCode(): String {
            return "USD"
        }
        override fun getCurrentOfferModel(): PackageOfferModel {
            return PackageOfferModel()
        }
        override fun setCurrentOfferModel(offerModel: PackageOfferModel) {
        }
        override fun hasErrors(): Boolean = false
        override val firstError: PackageApiError.Code = PackageApiError.Code.pkg_unknown_error
    }
}
