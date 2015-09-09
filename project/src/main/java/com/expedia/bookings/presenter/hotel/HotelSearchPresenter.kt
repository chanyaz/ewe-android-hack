package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ToggleButton
import com.expedia.account.graphics.ArrowXDrawable
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.SuggestionV4
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.widget.AlwaysFilterAutoCompleteTextView
import com.expedia.bookings.widget.HotelSuggestionAdapter
import com.expedia.bookings.widget.HotelTravelerPickerView
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribe
import com.expedia.util.subscribeOnClick
import com.expedia.util.subscribeToggleButton
import com.expedia.vm.HotelSearchViewModel
import com.expedia.vm.HotelSuggestionAdapterViewModel
import com.expedia.vm.HotelTravelerPickerViewModel
import com.mobiata.android.time.util.JodaUtils
import com.mobiata.android.time.widget.CalendarPicker
import com.mobiata.android.time.widget.DaysOfWeekView
import com.mobiata.android.time.widget.MonthView
import org.joda.time.LocalDate
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.properties.Delegates

public class HotelSearchPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    val searchLocation: AlwaysFilterAutoCompleteTextView by bindView(R.id.hotel_location)
    val clearLocationButton: ImageView by bindView(R.id.clear_location_button)
    val selectDate: ToggleButton by bindView(R.id.select_date)
    val selectTraveler: ToggleButton by bindView(R.id.select_traveler)
    val calendar: CalendarPicker by bindView(R.id.calendar)
    val monthView: MonthView by bindView(R.id.month)
    val monthSelectionView by Delegates.lazy {
       findViewById(R.id.previous_month).getParent() as LinearLayout
    }
    val traveler: HotelTravelerPickerView by bindView(R.id.traveler_view)
    val dayOfWeek: DaysOfWeekView by bindView(R.id.days_of_week)
    var navIcon: ArrowXDrawable
    var searchParamsContainerHeight: Int = 0

    val searchContainer: ViewGroup by bindView(R.id.search_container)
    val searchParamsContainer: ViewGroup by bindView(R.id.search_params_container)

    val toolbar: Toolbar by bindView(R.id.toolbar)
    val toolbarTitle by Delegates.lazy { toolbar.getChildAt(0) }
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

    // Classes for state
    public class HotelParamsDefault

    public class HotelParamsCalendar

    override fun onFinishInflate() {
        addTransition(defaultToCal)
        show(HotelParamsDefault())
    }

    var viewmodel: HotelSearchViewModel by notNullAndObservable { vm ->
        val maxDate = LocalDate.now().plusDays(getResources().getInteger(R.integer.calendar_max_selectable_date_range))
        calendar.setSelectableDateRange(LocalDate.now(), maxDate)
        calendar.setMaxSelectableDateRange(getResources().getInteger(R.integer.calendar_max_days_hotel_stay))
        calendar.setDateChangedListener { start, end ->
            if (JodaUtils.isEqual(start, end)) {
                if (!JodaUtils.isEqual(end, maxDate)) {
                    calendar.setSelectedDates(start, end.plusDays(1))
                } else {
                    // Do not select an end date beyond the allowed range
                    calendar.setSelectedDates(start, null)
                }
            } else {
                vm.datesObserver.onNext(Pair(start, end))
            }
        }
        calendar.setYearMonthDisplayedChangedListener {
            calendar.hideToolTip()
        }

        vm.calendarTooltipTextObservable.subscribe(endlessObserver { p ->
            val (top, bottom) = p
            calendar.setToolTipText(top, bottom, true)
        })

        vm.dateTextObservable.subscribeToggleButton(selectDate)

        selectDate.subscribeOnClick(vm.enableDateObserver)
        vm.enableDateObservable.subscribe { enable ->
            if (enable) {
                selectDate.setChecked(true)
                searchLocation.clearFocus()
                calendar.setVisibility(View.VISIBLE)
                traveler.setVisibility(View.GONE)
                show(HotelParamsCalendar(), Presenter.FLAG_CLEAR_BACKSTACK)
            } else {
                vm.errorNoOriginObservable.onNext(Unit)
            }
        }

        traveler.viewmodel.travelerParamsObservable.subscribe(vm.travelersObserver)
        traveler.viewmodel.guestsTextObservable.subscribeToggleButton(selectTraveler)
        selectTraveler.subscribeOnClick(vm.enableTravelerObserver)
        vm.enableTravelerObservable.subscribe { enable ->
            if (enable) {
                selectTraveler.setChecked(true)
                searchLocation.clearFocus()
                calendar.setVisibility(View.GONE)
                traveler.setVisibility(View.VISIBLE)
                calendar.hideToolTip()
            } else {
                vm.errorNoOriginObservable.onNext(Unit)
            }
        }

        searchLocation.setAdapter(hotelSuggestionAdapter)
        searchLocation.setOnFocusChangeListener { view, isFocused ->
            if (isFocused) {
                searchLocation.showDropDown()
                clearLocationButton.setVisibility(View.VISIBLE)
            } else{
                clearLocationButton.setVisibility(View.GONE)
                com.mobiata.android.util.Ui.hideKeyboard(this)
            }
        }

        searchLocation.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                clearLocationButton.setVisibility(if (Strings.isEmpty(s)) View.GONE else View.VISIBLE)

                if (selectDate.isChecked()) {
                    vm.suggestionTextChangedObserver.onNext(Unit)
                    selectDate.setChecked(false)
                    selectTraveler.setChecked(false)
                    calendar.setVisibility(View.GONE)
                    traveler.setVisibility(View.GONE)
                    calendar.hideToolTip()
                }
            }
        })

        searchLocation.setOnItemClickListener {
            adapterView, view, position, l ->
            vm.suggestionObserver.onNext(hotelSuggestionAdapter.getItem(position))
            selectDate.setChecked(true)
            selectTraveler.setChecked(true)

            searchLocation.clearFocus()
            calendar.setVisibility(View.VISIBLE)
            traveler.setVisibility(View.GONE)
            show(HotelParamsCalendar(), Presenter.FLAG_CLEAR_BACKSTACK)
        }

        vm.locationTextObservable.subscribe(searchLocation)

        clearLocationButton.setOnClickListener { view ->
            searchLocation.setText(null)
        }

        searchButton.subscribeOnClick(vm.searchObserver)
        vm.searchButtonObservable.subscribe { enable ->
            if (enable) {
                searchButton.setAlpha(1.0f)
            } else {
                searchButton.setAlpha(0.15f)
            }
        }
        vm.errorNoOriginObservable.subscribe {
            selectDate.setChecked(false)
            selectTraveler.setChecked(false)
            AnimUtils.doTheHarlemShake(searchLocation)
        }
        vm.errorNoDatesObservable.subscribe {
            if (calendar.getVisibility() == View.VISIBLE) {
                AnimUtils.doTheHarlemShake(calendar)
            } else {
                AnimUtils.doTheHarlemShake(selectDate)
            }
        }
        vm.searchParamsObservable.subscribe {
            calendar.hideToolTip()
        }
    }

    init {
        View.inflate(context, R.layout.widget_hotel_search_params, this)
        traveler.viewmodel = HotelTravelerPickerViewModel(getContext())
        searchLocation.requestFocus();
        calendar.setVisibility(View.INVISIBLE)
        traveler.setVisibility(View.GONE)
        selectDate.setChecked(false)
        selectTraveler.setChecked(false)

        val statusBarHeight = Ui.getStatusBarHeight(getContext())
        if (statusBarHeight > 0) {
            val color = getContext().getResources().getColor(R.color.hotels_primary_color)
            val statusBar = Ui.setUpStatusBar(getContext(), toolbar, searchContainer, color)
            addView(statusBar)
        }

        navIcon = ArrowXDrawableUtil.getNavigationIconDrawable(getContext(), ArrowXDrawableUtil.ArrowDrawableType.CLOSE)
        navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        toolbar.setNavigationIcon(navIcon)
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

        searchContainer.getViewTreeObserver().addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                searchContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this)
                searchParamsContainerHeight = searchParamsContainer.getMeasuredHeight()
            }
        })

        selectDate.setTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR))
        selectTraveler.setTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR))
        calendar.setMonthHeaderTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR))
        dayOfWeek.setTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR))
        monthView.setDaysTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_LIGHT))
        monthView.setTodayTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_MEDIUM))
    }

    private val defaultToCal = object : Presenter.Transition(javaClass<HotelParamsDefault>(), javaClass<HotelParamsCalendar>()) {
        private var calendarHeight: Int = 0

        override fun startTransition(forward: Boolean) {
            val parentHeight = getHeight()
            calendarHeight = calendar.getHeight()
            val pos = (if (forward) parentHeight + calendarHeight else calendarHeight).toFloat()
            calendar.setTranslationY(pos)
            calendar.setVisibility(View.VISIBLE)
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            val pos = if (forward) calendarHeight + (-f * calendarHeight) else (f * calendarHeight)
            calendar.setTranslationY(pos)
        }

        override fun endTransition(forward: Boolean) {
            calendar.setTranslationY((if (forward) 0 else calendarHeight).toFloat())
        }

        override fun finalizeTransition(forward: Boolean) {
            calendar.setTranslationY((if (forward) 0 else calendarHeight).toFloat())
            if (forward) {
                com.mobiata.android.util.Ui.hideKeyboard(this@HotelSearchPresenter)
                searchLocation.clearFocus()
                calendar.hideToolTip()
            }
            calendar.setVisibility(View.VISIBLE)
        }
    }

    var toolbarTitleTop = 0
    fun animationStart(forward : Boolean) {
        calendar.setTranslationY((if (forward) calendar.getHeight() else 0).toFloat())
        traveler.setTranslationY((if (forward) traveler.getHeight() else 0).toFloat())
        searchContainer.setBackgroundColor(Color.TRANSPARENT)
        toolbarTitleTop = toolbarTitle.getHeight() - toolbarTitle.getTop()

    }

    fun animationUpdate(f : Float, forward : Boolean) {
        val translationCalendar = if (forward) calendar.getHeight() * (1 - f) else calendar.getHeight() * f
        val layoutParams = searchParamsContainer.getLayoutParams()
        layoutParams.height = if (forward) (f * (searchParamsContainerHeight)).toInt() else (Math.abs(f - 1) * (searchParamsContainerHeight)).toInt()
        searchParamsContainer.setLayoutParams(layoutParams)

        calendar.setTranslationY(translationCalendar)
        traveler.setTranslationY(translationCalendar)
        val factor: Float = if (forward) f else Math.abs(1 - f)
        toolbar.setAlpha(factor)
        traveler.setAlpha(factor)
        monthView.setAlpha(factor)
        dayOfWeek.setAlpha(factor)
        monthSelectionView.setAlpha(factor)
        navIcon.setParameter(factor)

        toolbarTitle.setTranslationY((if (forward) Math.abs(1 - f) else f) * -toolbarTitleTop)
    }

    fun animationFinalize() {
        searchContainer.setBackgroundColor(Color.WHITE)
        navIcon.setParameter(ArrowXDrawableUtil.ArrowDrawableType.CLOSE.getType().toFloat())
    }
}
