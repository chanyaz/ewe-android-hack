package com.expedia.vm.test.rail

import android.app.Activity
import com.expedia.bookings.data.rail.responses.RailProduct
import com.expedia.bookings.data.rail.responses.RailOffer
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.testutils.JSONResourceReader
import com.expedia.vm.rail.RailFareRulesViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import rx.observers.TestSubscriber
import java.util.ArrayList
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class RailFareRulesViewModelTest {
    lateinit var fareRulesVM: RailFareRulesViewModel

    private var activity: Activity by Delegates.notNull()

    val expectedFareNoteOne = "Travel anytime of day"
    val expectedFareNoteTwo = "Additional information about this fare can be found at http://www.nationalrail.co.uk/times_fares/ticket_types.aspx"
    val expectedFareNoteThree = "Valid only for travel via (changing trains or passing through) London."

    val expectedRefundRuleOne = "Your ticket is refundable before 9 Dec, 2016 12:35"
    val expectedRefundRuleTwo = "If you cancel before your ticket is printed, an admin fee of 40.00 GBP will be deducted from your refund."

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
    }

    @Test
    fun fareNotesIncludedInFareRules() {
        fareRulesVM = RailFareRulesViewModel(activity)

        val fareRulesSubscriber = TestSubscriber<List<String>>()
        fareRulesVM.fareRulesObservable.subscribe(fareRulesSubscriber)

        val railProduct = generateRailProduct()
        railProduct.refundableRules = emptyList()
        fareRulesVM.railProductObservable.onNext(railProduct)

        assertEquals(3, fareRulesSubscriber.onNextEvents[0].size)
        assertFareNotes(fareRulesSubscriber.onNextEvents[0])
    }

    @Test
    fun refundableRulesIncludedInFareRules() {
        fareRulesVM = RailFareRulesViewModel(activity)

        val fareRulesSubscriber = TestSubscriber<List<String>>()
        fareRulesVM.fareRulesObservable.subscribe(fareRulesSubscriber)
        fareRulesVM.railProductObservable.onNext(generateRailProduct())

        assertEquals(6, fareRulesSubscriber.onNextEvents[0].size)
        assertFareNotesAndRefundableRules(fareRulesSubscriber.onNextEvents[0])
    }

    @Test
    fun emptyFareRules() {
        fareRulesVM = RailFareRulesViewModel(activity)

        val noFareRulesSubscriber = TestSubscriber<Boolean>()
        fareRulesVM.noFareRulesObservable.subscribe(noFareRulesSubscriber)

        fareRulesVM.railProductObservable.onNext(RailProduct())
        assertTrue(noFareRulesSubscriber.onNextEvents[0])
    }

    private fun assertFareNotes(fareRules: List<String>) {
        assertEquals(expectedFareNoteOne, fareRules[0])
        assertEquals(expectedFareNoteTwo, fareRules[1])
        assertEquals(expectedFareNoteThree, fareRules[2])
    }

    private fun assertFareNotesAndRefundableRules(fareRules: List<String>) {
        assertEquals(expectedFareNoteOne, fareRules[0])
        assertEquals(expectedFareNoteTwo, fareRules[1])
        assertEquals(expectedFareNoteThree, fareRules[2])
        assertEquals(expectedRefundRuleOne, fareRules[3])
        assertEquals(expectedRefundRuleTwo, fareRules[4])
    }

    private fun generateRailProduct(): RailProduct {
        val resourceReader = JSONResourceReader("src/test/resources/raw/rail/rail_product_segments_8_9_10.json")
        val railProduct = resourceReader.constructUsingGson(RailProduct::class.java)
        return railProduct
    }
}
