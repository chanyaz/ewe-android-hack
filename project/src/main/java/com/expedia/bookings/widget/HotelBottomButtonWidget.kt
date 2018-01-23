package com.expedia.bookings.widget

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import io.reactivex.subjects.PublishSubject

class HotelBottomButtonWidget(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    val changeDatesClickedSubject = PublishSubject.create<Unit>()
    val selectRoomClickedSubject = PublishSubject.create<Unit>()
    val selectDatesClickedSubject = PublishSubject.create<Unit>()

    private enum class Action {
        CHANGE_DATES, SELECT_DATES, SELECT_ROOM
    }
    private var action: Action = Action.SELECT_ROOM
    val buttonBottom: Button by bindView(R.id.sticky_bottom_button)

    init {
        View.inflate(getContext(), R.layout.hotel_bottom_button_widget, this)

        buttonBottom.setOnClickListener {
            when (action) {
                Action.SELECT_DATES -> selectDatesClickedSubject.onNext(Unit)
                Action.CHANGE_DATES -> changeDatesClickedSubject.onNext(Unit)
                else -> selectRoomClickedSubject.onNext(Unit)
            }
        }
    }

    fun showSelectRoom() {
        action = Action.SELECT_ROOM
        buttonBottom.setText(R.string.select_a_room_instruction)
        buttonBottom.setBackgroundColor(ContextCompat.getColor(context, R.color.app_primary))
    }

    fun showChangeDates() {
        action = Action.CHANGE_DATES
        buttonBottom.setText(R.string.change_dates)
        buttonBottom.setBackgroundColor(ContextCompat.getColor(context, R.color.gray600))
    }

    fun showSelectDates() {
        action = Action.SELECT_DATES
        buttonBottom.setText(R.string.hotel_info_site_select_dates)
        buttonBottom.setBackgroundColor(ContextCompat.getColor(context, R.color.app_primary))
    }
}
