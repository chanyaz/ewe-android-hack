package com.expedia.bookings.presenter.flight

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.location.CurrentLocationObservable
import com.expedia.bookings.presenter.BaseSearchPresenterV2
import com.expedia.bookings.services.SuggestionV4Services
import com.expedia.bookings.utils.SuggestionV4Utils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.PackageSuggestionAdapter
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeOnClick
import com.expedia.vm.AirportSuggestionViewModel
import com.expedia.vm.DatedSearchViewModel
import com.expedia.vm.FlightSearchViewModel
import com.expedia.vm.SuggestionAdapterViewModel

class FlightSearchPresenter(context: Context, attrs: AttributeSet) : BaseSearchPresenterV2(context, attrs) {

    val suggestionServices: SuggestionV4Services by lazy {
        Ui.getApplication(getContext()).flightComponent().suggestionsService()
    }
    lateinit private var departureAdapter: PackageSuggestionAdapter
    lateinit private var arrivalAdapter: PackageSuggestionAdapter

    var searchViewModel: FlightSearchViewModel by notNullAndObservable { vm ->
        calendarWidgetV2.viewModel = vm
        vm.searchButtonObservable.subscribe { enable ->
            searchButton.setTextColor(if (enable) ContextCompat.getColor(context, R.color.hotel_filter_spinner_dropdown_color) else ContextCompat.getColor(context, R.color.white_disabled))
        }
        searchButton.subscribeOnClick(vm.searchObserver)
    }

    init {
        departureSuggestionVM = AirportSuggestionViewModel(getContext(), suggestionServices, false, CurrentLocationObservable.create(getContext()))
        arrivalSuggestionVM = AirportSuggestionViewModel(getContext(), suggestionServices, true, null)
        departureAdapter = PackageSuggestionAdapter(departureSuggestionVM)
        arrivalAdapter = PackageSuggestionAdapter(arrivalSuggestionVM)
    }

    override fun inflate() {
        View.inflate(context, R.layout.widget_package_search, this)
        toolBarTitle.text = context.resources.getText(R.string.search_flights)
    }

    override fun getSuggestionHistoryFileName(): String {
        return SuggestionV4Utils.RECENT_AIRPORT_SUGGESTIONS_FILE
    }

    override fun getSuggestionViewModel(): SuggestionAdapterViewModel {
        return if (isCustomerSelectingDeparture) departureSuggestionVM else arrivalSuggestionVM
    }

    override fun getSuggestionAdapter(): RecyclerView.Adapter<RecyclerView.ViewHolder> {
        return if (isCustomerSelectingDeparture) departureAdapter else arrivalAdapter
    }

    override fun getSearchViewModel(): DatedSearchViewModel {
        return searchViewModel
    }

}