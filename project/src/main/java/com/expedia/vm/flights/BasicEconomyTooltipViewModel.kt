package com.expedia.vm.flights

import com.expedia.bookings.data.flights.FlightLeg
import io.reactivex.subjects.PublishSubject

class BasicEconomyTooltipViewModel {
    val basicEconomyTooltipInfo = PublishSubject.create<List<FlightLeg.BasicEconomyTooltipInfo>>()
    val basicEconomyTooltipFareRules = basicEconomyTooltipInfo.filter { it.isNotEmpty() }.map { it[0].fareRules }
    val basicEconomyTooltipTitle = basicEconomyTooltipInfo.filter { it.isNotEmpty() }.map { it[0].fareRulesTitle }
}
