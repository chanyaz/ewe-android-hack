package com.expedia.bookings.test.robolectric

import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.flights.TravelerFrequentFlyerMembership
import com.expedia.vm.traveler.FrequentFlyerProgramNumberViewModel
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FrequentFlyerProgramNumberViewModelTest {

    @Test
    fun testTravelerDataPointToRightFFMembershipNumber() {
        val traveler = Traveler()
        traveler.frequentFlyerMemberships.put("", TravelerFrequentFlyerMembership())
        val frequentFlyerProgramNumberViewModel = FrequentFlyerProgramNumberViewModel(traveler, "AA")
        frequentFlyerProgramNumberViewModel.textSubject.onNext("123")

        assertEquals(traveler.frequentFlyerMemberships.get("AA")!!.membershipNumber, "123")

        frequentFlyerProgramNumberViewModel.textSubject.onNext("1")

        assertEquals(traveler.frequentFlyerMemberships.get("AA")!!.membershipNumber, "1")

        frequentFlyerProgramNumberViewModel.textSubject.onNext("")

        assertEquals(traveler.frequentFlyerMemberships.get("AA")!!.membershipNumber, "")
    }
}
