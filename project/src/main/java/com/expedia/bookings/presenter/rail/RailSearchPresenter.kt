package com.expedia.bookings.presenter.rail

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.expedia.bookings.R
import com.expedia.bookings.presenter.BaseSearchPresenter
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.CalendarWidgetV2
import com.expedia.util.notNullAndObservable
import com.expedia.vm.RailSearchViewModel

class RailSearchPresenter(context: Context, attrs: AttributeSet) : BaseSearchPresenter(context, attrs) {

    val searchContainer: ViewGroup by bindView(R.id.search_container)
    val toolbar: Toolbar by bindView(R.id.toolbar)
    val searchButton: Button by bindView(R.id.search_button)

    val calendarWidgetV2: CalendarWidgetV2 by bindView(R.id.calendar_card)

    var searchViewModel: RailSearchViewModel by notNullAndObservable { vm ->
        calendarWidgetV2.viewModel = vm
    }

    init {
        Ui.getApplication(getContext()).railComponent().inject(this)
        View.inflate(context, R.layout.widget_rail_search_params, this)
        val statusBarHeight = Ui.getStatusBarHeight(getContext())
        if (statusBarHeight > 0) {
            val color = ContextCompat.getColor(context, R.color.rail_primary_color)
            val statusBar = Ui.setUpStatusBar(getContext(), toolbar, searchContainer, color)
            addView(statusBar)
        }

        searchButton.setOnClickListener({
            searchViewModel.searchObserver.onNext(Unit)
        })

        calendarWidgetV2.setOnClickListener {
            calendarWidgetV2.showCalendarDialog()
        }
    }
}