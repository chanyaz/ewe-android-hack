package com.expedia.bookings.itin.common

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.extensions.subscribeTextAndVisibility
import com.expedia.bookings.itin.scopes.ItinTimingsViewModelSetter
import com.expedia.bookings.itin.tripstore.data.ItinLOB
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable

class ItinTimingsWidget<T : ItinLOB>(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs), ItinTimingsViewModelSetter<T> {
    override fun setupViewModel(vm: ItinTimingsWidgetViewModel<T>) {
        viewModel = vm
    }

    val startTitle: TextView by bindView(R.id.start_title)
    val endTitle: TextView by bindView(R.id.end_title)
    val startDate: TextView by bindView(R.id.itin_start_date)
    val endDate: TextView by bindView(R.id.itin_end_date)
    val startTime: TextView by bindView(R.id.itin_start_time)
    val endTime: TextView by bindView(R.id.itin_end_time)

    init {
        View.inflate(context, R.layout.itin_timings_widget, this)
    }

    var viewModel: ItinTimingsWidgetViewModel<T> by notNullAndObservable { vm ->
        vm.startTitleSubject.subscribeTextAndVisibility(startTitle)
        vm.endTitleSubject.subscribeTextAndVisibility(endTitle)
        vm.startDateSubject.subscribeTextAndVisibility(startDate)
        vm.endDateSubject.subscribeTextAndVisibility(endDate)
        vm.startTimeSubject.subscribeTextAndVisibility(startTime)
        vm.endTimeSubject.subscribeTextAndVisibility(endTime)
    }
}
