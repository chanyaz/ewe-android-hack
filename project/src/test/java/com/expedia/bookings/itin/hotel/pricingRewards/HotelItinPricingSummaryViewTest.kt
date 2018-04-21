package com.expedia.bookings.itin.hotel.pricingRewards

import android.app.Activity
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItinCardData
import com.expedia.bookings.itin.helpers.MockLifecycleOwner
import com.expedia.bookings.itin.hotel.repositories.ItinHotelRepo
import com.expedia.bookings.itin.scopes.HotelItinPricingSummaryScope
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinDetailsResponse
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.mobiata.mocke3.mockObject
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelItinPricingSummaryViewTest {
    private val activity = Robolectric.buildActivity(Activity::class.java).create().start().get()
    private val testView = LayoutInflater.from(activity).inflate(R.layout.test_hotel_itin_pricing_summary_container, null) as HotelItinPricingSummaryView

    lateinit var roomObserver: TestObserver<List<HotelItinRoomPrices>>
    lateinit var containerObserver: TestObserver<Unit>
    lateinit var priceLineItemObserver: TestObserver<HotelItinPriceLineItem>

    private val ViewGroup.views: List<View>
        get() = (0 until childCount).map { getChildAt(it) }

    @Before
    fun setup() {
        roomObserver = TestObserver()
        containerObserver = TestObserver()
        priceLineItemObserver = TestObserver()
    }

    @After
    fun tearDown() {
        roomObserver.dispose()
        containerObserver.dispose()
        priceLineItemObserver.dispose()
    }

    @Test
    fun testViewRefreshesLineItemsWhenViewModelUpdates() {
        val viewModel = viewModelWithMultipleRooms()
        testView.viewModel = viewModel

        roomObserver.assertEmpty()
        viewModel.observer.onChanged(viewModel.scope.itinHotelRepo.liveDataHotel.value)

        assertEquals(17, getAllLineItemViews().size)

        viewModel.observer.onChanged(getScope(true).itinHotelRepo.liveDataHotel.value)

        assertEquals(5, getAllLineItemViews().size)
    }

    @Test
    fun testViewContainerSubjectRemovesAllViews() {
        val viewModel = MockPriceSummaryViewModel()
        viewModel.clearPriceSummaryContainerSubject.subscribe(containerObserver)
        testView.viewModel = viewModel

        viewModel.priceLineItemSubject.onNext(HotelItinPriceLineItem("test", "test", R.color.itin_price_summary_label_gray_dark))
        assertEquals(1, getAllLineItemViews().size)
        containerObserver.assertEmpty()
        viewModel.clearPriceSummaryContainerSubject.onNext(Unit)
        containerObserver.assertValueCount(1)
        assertEquals(0, getAllLineItemViews().size)
    }

    @Test
    fun testViewPriceLineSubject() {
        val viewModel = MockPriceSummaryViewModel()
        viewModel.priceLineItemSubject.subscribe(priceLineItemObserver)
        testView.viewModel = viewModel

        priceLineItemObserver.assertEmpty()
        assertEquals(0, getAllLineItemViews().size)
        viewModel.priceLineItemSubject.onNext(HotelItinPriceLineItem("test", "test", R.color.itin_price_summary_label_gray_dark))
        priceLineItemObserver.assertValueCount(1)
        assertEquals(1, getAllLineItemViews().size)
        val view = getAllLineItemViews()[0]
        assertEquals("test", view.labelTextView.text)
        assertEquals("test", view.priceTextView.text)
        assertEquals(ContextCompat.getColor(activity, R.color.itin_price_summary_label_gray_dark), view.labelTextView.currentTextColor)
        assertEquals(ContextCompat.getColor(activity, R.color.itin_price_summary_label_gray_dark), view.priceTextView.currentTextColor)
    }

    private fun getAllLineItemViews(): List<PriceSummaryItemView> {
        return testView.containerView.views.filter { it is PriceSummaryItemView }.map { it as PriceSummaryItemView }
    }

    private fun viewModelWithMultipleRooms(): HotelItinPricingSummaryViewModel<HotelItinPricingSummaryScope> {
        val viewModel = HotelItinPricingSummaryViewModel(getScope())

        viewModel.roomPriceBreakdownSubject.subscribe(roomObserver)

        return viewModel
    }

    private fun getScope(forSingleRoom: Boolean = false): HotelItinPricingSummaryScope {
        val itinId = if (forSingleRoom) "single" else ""
        val repo = ItinHotelRepo(itinId, MockReadJsonUtil, TestObservable)

        return HotelItinPricingSummaryScope(repo, MockStringProvider, MockLifecycleOwner())
    }

    object MockStringProvider : StringSource {
        override fun fetchWithPhrase(stringResource: Int, map: Map<String, String>): String {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun fetch(stringResource: Int): String {
            return RuntimeEnvironment.application.getString(stringResource)
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
        override val clearPriceSummaryContainerSubject = PublishSubject.create<Unit>()
        override val roomPriceBreakdownSubject = PublishSubject.create<List<HotelItinRoomPrices>>()
        override val priceLineItemSubject = PublishSubject.create<HotelItinPriceLineItem>()
    }
}
