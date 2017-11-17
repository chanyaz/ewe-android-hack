package com.expedia.bookings.itin.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.TravelerFrequentFlyerMembership
import com.expedia.bookings.itin.vm.FlightItinTravelerPreferenceViewModel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightItinTravelerPreferenceWidgetTest {
    lateinit var context: Context
    lateinit var sut: FlightItinTravelerPreferenceWidget

    @Before
    fun setup() {
        context = RuntimeEnvironment.application
        sut = LayoutInflater.from(context).inflate(R.layout.test_widget_itin_traveler_preference, null) as FlightItinTravelerPreferenceWidget
        sut.viewModel = FlightItinTravelerPreferenceViewModel()
    }

    @Test
    fun testFrequentFlyerHasMapSingle() {
        val map = HashMap<String, TravelerFrequentFlyerMembership>()
        val membership = TravelerFrequentFlyerMembership()
        membership.programName = "The Plan"
        membership.membershipNumber = "K123K444"
        membership.airlineCode = "123"
        map.put(membership.airlineCode, membership)
        assertEquals("", sut.frequentFlyerNumber.text)
        assertEquals(View.GONE, sut.frequentFlyerNumber.visibility)
        assertEquals("", sut.frequentFlyerPlan.text)
        assertEquals(View.VISIBLE, sut.frequentFlyerPlan.visibility)
        sut.viewModel.frequentFlyerSubject.onNext(map)
        assertEquals("K123K444", sut.frequentFlyerNumber.text.toString())
        assertEquals(View.VISIBLE, sut.frequentFlyerNumber.visibility)
        assertEquals("The Plan", sut.frequentFlyerPlan.text.toString())
        assertEquals(View.VISIBLE, sut.frequentFlyerPlan.visibility)
    }

    @Test
    fun testFrequentFlyerHasMapMulti() {
        val map = HashMap<String, TravelerFrequentFlyerMembership>()
        val membershipA = TravelerFrequentFlyerMembership()
        membershipA.programName = "The Plan"
        membershipA.membershipNumber = "K123K444"
        membershipA.airlineCode = "123"
        map.put(membershipA.airlineCode, membershipA)
        val membershipB = TravelerFrequentFlyerMembership()
        membershipB.programName = "The 2nd Plan"
        membershipB.membershipNumber = "s123s444"
        membershipB.airlineCode = "321"
        map.put(membershipB.airlineCode, membershipB)
        assertEquals("", sut.frequentFlyerNumber.text)
        assertEquals(View.GONE, sut.frequentFlyerNumber.visibility)
        assertEquals("", sut.frequentFlyerPlan.text)
        assertEquals(View.VISIBLE, sut.frequentFlyerPlan.visibility)
        sut.viewModel.frequentFlyerSubject.onNext(map)
        assertEquals("K123K444\ns123s444", sut.frequentFlyerNumber.text.toString())
        assertEquals(View.VISIBLE, sut.frequentFlyerNumber.visibility)
        assertEquals("The Plan\nThe 2nd Plan", sut.frequentFlyerPlan.text.toString())
        assertEquals(View.VISIBLE, sut.frequentFlyerPlan.visibility)
    }

    @Test
    fun testFrequentFlyerEmptyMap() {
        sut.resetWidget()
        val map = HashMap<String, TravelerFrequentFlyerMembership>()
        assertEquals("", sut.frequentFlyerNumber.text)
        assertEquals(View.GONE, sut.frequentFlyerNumber.visibility)
        assertEquals(context.getString(R.string.none), sut.frequentFlyerPlan.text)
        assertEquals(View.VISIBLE, sut.frequentFlyerPlan.visibility)
        sut.viewModel.frequentFlyerSubject.onNext(map)
        assertEquals("", sut.frequentFlyerNumber.text)
        assertEquals(View.GONE, sut.frequentFlyerNumber.visibility)
        assertEquals(context.getString(R.string.none), sut.frequentFlyerPlan.text)
        assertEquals(View.VISIBLE, sut.frequentFlyerPlan.visibility)
    }

    @Test
    fun testSpecialRequest() {
        sut.resetWidget()
        assertEquals(context.getString(R.string.none), sut.specialRequest.text)
        sut.viewModel.specialRequestSubject.onNext("123")
        assertEquals("123", sut.specialRequest.text)
    }

    @Test
    fun testRedressNumber() {
        assertEquals(View.GONE, sut.redressContainer.visibility)
        assertEquals("", sut.redressNumber.text)
        sut.viewModel.redressNumberSubject.onNext("123444")
        assertEquals(View.VISIBLE, sut.redressContainer.visibility)
        assertEquals("123444", sut.redressNumber.text)
    }

    @Test
    fun testKnownTravelerNumber() {
        sut.resetWidget()
        assertEquals(View.GONE, sut.knownTravelerContainer.visibility)
        assertEquals("", sut.knownTravelerNumber.text)
        sut.viewModel.knownTravelerNumberSubject.onNext("3333")
        assertEquals(View.VISIBLE, sut.knownTravelerContainer.visibility)
        assertEquals("3333", sut.knownTravelerNumber.text)
    }

    @Test
    fun testResetWidget() {
        sut.knownTravelerContainer.visibility = View.VISIBLE
        sut.redressContainer.visibility = View.VISIBLE
        sut.frequentFlyerNumber.visibility = View.VISIBLE
        sut.specialRequest.text = "333"
        sut.frequentFlyerPlan.text = "321"
        sut.frequentFlyerNumber.text = "123"
        sut.resetWidget()
        assertEquals(View.GONE, sut.knownTravelerContainer.visibility)
        assertEquals(View.GONE, sut.redressContainer.visibility)
        assertEquals(View.GONE, sut.frequentFlyerNumber.visibility)
        assertEquals("", sut.frequentFlyerNumber.text)
        assertEquals(context.getString(R.string.none), sut.frequentFlyerPlan.text)
        assertEquals(context.getString(R.string.none), sut.specialRequest.text)
    }
}