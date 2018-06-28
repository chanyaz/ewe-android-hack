package com.expedia.bookings.itin.lx.toolbar

import android.app.Activity
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.itin.common.ItinToolbar
import com.expedia.bookings.itin.helpers.MockItinToolbarViewModel
import com.expedia.bookings.itin.helpers.MockPOSInfoProvider
import com.expedia.bookings.itin.helpers.MockStringProvider
import com.expedia.bookings.itin.tripstore.data.Traveler
import com.expedia.bookings.itin.utils.ItinShareTextGenerator
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(RobolectricRunner::class)
class LxItinToolbarTest {

    lateinit var activity: Activity
    lateinit var sut: ItinToolbar

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        sut = LayoutInflater.from(activity).inflate(R.layout.test_widget_itin_toolbar, null) as ItinToolbar
        sut.viewModel = MockItinToolbarViewModel()
    }

    @Test
    fun testShareIconClickShowsDialog() {
        sut.viewModel.itinShareTextGeneratorSubject.onNext(getShareTextGenerator(null))
        sut.toolbarShareIcon.performClick()

        val shadowActivity = Shadows.shadowOf(activity)
        val dialogIntent = shadowActivity.peekNextStartedActivityForResult().intent
        assertNotNull(dialogIntent)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testCorrectTextGenerated() {
        val testObserver = TestObserver<ItinShareTextGenerator>()
        sut.viewModel.itinShareTextGeneratorSubject.subscribe(testObserver)
        sut.viewModel.itinShareTextGeneratorSubject.onNext(getShareTextGenerator(listOf(Traveler(fullName = "Bob Smith"), Traveler(fullName = "John Smith"))))
        sut.toolbarShareIcon.performClick()

        val textGenerator = testObserver.values()[0]
        val stringProvider = MockStringProvider()
        assertEquals("Activity", textGenerator.getLOBTypeString())
        assertEquals(stringProvider.fetchWithPhrase(R.string.itin_lx_share_email_subject_TEMPLATE,
                mapOf("trip" to "Trip title")), textGenerator.getEmailSubject())
        assertEquals(stringProvider.fetchWithPhrase(R.string.itin_lx_share_email_body_TEMPLATE,
                mapOf("trip" to "Trip title", "startdate" to "Monday, May 5", "enddate" to "Tuesday, May 6",
                        "travelers" to "Bob Smith, John Smith", "brand" to "Expedia", "link" to "app.info.url")),
                textGenerator.getEmailBody())
        assertEquals(stringProvider.fetchWithPhrase(R.string.itin_lx_share_sms_body_TEMPLATE,
                mapOf("trip" to "Trip title", "startdate" to "Monday, May 5", "enddate" to "Tuesday, May 6",
                        "travelers" to "Bob Smith, John Smith")), textGenerator.getSmsBody())
    }

    private fun getShareTextGenerator(travelersList: List<Traveler>?): ItinShareTextGenerator {
        return LxItinShareTextGenerator("Trip title", "Monday, May 5", "Tuesday, May 6", travelersList, MockStringProvider(), MockPOSInfoProvider())
    }
}
