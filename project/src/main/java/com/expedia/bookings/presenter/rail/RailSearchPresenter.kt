package com.expedia.bookings.presenter.rail

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.expedia.bookings.R
import com.expedia.bookings.adapter.RailSearchPagerAdapter
import com.expedia.bookings.presenter.BaseSearchPresenter
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.rail.PositionObservableTabLayout
import com.expedia.util.notNullAndObservable
import com.expedia.vm.rail.RailSearchViewModel

class RailSearchPresenter(context: Context, attrs: AttributeSet) : BaseSearchPresenter(context, attrs) {

    val searchContainer: ViewGroup by bindView(R.id.search_container)
    val toolbar: Toolbar by bindView(R.id.toolbar)
    val searchButton: Button by bindView(R.id.search_button)
    val tabs: PositionObservableTabLayout by bindView(R.id.tabs)
    val viewpager: ViewPager by bindView(R.id.viewpager)

    var searchViewModel: RailSearchViewModel by notNullAndObservable {
        viewpager.offscreenPageLimit = 2 //make sure that the first tab is always in the viewpager - we animate that one
        viewpager.adapter = RailSearchPagerAdapter(context, searchViewModel)
        tabs.setupWithViewPager(viewpager)

        tabs.singleToReturnScrollObservable.subscribe({
            //todo - animate calendar card 0->1 for single date vs 2 dates
        })

        tabs.returnToOpenReturnScrollObservable.subscribe({
            //todo - animate calendar card 0->1 for 2 dates vs "open return"
        })
    }

    init {
        Ui.getApplication(getContext()).railComponent().inject(this)
        View.inflate(context, R.layout.widget_rail_search_params, this)
        val statusBarHeight = Ui.getStatusBarHeight(getContext())
        if (statusBarHeight > 0) {
            val color = ContextCompat.getColor(context, R.color.rail_primary_color)
            val statusBar = Ui.setUpStatusBarWithTabs(getContext(), searchContainer, color)
            addView(statusBar)
        }

        searchButton.setOnClickListener({
            searchViewModel.searchObserver.onNext(Unit)
        })

        tabs.singleToReturnScrollObservable.subscribe() {
            val view = viewpager.getChildAt(0);
            if (view != null) {
                view.translationX = viewpager.scrollX.toFloat()
            }
        }

        tabs.returnToOpenReturnScrollObservable.subscribe() {
            val view = viewpager.getChildAt(0);
            if (view != null) {
                view.translationX = Math.min(viewpager.scrollX.toFloat(), (view.measuredWidth * 2).toFloat())
            }
        }
    }
}