package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.location.CurrentLocationObservable
import com.expedia.bookings.presenter.BaseSearchPresenter
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.SuggestionV4Utils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.HotelSuggestionAdapter
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeOnClick
import com.expedia.vm.BaseSearchViewModel
import com.expedia.vm.HotelSearchViewModel
import com.expedia.bookings.R
import com.expedia.bookings.widget.ShopWithPointsWidget
import com.expedia.vm.HotelSuggestionAdapterViewModel
import com.expedia.vm.SuggestionAdapterViewModel

class HotelSearchPresenter(context: Context, attrs: AttributeSet) : BaseSearchPresenter(context, attrs) {

    var searchViewModel: HotelSearchViewModel by notNullAndObservable { vm ->
        calendarWidgetV2.viewModel = vm
        travelerWidgetV2.travelersSubject.subscribe(vm.travelersObservable)
        vm.searchButtonObservable.subscribe { enable ->
            searchButton.setTextColor(if (enable) ContextCompat.getColor(context, R.color.hotel_filter_spinner_dropdown_color) else ContextCompat.getColor(context, R.color.white_disabled))
        }

        vm.locationTextObservable.subscribe { locationText ->
            firstLaunch = false
            destinationCardView.setText(locationText)
            if (this.visibility == VISIBLE && vm.startDate() == null) {
                calendarWidgetV2.showCalendarDialog()
            }
        }
        vm.errorNoDestinationObservable.subscribe {
            AnimUtils.doTheHarlemShake(destinationCardView)
        }

        vm.errorNoDatesObservable.subscribe {
            AnimUtils.doTheHarlemShake(calendarWidgetV2)
        }

        vm.errorMaxDurationObservable.subscribe { message ->
            showErrorDialog(message)
        }

        vm.errorMaxRangeObservable.subscribe { message ->
            showErrorDialog(message)
        }

        searchButton.subscribeOnClick(vm.searchObserver)
    }

    private val hotelSuggestionAdapter by lazy {
        HotelSuggestionAdapter(suggestionViewModel)
    }

    var suggestionViewModel: HotelSuggestionAdapterViewModel by notNullAndObservable { vm ->
        vm.suggestionSelectedSubject.subscribe { suggestion ->
            com.mobiata.android.util.Ui.hideKeyboard(this)
            searchViewModel.destinationLocationObserver.onNext(suggestion)
            val suggestionName = Html.fromHtml(suggestion.regionNames.displayName).toString()
            destinationCardView.setText(suggestionName)
            searchLocationEditText?.setQuery(suggestionName, false)
            SuggestionV4Utils.saveSuggestionHistory(context, suggestion, getSuggestionHistoryFileName())
            showDefault()
        }
    }

    override fun inflate() {
        View.inflate(context, R.layout.widget_search_params, this)
        travelerCardView.visibility = View.VISIBLE
        shopWithPointsWidget = swpWidgetStub.inflate().findViewById(R.id.widget_points_details) as ShopWithPointsWidget

    }

    override fun getSuggestionHistoryFileName(): String {
        return SuggestionV4Utils.RECENT_HOTEL_SUGGESTIONS_FILE
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        val service = Ui.getApplication(context).hotelComponent().suggestionsService()
        suggestionViewModel = HotelSuggestionAdapterViewModel(context, service, CurrentLocationObservable.create(context), true, true)
        searchLocationEditText?.queryHint = context.resources.getString(R.string.enter_destination_hint)
    }

    override fun getSearchViewModel(): BaseSearchViewModel {
       return searchViewModel
    }

    override fun getSuggestionViewModel(): SuggestionAdapterViewModel {
       return suggestionViewModel
    }

    override fun getSuggestionAdapter(): RecyclerView.Adapter<RecyclerView.ViewHolder> {
       return hotelSuggestionAdapter
    }

    override fun getOriginSearchBoxPlaceholderText(): String {
        throw UnsupportedOperationException("No origin for Hotel search")
    }

    override fun getDestinationSearchBoxPlaceholderText(): String {
        return context.resources.getString(R.string.enter_destination_hint)
    }
}
