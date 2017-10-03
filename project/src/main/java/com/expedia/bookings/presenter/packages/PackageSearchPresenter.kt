package com.expedia.bookings.presenter.packages

import android.content.Context
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
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.location.CurrentLocationObservable
import com.expedia.bookings.presenter.BaseTwoLocationSearchPresenter
import com.expedia.bookings.services.SuggestionV4Services
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.SuggestionV4Utils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.setAccessibilityHoverFocus
import com.expedia.bookings.widget.TravelerWidgetV2
import com.expedia.bookings.widget.suggestions.SuggestionAdapter
import com.expedia.util.PackageUtil
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeOnClick
import com.expedia.vm.BaseSearchViewModel
import com.expedia.vm.SuggestionAdapterViewModel
import com.expedia.vm.packages.PackageSearchViewModel
import com.expedia.vm.packages.PackageSuggestionAdapterViewModel
import com.squareup.phrase.Phrase
import kotlin.properties.Delegates

open class PackageSearchPresenter(context: Context, attrs: AttributeSet) : BaseTwoLocationSearchPresenter(context, attrs) {
    val suggestionServices: SuggestionV4Services by lazy {
        Ui.getApplication(getContext()).packageComponent().suggestionsService()
    }

    val travelerFlightCardViewStub: ViewStub by bindView(R.id.traveler_flight_stub)
    override val travelerWidgetV2: TravelerWidgetV2 by lazy {
        if (AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightTravelerFormRevamp)) {
            travelerFlightCardViewStub.inflate().findViewById<TravelerWidgetV2>(R.id.traveler_card)
        } else {
            travelerCardViewStub.inflate().findViewById<TravelerWidgetV2>(R.id.traveler_card)
        }
    }

    private var originSuggestionAdapter: SuggestionAdapter by Delegates.notNull()
    private var destinationSuggestionAdapter: SuggestionAdapter by Delegates.notNull()
    val widgetTravelerAndCabinClassStub: ViewStub by bindView(R.id.widget_traveler_and_cabin_clas_stub)

    var searchViewModel: PackageSearchViewModel by notNullAndObservable { vm ->
        calendarWidgetV2.viewModel = vm
        travelerWidgetV2.travelersSubject.subscribe(vm.travelersObservable)
        travelerWidgetV2.traveler.getViewModel().isInfantInLapObservable.subscribe(vm.isInfantInLapObserver)
        vm.formattedOriginObservable.subscribe {
            text ->
            originCardView.setText(text)
            originCardView.contentDescription = Phrase.from(context, R.string.search_flying_from_destination_cont_desc_TEMPLATE)
                    .put("from_destination", text)
                    .format().toString()
        }
        vm.formattedDestinationObservable.subscribe {
            text ->
            if (text.isNotEmpty()) {
                destinationCardView.setText(text)
                destinationCardView.contentDescription =
                            Phrase.from(context, R.string.search_flying_to_destination_cont_desc_TEMPLATE)
                                    .put("to_destination", text)
                                    .format().toString()
                if (this.visibility == VISIBLE && vm.startDate() == null && !AccessibilityUtil.isTalkBackEnabled(context)) {
                    calendarWidgetV2.showCalendarDialog()
                }
            }
            else {
                destinationCardView.setText(getDestinationSearchBoxPlaceholderText())
                destinationCardView.contentDescription = getDestinationSearchBoxPlaceholderText()
            }
        }
        vm.dateAccessibilityObservable.subscribe {
            text ->
            calendarWidgetV2.contentDescription = text
        }
        travelerWidgetV2.traveler.getViewModel().travelerParamsObservable.subscribe { travelers ->
            val noOfTravelers = travelers.getTravelerCount()
            travelerWidgetV2.contentDescription = Phrase.from(context.resources.getQuantityString(R.plurals.search_travelers_cont_desc_TEMPLATE, noOfTravelers)).
                    put("travelers", noOfTravelers).format().toString()
        }

        vm.previousSearchParamsObservable.subscribe { params ->
            travelerWidgetV2.traveler.getViewModel().travelerParamsObservable.onNext(TravelerParams(params.adults, params.children, emptyList(), emptyList()))
            val infantCount = params.children.count { age -> age < 2 }
            if (infantCount > 0) {
                travelerWidgetV2.traveler.getViewModel().infantInSeatObservable.onNext(!params.infantSeatingInLap)
            }
        }

        vm.searchButtonObservable.subscribe { enable ->
            searchButton.setTextColor(if (enable) ContextCompat.getColor(context, R.color.search_dialog_background_v2) else ContextCompat.getColor(context, R.color.white_disabled))
            if (AccessibilityUtil.isTalkBackEnabled(context)) {
                searchButton.isEnabled = enable
            }
        }
        vm.errorNoDestinationObservable.subscribe { AnimUtils.doTheHarlemShake(originCardView) }
        vm.errorNoDatesObservable.subscribe { AnimUtils.doTheHarlemShake(calendarWidgetV2) }
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

        originSuggestionViewModel = PackageSuggestionAdapterViewModel(getContext(), suggestionServices, false, CurrentLocationObservable.create(getContext()))
        destinationSuggestionViewModel = PackageSuggestionAdapterViewModel(getContext(), suggestionServices, true, null)
        originSuggestionViewModel.setCustomerSelectingOrigin(true)
        destinationSuggestionViewModel.setCustomerSelectingOrigin(false)
        originSuggestionAdapter = SuggestionAdapter(originSuggestionViewModel)
        destinationSuggestionAdapter = SuggestionAdapter(destinationSuggestionViewModel)
        travelerWidgetV2.traveler.getViewModel().showSeatingPreference = true
        travelerWidgetV2.traveler.getViewModel().lob = getLineOfBusiness()
    }

    init {
        widgetTravelerAndCabinClassStub.layoutResource = R.layout.widget_traveler_cabin_class_vertical
        widgetTravelerAndCabinClassStub.inflate()
    }

    override fun inflate() {
        val view = View.inflate(context, R.layout.widget_base_flight_search, this)
        val packageTitleText = view.findViewById<TextView>(R.id.title)
        packageTitleText.text = resources.getString(PackageUtil.packageTitle)
    }

    override fun getSuggestionHistoryFileName(): String {
        return SuggestionV4Utils.RECENT_PACKAGE_SUGGESTIONS_FILE
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

    override fun getLineOfBusiness(): LineOfBusiness {
        return LineOfBusiness.PACKAGES
    }
}
