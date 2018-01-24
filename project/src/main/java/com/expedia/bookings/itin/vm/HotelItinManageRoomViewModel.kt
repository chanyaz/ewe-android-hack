package com.expedia.bookings.itin.vm

import android.content.Context
import com.expedia.bookings.data.trips.TripHotelRoom
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.itin.widget.HotelItinManageRoomWidget
import io.reactivex.subjects.PublishSubject

class HotelItinManageRoomViewModel(context: Context) {
    val refreshItinCardDataSubject = PublishSubject.create<ItinCardDataHotel>()

    val roomDetailsSubject = PublishSubject.create<TripHotelRoom>()
    val roomChangeAndCancelRulesSubject = PublishSubject.create<List<String>>()
    val itinCardDataHotelSubject = PublishSubject.create<ItinCardDataHotel>()

    val manageRoomWidget by lazy {
        val widget = HotelItinManageRoomWidget(context, null)
        widget.viewModel = this
        widget
    }

    init {
        refreshItinCardDataSubject.subscribe {
            val room = it.getHotelRoom(0)
            if (room != null) {
                roomDetailsSubject.onNext(room)
            }
            val changeAndCancelRules = it.changeAndCancelRules
            if (changeAndCancelRules != null && changeAndCancelRules.isNotEmpty()) {
                roomChangeAndCancelRulesSubject.onNext(changeAndCancelRules)
            }
            itinCardDataHotelSubject.onNext(it)
        }
    }
}
