package com.expedia.bookings.presenter.rail

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.RailResultsViewModel

public class RailResultsPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

    var viewmodel: RailResultsViewModel by notNullAndObservable { vm ->
        vm.railResultsObservable.subscribe {
            resultsProgress.visibility = GONE
            resultsJson.visibility = VISIBLE
            resultsJson.text = it.toString()
        }
    }

    val resultsProgress: ProgressBar by bindView(R.id.results_progress)
    val resultsJson: TextView by bindView(R.id.rail_results_json)

    val resultsContainer: ViewGroup by bindView(R.id.results_container)
    val toolbar: Toolbar by bindView(R.id.toolbar)

    init {
        Ui.getApplication(getContext()).railComponent().inject(this)
        View.inflate(context, R.layout.widget_rail_results, this)
        val statusBarHeight = Ui.getStatusBarHeight(getContext())
        if (statusBarHeight > 0) {
            val color = ContextCompat.getColor(context, R.color.rail_primary_color)
            val statusBar = Ui.setUpStatusBar(getContext(), toolbar, resultsContainer, color)
            addView(statusBar)
        }

        resultsProgress.visibility = VISIBLE
        resultsJson.visibility = GONE
    }
}

