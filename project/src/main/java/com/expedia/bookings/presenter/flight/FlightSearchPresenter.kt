package com.expedia.bookings.presenter.flight

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.adapter.FlightSearchPageAdapter
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.TravelerParams
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightServiceClassType
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.extensions.ObservableOld
import com.expedia.bookings.extensions.setAccessibilityHoverFocus
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.flights.widget.adapter.FlightsSuggestionAdapter
import com.expedia.bookings.location.CurrentLocationObservable
import com.expedia.bookings.presenter.BaseTwoLocationSearchPresenter
import com.expedia.bookings.services.SuggestionV4Services
import com.expedia.bookings.tracking.flight.FlightSearchTrackingDataBuilder
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.AccessibilityUtil.setFocusForView
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.AnimationUtils.animateView
import com.expedia.bookings.utils.SuggestionV4Utils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isFlightGreedySearchEnabled
import com.expedia.bookings.utils.isRecentSearchesForFlightsEnabled
import com.expedia.bookings.utils.setContentDescriptionToolbarTabs
import com.expedia.bookings.widget.FlightAdvanceSearchWidget
import com.expedia.bookings.widget.FlightCabinClassWidget
import com.expedia.bookings.widget.FlightTravelerWidgetV2
import com.expedia.bookings.widget.TravelerWidgetV2
import com.expedia.bookings.widget.flights.RecentSearchWidgetContainer
import com.expedia.util.notNullAndObservable
import com.expedia.vm.FlightSuggestionAdapterViewModel
import com.expedia.vm.BaseSearchViewModel
import com.expedia.vm.BaseSuggestionAdapterViewModel
import com.expedia.vm.FlightSearchViewModel
import com.expedia.vm.flights.FlightAdvanceSearchViewModel
import com.squareup.phrase.Phrase
import javax.inject.Inject

open class FlightSearchPresenter(context: Context, attrs: AttributeSet) : BaseTwoLocationSearchPresenter(context, attrs) {

    val suggestionServices: SuggestionV4Services by lazy {
        Ui.getApplication(getContext()).flightComponent().suggestionsService()
    }
    val flightCabinClassStub: ViewStub by bindView(R.id.flight_cabin_class_stub)
    val flightCabinClassWidget by lazy {
        val cabinClassWidget = flightCabinClassStub.inflate().findViewById<FlightCabinClassWidget>(R.id.flight_cabin_class_widget)
        cabinClassWidget.lob = LineOfBusiness.FLIGHTS_V2
        cabinClassWidget
    }
    val widgetTravelerAndCabinClassStub: ViewStub by bindView(R.id.widget_traveler_and_cabin_clas_stub)

    val flightAdvanceSearchStub: ViewStub by bindView(R.id.flight_advanced_search_stub)
    val flightAdvanceSearchView: CardView by bindView(R.id.flight_advanced_search_card_view)

    val flightAdvanceSearchWidget by lazy {
        flightAdvanceSearchStub.inflate().findViewById<FlightAdvanceSearchWidget>(R.id.flight_advanced_search_widget)
    }

    lateinit var searchTrackingBuilder: FlightSearchTrackingDataBuilder
        @Inject set

    val errorDrawable = ContextCompat.getDrawable(context,
            Ui.obtainThemeResID(context, R.attr.skin_errorIndicationExclaimationDrawable))

    val isFlightAdvanceSearchTestEnabled = !PointOfSale.getPointOfSale().hideAdvancedSearchOnFlights() &&
            AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFlightAdvanceSearch)
    val swapFlightsLocationsButton: ImageView by bindView(R.id.swapFlightsLocationsButton)
    val flightsSearchDivider: View by bindView(R.id.flight_search_divider)
    val isSwitchToAndFromFieldsFeatureEnabled = AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFlightSwitchFields)

    val travelerFlightCardViewStub: ViewStub by bindView(R.id.traveler_flight_stub)
    override val travelerWidgetV2 by lazy {
        if (AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFlightTravelerFormRevamp))
            travelerFlightCardViewStub.inflate().findViewById<FlightTravelerWidgetV2>(R.id.traveler_card)
        else travelerCardViewStub.inflate().findViewById<TravelerWidgetV2>(R.id.traveler_card)
    }
    val isShowSuggestionLabelTestEnabled: Boolean = AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFlightSearchSuggestionLabel)

    val recentSearchStub: ViewStub by bindView(R.id.flight_recent_search_widget_stub)
    val flightRecentSearchCardView: CardView by bindView(R.id.flight_recent_search_widget_card_view)
    val recentSearchWidgetContainer by lazy {
        recentSearchStub.inflate().findViewById<RecentSearchWidgetContainer>(R.id.flight_recent_searches_widget)
    }
    private var anim: ValueAnimator? = null

    var searchViewModel: FlightSearchViewModel by notNullAndObservable { vm ->
        calendarWidgetV2.viewModel = vm
        travelerWidgetV2.travelersSubject.subscribe(vm.travelersObservable)
        travelerWidgetV2.traveler.getViewModel().isInfantInLapObservable.subscribe(vm.isInfantInLapObserver)
        flightCabinClassWidget.flightCabinClassView.viewmodel.flightCabinClassObservable.subscribe(vm.flightCabinClassObserver)
        if (isFlightAdvanceSearchTestEnabled) {
            flightAdvanceSearchWidget.viewModel.selectAdvancedSearch.subscribe(vm.advanceSearchObserver)
        }
        vm.searchButtonObservable.subscribe { enable ->
            searchButton.setTextColor(if (enable) ContextCompat.getColor(context, R.color.hotel_filter_spinner_dropdown_color) else ContextCompat.getColor(context, R.color.white_disabled))
            if (AccessibilityUtil.isTalkBackEnabled(context)) {
                searchButton.isEnabled = enable
            }
        }
        searchButton.setOnClickListener {
            searchTrackingBuilder.markSearchClicked()
            vm.performSearchObserver.onNext(Unit)
        }
        if (isSwitchToAndFromFieldsFeatureEnabled) {
            swapFlightsLocationsButton.setOnClickListener {
                if (!vm.toAndFromFlightFieldsSwitched)
                    AnimUtils.rotate(swapFlightsLocationsButton)
                else AnimUtils.reverseRotate(swapFlightsLocationsButton)
                vm.toAndFromFlightFieldsSwitched = !(vm.toAndFromFlightFieldsSwitched)
                vm.swapToFromFieldsObservable.onNext(Unit)
            }
        }
        if (isFlightGreedySearchEnabled(context)) {
            travelerWidgetV2.traveler.getViewModel().isDefaultSelectionChangedObservable.filter { it }.map { Unit }.subscribe(vm.abortGreedyCallObservable)
        }
        travelerWidgetV2.traveler.getViewModel().travelerParamsObservable.subscribe { travelers ->
            val noOfTravelers = travelers.getTravelerCount()
            travelerWidgetV2.contentDescription = Phrase.from(context.resources.getQuantityString(R.plurals.search_travelers_cont_desc_TEMPLATE, noOfTravelers))
                    .put("travelers", noOfTravelers).format().toString()
        }
        travelerWidgetV2.traveler.getViewModel().isTravelerSelectionChanged.filter { it }.subscribe {
            vm.trackFieldChange("Traveller.Edit")
        }
        vm.dateSelectionChanged.filter { it }.subscribe {
            if (vm.areRecentSearchDatesInPast) {
                vm.trackFieldChange("Dates.Past.Edit")
                vm.areRecentSearchDatesInPast = false
            } else {
                vm.trackFieldChange("Date.Edit")
            }
        }

        vm.errorNoDestinationObservable.subscribe {
            AnimUtils.doTheHarlemShake(destinationCardView)
            destinationCardView.setEndDrawable(errorDrawable)
        }
        vm.errorNoOriginObservable.subscribe {
            AnimUtils.doTheHarlemShake(originCardView)
            originCardView.setEndDrawable(errorDrawable)
        }
        vm.errorNoDatesObservable.subscribe {
            AnimUtils.doTheHarlemShake(calendarWidgetV2)
            calendarWidgetV2.setEndDrawable(errorDrawable)
        }
        vm.formattedOriginObservable.subscribe { text ->
            originCardView.setText(text)
            originCardView.setEndDrawable(null)
            originCardView.contentDescription = Phrase.from(context, R.string.search_flying_from_destination_cont_desc_TEMPLATE)
                    .put("from_destination", text)
                    .format().toString()
        }
        vm.a11yFocusSelectDatesObservable.subscribe {
            calendarWidgetV2.setAccessibilityHoverFocus()
        }
        vm.formattedDestinationObservable.subscribe {
            text ->
            destinationCardView.setText(if (text.isNotEmpty()) text else context.resources.getString(R.string.fly_to_hint))
            if (text.isNotEmpty()) {
                destinationCardView.setEndDrawable(null)
            }
            destinationCardView.contentDescription =
                    if (text.isNotEmpty())
                        Phrase.from(context, R.string.search_flying_to_destination_cont_desc_TEMPLATE)
                                .put("to_destination", text)
                                .format().toString()
                    else context.resources.getString(R.string.fly_to_hint)
            if (this.visibility == VISIBLE && vm.startDate() == null && text.isNotEmpty() && !vm.hasPreviousSearchParams) {
                calendarWidgetV2.showCalendarDialog()
            }
        }

        vm.flightsSourceObservable.subscribe {
            destinationSuggestionViewModel.suggestionToFilterFromHistory = it
        }

        vm.flightsDestinationObservable.subscribe {
            originSuggestionViewModel.suggestionToFilterFromHistory = it
        }
        vm.errorOriginSameAsDestinationObservable.subscribe { message ->
            showErrorDialog(message)
        }

        vm.dateAccessibilityObservable.subscribe { text ->
            calendarWidgetV2.contentDescription = text
        }

        vm.previousSearchParamsObservable.subscribe { params ->
            calendarWidgetV2.setBackgroundResource(0)
            anim = animateView(scrollView, scrollView.scrollY, 0, 300, 200)
            anim?.cancel()
            val animateRight = ValueAnimator.ofFloat(0f, 25f)
            val animatorLeft = ValueAnimator.ofFloat(25f, 0f)
            animateRight.duration = 200
            animateRight.cancel()
            animateRight.addUpdateListener {
                val animatedValue = it.animatedValue as Float
                animateSearchFormFields(animatedValue)
            }
            animateRight.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    vm.hasPreviousSearchParams = true
                    vm.shouldTrackEditSearchForm = false
                    params.flightCabinClass?.let {
                        flightCabinClassWidget.flightCabinClassView.viewmodel.flightCabinClassObservable.onNext(FlightServiceClassType.CabinCode.valueOf(it))
                    }
                    if (!params.isRoundTrip()) {
                        viewpager.currentItem = 1
                    } else {
                        viewpager.currentItem = 0
                    }
                    travelerWidgetV2.traveler.getViewModel().travelerParamsObservable.onNext(TravelerParams(params.adults, params.children, emptyList(), emptyList()))
                    val infantCount = params.children.count { infantAge -> infantAge < 2 }
                    if (infantCount > 0) {
                        travelerWidgetV2.traveler.getViewModel().infantInSeatObservable.onNext(!params.infantSeatingInLap)
                        travelerWidgetV2.traveler.getViewModel().isInfantInLapObservable.onNext(params.infantSeatingInLap)
                    }
                    vm.setupViewModelFromPastSearch(params)
                    vm.shouldTrackEditSearchForm = true
                    if (vm.getParamsBuilder().hasValidDates()) {
                        setFocusForView(searchButton)
                    } else {
                        setFocusForView(calendarWidgetV2)
                    }
                    vm.hasPreviousSearchParams = false
                    animatorLeft.start()
                }
            })
            animatorLeft.duration = 200
            animatorLeft.cancel()
            animatorLeft.addUpdateListener {
                val animatedValue = it.animatedValue as Float
                animateSearchFormFields(animatedValue)
            }
            animatorLeft.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    vm.wiggleAnimationEnd.onNext(Unit)
                }
            })

            anim?.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    animateRight.start()
                }
            })
            anim?.start()
        }

        if (isSwitchToAndFromFieldsFeatureEnabled) {
            ObservableOld.combineLatest(
                    vm.formattedOriginObservable,
                    vm.formattedDestinationObservable,
                    { origin, destination ->
                        if (origin.isNullOrBlank() || destination.isNullOrBlank()) {
                            swapFlightsLocationsButton.isEnabled = false
                            swapFlightsLocationsButton.setColorFilter(ContextCompat.getColor(getContext(), R.color.gray200))
                        } else {
                            swapFlightsLocationsButton.isEnabled = true
                            swapFlightsLocationsButton.setColorFilter(ContextCompat.getColor(getContext(), R.color.gray700))
                        }
                    }).subscribe()
        }

        ObservableOld.combineLatest(vm.hasValidDatesObservable, vm.errorNoDatesObservable, { hasValidDates, _ -> hasValidDates }).subscribe { hasValidDates ->
            calendarWidgetV2.setEndDrawable(if (hasValidDates) null else errorDrawable)
        }

        originSuggestionViewModel = FlightSuggestionAdapterViewModel(getContext(), suggestionServices, false, CurrentLocationObservable.create(getContext()))
        destinationSuggestionViewModel = FlightSuggestionAdapterViewModel(getContext(), suggestionServices, true, null)

        originSuggestionAdapter = FlightsSuggestionAdapter(originSuggestionViewModel)
        destinationSuggestionAdapter = FlightsSuggestionAdapter(destinationSuggestionViewModel)

        setContentDescriptionToolbarTabs(context, tabs)

        if (isRecentSearchesForFlightsEnabled(context)) {
            ObservableOld.combineLatest(vm.wiggleAnimationEnd, vm.highlightCalendarObservable, { _, border ->
                calendarWidgetV2.setBackgroundResource(border)
            }).subscribe()
        }
    }

    private lateinit var originSuggestionAdapter: FlightsSuggestionAdapter
    private lateinit var destinationSuggestionAdapter: FlightsSuggestionAdapter
    override val delayBeforeShowingDestinationSuggestions = 5L
    override val waitForOtherSuggestionListeners = 5L

    init {
        Ui.getApplication(getContext()).flightComponent().inject(this)

        suggestionServices.essDomainResolution()
                .subscribe({}, {})

        if (isFlightAdvanceSearchTestEnabled) {
            widgetTravelerAndCabinClassStub.layoutResource = R.layout.widget_traveler_cabin_class_horizontal
            flightAdvanceSearchView.visibility = View.VISIBLE
            flightAdvanceSearchWidget.viewModel = FlightAdvanceSearchViewModel()
        } else {
            widgetTravelerAndCabinClassStub.layoutResource = R.layout.widget_traveler_cabin_class_vertical
        }
        widgetTravelerAndCabinClassStub.inflate()
        if (isFlightAdvanceSearchTestEnabled) {
            flightCabinClassWidget.compoundDrawablePadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20f, resources.displayMetrics).toInt()
        }
        travelerWidgetV2.traveler.getViewModel().showSeatingPreference = true
        travelerWidgetV2.traveler.getViewModel().lob = LineOfBusiness.FLIGHTS_V2 //Not sure why we still have Flights V2 all over the place??
        showTabOptionsOnSearchForm = true

        if (isSwitchToAndFromFieldsFeatureEnabled) {
            swapFlightsLocationsButton.isEnabled = false
            swapFlightsLocationsButton.setColorFilter(ContextCompat.getColor(getContext(), R.color.gray200))
            swapFlightsLocationsButton.visibility = View.VISIBLE

            val dividerParams = flightsSearchDivider.layoutParams as ViewGroup.MarginLayoutParams
            val paddingRight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 55f, resources.displayMetrics).toInt()
            val paddingLeft = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56f, resources.displayMetrics).toInt()

            dividerParams.setMargins(paddingLeft, 0, paddingRight, 0)
            flightsSearchDivider.layoutParams = dividerParams
        }

        if (isRecentSearchesForFlightsEnabled(context)) {
            flightRecentSearchCardView.visibility = View.VISIBLE
            recentSearchWidgetContainer.viewModel.fetchRecentSearchesObservable.onNext(Unit)
            recentSearchWidgetContainer.viewModel.selectedRecentSearch.subscribe { searchParams ->
                searchViewModel.previousSearchParamsObservable.onNext(searchParams)
            }
        }
    }

    override fun inflate() {
        View.inflate(context, R.layout.widget_base_flight_search, this)
        toolBarTitle.text = context.resources.getText(R.string.flights_title)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        initializeToolbarTabs()
    }

    override fun getSuggestionHistoryFileName(): String {
        return SuggestionV4Utils.RECENT_AIRPORT_SUGGESTIONS_FILE
    }

    override fun shouldSaveSuggestionHierarchyChildInfo(): Boolean {
        return true
    }

    override fun getSuggestionViewModel(): BaseSuggestionAdapterViewModel {
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

    override fun getLineOfBusiness(): LineOfBusiness {
        return LineOfBusiness.FLIGHTS
    }

    private fun initializeToolbarTabs() {
        tabs.visibility = View.VISIBLE
        val pagerAdapter = FlightSearchPageAdapter(context)
        viewpager.adapter = pagerAdapter
        viewpager.overScrollMode = ViewPager.OVER_SCROLL_NEVER

        tabs.setupWithViewPager(viewpager)

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
                // do nothing
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // do nothing
            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                val isRoundTripSearch = tab.position == 0
                roundTripChanged(isRoundTripSearch)
            }
        })
    }

    private fun roundTripChanged(roundTrip: Boolean) {
        if (isFlightGreedySearchEnabled(context) && searchViewModel.isGreedyCallStarted) {
            searchViewModel.abortGreedyCallObservable.onNext(Unit)
        }
        searchViewModel.isRoundTripSearchObservable.onNext(roundTrip)
        if (roundTrip) {
            announceForAccessibility(context.getString(R.string.flights_tab_selection_accouncement_roundtrip))
        } else {
            announceForAccessibility(context.getString(R.string.flights_tab_selection_accouncement_oneway))
        }
        searchViewModel.trackFieldChange("SearchType.Edit")
    }

    private fun animateSearchFormFields(animatedValue: Float) {
        originCardView.translationX = animatedValue
        destinationCardView.translationX = animatedValue
        calendarWidgetV2.translationX = animatedValue
        travelerWidgetV2.translationX = animatedValue
        flightCabinClassWidget.translationX = animatedValue
    }
}
