package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.flights.AdvanceSearchFilter
import com.expedia.vm.flights.FlightAdvanceSearchViewModel
import com.squareup.phrase.Phrase

class FlightAdvanceSearchWidget(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    val collapsedAdvanceSearchView: LinearLayout by bindView(R.id.advance_search_view_header)
    val advancedSearchChevron: ImageView by bindView(R.id.advanced_search_chevron)
    val expandedAdvanceSearchView: android.widget.HorizontalScrollView by bindView(R.id.expanded_advance_search_view)
    //val advanceSearchFilterContainer: LinearLayout by bindView(R.id.advance_search_filter_container)

    var viewModel: FlightAdvanceSearchViewModel by notNullAndObservable { vm ->
        //advanceSearchFilterContainer.removeAllViews()
        for (filter in AdvanceSearchFilter.values()) {
            val view = Ui.inflate<AdvanceSearchCheckableFilter>(LayoutInflater.from(context), R.layout.flight_advance_search_row, this, false)
            view.bind(filter, vm.selectAdvancedSearch)
          //  advanceSearchFilterContainer.addView(view)
        }
        vm.applySelectedFilter.subscribe {
           // (advanceSearchFilterContainer.getChildAt(it) as AdvanceSearchCheckableFilter).checkObserver.onNext(Unit)
        }
    }

    init {
        View.inflate(context, R.layout.flight_advanced_search_view, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        setContentDescription()
        collapsedAdvanceSearchView.setOnClickListener {
            if (isAdvanceSearchViewExpanded()) {
                collapsedAdvanceSearchView()
            } else {
                expandedAdvanceSearchView()
            }
        }
    }

    fun collapsedAdvanceSearchView() {
        expandedAdvanceSearchView.visibility = View.GONE
        AnimUtils.reverseRotate(advancedSearchChevron)
        setContentDescription()
    }

    fun expandedAdvanceSearchView() {
        expandedAdvanceSearchView.visibility = View.VISIBLE
        AnimUtils.rotate(advancedSearchChevron)
        setContentDescription()
    }

    fun isAdvanceSearchViewExpanded(): Boolean {
        return expandedAdvanceSearchView.visibility == View.VISIBLE
    }

    fun toggleAdvanceSearchWidget() {
        if (viewModel.isAdvanceSearchFilterSelected) {
            expandedAdvanceSearchView()
        } else {
            collapsedAdvanceSearchView()
        }
    }

    fun setContentDescription() {
        val expandState = if (isAdvanceSearchViewExpanded()) context.getString(R.string.row_collapse_button_description) else context.getString(R.string.row_expand_button_description)
        collapsedAdvanceSearchView.contentDescription = Phrase.from(context, R.string.advance_search_cont_desc_TEMPLATE)
                .put("expandstate", expandState)
                .format()
                .toString()
    }
}
