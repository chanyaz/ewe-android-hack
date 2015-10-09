package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ToggleButton
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.SuggestionV4
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.AlwaysFilterAutoCompleteTextView
import com.expedia.bookings.widget.HotelSuggestionAdapter
import com.expedia.bookings.widget.HotelTravelerPickerView
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribe
import com.expedia.util.subscribeOnClick
import com.expedia.vm.HotelSearchViewModel
import com.expedia.vm.HotelSuggestionAdapterViewModel
import com.expedia.vm.HotelTravelerPickerViewModel
import com.mobiata.android.time.widget.CalendarPicker
import com.mobiata.android.time.widget.DaysOfWeekView
import com.mobiata.android.time.widget.MonthView
import org.joda.time.LocalDate
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.properties.Delegates

public class HotelSearchPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

    val searchLocation: AlwaysFilterAutoCompleteTextView by bindView(R.id.hotel_location)
    val selectDate: Button by bindView(R.id.select_date)
    val selectTraveler: Button by bindView(R.id.select_traveler)
    val calendar: CalendarPicker by bindView(R.id.calendar)
    val monthView: MonthView by bindView(R.id.month)
    val traveler: HotelTravelerPickerView by bindView(R.id.traveler_view)
    val dayOfWeek: DaysOfWeekView by bindView(R.id.days_of_week)

    val searchContainer: ViewGroup by bindView(R.id.search_container)
    val toolbar: Toolbar by bindView(R.id.toolbar)
    val searchButton: Button by Delegates.lazy {
        val button = LayoutInflater.from(getContext()).inflate(R.layout.toolbar_checkmark_item, null) as Button
        val navIcon = getResources().getDrawable(R.drawable.ic_check_white_24dp).mutate()
        navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        button.setCompoundDrawablesWithIntrinsicBounds(navIcon, null, null, null)
        button.setTextColor(getResources().getColor(android.R.color.white))
        // Disable search button initially
        button.setAlpha(0.15f)
        toolbar.getMenu().findItem(R.id.menu_check).setActionView(button)

        button
    }

    private val hotelSuggestionAdapter by Delegates.lazy {
        val service = Ui.getApplication(getContext()).hotelComponent().suggestionsService()
        HotelSuggestionAdapter(HotelSuggestionAdapterViewModel(service))
    }

    var viewmodel: HotelSearchViewModel by notNullAndObservable { vm ->
        calendar.setSelectableDateRange(LocalDate.now(), LocalDate.now().plusDays(getResources().getInteger(R.integer.calendar_max_selectable_date_range)))
        calendar.setMaxSelectableDateRange(getResources().getInteger(R.integer.calendar_max_days_hotel_stay))
        calendar.setDateChangedListener { start, end ->
            vm.datesObserver.onNext(Pair(start, end))
        }
        calendar.setYearMonthDisplayedChangedListener {
            calendar.hideToolTip()
        }

        vm.calendarTooltipTextObservable.subscribe(endlessObserver { p ->
            val (top, bottom) = p
            calendar.setToolTipText(top, bottom)
        })

        vm.dateTextObservable.subscribe(selectDate)
        selectDate.setOnClickListener {
            calendar.setVisibility(View.VISIBLE)
            traveler.setVisibility(View.GONE)
        }

        traveler.viewmodel.travelerParamsObservable.subscribe(vm.travelersObserver)
        traveler.viewmodel.guestsTextObservable.subscribe(selectTraveler)
        selectTraveler.setOnClickListener {
            calendar.setVisibility(View.GONE)
            traveler.setVisibility(View.VISIBLE)
            calendar.hideToolTip()
        }

        searchLocation.setAdapter(hotelSuggestionAdapter)
        searchLocation.setOnItemClickListener {
            adapterView, view, position, l ->
            vm.suggestionObserver.onNext(hotelSuggestionAdapter.getItem(position))
            com.mobiata.android.util.Ui.hideKeyboard(this)
        }

        vm.locationTextObservable.subscribe(searchLocation)

        searchButton.subscribeOnClick(vm.searchObserver)
        vm.searchButtonObservable.subscribe { enable ->
            if (enable) {
                searchButton.setAlpha(1.0f)
            } else {
                searchButton.setAlpha(0.15f)
            }
        }
        vm.errorNoOriginObservable.subscribe {
            AnimUtils.doTheHarlemShake(searchLocation)
        }
        vm.errorNoDatesObservable.subscribe {
            AnimUtils.doTheHarlemShake(calendar)
        }
        vm.searchParamsObservable.subscribe {
            calendar.hideToolTip()
        }
    }

    init {
        View.inflate(context, R.layout.widget_hotel_search_params, this)
        traveler.viewmodel = HotelTravelerPickerViewModel(getContext())

        val statusBarHeight = Ui.getStatusBarHeight(getContext())
        if (statusBarHeight > 0) {
            val color = getContext().getResources().getColor(R.color.hotels_primary_color)
            val statusBar = Ui.setUpStatusBar(getContext(), toolbar, searchContainer, color)
            addView(statusBar)
        }

        toolbar.inflateMenu(R.menu.cars_search_menu)

        monthView.setTextEqualDatesColor(Color.WHITE)
        monthView.setMaxTextSize(getResources().getDimension(R.dimen.car_calendar_month_view_max_text_size))

        dayOfWeek.setDayOfWeekRenderer { dayOfWeek: LocalDate.Property ->
            if (Build.VERSION.SDK_INT >= 18) {
                val sdf = SimpleDateFormat("EEEEE", Locale.getDefault())
                sdf.format(dayOfWeek.getLocalDate().toDate())
            } else if (Locale.getDefault().getLanguage() == "en") {
                dayOfWeek.getAsShortText().toUpperCase(Locale.getDefault()).substring(0, 1)
            } else {
                DaysOfWeekView.DayOfWeekRenderer.DEFAULT.renderDayOfWeek(dayOfWeek)
            }
        }

        selectDate.setTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR))
        selectTraveler.setTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR))
        calendar.setMonthHeaderTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR))
        dayOfWeek.setTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR))
        monthView.setDaysTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_LIGHT))
        monthView.setTodayTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_MEDIUM))
    }
}
