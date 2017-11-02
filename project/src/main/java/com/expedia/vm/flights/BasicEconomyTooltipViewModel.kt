package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.FlightLeg
import com.squareup.phrase.Phrase
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class BasicEconomyTooltipViewModel {
    val basicEconomyTooltipInfo = PublishSubject.create<List<FlightLeg.BasicEconomyTooltipInfo>>()
    val basicEconomyTooltipFareRules = basicEconomyTooltipInfo.map { it[0].fareRules }
    val basicEconomyTooltipTitle = basicEconomyTooltipInfo.map { it[0].fareRulesTitle }
}