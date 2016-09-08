package com.expedia.bookings.presenter.rail

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.rail.RailDetailsFareOptionsView
import com.expedia.bookings.widget.rail.RailDetailsTimeline
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.vm.rail.RailDetailsViewModel

class RailDetailsPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    val detailsContainer: ViewGroup by bindView(R.id.details_container)
    val toolbar: Toolbar by bindView(R.id.toolbar)
    val timeline: RailDetailsTimeline by bindView(R.id.details_timeline)
    val fareOptionsView: RailDetailsFareOptionsView by bindView(R.id.details_fare_options)
    val timeRangeTextView: TextView by bindView(R.id.details_times)
    val infoLine: TextView by bindView(R.id.details_info)

    var viewmodel: RailDetailsViewModel by notNullAndObservable { vm ->
        timeline.viewmodel = vm
        fareOptionsView.viewmodel = vm
        vm.offerViewModel.formattedTimeIntervalSubject.subscribeText(timeRangeTextView)
        vm.offerViewModel.formattedLegInfoSubject.subscribeText(infoLine)
    }

    init {
        Ui.getApplication(context).railComponent().inject(this)
        View.inflate(context, R.layout.rail_details_presenter, this)

        toolbar.setNavigationOnClickListener {
            val activity = context as AppCompatActivity
            activity.onBackPressed()
        }
        val statusBarHeight = Ui.getStatusBarHeight(context)
        if (statusBarHeight > 0) {
            val color = ContextCompat.getColor(context, R.color.rail_primary_color)
            val statusBar = Ui.setUpStatusBar(context, toolbar, detailsContainer, color)
            addView(statusBar)
        }
    }
}


