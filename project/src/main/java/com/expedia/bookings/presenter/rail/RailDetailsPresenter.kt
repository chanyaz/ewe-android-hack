package com.expedia.bookings.presenter.rail

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.rail.RailDetailsFareOptionsView
import com.expedia.bookings.widget.rail.RailDetailsTimeline
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.util.subscribeVisibility
import com.expedia.vm.rail.BaseRailDetailsViewModel
import com.expedia.vm.rail.RailFareOptionsViewModel

open class RailDetailsPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    val toolbar: Toolbar by bindView(R.id.rail_details_toolbar)
    val timeline: RailDetailsTimeline by bindView(R.id.details_timeline)
    val fareOptionsView: RailDetailsFareOptionsView by bindView(R.id.details_fare_options)
    val timeRangeTextView: TextView by bindView(R.id.details_times)
    val infoLine: TextView by bindView(R.id.details_info)
    val overtakenMessage: TextView by bindView(R.id.overtaken_message)
    val overtakenDivider: View by bindView(R.id.overtaken_message_divider)

    private val fareOptionsViewModel = RailFareOptionsViewModel()

    var viewModel: BaseRailDetailsViewModel by notNullAndObservable { detailsVM ->
        detailsVM.formattedTimeIntervalSubject.subscribeText(timeRangeTextView)
        detailsVM.formattedLegInfoSubject.subscribeText(infoLine)
        detailsVM.overtaken.subscribeVisibility(overtakenMessage)
        detailsVM.overtaken.subscribeVisibility(overtakenDivider)

        detailsVM.railLegOptionSubject.subscribe(timeline.railLegOptionObserver)
        detailsVM.railOffersPairSubject.subscribe(fareOptionsViewModel.railOffersPairSubject)

        fareOptionsViewModel.showAmenitiesSubject.subscribe(detailsVM.showAmenitiesObservable)
        fareOptionsViewModel.offerSelectedSubject.subscribe(detailsVM.offerSelectedObservable)
        fareOptionsViewModel.showFareRulesSubject.subscribe(detailsVM.showFareRulesObservable)
        fareOptionsView.viewModel = fareOptionsViewModel
    }

    init {
        Ui.getApplication(context).railComponent().inject(this)
        View.inflate(context, R.layout.rail_details_presenter, this)

        toolbar.setNavigationOnClickListener {
            val activity = context as AppCompatActivity
            activity.onBackPressed()
        }
    }
}


