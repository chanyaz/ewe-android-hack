package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.presenter.BaseSearchPresenter
import com.expedia.util.notNullAndObservable
import com.expedia.vm.HotelSearchViewModel
import com.expedia.vm.HotelTravelerParams
import org.joda.time.LocalDate

abstract class BaseHotelSearchPresenter(context: Context, attrs: AttributeSet) : BaseSearchPresenter(context, attrs) {

    open var searchViewModel: HotelSearchViewModel by notNullAndObservable { vm ->
    }

    open fun animationStart(forward: Boolean) {
    }

    open fun animationUpdate(f: Float, forward: Boolean) {
    }

    open fun animationFinalize(forward: Boolean) {
    }

    abstract fun selectTravelers(hotelTravelerParams: HotelTravelerParams)
    abstract fun selectDates(startDate: LocalDate?, endDate: LocalDate?)

}
