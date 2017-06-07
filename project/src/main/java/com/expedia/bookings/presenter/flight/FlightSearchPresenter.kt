package com.expedia.bookings.presenter.flight

import android.content.Context
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import com.expedia.bookings.R
import com.expedia.bookings.adapter.FlightSearchPageAdapter
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.TravelerParams
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightServiceClassType
import com.expedia.bookings.location.CurrentLocationObservable
import com.expedia.bookings.presenter.BaseTwoLocationSearchPresenter
import com.expedia.bookings.services.SuggestionV4Services
import com.expedia.bookings.tracking.flight.FlightSearchTrackingDataBuilder
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.SuggestionV4Utils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.setAccessibilityHoverFocus
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FlightCabinClassWidget
import com.expedia.bookings.widget.TravelerPickerView
import com.expedia.bookings.widget.suggestions.SuggestionAdapter
import com.expedia.util.notNullAndObservable
import com.expedia.vm.AirportSuggestionViewModel
import com.expedia.vm.BaseSearchViewModel
import com.expedia.vm.FlightSearchViewModel
import com.expedia.vm.SuggestionAdapterViewModel
import com.squareup.phrase.Phrase
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import javax.inject.Inject

open class FlightSearchPresenter(context: Context, attrs: AttributeSet) : BaseTwoLocationSearchPresenter(context, attrs) {

    val suggestionServices: SuggestionV4Services by lazy {
        Ui.getApplication(getContext()).flightComponent().suggestionsService()
    }

    val flightCabinClassStub: ViewStub by bindView(R.id.flight_cabin_class_stub)
    val flightCabinClassWidget by lazy {
        flightCabinClassStub.inflate().findViewById(R.id.flight_cabin_class_widget) as FlightCabinClassWidget
    }
    lateinit var searchTrackingBuilder: FlightSearchTrackingDataBuilder
        @Inject set

    val errorDrawable = ContextCompat.getDrawable(context,
            Ui.obtainThemeResID(context, R.attr.skin_errorIndicationExclaimationDrawable))

    var searchViewModel: FlightSearchViewModel by notNullAndObservable { vm ->
        calendarWidgetV2.viewModel = vm
        travelerWidgetV2.travelersSubject.subscribe(vm.travelersObservable)
        travelerWidgetV2.traveler.getViewModel().isInfantInLapObservable.subscribe(vm.isInfantInLapObserver)
        flightCabinClassWidget.flightCabinClassView.viewmodel.flightCabinClassSelectedObservable.subscribe(vm.abortTimerObservable)
        flightCabinClassWidget.flightCabinClassView.viewmodel.flightCabinClassObservable.subscribe(vm.flightCabinClassObserver)
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
        travelerWidgetV2.traveler.getViewModel().travelerSelectedObservable.subscribe(vm.abortTimerObservable)
        travelerWidgetV2.traveler.getViewModel().travelerParamsObservable.subscribe { travelers ->
            val noOfTravelers = travelers.getTravelerCount()
            travelerWidgetV2.contentDescription = Phrase.from(context.resources.getQuantityString(R.plurals.search_travelers_cont_desc_TEMPLATE, noOfTravelers)).
                    put("travelers", noOfTravelers).format().toString()
        }

        val isUserBucketedInSearchFormValidation = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightSearchFormValidation)
        vm.errorNoDestinationObservable.subscribe {
            AnimUtils.doTheHarlemShake(destinationCardView)
            if (isUserBucketedInSearchFormValidation) {
                destinationCardView.setEndDrawable(errorDrawable)
            }
        }
        vm.errorNoOriginObservable.subscribe {
            AnimUtils.doTheHarlemShake(originCardView)
            if (isUserBucketedInSearchFormValidation) {
                originCardView.setEndDrawable(errorDrawable)
            }
        }
        vm.errorNoDatesObservable.subscribe {
            AnimUtils.doTheHarlemShake(calendarWidgetV2)
            if (isUserBucketedInSearchFormValidation) {
                calendarWidgetV2.setEndDrawable(errorDrawable)
            }
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
                    else
                        context.resources.getString(R.string.fly_to_hint)
            if (this.visibility == VISIBLE && vm.startDate() == null && text.isNotEmpty() && !vm.hasPreviousSearchParams) {
                calendarWidgetV2.showCalendarDialog()
            }
        }

        vm.errorOriginSameAsDestinationObservable.subscribe { message ->
            showErrorDialog(message)
        }

        vm.dateAccessibilityObservable.subscribe { text ->
            calendarWidgetV2.contentDescription = text
        }

        vm.previousSearchParamsObservable.subscribe { params ->
            val cabinClass = params.flightCabinClass
            if (cabinClass != null) {
                flightCabinClassWidget.flightCabinClassView.viewmodel.flightCabinClassObservable.onNext(FlightServiceClassType.CabinCode.valueOf(cabinClass))
            }
            if (!params.isRoundTrip()) {
                viewpager.currentItem = 1
            }
            travelerWidgetV2.traveler.getViewModel().travelerParamsObservable.onNext(TravelerParams(params.adults, params.children, emptyList(), emptyList()))
            if (params.children.contains(0) && !params.infantSeatingInLap) {
                travelerWidgetV2.traveler.getViewModel().infantInSeatObservable.onNext(Unit)
            }
        }

        if (isUserBucketedInSearchFormValidation) {
            Observable.combineLatest(vm.hasValidDatesObservable, vm.errorNoDatesObservable, { hasValidDates, invalidDates -> hasValidDates }).subscribe { hasValidDates ->
                calendarWidgetV2.setEndDrawable(if (hasValidDates) null else errorDrawable)
            }
        }
        
        originSuggestionViewModel = AirportSuggestionViewModel(getContext(), suggestionServices, false, CurrentLocationObservable.create(getContext()))
        destinationSuggestionViewModel = AirportSuggestionViewModel(getContext(), suggestionServices, true, null)
        originSuggestionAdapter = SuggestionAdapter(originSuggestionViewModel)
        destinationSuggestionAdapter = SuggestionAdapter(destinationSuggestionViewModel)
    }

    lateinit private var originSuggestionAdapter: SuggestionAdapter
    lateinit private var destinationSuggestionAdapter: SuggestionAdapter
    override val delayBeforeShowingDestinationSuggestions = 5L
    override val waitForOtherSuggestionListeners = 5L

    init {
        Ui.getApplication(getContext()).flightComponent().inject(this)
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
                announceForAccessibility(if (isRoundTripSearch)
                    context.getString(R.string.flights_tab_selection_accouncement_roundtrip)
                else
                    context.getString(R.string.flights_tab_selection_accouncement_oneway))
            }
        })
    }

    override fun getSuggestionHistoryFileName(): String {
        return SuggestionV4Utils.RECENT_AIRPORT_SUGGESTIONS_FILE
    }

    override fun shouldSaveSuggestionHierarchyChildInfo(): Boolean {
        return true
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
