package com.expedia.bookings.presenter.lx

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.location.CurrentLocationObservable
import com.expedia.bookings.presenter.BaseSearchPresenter
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.SuggestionV4Utils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.LXSuggestionAdapter
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeOnClick
import com.expedia.vm.BaseSearchViewModel
import com.expedia.vm.LXSearchViewModel
import com.expedia.vm.LXSuggestionAdapterViewModel
import com.expedia.vm.SuggestionAdapterViewModel

class LXSearchPresenter(context: Context, attrs: AttributeSet) : BaseSearchPresenter(context, attrs) {

    var searchViewModel: LXSearchViewModel by notNullAndObservable { vm ->
        calendarWidgetV2.viewModel = vm
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

        searchButton.subscribeOnClick(vm.searchObserver)
    }

    private val lxSuggestionAdapter by lazy {
        LXSuggestionAdapter(suggestionViewModel)
    }

    var suggestionViewModel: LXSuggestionAdapterViewModel by notNullAndObservable { vm ->
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
    }

    override fun getSuggestionHistoryFileName(): String {
        return SuggestionV4Utils.RECENT_LX_SUGGESTIONS_FILE
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        val service = Ui.getApplication(context).lxComponent().suggestionsService()
        suggestionViewModel = LXSuggestionAdapterViewModel(context, service, CurrentLocationObservable.create(context), true, true)
        searchLocationEditText?.queryHint = context.resources.getString(R.string.location_activity_details)
    }

    override fun getSearchViewModel(): BaseSearchViewModel {
       return searchViewModel
    }

    override fun getSuggestionViewModel(): SuggestionAdapterViewModel {
       return suggestionViewModel
    }

    override fun getSuggestionAdapter(): RecyclerView.Adapter<RecyclerView.ViewHolder> {
       return lxSuggestionAdapter
    }

    override fun getOriginSearchBoxPlaceholderText(): String {
        throw UnsupportedOperationException("No origin for LX search")
    }

    override fun getDestinationSearchBoxPlaceholderText(): String {
        return context.resources.getString(R.string.enter_destination_hint)
    }
}
