package com.expedia.vm.traveler

import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.flights.TravelerFrequentFlyerMembership
import com.expedia.bookings.section.InvalidCharacterHelper
import kotlin.properties.Delegates

class FrequentFlyerProgramNumberViewModel(traveler: Traveler, var airlineKey: String) : BaseTravelerValidatorViewModel() {
    var traveler: Traveler by Delegates.notNull()
    var airlineId: String = ""
    var planCode: String = ""

    override val invalidCharacterMode = InvalidCharacterHelper.Mode.ANY

    init {
        this.traveler = traveler
        textSubject.subscribe {
            if (!airlineKey.isNullOrBlank()) {
                if (traveler.frequentFlyerMemberships.containsKey(airlineKey)) {
                    traveler.frequentFlyerMemberships[airlineKey]?.membershipNumber = it
                    if (!airlineId.isNullOrEmpty()) {
                        traveler.frequentFlyerMemberships[airlineKey]?.frequentFlyerPlanID = airlineId
                    }
                    if (!planCode.isNullOrEmpty()) {
                        traveler.frequentFlyerMemberships[airlineKey]?.planCode = planCode
                    }
                } else {
                    val frequentFlyerMembership = TravelerFrequentFlyerMembership()
                    frequentFlyerMembership.airlineCode = airlineKey
                    frequentFlyerMembership.membershipNumber = it
                    frequentFlyerMembership.planCode = planCode
                    frequentFlyerMembership.frequentFlyerPlanID = airlineId
                    traveler.frequentFlyerMemberships.put(airlineKey, frequentFlyerMembership)
                }
            }
        }
    }

    override fun isValid(): Boolean {
        return true
    }
}
