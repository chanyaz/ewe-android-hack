package com.expedia.bookings.presenter.packages

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.location.CurrentLocationObservable
import com.expedia.bookings.presenter.BaseTwoLocationSearchPresenter
import com.expedia.bookings.services.SuggestionV4Services
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.SuggestionV4Utils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.suggestions.PackageSuggestionAdapter
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeOnClick
import com.expedia.vm.BaseSearchViewModel
import com.expedia.vm.SuggestionAdapterViewModel
import com.expedia.vm.packages.PackageSearchViewModel
import com.expedia.vm.packages.PackageSuggestionAdapterViewModel
import kotlin.properties.Delegates

class PackageSearchPresenter(context: Context, attrs: AttributeSet) : BaseTwoLocationSearchPresenter(context, attrs) {

    val suggestionServices: SuggestionV4Services by lazy {
        Ui.getApplication(getContext()).packageComponent().suggestionsService()
    }

    private var originSuggestionAdapter: PackageSuggestionAdapter by Delegates.notNull()
    private var destinationSuggestionAdapter: PackageSuggestionAdapter by Delegates.notNull()

    var searchViewModel: PackageSearchViewModel by notNullAndObservable { vm ->
        calendarWidgetV2.viewModel = vm
        travelerWidgetV2.travelersSubject.subscribe(vm.travelersObserver)
        travelerWidgetV2.traveler.viewmodel.isInfantInLapObservable.subscribe(vm.isInfantInLapObserver)
        vm.formattedOriginObservable.subscribe { text -> originCardView.setText(text) }
        vm.formattedDestinationObservable.subscribe {
            text -> destinationCardView.setText(text)
            if (this.visibility == VISIBLE && vm.startDate() == null) {
                calendarWidgetV2.showCalendarDialog()
            }
        }

        vm.searchButtonObservable.subscribe { enable ->
            searchButton.setTextColor(if (enable) ContextCompat.getColor(context, R.color.hotel_filter_spinner_dropdown_color) else ContextCompat.getColor(context, R.color.white_disabled))
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

        originSuggestionViewModel = PackageSuggestionAdapterViewModel(getContext(), suggestionServices, false, CurrentLocationObservable.create(getContext()))
        destinationSuggestionViewModel = PackageSuggestionAdapterViewModel(getContext(), suggestionServices, true, null)
        originSuggestionAdapter = PackageSuggestionAdapter(originSuggestionViewModel)
        destinationSuggestionAdapter = PackageSuggestionAdapter(destinationSuggestionViewModel)
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
        return if (isCustomerSelectingOrigin) originSuggestionViewModel else destinationSuggestionViewModel
    }

    override fun getSuggestionAdapter(): RecyclerView.Adapter<RecyclerView.ViewHolder> {
        return if (isCustomerSelectingOrigin) originSuggestionAdapter else destinationSuggestionAdapter
    }

    override fun getSearchViewModel(): BaseSearchViewModel {
        return searchViewModel
    }
}
