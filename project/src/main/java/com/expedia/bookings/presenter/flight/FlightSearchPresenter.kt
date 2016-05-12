package com.expedia.bookings.presenter.flight

import android.content.Context
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.adapter.FlightSearchPageAdapter
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.location.CurrentLocationObservable
import com.expedia.bookings.presenter.BaseTwoLocationSearchPresenter
import com.expedia.bookings.services.SuggestionV4Services
import com.expedia.bookings.utils.SuggestionV4Utils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.suggestions.FlightSuggestionAdapter
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeOnClick
import com.expedia.vm.AirportSuggestionViewModel
import com.expedia.vm.BaseSearchViewModel
import com.expedia.vm.FlightSearchViewModel
import com.expedia.vm.SuggestionAdapterViewModel

class FlightSearchPresenter(context: Context, attrs: AttributeSet) : BaseTwoLocationSearchPresenter(context, attrs) {

    val suggestionServices: SuggestionV4Services by lazy {
        Ui.getApplication(getContext()).flightComponent().suggestionsService()
    }

    lateinit private var originSuggestionAdapter: FlightSuggestionAdapter
    lateinit private var destinationSuggestionAdapter: FlightSuggestionAdapter

    var searchViewModel: FlightSearchViewModel by notNullAndObservable { vm ->
        calendarWidgetV2.viewModel = vm
        vm.searchButtonObservable.subscribe { enable ->
            searchButton.setTextColor(if (enable) ContextCompat.getColor(context, R.color.hotel_filter_spinner_dropdown_color) else ContextCompat.getColor(context, R.color.white_disabled))
        }
        searchButton.subscribeOnClick(vm.searchObserver)
        vm.formattedOriginObservable.subscribe { text -> originCardView.setText(text) }
        vm.formattedDestinationObservable.subscribe {
            text -> destinationCardView.setText(text)
            if (this.visibility == VISIBLE && vm.startDate() == null) {
                calendarWidgetV2.showCalendarDialog()
            }
        }

        originSuggestionViewModel = AirportSuggestionViewModel(getContext(), suggestionServices, false, CurrentLocationObservable.create(getContext()))
        destinationSuggestionViewModel = AirportSuggestionViewModel(getContext(), suggestionServices, true, null)
        originSuggestionAdapter = FlightSuggestionAdapter(originSuggestionViewModel)
        destinationSuggestionAdapter = FlightSuggestionAdapter(destinationSuggestionViewModel)
    }

    init {
        travelerWidgetV2.traveler.viewmodel.showSeatingPreference = true
        travelerWidgetV2.traveler.viewmodel.lob = LineOfBusiness.FLIGHTS_V2
        showFlightOneWayRoundTripOptions = true
    }

    override fun inflate() {
        View.inflate(context, R.layout.widget_package_search, this)
        toolBarTitle.text = context.resources.getText(R.string.search_flights)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        setSearchContainerTopMargin(showingSuggestions = false)
        tabs.visibility = View.VISIBLE

        val pagerAdapter = FlightSearchPageAdapter(context)
        viewpager.adapter = pagerAdapter
        viewpager.overScrollMode = ViewPager.OVER_SCROLL_NEVER

        tabs.setupWithViewPager(viewpager)

        tabs.setOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
                // do nothing
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // do nothing
            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                searchViewModel.resetDates()
                searchViewModel.isRoundTripSearch = (tab.position == 0)
            }
        })
    }

    override fun getSuggestionHistoryFileName(): String {
        return SuggestionV4Utils.RECENT_AIRPORT_SUGGESTIONS_FILE
    }

    override fun getSuggestionViewModel(): SuggestionAdapterViewModel {
        return if (isCustomerSelectingOrigin) originSuggestionViewModel else destinationSuggestionViewModel
    }

    override fun getSuggestionAdapter(): RecyclerView.Adapter<RecyclerView.ViewHolder> {
        return if (isCustomerSelectingOrigin) originSuggestionAdapter else destinationSuggestionAdapter
    }

    override fun getSearchViewModel(): BaseSearchViewModel {
        return searchViewModel
    }

}
