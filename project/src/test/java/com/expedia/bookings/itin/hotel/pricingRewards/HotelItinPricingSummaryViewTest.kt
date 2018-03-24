package com.expedia.bookings.itin.hotel.pricingRewards

import android.app.Activity
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

    lateinit var observer: TestObserver<List<HotelItinPricingSummary>>

    private val ViewGroup.views: List<View>
        get() = (0 until childCount).map { getChildAt(it) }

    @Before
    fun setup() {
        observer = TestObserver()
    }

    @After
    fun tearDown() {
        observer.dispose()
    }

    @Test
    fun testViewRefreshesLineItemsWhenViewModelUpdates() {
        val viewModel = viewModelWithMultipleRooms()
        testView.viewModel = viewModel

        observer.assertEmpty()
        viewModel.observer.onChanged(viewModel.scope.itinHotelRepo.liveDataHotel.value)

        assertEquals(14, getAllLineItemViews().size)

        viewModel.observer.onChanged(getScope(true).itinHotelRepo.liveDataHotel.value)

        assertEquals(5, getAllLineItemViews().size)
    }

    private fun getAllLineItemViews(): List<HotelItinLineItemView> {
        return testView.containerView.views.filter { it is HotelItinLineItemView }.map { it as HotelItinLineItemView }
    }

    private fun viewModelWithMultipleRooms(): HotelItinPricingSummaryViewModel<HotelItinPricingSummaryScope> {
        val viewModel = HotelItinPricingSummaryViewModel(getScope())

        viewModel.lineItemViewModelSubject.subscribe(observer)

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
}
