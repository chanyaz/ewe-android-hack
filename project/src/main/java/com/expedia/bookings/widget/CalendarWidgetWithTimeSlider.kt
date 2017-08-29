package com.expedia.bookings.widget

import android.content.Context
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.expedia.bookings.R
import com.expedia.bookings.fragment.CalendarDialogFragment
import com.expedia.bookings.fragment.TimeAndCalendarDialogFragment
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.vm.SearchViewModelWithTimeSliderCalendar

class CalendarWidgetWithTimeSlider(context: Context, attrs: AttributeSet?) : CardView(context, attrs) {

    val dateText: TextView by bindView(R.id.dateLabel)
    var fullHeight = 0
    var smallHeight = 0
    val interpolator = AccelerateDecelerateInterpolator()

    init {
        View.inflate(context, R.layout.widget_rail_calendar, this)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (fullHeight == 0) {
            fullHeight = measuredHeight
            val innerSize = (measuredHeight - paddingTop - paddingBottom)
            smallHeight = innerSize / 2 + paddingTop + paddingBottom
        }
    }

    var viewModel: SearchViewModelWithTimeSliderCalendar by notNullAndObservable {
        it.dateTextObservable.subscribeText(this.dateText)
        it.errorNoDestinationObservable.subscribe { AnimUtils.doTheHarlemShake(this) }
        it.errorNoOriginObservable.subscribe { AnimUtils.doTheHarlemShake(this) }
        it.errorNoDatesObservable.subscribe { AnimUtils.doTheHarlemShake(this) }
    }

    var calendarDialog: CalendarDialogFragment? = null

    fun showCalendarDialog() {
        calendarDialog = TimeAndCalendarDialogFragment.createFragment(viewModel)

        val fragmentManager = (context as FragmentActivity).supportFragmentManager
        calendarDialog?.show(fragmentManager, Constants.TAG_CALENDAR_DIALOG)
    }

    fun dismissDialog() {
        calendarDialog?.dismiss()
    }
}