package com.expedia.bookings.packages.presenter

import android.content.Context
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.TravelerParams
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightServiceClassType
import com.expedia.bookings.extensions.setAccessibilityHoverFocus
import com.expedia.bookings.extensions.subscribeOnClick
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.location.CurrentLocationObservable
import com.expedia.bookings.presenter.BaseTwoLocationSearchPresenter
import com.expedia.bookings.services.SuggestionV4Services
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.SuggestionV4Utils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isFHCPackageWebViewEnabled
import com.expedia.bookings.widget.FlightCabinClassWidget
import com.expedia.bookings.widget.TravelerWidgetV2
import com.expedia.bookings.packages.adapter.PackageSuggestionAdapter
import com.expedia.bookings.widget.suggestions.BaseSuggestionAdapter
import com.expedia.util.PackageUtil
import com.expedia.util.notNullAndObservable
import com.expedia.vm.BaseSearchViewModel
import com.expedia.vm.BaseSuggestionAdapterViewModel
import com.expedia.bookings.packages.vm.PackageSearchViewModel
import com.expedia.bookings.packages.vm.PackageSuggestionAdapterViewModel
import com.expedia.bookings.utils.isPackagesSearchFormRenameToFromEnabled
import com.squareup.phrase.Phrase
import kotlin.properties.Delegates

open class PackageSearchPresenter(context: Context, attrs: AttributeSet) : BaseTwoLocationSearchPresenter(context, attrs) {
    val suggestionServices: SuggestionV4Services by lazy {
        Ui.getApplication(getContext()).packageComponent().suggestionsService()
    }

    val travelerFlightCardViewStub: ViewStub by bindView(R.id.traveler_flight_stub)
    override val travelerWidgetV2: TravelerWidgetV2 by lazy {
        travelerFlightCardViewStub.inflate().findViewById<TravelerWidgetV2>(R.id.traveler_card)
    }

    private var originSuggestionAdapter: BaseSuggestionAdapter by Delegates.notNull()
    private var destinationSuggestionAdapter: BaseSuggestionAdapter by Delegates.notNull()

    val errorDrawable = ContextCompat.getDrawable(context,
            Ui.obtainThemeResID(context, R.attr.skin_errorIndicationExclaimationDrawable))
    val flightCabinClassStub: ViewStub by bindView(R.id.flight_cabin_class_stub)
    val flightCabinClassWidget by lazy {
        val cabinClassWidget = flightCabinClassStub.inflate().findViewById<FlightCabinClassWidget>(R.id.flight_cabin_class_widget)
        cabinClassWidget.lob = LineOfBusiness.PACKAGES
        cabinClassWidget
    }

    val widgetTravelerAndCabinClassStub: ViewStub by bindView(R.id.widget_traveler_and_cabin_clas_stub)

    var searchViewModel: PackageSearchViewModel by notNullAndObservable { vm ->
        calendarWidgetV2.viewModel = vm
        travelerWidgetV2.travelersSubject.subscribe(vm.travelersObservable)
        travelerWidgetV2.traveler.getViewModel().isInfantInLapObservable.subscribe(vm.isInfantInLapObserver)
        if (AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppPackagesFFPremiumClass)) {
            flightCabinClassWidget.flightCabinClassView.viewmodel.flightCabinClassObservable.subscribe(vm.flightCabinClassObserver)
        }
        vm.formattedOriginObservable.subscribe {
            text ->
            originCardView.setText(text)
            originCardView.setEndDrawable(null)
            originCardView.contentDescription = Phrase.from(context,
                    if (isPackagesSearchFormRenameToFromEnabled(context)) {
                        R.string.search_enter_origin_cont_desc_TEMPLATE
                    } else {
                        R.string.search_flying_from_destination_cont_desc_TEMPLATE
                    }).put("from_destination", text)
                    .format().toString()
        }
        vm.formattedDestinationObservable.subscribe {
            text ->
            if (text.isNotEmpty()) {
                destinationCardView.setText(text)
                destinationCardView.setEndDrawable(null)
                destinationCardView.contentDescription = Phrase.from(context,
                        if (isPackagesSearchFormRenameToFromEnabled(context)) {
                            R.string.search_enter_destination_cont_desc_TEMPLATE
                        } else {
                            R.string.search_flying_to_destination_cont_desc_TEMPLATE
                        }).put("to_destination", text)
                        .format().toString()

                if (this.visibility == VISIBLE && vm.startDate() == null
                        && !vm.isTalkbackActive() && !vm.isSearchDateExpired) {
                    calendarWidgetV2.showCalendarDialog()
                }
            } else {
                destinationCardView.setText(getDestinationSearchBoxPlaceholderText())
                destinationCardView.contentDescription = context.getString(
                        if (isPackagesSearchFormRenameToFromEnabled(context)) {
                            R.string.packages_search_enter_destination_cont_desc
                        } else {
                            R.string.packages_search_flying_to_cont_desc
                        })
            }
        }
        vm.dateAccessibilityObservable.subscribe {
            text ->
            calendarWidgetV2.contentDescription = text
        }
        travelerWidgetV2.traveler.getViewModel().travelerParamsObservable.subscribe { travelers ->
            val noOfTravelers = travelers.getTravelerCount()
            travelerWidgetV2.contentDescription = Phrase.from(context.resources.getQuantityString(R.plurals.search_travelers_cont_desc_TEMPLATE, noOfTravelers))
                    .put("travelers", noOfTravelers).format().toString()
        }

        vm.previousSearchParamsObservable.subscribe { params ->
            travelerWidgetV2.traveler.getViewModel().travelerParamsObservable.onNext(TravelerParams(params.adults, params.children, emptyList(), emptyList()))
            val infantCount = params.children.count { age -> age < 2 }
            if (infantCount > 0) {
                travelerWidgetV2.traveler.getViewModel().infantInSeatObservable.onNext(!params.infantSeatingInLap)
                travelerWidgetV2.traveler.getViewModel().isInfantInLapObservable.onNext(params.infantSeatingInLap)
            }
            val cabinClass = params.flightCabinClass
            if (cabinClass != null && AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppPackagesFFPremiumClass)) {
                flightCabinClassWidget.flightCabinClassView.viewmodel.flightCabinClassObservable.onNext(FlightServiceClassType.getCabinCodeFromMIDParam(cabinClass))
            }
        }

        vm.searchButtonObservable.subscribe { enable ->
            searchButton.setTextColor(if (enable) ContextCompat.getColor(context, R.color.search_dialog_background_v2) else ContextCompat.getColor(context, R.color.white_disabled))
            if (vm.isTalkbackActive()) {
                searchButton.isEnabled = enable
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
        vm.errorMaxDurationObservable.subscribe { message ->
            showErrorDialog(message)
        }
        vm.errorMaxRangeObservable.subscribe { message ->
            showErrorDialog(message)
        }
        vm.errorOriginSameAsDestinationObservable.subscribe { message ->
            showErrorDialog(message)
        }
        searchButton.subscribeOnClick(vm.searchObserver)

        vm.a11yFocusSelectDatesObservable.subscribe {
            calendarWidgetV2.setAccessibilityHoverFocus()
        }
        vm.hasValidDatesObservable.subscribe { hasValidDates ->
            if (hasValidDates) {
                calendarWidgetV2.setEndDrawable(null)
            }
        }

        originSuggestionViewModel = PackageSuggestionAdapterViewModel(getContext(), suggestionServices, false, CurrentLocationObservable.create(getContext()))
        destinationSuggestionViewModel = PackageSuggestionAdapterViewModel(getContext(), suggestionServices, true, null)
        originSuggestionAdapter = PackageSuggestionAdapter(originSuggestionViewModel, isOrigin = true)
        destinationSuggestionAdapter = PackageSuggestionAdapter(destinationSuggestionViewModel, isOrigin = false)
        travelerWidgetV2.traveler.getViewModel().showSeatingPreference = true
        travelerWidgetV2.traveler.getViewModel().lob = getLineOfBusiness()
    }

    init {
        widgetTravelerAndCabinClassStub.layoutResource = R.layout.widget_traveler_cabin_class_vertical
        widgetTravelerAndCabinClassStub.inflate()
        if (isFHCPackageWebViewEnabled(context)) {
            showTabOptionsOnSearchForm = true
        }
    }

    override fun inflate() {
        val view = View.inflate(context, R.layout.widget_base_flight_search, this)
        val packageTitleText = view.findViewById<TextView>(R.id.title)
        packageTitleText.text = resources.getString(PackageUtil.packageTitle(context))
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (isFHCPackageWebViewEnabled(context)) {
            initializeToolbarTabs()
        }
        originCardView.text = getOriginSearchBoxPlaceholderText()
        destinationCardView.text = getDestinationSearchBoxPlaceholderText()

        if (isPackagesSearchFormRenameToFromEnabled(context)) {
            originCardView.contentDescription = context.resources.getString(R.string.packages_search_enter_origin_cont_desc)
            destinationCardView.contentDescription = context.resources.getString(R.string.packages_search_enter_destination_cont_desc)
        }
    }

    override fun getSuggestionHistoryFileName(): String {
        if (isCustomerSelectingOrigin) {
            return SuggestionV4Utils.RECENT_PACKAGE_DEPARTURE_SUGGESTIONS_FILE
        } else {
            return SuggestionV4Utils.RECENT_PACKAGE_ARRIVAL_SUGGESTIONS_FILE
        }
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
        return context.resources.getString(
                if (isPackagesSearchFormRenameToFromEnabled(context))
                    R.string.packages_enter_origin_hint
                else R.string.fly_from_hint)
    }

    override fun getDestinationSearchBoxPlaceholderText(): String {
        return context.resources.getString(
                if (isPackagesSearchFormRenameToFromEnabled(context))
                    R.string.packages_enter_destination_hint
                else R.string.fly_to_hint)
    }

    override fun getLineOfBusiness(): LineOfBusiness {
        return LineOfBusiness.PACKAGES
    }

    private fun initializeToolbarTabs() {
        tabs.visibility = View.VISIBLE
        tabs.addTab(tabs.newTab().setText(R.string.nav_hotel_plus_flight))
        tabs.addTab(tabs.newTab().setText(R.string.nav_hotel_plus_flight_plus_car))

        var hasFHCTabBeenClickedOnce = false

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
                // do nothing
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // do nothing
            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                val isFHPackageSearch = tab.position == 0
                searchViewModel.isFHPackageSearch = isFHPackageSearch
                if (!isFHPackageSearch && !hasFHCTabBeenClickedOnce) {
                    hasFHCTabBeenClickedOnce = true
                    PackagesTracking().trackFHCTabClick()
                }
            }
        })
    }
}
