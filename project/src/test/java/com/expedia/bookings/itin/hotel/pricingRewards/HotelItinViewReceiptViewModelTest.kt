package com.expedia.bookings.itin.hotel.pricingRewards

import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.R
import com.expedia.bookings.features.Feature
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockHotelRepo
import com.expedia.bookings.itin.helpers.MockLifecycleOwner
import com.expedia.bookings.itin.helpers.MockStringProvider
import com.expedia.bookings.itin.helpers.MockTripsTracking
import com.expedia.bookings.itin.helpers.MockWebViewLauncher
import com.expedia.bookings.itin.scopes.HasFeature
import com.expedia.bookings.itin.scopes.HasHotelRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.scopes.HasWebViewLauncher
import com.expedia.bookings.itin.tripstore.extensions.firstHotel
import com.expedia.bookings.itin.utils.IWebViewLauncher
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.services.TestObserver
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HotelItinViewReceiptViewModelTest {

    @Test
    fun testShowReceiptValidUrlHotelName() {
        val showReceiptTestObserver = TestObserver<Unit>()
        val scope = MockHotelItinViewReceiptScope()
        val viewModel = HotelItinViewReceiptViewModel(scope)
        viewModel.showReceiptSubject.subscribe(showReceiptTestObserver)

        showReceiptTestObserver.assertNoValues()
        viewModel.itinObserver.onChanged(ItinMocker.hotelDetailsHappy)
        showReceiptTestObserver.assertValueCount(1)
        showReceiptTestObserver.assertValuesAndClear(Unit)

        scope.mockFeature.featureEnabled = false
        showReceiptTestObserver.assertNoValues()
        viewModel.itinObserver.onChanged(ItinMocker.hotelDetailsHappy)
        showReceiptTestObserver.assertNoValues()

        showReceiptTestObserver.dispose()
    }

    @Test
    fun testShowReceiptInValidUrlHotelName() {
        val showReceiptTestObserver = TestObserver<Unit>()
        val scope = MockHotelItinViewReceiptScope()
        val viewModel = HotelItinViewReceiptViewModel(scope)
        viewModel.showReceiptSubject.subscribe(showReceiptTestObserver)

        showReceiptTestObserver.assertNoValues()
        viewModel.itinObserver.onChanged(ItinMocker.hotelDetailsNoPriceDetails)
        showReceiptTestObserver.assertNoValues()

        scope.mockFeature.featureEnabled = false
        showReceiptTestObserver.assertNoValues()
        viewModel.itinObserver.onChanged(ItinMocker.hotelDetailsNoPriceDetails)
        showReceiptTestObserver.assertNoValues()

        showReceiptTestObserver.dispose()
    }

    @Test
    fun testShouldShowViewReceipt() {
        val scope = MockHotelItinViewReceiptScope()
        val viewModel = HotelItinViewReceiptViewModel(scope)

        assertFalse(viewModel.shouldShowViewReceipt(null))
        assertFalse(viewModel.shouldShowViewReceipt(ItinMocker.hotelPackageHappy))
        assertFalse(viewModel.shouldShowViewReceipt(ItinMocker.mickoMultiHotel))
        assertTrue(viewModel.shouldShowViewReceipt(ItinMocker.hotelDetailsHappy))

        scope.mockFeature.featureEnabled = false
        assertFalse(viewModel.shouldShowViewReceipt(ItinMocker.hotelDetailsHappy))
    }

    @Test
    fun testViewReceiptClickSubjectValidItin() {
        val scope = MockHotelItinViewReceiptScope()
        val viewModel = HotelItinViewReceiptViewModel(scope)
        val hotelDetailsHappy = ItinMocker.hotelDetailsHappy
        viewModel.itinObserver.onChanged(hotelDetailsHappy)

        viewModel.viewReceiptClickSubject.onNext(Unit)
        assertTrue(scope.webLauncherMock.sharableWebviewCalled)
        assertTrue(scope.viewReceiptTracking.trackItinHotelViewReceiptCalled)
        val hotel = hotelDetailsHappy.firstHotel()
        val name = hotel!!.hotelPropertyInfo!!.name
        assertEquals((R.string.itin_hotel_view_receipt_title_TEMPLATE).toString().plus(mapOf("hotelname" to name)), scope.webLauncherMock.toolbarTitle)
        assertEquals("https://www.expedia.com/itinerary-receipt?tripid=231f5318-59f5-4a00-a957-a0ba2688b9c6", scope.webLauncherMock.lastSeenURL)
        assertEquals("58fc868b-63e9-42cc-a0c3-6ac4dd78beaa", scope.webLauncherMock.lastSeenTripId)
        assertEquals(false, scope.webLauncherMock.isGuest)
    }

    @Test
    fun testViewReceiptClickSubjectInValidItin() {
        val scope = MockHotelItinViewReceiptScope()
        val viewModel = HotelItinViewReceiptViewModel(scope)
        val hotelDetailsNoPriceDetails = ItinMocker.hotelDetailsNoPriceDetails
        viewModel.itinObserver.onChanged(hotelDetailsNoPriceDetails)

        viewModel.viewReceiptClickSubject.onNext(Unit)
        assertFalse(scope.webLauncherMock.sharableWebviewCalled)
        assertFalse(scope.viewReceiptTracking.trackItinHotelViewReceiptCalled)
    }
}

class MockHotelItinViewReceiptScope : HasHotelRepo, HasStringProvider, HasLifecycleOwner, HasTripsTracking, HasWebViewLauncher, HasFeature {
    override val strings: StringSource = MockStringProvider()
    override val lifecycleOwner: LifecycleOwner = MockLifecycleOwner()
    val viewReceiptTracking = MockTripsTracking()
    override val tripsTracking = viewReceiptTracking
    override val itinHotelRepo = MockHotelRepo()
    val webLauncherMock = MockWebViewLauncher()
    override val webViewLauncher: IWebViewLauncher = webLauncherMock
    val mockFeature = MockFeature()
    override val feature = mockFeature
}

class MockFeature : Feature {
    var featureEnabled = true
    override val name: String = "MockFeature"
    override fun enabled(): Boolean {
        return featureEnabled
    }
}
