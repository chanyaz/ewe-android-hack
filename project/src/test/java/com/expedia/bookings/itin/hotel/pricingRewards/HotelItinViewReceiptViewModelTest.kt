package com.expedia.bookings.itin.hotel.pricingRewards

import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockHotelRepo
import com.expedia.bookings.itin.helpers.MockLifecycleOwner
import com.expedia.bookings.itin.helpers.MockStringProvider
import com.expedia.bookings.itin.helpers.MockTripsTracking
import com.expedia.bookings.itin.helpers.MockWebViewLauncher
import com.expedia.bookings.itin.scopes.HasHotelRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.scopes.HasWebViewLauncher
import com.expedia.bookings.itin.tripstore.extensions.firstHotel
import com.expedia.bookings.itin.utils.IWebViewLauncher
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.services.TestObserver
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HotelItinViewReceiptViewModelTest {

    lateinit var sut: HotelItinViewReceiptViewModel<MockHotelItinViewReceiptScope>
    lateinit var scope: MockHotelItinViewReceiptScope
    private val toolbarTitleTestObserver = TestObserver<String>()
    private val receiptTestObserver = TestObserver<HotelItinViewReceiptViewModel.HotelItinReceipt>()
    private val showReceiptTestObserver = TestObserver<Unit>()
    val tripID = "58fc868b-63e9-42cc-a0c3-6ac4dd78beaa"
    val url = "https://www.expedia.com/itinerary-receipt?tripid=231f5318-59f5-4a00-a957-a0ba2688b9c6"
    private val receiptItem = HotelItinViewReceiptViewModel.HotelItinReceipt(url, tripID)

    @Before
    fun setUp() {
        scope = MockHotelItinViewReceiptScope()
        sut = HotelItinViewReceiptViewModel(scope)
    }

    @Test
    fun titleSubjectHappyPathTest() {
        sut.titleSubject.subscribe(toolbarTitleTestObserver)
        toolbarTitleTestObserver.assertNoValues()

        sut.itinHotelObserver.onChanged(ItinMocker.hotelDetailsHappy.firstHotel())
        toolbarTitleTestObserver.assertValue("somePhraseString")
    }

    @Test
    fun titleSubjectNullTest() {
        sut.titleSubject.subscribe(toolbarTitleTestObserver)
        toolbarTitleTestObserver.assertNoValues()
        val hotelWithNullTitle = ItinMocker.hotelDetailsHappy.firstHotel()
        hotelWithNullTitle?.hotelPropertyInfo?.name = null
        sut.itinHotelObserver.onChanged(hotelWithNullTitle)
        toolbarTitleTestObserver.assertNoValues()
    }

    @Test
    fun urlHappyPathTest() {
        sut.receiptSubject.subscribe(receiptTestObserver)
        receiptTestObserver.assertNoValues()

        sut.itinObserver.onChanged(ItinMocker.hotelDetailsHappy)
        receiptTestObserver.assertValue(receiptItem)
    }

    @Test
    fun urlNullHappyPathTest() {
        sut.receiptSubject.subscribe(receiptTestObserver)
        receiptTestObserver.assertNoValues()

        sut.itinObserver.onChanged(ItinMocker.hotelDetailsNoPriceDetails)
        receiptTestObserver.assertNoValues()
    }

    @Test
    fun zipReceiptSubjectOnlyTest() {
        sut.showReceipt.subscribe(showReceiptTestObserver)
        showReceiptTestObserver.assertNoValues()
        sut.receiptSubject.onNext(receiptItem)
        showReceiptTestObserver.assertNoValues()
    }

    @Test
    fun zipTitleSubjectOnlyTest() {
        sut.showReceipt.subscribe(showReceiptTestObserver)
        showReceiptTestObserver.assertNoValues()
        sut.titleSubject.onNext("somestring")
        showReceiptTestObserver.assertNoValues()
    }

    @Test
    fun zipHappyPath() {
        sut.showReceipt.subscribe(showReceiptTestObserver)
        sut.titleSubject.onNext("somestring")
        sut.receiptSubject.onNext(receiptItem)
        assertFalse(scope.tripsTracking.trackItinHotelViewReceiptCalled)
        assertFalse(scope.webLauncherMock.sharableWebviewCalled)

        showReceiptTestObserver.assertValue(Unit)

        sut.viewReceiptClickSubject.onNext(Unit)
        assertTrue(scope.tripsTracking.trackItinHotelViewReceiptCalled)
        assertTrue(scope.webLauncherMock.sharableWebviewCalled)
    }
}

class MockHotelItinViewReceiptScope() : HasHotelRepo, HasStringProvider, HasLifecycleOwner, HasTripsTracking, HasWebViewLauncher {
    val mockStrings = MockStringProvider()
    override val strings: StringSource = mockStrings
    override val lifecycleOwner: LifecycleOwner = MockLifecycleOwner()
    val viewReceiptTracking = MockTripsTracking()
    override val tripsTracking = viewReceiptTracking
    override val itinHotelRepo = MockHotelRepo()
    val webLauncherMock = MockWebViewLauncher()
    override val webViewLauncher: IWebViewLauncher = webLauncherMock
}
