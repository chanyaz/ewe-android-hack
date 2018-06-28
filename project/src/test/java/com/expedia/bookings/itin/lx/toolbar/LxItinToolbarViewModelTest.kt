package com.expedia.bookings.itin.lx.toolbar

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.R
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockLifecycleOwner
import com.expedia.bookings.itin.helpers.MockLxRepo
import com.expedia.bookings.itin.helpers.MockPOSInfoProvider
import com.expedia.bookings.itin.helpers.MockStringProvider
import com.expedia.bookings.itin.lx.ItinLxRepoInterface
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasLxRepo
import com.expedia.bookings.itin.scopes.HasPOSProvider
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.tripstore.extensions.firstLx
import com.expedia.bookings.itin.utils.IPOSInfoProvider
import com.expedia.bookings.itin.utils.ItinShareTextGenerator
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class LxItinToolbarViewModelTest {
    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()
    private val toolbarTitleTestObserver = TestObserver<String>()
    private val toolbarSubTitleTestObserver = TestObserver<String>()

    val startTime = "Oct 24"

    lateinit var sut: LxItinToolbarViewModel<MockItinLxToolbarScope>

    @Before
    fun setup() {
        sut = LxItinToolbarViewModel(MockItinLxToolbarScope())
    }

    @Test
    fun testItinObserver() {
        sut.toolbarSubTitleSubject.subscribe(toolbarSubTitleTestObserver)
        toolbarSubTitleTestObserver.assertNoValues()

        sut.itinObserver.onChanged(ItinMocker.lxDetailsHappy)

        toolbarSubTitleTestObserver.assertValue(startTime)
    }

    @Test
    fun testItinLxObserver() {
        val lx = ItinMocker.lxDetailsHappy.firstLx()
        sut.toolbarTitleSubject.subscribe(toolbarTitleTestObserver)
        toolbarTitleTestObserver.assertNoValues()
        sut.itinLxObserver.onChanged(lx)
        toolbarTitleTestObserver.assertValue((R.string.itin_lx_toolbar_title_TEMPLATE).toString().plus(mapOf("location" to "San Francisco")))
    }

    @Test
    fun testNullItin() {
        val shareTextGeneratorTestObserver = TestObserver<ItinShareTextGenerator>()
        sut.itinShareTextGeneratorSubject.subscribe(shareTextGeneratorTestObserver)
        toolbarTitleTestObserver.assertNoValues()
        sut.itinObserver.onChanged(null)

        toolbarSubTitleTestObserver.assertNoValues()
    }

    @Test
    fun testNullLxItin() {
        val shareTextGeneratorTestObserver = TestObserver<ItinShareTextGenerator>()
        sut.itinShareTextGeneratorSubject.subscribe(shareTextGeneratorTestObserver)
        toolbarTitleTestObserver.assertNoValues()
        sut.itinLxObserver.onChanged(null)

        toolbarTitleTestObserver.assertNoValues()
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testItinLxShareText() {
        val lx = ItinMocker.lxDetailsAlsoHappy.firstLx()
        val shareTextGeneratorTestObserver = TestObserver<ItinShareTextGenerator>()
        sut.itinShareTextGeneratorSubject.subscribe(shareTextGeneratorTestObserver)
        toolbarTitleTestObserver.assertNoValues()
        sut.itinLxObserver.onChanged(lx)

        val textGenerator = shareTextGeneratorTestObserver.values().first()
        assertEquals("Activity", textGenerator.getLOBTypeString())
        assertEquals((R.string.itin_lx_share_email_subject_TEMPLATE).toString().plus(mapOf("trip" to "California Academy of Sciences General Admission: General Admission ")), textGenerator.getEmailSubject())
        assertEquals((R.string.itin_lx_share_email_body_TEMPLATE).toString().plus(mapOf("trip" to "California Academy of Sciences General Admission: General Admission ", "startdate" to "Wed, Oct 24", "enddate" to "Wed, Oct 24", "travelers" to "Nina Ricci", "brand" to "Expedia", "link" to "app.info.url")), textGenerator.getEmailBody())
        assertEquals((R.string.itin_lx_share_sms_body_TEMPLATE).toString().plus(mapOf("trip" to "California Academy of Sciences General Admission: General Admission ", "startdate" to "Wed, Oct 24", "enddate" to "Wed, Oct 24", "travelers" to "Nina Ricci")), textGenerator.getSmsBody())
    }

    @Test
    fun testEmptyItinShareText() {
        val itin = ItinMocker.lxDetailsMissingFields
        val shareTextGeneratorTestObserver = TestObserver<ItinShareTextGenerator>()
        sut.itinShareTextGeneratorSubject.subscribe(shareTextGeneratorTestObserver)
        toolbarTitleTestObserver.assertNoValues()
        sut.itinObserver.onChanged(itin)

        toolbarSubTitleTestObserver.assertNoValues()
    }

    @Test
    fun testEmptyLxItinShareText() {
        val itinLx = ItinMocker.lxDetailsMissingFields.firstLx()
        val shareTextGeneratorTestObserver = TestObserver<ItinShareTextGenerator>()
        sut.itinShareTextGeneratorSubject.subscribe(shareTextGeneratorTestObserver)
        toolbarTitleTestObserver.assertNoValues()
        sut.itinLxObserver.onChanged(itinLx)

        val textGenerator = shareTextGeneratorTestObserver.values().first() as LxItinShareTextGenerator
        assertEquals(null, textGenerator.travelers)
        assertEquals("", textGenerator.trip)
        assertEquals("", textGenerator.startDate)
        assertEquals("", textGenerator.endDate)
    }
}

class MockItinLxToolbarScope : HasLxRepo, HasStringProvider, HasLifecycleOwner, HasPOSProvider {
    override val strings: StringSource = MockStringProvider()
    override val lifecycleOwner: LifecycleOwner = MockLifecycleOwner()
    override val itinLxRepo: ItinLxRepoInterface = MockLxRepo()
    override val posInfoProvider: IPOSInfoProvider = MockPOSInfoProvider()
}
