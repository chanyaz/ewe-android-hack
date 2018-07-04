package com.expedia.bookings.lx.presenter

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.lx.LxSearchParams
import com.expedia.bookings.extensions.setAccessibilityHoverFocus
import com.expedia.bookings.extensions.subscribeOnClick
import com.expedia.bookings.lx.vm.LXSearchViewModel
import com.expedia.bookings.location.CurrentLocationObservable
import com.expedia.bookings.presenter.BaseSearchPresenter
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.SuggestionV4Utils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.isLXMultipleDatesSearchEnabled
import com.expedia.bookings.widget.LXSuggestionAdapter
import com.expedia.util.notNullAndObservable
import com.expedia.vm.BaseSearchViewModel
import com.expedia.bookings.lx.vm.LXSuggestionAdapterViewModel
import com.expedia.util.endlessObserver
import com.expedia.vm.BaseSuggestionAdapterViewModel
import com.squareup.phrase.Phrase
import io.reactivex.Observer

class LXSearchPresenter(context: Context, attrs: AttributeSet) : BaseSearchPresenter(context, attrs) {

    val searchParamsToFillFormObserver: Observer<LxSearchParams> = endlessObserver { searchParams ->
        searchViewModel.destinationLocationObserver.onNext(getSuggestionFromLocation(searchParams.location))
        searchViewModel.datesUpdated(searchParams.startDate, searchParams.endDate)
        selectDates(searchParams.startDate, searchParams.endDate)
        searchViewModel.searchButtonObservable.onNext(true)
    }

    var searchViewModel: LXSearchViewModel by notNullAndObservable { vm ->
        calendarWidgetV2.viewModel = vm
        vm.searchButtonObservable.subscribe { enable ->
            searchButton.setTextColor(if (enable) ContextCompat.getColor(context, R.color.hotel_filter_spinner_dropdown_color) else ContextCompat.getColor(context, R.color.white_disabled))
            if (AccessibilityUtil.isTalkBackEnabled(context)) {
                searchButton.isEnabled = enable
            }
        }

        vm.locationTextObservable.subscribe { locationText ->
            firstLaunch = false
            destinationCardView.setText(locationText)
            destinationCardView.contentDescription = Phrase.from(context, R.string.location_edit_box_cont_desc_TEMPLATE)
                    .put("location", locationText)
                    .format().toString()
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
        vm.dateAccessibilityObservable.subscribe {
            text ->
            calendarWidgetV2.contentDescription = text
        }

        vm.a11yFocusSelectDatesObservable.subscribe {
            calendarWidgetV2.setAccessibilityHoverFocus()
        }
    }

    private val lxSuggestionAdapter by lazy {
        LXSuggestionAdapter(suggestionViewModel)
    }

    var suggestionViewModel: LXSuggestionAdapterViewModel by notNullAndObservable { vm ->
        vm.suggestionSelectedSubject.subscribe { searchSuggestion ->
            com.mobiata.android.util.Ui.hideKeyboard(this)
            val suggestion = searchSuggestion.suggestionV4
            searchViewModel.destinationLocationObserver.onNext(suggestion)
            val suggestionName = HtmlCompat.stripHtml(suggestion.regionNames.displayName)
            destinationCardView.setText(suggestionName)
            searchLocationEditText?.setQuery(suggestionName, false)
            SuggestionV4Utils.saveSuggestionHistory(context, suggestion, getSuggestionHistoryFileName(), shouldSaveSuggestionHierarchyChildInfo())
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
        suggestionViewModel = LXSuggestionAdapterViewModel(context, service, CurrentLocationObservable.create(context), true)
        searchLocationEditText?.queryHint = context.resources.getString(R.string.location_activity_details)
        if (isLXMultipleDatesSearchEnabled()) {
            calendarWidgetV2.text = context.resources.getString(R.string.select_dates)
            calendarWidgetV2.contentDescription = context.resources.getString(R.string.base_search_dates_button_cont_desc)
        } else {
            calendarWidgetV2.text = context.resources.getString(R.string.select_start_date)
            calendarWidgetV2.contentDescription = context.resources.getString(R.string.lx_search_start_date_button_cont_desc)
        }
    }

    override fun getSearchViewModel(): BaseSearchViewModel {
        return searchViewModel
    }

    override fun getSuggestionViewModel(): BaseSuggestionAdapterViewModel {
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

    override fun getLineOfBusiness(): LineOfBusiness {
        return LineOfBusiness.LX
    }

    fun getSuggestionFromLocation(locationName: String): SuggestionV4 {
        val suggestionV4 = SuggestionV4()
        val regionNames = SuggestionV4.RegionNames()
        regionNames.fullName = locationName
        regionNames.displayName = locationName
        regionNames.shortName = locationName
        suggestionV4.regionNames = regionNames
        return suggestionV4
    }
}
