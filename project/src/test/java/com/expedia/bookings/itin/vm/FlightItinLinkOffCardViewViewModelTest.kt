package com.expedia.bookings.itin.vm

import com.expedia.bookings.itin.common.ItinLinkOffCardViewViewModel
import com.expedia.bookings.itin.flight.details.FlightItinLinkOffCardViewViewModel
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(RobolectricRunner::class)
class FlightItinLinkOffCardViewViewModelTest {

    private val createAdditionalInfoSubject = TestObserver<ItinLinkOffCardViewViewModel.CardViewParams>()
    private lateinit var sut: FlightItinLinkOffCardViewViewModel

    @Before
    fun setup() {
        sut = FlightItinLinkOffCardViewViewModel()
    }

    @Test
    fun testUpdateCardView() {
        sut.cardViewParamsSubject.subscribe(createAdditionalInfoSubject)
        createAdditionalInfoSubject.assertNoValues()
        val params = ItinLinkOffCardViewViewModel.CardViewParams(
                "Test Heading",
                "Test SubHeading",
                true,
                0,
                null
        )
        sut.updateCardView(params)
        createAdditionalInfoSubject.assertValue(params)
    }
}
