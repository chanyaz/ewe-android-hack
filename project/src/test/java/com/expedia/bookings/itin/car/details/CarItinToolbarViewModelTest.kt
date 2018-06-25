package com.expedia.bookings.itin.car.details

import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.R
import com.expedia.bookings.itin.cars.details.CarItinToolbarViewModel
import com.expedia.bookings.itin.common.ItinRepoInterface
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockItinRepo
import com.expedia.bookings.itin.helpers.MockLifecycleOwner
import com.expedia.bookings.itin.helpers.MockPOSInfoProvider
import com.expedia.bookings.itin.helpers.MockStringProvider
import com.expedia.bookings.itin.helpers.MockTripsTracking
import com.expedia.bookings.itin.scopes.HasItinRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasPOSProvider
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.utils.IPOSInfoProvider
import com.expedia.bookings.itin.utils.ItinShareTextGenerator
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.services.TestObserver
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CarItinToolbarViewModelTest {
    private lateinit var sut: CarItinToolbarViewModel<MockScope>
    private lateinit var mockScope: MockScope
    private lateinit var toolbarTitleTestObserver: TestObserver<String>
    private lateinit var toolbarSubTitleTestObserver: TestObserver<String>
    private lateinit var shareTextGeneratorTestObserver: TestObserver<ItinShareTextGenerator>

    @Before
    fun setup() {
        toolbarTitleTestObserver = TestObserver()
        toolbarSubTitleTestObserver = TestObserver()
        shareTextGeneratorTestObserver = TestObserver()
        mockScope = MockScope()
        sut = CarItinToolbarViewModel(mockScope)
        sut.toolbarSubTitleSubject.subscribe(toolbarSubTitleTestObserver)
        sut.toolbarTitleSubject.subscribe(toolbarTitleTestObserver)
        sut.itinShareTextGeneratorSubject.subscribe(shareTextGeneratorTestObserver)
    }

    @After
    fun breakdown() {
        toolbarSubTitleTestObserver.dispose()
        toolbarTitleTestObserver.dispose()
        shareTextGeneratorTestObserver.dispose()
    }

    @Test
    fun happyTest() {
        toolbarTitleTestObserver.assertNoValues()
        toolbarSubTitleTestObserver.assertNoValues()

        sut.itinObserver.onChanged(ItinMocker.carDetailsHappy)

        toolbarTitleTestObserver.assertValue((R.string.itin_car_toolbar_title_TEMPLATE).toString().plus(mapOf("location" to "Sydney")))
        toolbarSubTitleTestObserver.assertValue((R.string.itin_car_toolbar_subtitle_date_to_date_TEMPLATE).toString().plus(mapOf("startdate" to "Apr 15", "enddate" to "Apr 15")))
    }

    @Test
    fun unhappyTest() {
        toolbarTitleTestObserver.assertNoValues()
        toolbarSubTitleTestObserver.assertNoValues()

        sut.itinObserver.onChanged(ItinMocker.carDetailsBadPickupAndTimes)

        toolbarTitleTestObserver.assertNoValues()
        toolbarSubTitleTestObserver.assertNoValues()
    }

    @Test
    fun testShareMessages() {
        shareTextGeneratorTestObserver.assertNoValues()
        sut.itinObserver.onChanged(ItinMocker.carDetailsHappy)
        assertTrue(shareTextGeneratorTestObserver.valueCount() == 1)
        val textGenerator = shareTextGeneratorTestObserver.values()[0]

        assertEquals("Car", textGenerator.getLOBTypeString())
        assertEquals((R.string.itin_car_share_email_subject_TEMPLATE).toString()
                .plus(mapOf("reservation" to "Car rental in Sydney")), textGenerator.getEmailSubject())
        assertEquals((R.string.itin_car_share_email_body_TEMPLATE).toString()
                .plus(mapOf("reservation" to "Car rental in Sydney", "itin_number" to "7175610882378",
                        "vehicle_type" to "2 or 4-Door Car", "pickup_date" to "Sat, Apr 15", "pickup_time" to "7:30am",
                        "drop_off_date" to "Sat, Apr 15", "drop_off_time" to "5:00pm", "phone_number" to "02 9221 2231",
                        "vendor" to "Thrifty", "pickup_address" to "Sir John Young Crescent Domain Car Park",
                        "pickup_city" to "Sydney", "pickup_state" to "Victoria", "pickup_postal_code" to "98188",
                        "drop_off_address" to "99 Spencer Street", "drop_off_city" to "Docklands",
                        "drop_off_state" to "Victoria", "drop_off_postal_code" to "98188", "brand" to "Expedia",
                        "link" to "app.info.url")), textGenerator.getEmailBody())
        assertEquals((R.string.itin_car_share_sms_body_TEMPLATE).toString()
                .plus(mapOf("reservation" to "Car rental in Sydney", "vehicle_type" to "2 or 4-Door Car",
                        "pickup_date" to "Apr 15", "pickup_time" to "7:30am", "drop_off_date" to "Apr 15",
                        "drop_off_time" to "5:00pm", "pickup_address" to "Sir John Young Crescent Domain Car Park",
                        "pickup_city" to "Sydney", "pickup_state" to "Victoria", "pickup_postal_code" to "98188",
                        "drop_off_address" to "99 Spencer Street", "drop_off_city" to "Docklands",
                        "drop_off_state" to "Victoria", "drop_off_postal_code" to "98188")), textGenerator.getSmsBody())
    }

    @Test
    fun testShareMessagesForEmptyTrip() {
        shareTextGeneratorTestObserver.assertNoValues()
        sut.itinObserver.onChanged(ItinMocker.emptyTrip)
        val textGenerator = shareTextGeneratorTestObserver.values()[0]

        assertEquals((R.string.itin_car_share_email_subject_TEMPLATE).toString()
                .plus(mapOf("reservation" to "")), textGenerator.getEmailSubject())
        assertEquals((R.string.itin_car_share_email_body_TEMPLATE).toString()
                .plus(mapOf("reservation" to "", "itin_number" to "", "vehicle_type" to "", "pickup_date" to "",
                        "pickup_time" to "", "drop_off_date" to "", "drop_off_time" to "", "phone_number" to "",
                        "vendor" to "", "pickup_address" to "", "pickup_city" to "", "pickup_state" to "",
                        "pickup_postal_code" to "", "drop_off_address" to "", "drop_off_city" to "",
                        "drop_off_state" to "", "drop_off_postal_code" to "", "brand" to "Expedia", "link" to "app.info.url")),
                textGenerator.getEmailBody())
        assertEquals((R.string.itin_car_share_sms_body_TEMPLATE).toString()
                .plus(mapOf("reservation" to "", "vehicle_type" to "", "pickup_date" to "", "pickup_time" to "",
                        "drop_off_date" to "", "drop_off_time" to "", "pickup_address" to "", "pickup_city" to "",
                        "pickup_state" to "", "pickup_postal_code" to "", "drop_off_address" to "",
                        "drop_off_city" to "", "drop_off_state" to "", "drop_off_postal_code" to "")),
                textGenerator.getSmsBody())
    }

    @Test
    fun testShareIconClickTracked() {
        assertFalse(sut.scope.tripsTracking.trackItinCarShareIconClicked)
        sut.itinObserver.onChanged(ItinMocker.carDetailsHappy)
        sut.shareIconClickedSubject.onNext(Unit)
        assertTrue(sut.scope.tripsTracking.trackItinCarShareIconClicked)
    }

    private class MockScope : HasItinRepo, HasStringProvider, HasLifecycleOwner, HasPOSProvider, HasTripsTracking {
        override val strings: StringSource = MockStringProvider()
        override val lifecycleOwner: LifecycleOwner = MockLifecycleOwner()
        override val itinRepo: ItinRepoInterface = MockItinRepo()
        override var posInfoProvider: IPOSInfoProvider = MockPOSInfoProvider()
        override val tripsTracking = MockTripsTracking()
    }
}
