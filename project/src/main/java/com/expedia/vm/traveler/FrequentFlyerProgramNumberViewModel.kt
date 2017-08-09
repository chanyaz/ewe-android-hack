package com.expedia.vm.traveler

import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.flights.TravelerFrequentFlyerMembership
import com.expedia.bookings.section.InvalidCharacterHelper
import kotlin.properties.Delegates

class FrequentFlyerProgramNumberViewModel(traveler: Traveler, var airlineKey: String) : BaseTravelerValidatorViewModel() {
    var traveler: Traveler by Delegates.notNull()

    override val invalidCharacterMode = InvalidCharacterHelper.Mode.ALPHANUMERIC

    init {
        this.traveler = traveler
        textSubject.subscribe {
            if (!airlineKey.isNullOrBlank()) {
                if (traveler.frequentFlyerMemberships.containsKey(airlineKey)) {
                    traveler.frequentFlyerMemberships.get(airlineKey)?.setMembershipNumber(it)
                } else {
                    val frequentFlyerMembership = TravelerFrequentFlyerMembership()
                    frequentFlyerMembership.setAirlineCode(airlineKey!!)
                    frequentFlyerMembership.setMembershipNumber(it)
                    frequentFlyerMembership.setPlanCode("")
                    traveler.frequentFlyerMemberships.put(airlineKey,frequentFlyerMembership)
                }
            }
        }
    }

    fun setAirline(airlineKey: String) {
        this.airlineKey = airlineKey
    }

    override fun isValid(): Boolean {
        return true
    }
}