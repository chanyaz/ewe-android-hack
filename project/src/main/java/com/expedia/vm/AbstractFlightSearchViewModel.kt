package com.expedia.vm

import android.content.Context
import com.expedia.bookings.data.AbstractFlightSearchParams
import com.expedia.bookings.data.BaseSearchParams
import com.expedia.bookings.enums.PassengerCategory


abstract class AbstractFlightSearchViewModel(context: Context) : BaseSearchViewModel(context) {

    override fun getPassengerCategory(childAge: Int, params: BaseSearchParams): PassengerCategory {
        params as AbstractFlightSearchParams
        var category = PassengerCategory.CHILD
        if (childAge < 2) {
            category = if (params.infantSeatingInLap) PassengerCategory.INFANT_IN_LAP else PassengerCategory.INFANT_IN_SEAT
        } else if (childAge < 12) {
            category = PassengerCategory.CHILD
        } else if (childAge < 18) {
            category = PassengerCategory.ADULT_CHILD
        }
        return category
    }
}
