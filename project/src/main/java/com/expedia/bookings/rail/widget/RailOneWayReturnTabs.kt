package com.expedia.bookings.rail.widget

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.extensions.subscribeOnClick
import com.expedia.bookings.extensions.unsubscribeOnClick
import com.expedia.bookings.hotel.widget.TwoTabCardView
import io.reactivex.subjects.PublishSubject

class RailOneWayReturnTabs(context: Context, attrs: AttributeSet?) : TwoTabCardView(context, attrs) {
    val oneWayClickedSubject = PublishSubject.create<Unit>()
    val returnClickedSubject = PublishSubject.create<Unit>()

    init {
        oneWayClickedSubject.subscribe {
            selectOneWayTab()
        }

        returnClickedSubject.subscribe {
            selectReturnTab()
        }

        selectOneWayTab()
    }

    private fun selectOneWayTab() {
        selectLeft()
        rightTabContainer.subscribeOnClick(returnClickedSubject)
        leftTabContainer.unsubscribeOnClick()
    }

    private fun selectReturnTab() {
        selectRight()
        leftTabContainer.subscribeOnClick(oneWayClickedSubject)
        rightTabContainer.unsubscribeOnClick()
    }
}
