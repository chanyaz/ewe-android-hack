package com.expedia.bookings.presenter.rail

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.expedia.bookings.R
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.RailSearchViewModel

public class RailSearchPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

    val searchContainer: ViewGroup by bindView(R.id.search_container)
    val toolbar: Toolbar by bindView(R.id.toolbar)
    val searchButton: Button by bindView(R.id.search_button)

    var searchViewModel: RailSearchViewModel by notNullAndObservable { vm ->
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
    }
}