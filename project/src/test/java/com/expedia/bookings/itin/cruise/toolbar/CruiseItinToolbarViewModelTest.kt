package com.expedia.bookings.itin.cruise.toolbar

import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.R
import com.expedia.bookings.itin.common.ItinRepoInterface
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockItinRepo
import com.expedia.bookings.itin.helpers.MockLifecycleOwner
import com.expedia.bookings.itin.helpers.MockStringProvider
import com.expedia.bookings.itin.helpers.MockTripsTracking
import com.expedia.bookings.itin.scopes.HasItinRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.utils.ItinShareTextGenerator
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.services.TestObserver
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CruiseItinToolbarViewModelTest {

    lateinit var toolbarTitleTestObserver: TestObserver<String>
    lateinit var shareTextGeneratorTestObserver: TestObserver<ItinShareTextGenerator>
    lateinit var viewModel: CruiseItinToolbarViewModel<MockScope>

    @Before
    fun setup() {
        toolbarTitleTestObserver = TestObserver()
        shareTextGeneratorTestObserver = TestObserver()
        viewModel = CruiseItinToolbarViewModel(MockScope())
        viewModel.toolbarTitleSubject.subscribe(toolbarTitleTestObserver)
        viewModel.itinShareTextGeneratorSubject.subscribe(shareTextGeneratorTestObserver)
    }

    @After
    fun dispose() {
        toolbarTitleTestObserver.dispose()
        shareTextGeneratorTestObserver.dispose()
    }

    @Test
    fun testToolbarTitleAndShareText() {
        viewModel.itinObserver.onChanged(ItinMocker.cruiseDetailsHappy)

        toolbarTitleTestObserver.assertValue(R.string.itin_cruise_toolbar_title_TEMPLATE.toString()
                .plus(mapOf("destination" to "Alaska")))

        assertTrue(shareTextGeneratorTestObserver.valueCount() == 1)
        val textGenerator = shareTextGeneratorTestObserver.values()[0]

        assertEquals("Cruise", textGenerator.getLOBTypeString())
        assertEquals((R.string.itin_cruise_share_email_subject).toString(), textGenerator.getEmailSubject())
        assertEquals((R.string.itin_cruise_share_email_body_TEMPLATE).toString()
                .plus(mapOf("reservation" to "7-night Alaska Cruise from Seattle (Roundtrip)",
                        "itin_number" to "71296028520", "cruise_line" to "Norwegian Cruise Line",
                        "ship_name" to "Norwegian Pearl", "embark_time" to "4:00 PM",
                        "embark_date" to "Sunday, September 23, 2018", "departure_port" to "Seattle, Washington ",
                        "disembark_time" to "8:00 AM", "disembark_date" to "Sunday, September 30, 2018",
                        "disembark_port" to "Seattle, Washington ")), textGenerator.getEmailBody())
        assertEquals((R.string.itin_cruise_share_sms_TEMPLATE).toString()
                .plus(mapOf("reservation" to "7-night Alaska Cruise from Seattle (Roundtrip)", "depart_time" to "4:00 PM",
                        "depart_date" to "Sun, 23 Sep", "arrive_time" to "8:00 AM", "arrive_date" to "Sun, 30 Sep",
                        "itin_number" to "71296028520", "cruise_line" to "Norwegian Cruise Line",
                        "ship_name" to "Norwegian Pearl")), textGenerator.getSmsBody())
    }

    @Test
    fun testEmptyTrip() {
        viewModel.itinObserver.onChanged(ItinMocker.emptyTrip)

        toolbarTitleTestObserver.assertNoValues()

        assertTrue(shareTextGeneratorTestObserver.valueCount() == 1)
        val textGenerator = shareTextGeneratorTestObserver.values()[0]

        assertEquals((R.string.itin_cruise_share_email_subject).toString(), textGenerator.getEmailSubject())
        assertEquals((R.string.itin_cruise_share_email_body_TEMPLATE).toString()
                .plus(mapOf("reservation" to "", "itin_number" to "", "cruise_line" to "", "ship_name" to "",
                        "embark_time" to "", "embark_date" to "", "departure_port" to "", "disembark_time" to "",
                        "disembark_date" to "", "disembark_port" to "")), textGenerator.getEmailBody())
        assertEquals((R.string.itin_cruise_share_sms_TEMPLATE).toString()
                .plus(mapOf("reservation" to "", "depart_time" to "",
                        "depart_date" to "", "arrive_time" to "", "arrive_date" to "",
                        "itin_number" to "", "cruise_line" to "",
                        "ship_name" to "")), textGenerator.getSmsBody())
    }

    @Test
    fun testToolbarShareIconClickTracked() {
        viewModel.itinObserver.onChanged(ItinMocker.cruiseDetailsHappy)
        assertFalse(viewModel.scope.tripsTracking.trackItinCruiseShareIconClicked)
        viewModel.shareIconClickedSubject.onNext(Unit)
        assertTrue(viewModel.scope.tripsTracking.trackItinCruiseShareIconClicked)
    }

    class MockScope : HasItinRepo, HasStringProvider, HasLifecycleOwner, HasTripsTracking {
        override val strings: StringSource = MockStringProvider()
        override val lifecycleOwner: LifecycleOwner = MockLifecycleOwner()
        override val itinRepo: ItinRepoInterface = MockItinRepo()
        override val tripsTracking = MockTripsTracking()
    }
}
