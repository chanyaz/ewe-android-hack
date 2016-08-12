package com.expedia.vm.test.rail

import android.app.Activity
import com.expedia.bookings.data.rail.responses.RailProduct
import com.expedia.bookings.data.rail.responses.RailSearchResponse.RailOffer
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.bookings.test.robolectric.RobolectricRunner
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

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
    }

    @Test
    fun fareNotesIncludedInFareRules() {
        fareRulesVM = RailFareRulesViewModel(activity)

        val fareRulesSubscriber = TestSubscriber<List<String>>()
        fareRulesVM.fareRulesObservable.subscribe(fareRulesSubscriber)
        fareRulesVM.offerObservable.onNext(buildRailOfferWithFareNotesOnly())

        assertEquals(2, fareRulesSubscriber.onNextEvents[0].size)
        assertFareNotes(fareRulesSubscriber.onNextEvents[0])
    }

    @Test
    fun refundableRulesIncludedInFareRules() {
        fareRulesVM = RailFareRulesViewModel(activity)

        val fareRulesSubscriber = TestSubscriber<List<String>>()
        fareRulesVM.fareRulesObservable.subscribe(fareRulesSubscriber)
        fareRulesVM.offerObservable.onNext(buildRailOfferWithRefundableRulesAndFareNotes())

        assertEquals(4, fareRulesSubscriber.onNextEvents[0].size)
        assertFareNotesAndRefundableRules(fareRulesSubscriber.onNextEvents[0])
    }

    @Test
    fun emptyFareRules() {
        fareRulesVM = RailFareRulesViewModel(activity)

        val noFareRulesSubscriber = TestSubscriber<Boolean>()
        fareRulesVM.noFareRulesObservable.subscribe(noFareRulesSubscriber)

        fareRulesVM.offerObservable.onNext(buildRailOfferWithNoFareRules())
        assertTrue(noFareRulesSubscriber.onNextEvents[0])
    }

    private fun assertFareNotes(fareRules: List<String>) {
        assertEquals("Fare note 1", fareRules[0])
        assertEquals("Fare note 2", fareRules[1])
    }

    private fun assertFareNotesAndRefundableRules(fareRules: List<String>) {
        assertEquals("Fare note 1", fareRules[0])
        assertEquals("Fare note 2", fareRules[1])
        assertEquals("Refundable 1", fareRules[2])
        assertEquals("Refundable 2", fareRules[3])
    }

    private fun buildRailOfferWithRefundableRulesAndFareNotes(): RailOffer {
        val offer = buildRailOfferWithFareNotesOnly()
        offer.railProductList[0].refundableRules = ArrayList<String>()
        offer.railProductList[0].refundableRules.add("Refundable 1")
        offer.railProductList[0].refundableRules.add("Refundable 2")

        return offer
    }

    private fun buildRailOfferWithFareNotesOnly(): RailOffer {
        var offer = RailOffer()

        var product = RailProduct()
        product.fareNotes = ArrayList<String>()
        product.fareNotes.add("Fare note 1")
        product.fareNotes.add("Fare note 2")
        offer.railProductList = ArrayList<RailProduct>()
        offer.railProductList.add(product)

        return offer
    }

    private fun buildRailOfferWithNoFareRules(): RailOffer {
        var offer = RailOffer()
        offer.railProductList = ArrayList<RailProduct>()
        offer.railProductList.add(RailProduct())
        return offer
    }
}
