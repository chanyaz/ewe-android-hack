package com.expedia.bookings.presenter.packages

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.location.CurrentLocationObservable
import com.expedia.bookings.presenter.BaseSearchPresenterV2
import com.expedia.bookings.services.SuggestionV4Services
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.SuggestionV4Utils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.PackageSuggestionAdapter
import com.expedia.bookings.widget.SearchInputCardView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeOnClick
import com.expedia.vm.DatedSearchViewModel
import com.expedia.vm.packages.PackageSearchViewModel
import com.expedia.vm.packages.PackageSuggestionAdapterViewModel
import com.expedia.vm.SuggestionAdapterViewModel
import kotlin.properties.Delegates

class PackageSearchPresenter(context: Context, attrs: AttributeSet) : BaseSearchPresenterV2(context, attrs) {
    val suggestionServices: SuggestionV4Services by lazy {
        Ui.getApplication(getContext()).packageComponent().suggestionsService()
    }
    private var departureAdapter: PackageSuggestionAdapter by Delegates.notNull()
    private var arrivalAdapter: PackageSuggestionAdapter by Delegates.notNull()

    var searchViewModel: PackageSearchViewModel by notNullAndObservable { vm ->
        calendarWidgetV2.viewModel = vm
        travelerWidgetV2.travelersSubject.subscribe(vm.travelersObserver)
        travelerWidgetV2.traveler.viewmodel.isInfantInLapObservable.subscribe(vm.isInfantInLapObserver)
        vm.departureTextObservable.subscribe { text -> destinationCardView.setText(text) }
        vm.arrivalTextObservable.subscribe { text -> arrivalCardView!!.setText(text) }

        vm.searchButtonObservable.subscribe { enable ->
            searchButton.setTextColor(if (enable) ContextCompat.getColor(context, R.color.hotel_filter_spinner_dropdown_color) else ContextCompat.getColor(context, R.color.white_disabled))
        }
        vm.errorNoOriginObservable.subscribe { AnimUtils.doTheHarlemShake(destinationCardView) }
        vm.errorNoDatesObservable.subscribe { AnimUtils.doTheHarlemShake(calendarWidgetV2) }
        vm.errorMaxDatesObservable.subscribe { message ->
            showErrorDialog(message)
        }
        vm.errorDepartureSameAsOrigin.subscribe { message ->
            showErrorDialog(message)
        }
        searchButton.subscribeOnClick(vm.searchObserver)
    }

    init {
        departureSuggestionVM = PackageSuggestionAdapterViewModel(getContext(), suggestionServices, false, CurrentLocationObservable.create(getContext()))
        arrivalSuggestionVM = PackageSuggestionAdapterViewModel(getContext(), suggestionServices, true, null)
        departureAdapter = PackageSuggestionAdapter(departureSuggestionVM)
        arrivalAdapter = PackageSuggestionAdapter(arrivalSuggestionVM)
        travelerWidgetV2.traveler.viewmodel.showSeatingPreference = true
        travelerWidgetV2.traveler.viewmodel.lob = LineOfBusiness.PACKAGES
    }

    override fun inflate() {
        View.inflate(context, R.layout.widget_package_search, this)
    }
    
    override fun getSuggestionHistoryFileName(): String {
        return SuggestionV4Utils.RECENT_PACKAGE_SUGGESTIONS_FILE
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
