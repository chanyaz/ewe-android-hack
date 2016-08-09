package com.expedia.bookings.presenter.car

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.location.CurrentLocationObservable
import com.expedia.bookings.presenter.BaseTwoLocationSearchPresenter
import com.expedia.bookings.services.SuggestionV4Services
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.SuggestionV4Utils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.suggestions.CarSuggestionAdapter
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeOnClick
import com.expedia.vm.BaseSearchViewModel
import com.expedia.vm.CarSuggestionAdapterViewModel
import com.expedia.vm.SuggestionAdapterViewModel
import com.expedia.vm.cars.CarSearchViewModel
import kotlin.properties.Delegates
import android.app.AlertDialog
import android.content.DialogInterface
import com.expedia.bookings.data.cars.CarSearchParam
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.CarDataUtils
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class CarSearchPresenter(context: Context, attrs: AttributeSet) : BaseTwoLocationSearchPresenter(context, attrs) {

    val suggestionServices: SuggestionV4Services by lazy {
        Ui.getApplication(getContext()).carComponent().suggestionsService()
    }
    private var originSuggestionAdapter: CarSuggestionAdapter by Delegates.notNull()
    private var destinationSuggestionAdapter: CarSuggestionAdapter by Delegates.notNull()


    var searchViewModel: CarSearchViewModel by notNullAndObservable { vm ->
        calendarWidget.viewModel = vm
        vm.formattedOriginObservable.subscribe {
            text -> originCardView.setText(text)
            if (this.visibility == VISIBLE && vm.startDate() == null) {
                calendarWidget.showCalendarDialog()
            }
        }

        vm.dateAccessibilityObservable.subscribe {
            text ->
            calendarWidget.contentDescription = text
        }

        vm.searchButtonObservable.subscribe { enable ->
            searchButton.setTextColor(if (enable) ContextCompat.getColor(context, R.color.hotel_filter_spinner_dropdown_color) else ContextCompat.getColor(context, R.color.white_disabled))
            if (AccessibilityUtil.isTalkBackEnabled(context)) {
                searchButton.isEnabled = enable
            }
        }
        vm.errorNoDatesObservable.subscribe { AnimUtils.doTheHarlemShake(calendarWidget) }
        vm.errorMaxDurationObservable.subscribe { message ->
            showErrorDialog(message)
        }
        vm.errorNoDestinationObservable.subscribe { AnimUtils.doTheHarlemShake(originCardView) }
        vm.errorMaxRangeObservable.subscribe { message ->
            showErrorDialog(message)
        }
        searchButton.subscribeOnClick(vm.searchObserver)

        originSuggestionViewModel = CarSuggestionAdapterViewModel(getContext(), suggestionServices, CurrentLocationObservable.create(getContext()), true, false)
        destinationSuggestionViewModel = CarSuggestionAdapterViewModel(getContext(), suggestionServices, CurrentLocationObservable.create(getContext()), true, false)
        originSuggestionViewModel.setCustomerSelectingOrigin(true)
        destinationSuggestionViewModel.setCustomerSelectingOrigin(false)
        originSuggestionAdapter = CarSuggestionAdapter(originSuggestionViewModel)
        destinationSuggestionAdapter = CarSuggestionAdapter(destinationSuggestionViewModel)
        }

    override fun inflate() {
        View.inflate(context, R.layout.widget_car_search, this)
    }

    override fun onFinishInflate(){
        super.onFinishInflate()

        destinationCardView.setOnClickListener({ showAlertMessage(R.string.drop_off_same_as_pick_up, R.string.ok) })
        calendarWidget.setOnClickListener {
            calendarWidget.showCalendarDialog()
        }
        if (AccessibilityUtil.isTalkBackEnabled(context)) {
            searchButton.isEnabled = false
        }
    }

    override var originSuggestionViewModel: SuggestionAdapterViewModel by notNullAndObservable { vm ->
        val suggestionSelectedObserver = suggestionSelectedObserver(getSearchViewModel().originLocationObserver)
        val delayBeforeShowingDestinationSuggestions = 325L
        val waitForOtherSuggestionListeners = 350L

        vm.suggestionSelectedSubject
                .doOnNext(suggestionSelectedObserver)
                .debounce(waitForOtherSuggestionListeners + delayBeforeShowingDestinationSuggestions, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .filter { destinationCardView.text.equals(getDestinationSearchBoxPlaceholderText()) }
                .subscribe ()
    }
    override fun getSuggestionHistoryFileName(): String {
        return SuggestionV4Utils.RECENT_CAR_SUGGESTIONS_FILE
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
        return context.resources.getString(R.string.car_pick_up_hint)
    }

    override fun getDestinationSearchBoxPlaceholderText(): String {
        return context.resources.getString(R.string.fly_to_hint)
    }

    override fun requestA11yFocus(isOrigin: Boolean) {
        AccessibilityUtil.setFocusToToolbarNavigationIcon(toolbar)
    }

    fun showAlertMessage(messageResourceId: Int, confirmButtonResourceId: Int) {
        val b = AlertDialog.Builder(getContext());
        b.setCancelable(false)
                .setMessage(messageResourceId)
                .setPositiveButton(confirmButtonResourceId, DialogInterface.OnClickListener { dialog, i ->
                    dialog.dismiss();
                    AccessibilityUtil.delayedFocusToView(destinationCardView, 300)
                }).create()
                .show();
    }

    fun updateSearchViewModel(carSearchParams: CarSearchParam) {
        searchViewModel.enableDateObserver.onNext(Unit)
        searchViewModel.originLocationObserver.onNext(CarDataUtils.getSuggestionFromLocation(carSearchParams.originLocation,
                carSearchParams.pickupLocationLatLng, carSearchParams.originDescription))
        val times = Pair(carSearchParams.startDateTime.millisOfDay,
                carSearchParams.endDateTime.millisOfDay)
        val dates = Pair(carSearchParams.startDateTime.toLocalDate(),
                carSearchParams.endDateTime.toLocalDate())
        searchViewModel.datesObserver.onNext(dates)
        searchViewModel.departTimeSubject.onNext(times.first)
        searchViewModel.returnTimeSubject.onNext(times.second)
        searchViewModel.setUpTimeSliderSubject.onNext(dates)
        searchViewModel.onTimesChanged(times)
        selectDates(dates.first, dates.second)
        searchViewModel.searchButtonObservable.onNext(true)
    }
}