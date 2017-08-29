package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.vm.hotel.BaseHotelFilterViewModel

class HotelClientFilterView(context: Context, attrs: AttributeSet?) : BaseHotelFilterView(context, attrs) {
    val dynamicFeedbackWidget: DynamicFeedbackWidget by bindView(R.id.dynamic_feedback_container)
    val dynamicFeedbackClearButton: TextView by bindView(R.id.dynamic_feedback_clear_button)

    init {
        dynamicFeedbackWidget.hideDynamicFeedback()
        dynamicFeedbackClearButton.setOnClickListener(clearFilterClickListener)
    }

    override fun inflate() {
        View.inflate(context, R.layout.hotel_client_filter_view, this)
    }

    override fun bindViewModel(vm: BaseHotelFilterViewModel) {
        super.bindViewModel(vm)
        vm.finishClear.subscribe {
            dynamicFeedbackWidget.hideDynamicFeedback()
        }

        vm.updateDynamicFeedbackWidget.subscribe {
            if (it < 0) {
                dynamicFeedbackWidget.hideDynamicFeedback()
            } else {
                dynamicFeedbackWidget.showDynamicFeedback()
                dynamicFeedbackWidget.setDynamicCounterText(it)
            }
        }

        vm.filteredZeroResultObservable.subscribe {
            shakeForError()
        }
    }

    override fun shakeForError() {
        dynamicFeedbackWidget.animateDynamicFeedbackWidget()
    }
}