package com.expedia.bookings.widget.flights

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.extensions.subscribeOnClick
import com.expedia.bookings.extensions.unsubscribeOnClick
import com.expedia.bookings.hotel.widget.TwoTabCardView
import io.reactivex.subjects.PublishSubject

class FlightOneWayRoundTripTabs(context: Context, attrs: AttributeSet?) : TwoTabCardView(context, attrs) {
    val oneWayClickedSubject = PublishSubject.create<Unit>()
    val roundTripClickedSubject = PublishSubject.create<Unit>()

    init {
        selectRoundTripTab()

        oneWayClickedSubject.subscribe {
            selectOneWayTab()
        }

        roundTripClickedSubject.subscribe {
            selectRoundTripTab()
        }
    }

    private fun selectRoundTripTab() {
        selectLeft()
        rightTabContainer.subscribeOnClick(oneWayClickedSubject)
        leftTabContainer.unsubscribeOnClick()
    }

    private fun selectOneWayTab() {
        selectRight()
        leftTabContainer.subscribeOnClick(roundTripClickedSubject)
        rightTabContainer.unsubscribeOnClick()
    }
}
