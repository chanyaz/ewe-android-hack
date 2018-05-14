package com.expedia.bookings.itin.hotel.pricingRewards

import android.app.Activity
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItinCardData
import com.expedia.bookings.itin.helpers.MockActivityLauncher
import com.expedia.bookings.itin.helpers.MockLifecycleOwner
import com.expedia.bookings.itin.hotel.repositories.ItinHotelRepo
import com.expedia.bookings.itin.scopes.HotelItinPricingSummaryScope
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinDetailsResponse
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.FontCache.Font
import com.expedia.bookings.widget.TextView
import com.mobiata.mocke3.mockObject
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelItinPricingSummaryViewTest {
    private val activity = Robolectric.buildActivity(Activity::class.java).create().start().get()
    private val testView = LayoutInflater.from(activity).inflate(R.layout.test_hotel_itin_pricing_summary_container, null) as HotelItinPricingSummaryView

    private lateinit var multipleGuestItemObserver: TestObserver<HotelItinPriceLineItem>
    private lateinit var taxesAndFeesItemObeserver: TestObserver<HotelItinPriceLineItem>
    private lateinit var couponViewItemObserver: TestObserver<HotelItinPriceLineItem>
    private lateinit var pointViewItemObserver: TestObserver<HotelItinPriceLineItem>
    private lateinit var currencyDisclaimerObserver: TestObserver<String>
    private lateinit var totalPriceItemObserver: TestObserver<HotelItinPriceLineItem>
    private lateinit var totalPricePosCurrencyObserver: TestObserver<HotelItinPriceLineItem>
    private lateinit var roomContainerItemObserver: TestObserver<HotelItinPriceLineItem>
    private lateinit var roomContainerClearObserver: TestObserver<Unit>

    private val ViewGroup.views: List<View>
        get() = (0 until childCount).map { getChildAt(it) }

    @Before
    fun setup() {
        multipleGuestItemObserver = TestObserver()
        taxesAndFeesItemObeserver = TestObserver()
        couponViewItemObserver = TestObserver()
        pointViewItemObserver = TestObserver()
        currencyDisclaimerObserver = TestObserver()
        totalPriceItemObserver = TestObserver()
        totalPricePosCurrencyObserver = TestObserver()
        roomContainerClearObserver = TestObserver()
        roomContainerItemObserver = TestObserver()
    }

    @After
    fun tearDown() {
        multipleGuestItemObserver.dispose()
        taxesAndFeesItemObeserver.dispose()
        couponViewItemObserver.dispose()
        pointViewItemObserver.dispose()
        currencyDisclaimerObserver.dispose()
        totalPriceItemObserver.dispose()
        totalPricePosCurrencyObserver.dispose()
        roomContainerClearObserver.dispose()
        roomContainerItemObserver.dispose()
    }

    @Test
    fun testRoomViewRefreshesLineItemsWhenViewModelUpdates() {
        val viewModel = viewModelWithMultipleRooms()
        testView.viewModel = viewModel

        roomContainerItemObserver.assertEmpty()
        roomContainerClearObserver.assertEmpty()
        assertEquals(0, getAllLineItemViews().size)
        viewModel.hotelObserver.onChanged(viewModel.scope.itinHotelRepo.liveDataHotel.value)

        assertEquals(17, getAllLineItemViews().size)

        viewModel.hotelObserver.onChanged(getScope(true).itinHotelRepo.liveDataHotel.value)

        assertEquals(5, getAllLineItemViews().size)
    }

    @Test
    fun testMultipleGuestItemSubject() {
        val viewModel = MockPriceSummaryViewModel()
        val view = testView.multipleGuestView
        viewModel.multipleGuestItemSubject.subscribe(multipleGuestItemObserver)
        testView.viewModel = viewModel

        assertEquals(View.GONE, view.visibility)
        multipleGuestItemObserver.assertEmpty()

        viewModel.multipleGuestItemSubject.onNext(HotelItinPriceLineItem("test", "test", R.color.itin_price_summary_label_gray_dark))
        multipleGuestItemObserver.assertValueCount(1)
        assertEquals("test", view.labelTextView.text)
        assertEquals("test", view.priceTextView.text)
        assertEquals(View.VISIBLE, view.visibility)
        assertEquals(ContextCompat.getColor(activity, R.color.itin_price_summary_label_gray_dark), view.labelTextView.currentTextColor)
        assertEquals(ContextCompat.getColor(activity, R.color.itin_price_summary_label_gray_dark), view.priceTextView.currentTextColor)
        assertEquals(14.0f, view.priceTextView.textSize)
        assertEquals(14.0f, view.labelTextView.textSize)
    }

    @Test
    fun testTaxesAndFeesItemSubject() {
        val viewModel = MockPriceSummaryViewModel()
        val view = testView.taxesAndFeesView
        viewModel.taxesAndFeesItemSubject.subscribe(taxesAndFeesItemObeserver)
        testView.viewModel = viewModel

        assertEquals(View.GONE, view.visibility)
        taxesAndFeesItemObeserver.assertEmpty()

        viewModel.taxesAndFeesItemSubject.onNext(HotelItinPriceLineItem("test", "test", R.color.itin_price_summary_label_gray_dark))
        taxesAndFeesItemObeserver.assertValueCount(1)
        assertEquals("test", view.labelTextView.text)
        assertEquals("test", view.priceTextView.text)
        assertEquals(View.VISIBLE, view.visibility)
        assertEquals(ContextCompat.getColor(activity, R.color.itin_price_summary_label_gray_dark), view.labelTextView.currentTextColor)
        assertEquals(ContextCompat.getColor(activity, R.color.itin_price_summary_label_gray_dark), view.priceTextView.currentTextColor)
        assertEquals(14.0f, view.priceTextView.textSize)
        assertEquals(14.0f, view.labelTextView.textSize)
    }

    @Test
    fun testCouponViewItemSubject() {
        val viewModel = MockPriceSummaryViewModel()
        val view = testView.couponsView
        viewModel.couponsItemSubject.subscribe(couponViewItemObserver)
        testView.viewModel = viewModel

        assertEquals(View.GONE, view.visibility)
        couponViewItemObserver.assertEmpty()

        viewModel.couponsItemSubject.onNext(HotelItinPriceLineItem("test", "-$2.00", R.color.itin_price_summary_label_green))
        couponViewItemObserver.assertValueCount(1)
        assertEquals("test", view.labelTextView.text)
        assertEquals("-$2.00", view.priceTextView.text)
        assertEquals("minus $2.00", view.priceTextView.contentDescription.toString())
        assertEquals(View.VISIBLE, view.visibility)
        assertEquals(ContextCompat.getColor(activity, R.color.itin_price_summary_label_green), view.labelTextView.currentTextColor)
        assertEquals(ContextCompat.getColor(activity, R.color.itin_price_summary_label_green), view.priceTextView.currentTextColor)
        assertEquals(14.0f, view.priceTextView.textSize)
        assertEquals(14.0f, view.labelTextView.textSize)
    }

    @Test
    fun testPointsViewItemSubject() {
        val viewModel = MockPriceSummaryViewModel()
        val view = testView.pointsView
        viewModel.pointsItemSubject.subscribe(pointViewItemObserver)
        testView.viewModel = viewModel

        assertEquals(View.GONE, view.visibility)
        pointViewItemObserver.assertEmpty()

        viewModel.pointsItemSubject.onNext(HotelItinPriceLineItem("test", "-$2.00", R.color.itin_price_summary_label_green))
        pointViewItemObserver.assertValueCount(1)
        assertEquals("test", view.labelTextView.text)
        assertEquals("-$2.00", view.priceTextView.text)
        assertEquals("minus $2.00", view.priceTextView.contentDescription.toString())
        assertEquals(View.VISIBLE, view.visibility)
        assertEquals(ContextCompat.getColor(activity, R.color.itin_price_summary_label_green), view.labelTextView.currentTextColor)
        assertEquals(ContextCompat.getColor(activity, R.color.itin_price_summary_label_green), view.priceTextView.currentTextColor)
        assertEquals(14.0f, view.priceTextView.textSize)
        assertEquals(14.0f, view.labelTextView.textSize)
    }

    @Test
    fun testCurrencyDisclaimerSubject() {
        val viewModel = MockPriceSummaryViewModel()
        val view = testView.currencyDisclaimerView
        viewModel.currencyDisclaimerSubject.subscribe(currencyDisclaimerObserver)
        testView.viewModel = viewModel

        assertEquals(View.GONE, view.visibility)
        currencyDisclaimerObserver.assertEmpty()

        viewModel.currencyDisclaimerSubject.onNext("CURRENCY DISCLAIMER")
        currencyDisclaimerObserver.assertValueCount(1)
        assertEquals(View.VISIBLE, view.visibility)
        assertEquals("CURRENCY DISCLAIMER", view.text)
        assertEquals(ContextCompat.getColor(activity, R.color.itin_price_summary_label_gray_light), view.currentTextColor)
        assertEquals(14.0f, view.textSize)
    }

    @Test
    fun testTotalPriceSubject() {
        val viewModel = MockPriceSummaryViewModel()
        val view = testView.totalPriceView
        viewModel.totalPriceItemSubject.subscribe(totalPriceItemObserver)
        testView.viewModel = viewModel

        assertEquals(View.GONE, view.visibility)
        totalPriceItemObserver.assertEmpty()

        viewModel.totalPriceItemSubject.onNext(HotelItinPriceLineItem("test", "test", R.color.itin_price_summary_label_gray_dark, 16.0f, Font.ROBOTO_BOLD))
        totalPriceItemObserver.assertValueCount(1)
        assertEquals("test", view.labelTextView.text)
        assertEquals("test", view.priceTextView.text)
        assertEquals(View.VISIBLE, view.visibility)
        assertEquals(ContextCompat.getColor(activity, R.color.itin_price_summary_label_gray_dark), view.labelTextView.currentTextColor)
        assertEquals(ContextCompat.getColor(activity, R.color.itin_price_summary_label_gray_dark), view.priceTextView.currentTextColor)
        assertEquals(16.0f, view.priceTextView.textSize)
        assertEquals(16.0f, view.labelTextView.textSize)
    }

    @Test
    fun testTotalPricePosCurrencySubject() {
        val viewModel = MockPriceSummaryViewModel()
        val view = testView.totalPricePosCurrencyView
        viewModel.totalPriceInPosCurrencyItemSubject.subscribe(totalPricePosCurrencyObserver)
        testView.viewModel = viewModel

        assertEquals(View.GONE, view.visibility)
        totalPricePosCurrencyObserver.assertEmpty()

        viewModel.totalPriceInPosCurrencyItemSubject.onNext(HotelItinPriceLineItem("test", "test", R.color.itin_price_summary_label_gray_light))
        totalPricePosCurrencyObserver.assertValueCount(1)
        assertEquals("test", view.labelTextView.text)
        assertEquals("test", view.priceTextView.text)
        assertEquals(View.VISIBLE, view.visibility)
        assertEquals(ContextCompat.getColor(activity, R.color.itin_price_summary_label_gray_light), view.labelTextView.currentTextColor)
        assertEquals(ContextCompat.getColor(activity, R.color.itin_price_summary_label_gray_light), view.priceTextView.currentTextColor)
        assertEquals(14.0f, view.priceTextView.textSize)
        assertEquals(14.0f, view.labelTextView.textSize)
    }

    @Test
    fun testAdditionalInfoSubject() {
        val testObserver = TestObserver<Unit>()
        val viewModel = MockPriceSummaryViewModel()
        val view = testView.additionalPriceInfoButton
        viewModel.additionalPricingInfoSubject.subscribe(testObserver)
        testView.viewModel = viewModel
        val additionalPricingInfoTextView = testView.additionalPriceInfoButton.getChildAt(0) as TextView

        assertEquals("Additional pricing information", additionalPricingInfoTextView.text)
        testObserver.assertEmpty()
        view.performClick()
        testObserver.assertValueCount(1)

        testObserver.dispose()
    }

    private fun getAllLineItemViews(): List<PriceSummaryItemView> {
        return testView.roomContainerView.views.filter { it is PriceSummaryItemView }.map { it as PriceSummaryItemView }
    }

    private fun viewModelWithMultipleRooms(): HotelItinPricingSummaryViewModel<HotelItinPricingSummaryScope> {
        val viewModel = HotelItinPricingSummaryViewModel(getScope())

        viewModel.roomContainerClearSubject.subscribe(roomContainerClearObserver)
        viewModel.roomContainerItemSubject.subscribe(roomContainerItemObserver)
        return viewModel
    }

    private fun getScope(forSingleRoom: Boolean = false): HotelItinPricingSummaryScope {
        val itinId = if (forSingleRoom) "single" else ""
        val repo = ItinHotelRepo(itinId, MockReadJsonUtil, TestObservable)

        return HotelItinPricingSummaryScope(repo, MockStringProvider, MockActivityLauncher(), MockLifecycleOwner())
    }

    object MockStringProvider : StringSource {
        override fun fetchWithPhrase(stringResource: Int, map: Map<String, String>): String {
            return "test"
        }

        override fun fetch(stringResource: Int): String {
            return "test"
        }
    }

    object MockReadJsonUtil : IJsonToItinUtil {
        override fun getItin(itinId: String?): Itin? {
            var mockName = "api/trips/hotel_trip_details_with_multiple_rooms_for_mocker.json"

            if (itinId == "single") {
                mockName = "api/trips/hotel_trip_details_for_mocker.json"
            }

            return mockObject(ItinDetailsResponse::class.java, mockName)?.itin
        }
    }

    object TestObservable : Observable<MutableList<ItinCardData>>() {
        override fun subscribeActual(observer: Observer<in MutableList<ItinCardData>>?) {}
    }

    class MockPriceSummaryViewModel : IHotelItinPricingSummaryViewModel {
        override val roomContainerClearSubject = PublishSubject.create<Unit>()
        override val roomContainerItemSubject = PublishSubject.create<HotelItinPriceLineItem>()
        override val multipleGuestItemSubject = PublishSubject.create<HotelItinPriceLineItem>()
        override val taxesAndFeesItemSubject = PublishSubject.create<HotelItinPriceLineItem>()
        override val couponsItemSubject = PublishSubject.create<HotelItinPriceLineItem>()
        override val pointsItemSubject = PublishSubject.create<HotelItinPriceLineItem>()
        override val currencyDisclaimerSubject = PublishSubject.create<String>()
        override val totalPriceItemSubject = PublishSubject.create<HotelItinPriceLineItem>()
        override val totalPriceInPosCurrencyItemSubject = PublishSubject.create<HotelItinPriceLineItem>()
        override val additionalPricingInfoSubject = PublishSubject.create<Unit>()
    }
}
