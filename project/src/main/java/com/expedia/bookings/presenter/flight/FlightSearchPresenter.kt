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
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.SuggestionV4Utils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.suggestions.SuggestionAdapter
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeOnClick
import com.expedia.vm.AirportSuggestionViewModel
import com.expedia.vm.BaseSearchViewModel
import com.expedia.vm.FlightSearchViewModel
import com.expedia.vm.SuggestionAdapterViewModel
import com.squareup.phrase.Phrase

open class FlightSearchPresenter(context: Context, attrs: AttributeSet) : BaseTwoLocationSearchPresenter(context, attrs) {

    val suggestionServices: SuggestionV4Services by lazy {
        Ui.getApplication(getContext()).flightComponent().suggestionsService()
    }

    var searchViewModel: FlightSearchViewModel by notNullAndObservable { vm ->
        calendarWidgetV2.viewModel = vm
        travelerWidgetV2.travelersSubject.subscribe(vm.travelersObservable)
        travelerWidgetV2.traveler.getViewModel().isInfantInLapObservable.subscribe(vm.isInfantInLapObserver)
        vm.searchButtonObservable.subscribe { enable ->
            searchButton.setTextColor(if (enable) ContextCompat.getColor(context, R.color.hotel_filter_spinner_dropdown_color) else ContextCompat.getColor(context, R.color.white_disabled))
        }
        searchButton.subscribeOnClick(vm.performSearchObserver)

        travelerWidgetV2.traveler.getViewModel().travelerParamsObservable.subscribe { travelers ->
            val noOfTravelers = travelers.getTravelerCount()
            travelerWidgetV2.contentDescription = Phrase.from(context.resources.getQuantityString(R.plurals.search_travelers_cont_desc_TEMPLATE, noOfTravelers)).
                    put("travelers", noOfTravelers).format().toString()
        }

        vm.errorNoDestinationObservable.subscribe { AnimUtils.doTheHarlemShake(destinationCardView) }
        vm.errorNoOriginObservable.subscribe { AnimUtils.doTheHarlemShake(originCardView) }
        vm.errorNoDatesObservable.subscribe { AnimUtils.doTheHarlemShake(calendarWidgetV2) }
        vm.formattedOriginObservable.subscribe { text -> originCardView.setText(text) }
        vm.formattedDestinationObservable.subscribe {
            text ->
            destinationCardView.setText(if (text.isNotEmpty()) text else context.resources.getString(R.string.fly_to_hint))
            if (this.visibility == VISIBLE && vm.startDate() == null && text.isNotEmpty()) {
                calendarWidgetV2.showCalendarDialog()
            }
        }

        vm.errorOriginSameAsDestinationObservable.subscribe { message ->
            showErrorDialog(message)
        }

        originSuggestionViewModel = AirportSuggestionViewModel(getContext(), suggestionServices, false, CurrentLocationObservable.create(getContext()))
        destinationSuggestionViewModel = AirportSuggestionViewModel(getContext(), suggestionServices, true, null)
        originSuggestionAdapter = SuggestionAdapter(originSuggestionViewModel)
        destinationSuggestionAdapter = SuggestionAdapter(destinationSuggestionViewModel)
    }

    lateinit private var originSuggestionAdapter: SuggestionAdapter
    lateinit private var destinationSuggestionAdapter: SuggestionAdapter

    init {
        travelerWidgetV2.traveler.getViewModel().showSeatingPreference = true
        travelerWidgetV2.traveler.getViewModel().lob = LineOfBusiness.FLIGHTS_V2
        showFlightOneWayRoundTripOptions = true
    }

    override fun inflate() {
        View.inflate(context, R.layout.widget_base_flight_search, this)
        toolBarTitle.text = context.resources.getText(R.string.flights_title)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        tabs.visibility = View.VISIBLE

        val pagerAdapter = FlightSearchPageAdapter(context)
        viewpager.adapter = pagerAdapter
        viewpager.overScrollMode = ViewPager.OVER_SCROLL_NEVER

        tabs.setupWithViewPager(viewpager)

        tabs.setOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
                // do nothing
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // do nothing
            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                val isRoundTripSearch = tab.position == 0
                searchViewModel.isRoundTripSearchObservable.onNext(isRoundTripSearch)
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

    override fun getOriginSearchBoxPlaceholderText(): String {
        return context.resources.getString(R.string.fly_from_hint)
    }

    override fun getDestinationSearchBoxPlaceholderText(): String {
        return context.resources.getString(R.string.fly_to_hint)
    }
}
