package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.HotelSearchParams
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.TravelerParams
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.travelgraph.SearchInfo
import com.expedia.bookings.extensions.setAccessibilityHoverFocus
import com.expedia.bookings.extensions.setInverseVisibility
import com.expedia.bookings.extensions.setVisibility
import com.expedia.bookings.extensions.subscribeText
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.hotel.tracking.SuggestionTrackingData
import com.expedia.bookings.hotel.widget.AdvancedSearchOptionsView
import com.expedia.bookings.hotel.widget.HotelSuggestionAdapter
import com.expedia.bookings.location.CurrentLocationObservable
import com.expedia.bookings.presenter.BaseSearchPresenter
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.tracking.hotel.HotelSearchTrackingDataBuilder
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.travelgraph.vm.TravelGraphViewModel
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.AnimUtils

import com.expedia.bookings.utils.SuggestionV4Utils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.ShopWithPointsWidget
import com.expedia.bookings.widget.shared.SearchInputTextView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.BaseSearchViewModel
import com.expedia.vm.BaseSuggestionAdapterViewModel
import com.expedia.vm.HotelSearchViewModel
import com.expedia.vm.HotelSuggestionAdapterViewModel
import com.expedia.vm.hotel.AdvancedSearchOptionsViewModel
import com.squareup.phrase.Phrase
import javax.inject.Inject

class HotelSearchPresenter(context: Context, attrs: AttributeSet) : BaseSearchPresenter(context, attrs) {
    lateinit var searchTrackingBuilder: HotelSearchTrackingDataBuilder
        @Inject set

    val params = HotelSearchParams()

    private val mainContainer: LinearLayout by bindView(R.id.main_container)
    private val advancedOptionsContainerCard: FrameLayout by bindView(R.id.advanced_options_card_view)
    private val advancedOptionsView: SearchInputTextView by bindView(R.id.advanced_options_view)
    private val advancedOptionsDetails: AdvancedSearchOptionsView by bindView(R.id.search_options_details_view)

    private val advancedOptionsViewModel = AdvancedSearchOptionsViewModel(context)

    var searchViewModel: HotelSearchViewModel by notNullAndObservable { vm ->
        calendarWidgetV2.viewModel = vm
        travelerWidgetV2.travelersSubject.subscribe(vm.travelersObservable)
        vm.searchButtonObservable.subscribe { enable ->
            searchButton.setTextColor(if (enable) ContextCompat.getColor(context, R.color.search_dialog_background_v2) else ContextCompat.getColor(context, R.color.white_disabled))
            if (AccessibilityUtil.isTalkBackEnabled(context)) {
                searchButton.isEnabled = enable
            }
        }

        vm.locationTextObservable.subscribe { locationText ->
            firstLaunch = false
            updateDestinationText(locationText)
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

        vm.dateAccessibilityObservable.subscribe { text ->
            calendarWidgetV2.contentDescription = text
        }

        vm.a11yFocusSelectDatesObservable.subscribe {
            calendarWidgetV2.setAccessibilityHoverFocus()
        }

        advancedOptionsViewModel.searchOptionsSubject.subscribe(searchViewModel.advancedOptionsObserver)

        searchButton.setOnClickListener {
            searchTrackingBuilder.markSearchClicked()
            val lastSuggestionV4 = suggestionViewModel.getLastSelectedSuggestion()
            if (lastSuggestionV4 != null) {
                suggestionTrackingData.updateData(lastSuggestionV4)
            }
            suggestionTrackingData.suggestionsFocused = suggestionListFocused
            OmnitureTracking.trackHotelSuggestionBehavior(suggestionTrackingData)
            vm.searchObserver.onNext(Unit)
        }
    }

    private var suggestionListFocused = false

    private var suggestionTrackingData = SuggestionTrackingData()

    private val hotelSuggestionAdapter by lazy { HotelSuggestionAdapter(suggestionViewModel) }

    var suggestionViewModel: HotelSuggestionAdapterViewModel by notNullAndObservable { vm ->
        vm.suggestionSelectedSubject.subscribe { searchSuggestion ->
            com.mobiata.android.util.Ui.hideKeyboard(this)

            suggestionTrackingData = searchSuggestion.trackingData!!
            suggestionTrackingData.suggestionSelected = true
            suggestionTrackingData.charactersTypedCount = suggestionViewModel.getLastQuery().count()

            val suggestion = searchSuggestion.suggestionV4
            searchViewModel.destinationLocationObserver.onNext(suggestion)
            SuggestionV4Utils.saveSuggestionHistory(context, suggestion, getSuggestionHistoryFileName(), shouldSaveSuggestionHierarchyChildInfo())
            updateSearchOptions(suggestion)

            showDefault()
        }

        vm.searchInfoSelectedSubject.subscribe { searchInfo ->
            com.mobiata.android.util.Ui.hideKeyboard(this)
            updateWithRecentSearchInfo(searchInfo)

            HotelTracking.trackHotelRecentSearchClick()
            showDefault()
        }
    }

    private var travelGraphViewModel: TravelGraphViewModel

    override fun inflate() {
        View.inflate(context, R.layout.widget_hotel_search, this)
        shopWithPointsWidget = swpWidgetStub.inflate().findViewById<ShopWithPointsWidget>(R.id.widget_points_details)
    }

    init {
        Ui.getApplication(getContext()).hotelComponent().inject(this)

        suggestionListShownSubject.subscribe {
            suggestionListFocused = true
        }

        val tgService = Ui.getApplication(context).hotelComponent().travelGraphServices()
        travelGraphViewModel = TravelGraphViewModel(context, tgService)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        val service = Ui.getApplication(context).hotelComponent().suggestionsService()
        suggestionViewModel = HotelSuggestionAdapterViewModel(context, service, CurrentLocationObservable.create(context))
        searchLocationEditText?.queryHint = context.resources.getString(R.string.enter_destination_hint)

        travelGraphViewModel.searchHistoryResultSubject.subscribe { searchHistory ->
            suggestionViewModel.setUserSearchHistory(searchHistory.getRecentSearchInfos())
        }

        advancedOptionsDetails.viewModel = advancedOptionsViewModel
        addTransition(searchToAdvancedOptions)

        advancedOptionsView.setOnClickListener {
            show(advancedOptionsDetails)
            HotelTracking.trackHotelSuperSearchFilter()
        }

        advancedOptionsViewModel.doneClickedSubject.subscribe {
            showDefault()
        }

        advancedOptionsViewModel.searchOptionsSummarySubject.subscribeText(advancedOptionsView)

        val showAdvancedOptions = AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppHotelSuperSearch)
        advancedOptionsContainerCard.setVisibility(showAdvancedOptions)

        fetchUserSearchHistory()
    }

    fun animationFinalize(forward: Boolean) {
        super.animationFinalize()
        if (!forward) {
            fetchUserSearchHistory()
        }
    }

    override fun back(): Boolean {
        if (AdvancedSearchOptionsView::class.java.name == currentState) {
            advancedOptionsViewModel.doneObservable.onNext(Unit)
            return back(0)
        }
        return super.back()
    }

    fun resetSuggestionTracking() {
        suggestionListFocused = false
        suggestionTrackingData.reset()
    }

    override fun getSuggestionHistoryFileName(): String {
        return SuggestionV4Utils.RECENT_HOTEL_SUGGESTIONS_FILE
    }

    override fun getSearchViewModel(): BaseSearchViewModel {
        return searchViewModel
    }

    override fun getSuggestionViewModel(): BaseSuggestionAdapterViewModel {
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

    override fun getLineOfBusiness(): LineOfBusiness {
        return LineOfBusiness.HOTELS
    }

    fun resetSearchOptions() {
        if (AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppHotelSuperSearch)) {
            advancedOptionsViewModel.resetSearchOptionsObservable.onNext(Unit)
        }
    }

    private fun fetchUserSearchHistory() {
        if (isUserSearchHistoryEnabled()) {
            suggestionViewModel.setUserSearchHistory(emptyList())
            travelGraphViewModel.fetchUserHistory()
        }
    }

    private fun isUserSearchHistoryEnabled(): Boolean =
            AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.HotelRecentSearch)

    private fun updateSearchOptions(suggestion: SuggestionV4) {
        if (!AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppHotelSuperSearch)) {
            return
        }

        if (suggestion.isPinnedHotelSearch) {
            resetSearchOptions()
        }
        advancedOptionsContainerCard.setInverseVisibility(suggestion.isPinnedHotelSearch)
    }

    private fun updateDestinationText(locationText: String) {
        destinationCardView.setText(locationText)
        destinationCardView.contentDescription = Phrase.from(context, R.string.hotel_search_destination_cont_desc_TEMPLATE)
                .put("destination", locationText)
                .format().toString()
    }

    private fun updateWithRecentSearchInfo(searchInfo: SearchInfo) {
        searchViewModel.datesUpdated(searchInfo.startDate, searchInfo.endDate)
        searchViewModel.destinationLocationObserver.onNext(searchInfo.destination)
        selectTravelers(TravelerParams(searchInfo.travelers.numOfAdults, searchInfo.travelers.agesOfChildren, emptyList(), emptyList()))
    }

    private val searchToAdvancedOptions = object : Transition(InputSelectionState::class.java, AdvancedSearchOptionsView::class.java) {
        override fun endTransition(forward: Boolean) {
            advancedOptionsDetails.setVisibility(forward)
            mainContainer.setInverseVisibility(forward)
        }
    }
}
