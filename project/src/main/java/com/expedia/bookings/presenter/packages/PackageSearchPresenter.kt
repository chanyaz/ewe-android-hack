package com.expedia.bookings.presenter.packages

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ToggleButton
import com.expedia.account.graphics.ArrowXDrawable
import com.expedia.bookings.R
import com.expedia.bookings.location.CurrentLocationObservable
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.services.SuggestionV4Services
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.HotelTravelerPickerView
import com.expedia.bookings.widget.PackageSuggestionAdapter
import com.expedia.bookings.widget.SearchAutoCompleteView
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeOnClick
import com.expedia.util.subscribeToggleButton
import com.expedia.vm.HotelTravelerPickerViewModel
import com.expedia.vm.PackageSearchViewModel
import com.expedia.vm.PackageSuggestionAdapterViewModel
import com.mobiata.android.time.util.JodaUtils
import com.mobiata.android.time.widget.CalendarPicker
import com.mobiata.android.time.widget.DaysOfWeekView
import com.mobiata.android.time.widget.MonthView
import org.joda.time.LocalDate
import java.text.SimpleDateFormat
import java.util.Locale

class PackageSearchPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

    val suggestionServices: SuggestionV4Services by lazy {
        Ui.getApplication(getContext()).packageComponent().suggestionsService()
    }

    val flyingFromAutoComplete: SearchAutoCompleteView by bindView(R.id.flying_from)
    val flyingToAutoComplete: SearchAutoCompleteView by bindView(R.id.flying_to)
    val searchContainer: ViewGroup by bindView(R.id.search_container)
    val toolbar: Toolbar by bindView(R.id.toolbar)
    val selectDate: ToggleButton by bindView(R.id.select_date)
    val selectTraveler: ToggleButton by bindView(R.id.select_traveler)
    val calendar: CalendarPicker by bindView(R.id.calendar)
    val monthView: MonthView by bindView(R.id.month)
    val traveler: HotelTravelerPickerView by bindView(R.id.traveler_view)
    val dayOfWeek: DaysOfWeekView by bindView(R.id.days_of_week)

    var navIcon: ArrowXDrawable
    var isFromUser = true

    val dialog: AlertDialog by lazy {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.search_error)
        builder.setMessage(context.getString(R.string.hotel_search_range_error_TEMPLATE, resources.getInteger(R.integer.calendar_max_days_package_stay)))
        builder.setPositiveButton(context.getString(R.string.DONE), { dialog, which -> dialog.dismiss() })
        builder.create()
    }

    val searchButton: Button by lazy {
        val button = LayoutInflater.from(getContext()).inflate(R.layout.toolbar_checkmark_item, null) as Button
        val navIcon = ContextCompat.getDrawable(context, R.drawable.ic_check_white_24dp).mutate()
        navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        button.setCompoundDrawablesWithIntrinsicBounds(navIcon, null, null, null)
        button.setTextColor(ContextCompat.getColor(context, android.R.color.white))
        button.alpha = 0.15f
        toolbar.menu.findItem(R.id.menu_check).setActionView(button)
        button
    }

    var searchViewModel: PackageSearchViewModel by notNullAndObservable { vm ->
        val maxDate = LocalDate.now().plusDays(resources.getInteger(R.integer.calendar_max_selectable_date_range))
        calendar.setSelectableDateRange(LocalDate.now(), maxDate)
        calendar.setMaxSelectableDateRange(resources.getInteger(R.integer.calendar_max_selectable_date_range))
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
        vm.calendarTooltipTextObservable.subscribe(endlessObserver { p ->
            val (top, bottom) = p
            calendar.setToolTipText(top, bottom, true)
        })

        vm.dateTextObservable.subscribeToggleButton(selectDate)

        selectDate.subscribeOnClick(vm.enableDateObserver)
        vm.enableDateObservable.subscribe { enable ->
            if (enable) {
                selectDate.isChecked = true
                flyingFromAutoComplete.locationEditText.clearFocus()
                flyingToAutoComplete.locationEditText.clearFocus()
                traveler.visibility = View.GONE
                calendar.visibility = View.VISIBLE
                show(PackageParamsCalendar(), FLAG_CLEAR_BACKSTACK)
            } else {
                vm.errorNoOriginObservable.onNext(false)
            }
        }

        traveler.viewmodel.travelerParamsObservable.subscribe(vm.travelersObserver)
        traveler.viewmodel.isInfantInLapObservable.subscribe(vm.isInfantInLapObserver)
        traveler.viewmodel.guestsTextObservable.subscribeToggleButton(selectTraveler)
        selectTraveler.subscribeOnClick(vm.enableTravelerObserver)
        vm.enableTravelerObservable.subscribe { enable ->
            if (enable) {
                selectTraveler.isChecked = true
                flyingFromAutoComplete.locationEditText.clearFocus()
                flyingToAutoComplete.locationEditText.clearFocus()
                calendar.visibility = View.GONE
                traveler.visibility = View.VISIBLE
                calendar.hideToolTip()
            } else {
                vm.errorNoOriginObservable.onNext(false)
            }
        }

        vm.originTextObservable.subscribe { text ->
            isFromUser = false;
            flyingFromAutoComplete.locationEditText.setText(text)
        }

        vm.destinationTextObservable.subscribe { text ->
            isFromUser = false;
            flyingToAutoComplete.locationEditText.setText(text)
        }

        searchButton.subscribeOnClick(vm.searchObserver)
        vm.searchButtonObservable.subscribe { enable ->
            if (enable) {
                searchButton.alpha = 1.0f
            } else {
                searchButton.alpha = 0.15f
            }
        }
        vm.errorNoOriginObservable.subscribe {
            selectDate.isChecked = false
            selectTraveler.isChecked = false
            show(PackageParamsDefault())
            val editText = if (!it) flyingFromAutoComplete.locationEditText else flyingToAutoComplete.locationEditText
            editText.requestFocus()
            com.mobiata.android.util.Ui.showKeyboard(editText, null)
            AnimUtils.doTheHarlemShake(editText)
        }
        vm.errorNoDatesObservable.subscribe {
            if (calendar.visibility == View.VISIBLE) {
                AnimUtils.doTheHarlemShake(calendar)
            } else {
                AnimUtils.doTheHarlemShake(selectDate)
            }
        }
        vm.errorMaxDatesObservable.subscribe {
            dialog.show()
        }
        vm.searchParamsObservable.subscribe {
            calendar.hideToolTip()
        }
    }

    private var originSuggestionVM: PackageSuggestionAdapterViewModel by notNullAndObservable { vm ->
        vm.suggestionSelectedSubject.subscribe { suggestion ->
            searchViewModel.originObserver.onNext(suggestion)
            selectSuggestion()
        }
    }

    private var destinationSuggestionVM: PackageSuggestionAdapterViewModel by notNullAndObservable { vm ->
        vm.suggestionSelectedSubject.subscribe { suggestion ->
            searchViewModel.destinationObserver.onNext(suggestion)
            selectSuggestion()
        }
    }

    private fun selectSuggestion() {
        selectDate.isChecked = true
        selectTraveler.isChecked = true
        flyingFromAutoComplete.resetFocus()
        flyingToAutoComplete.resetFocus()
        resetAutoCompleteWeights()
    }

    private fun resetAutoCompleteWeights() {
        val expandedLp = flyingFromAutoComplete.layoutParams as LinearLayout.LayoutParams
        val unexpandedLp = flyingToAutoComplete.layoutParams as LinearLayout.LayoutParams
        expandedLp.weight = 1f
        unexpandedLp.weight = 1f
        flyingFromAutoComplete.layoutParams = expandedLp
        flyingToAutoComplete.layoutParams = unexpandedLp
        flyingFromAutoComplete.invalidate()
        flyingToAutoComplete.invalidate()
        show(PackageParamsCalendar(), Presenter.FLAG_CLEAR_BACKSTACK)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        addDefaultTransition(default)
        addTransition(defaultToCal)
        flyingFromAutoComplete.showSuggestions()
        show(PackageParamsDefault())
    }

    init {
        View.inflate(context, R.layout.widget_package_search_params, this)
        val statusBarHeight = Ui.getStatusBarHeight(getContext())
        if (statusBarHeight > 0) {
            val color = ContextCompat.getColor(context, R.color.packages_primary_color)
            val statusBar = Ui.setUpStatusBar(getContext(), toolbar, searchContainer, color)
            addView(statusBar)
        }

        navIcon = ArrowXDrawableUtil.getNavigationIconDrawable(getContext(), ArrowXDrawableUtil.ArrowDrawableType.CLOSE)
        navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        toolbar.navigationIcon = navIcon
        toolbar.setNavigationOnClickListener {
            com.mobiata.android.util.Ui.hideKeyboard(this@PackageSearchPresenter)
            val activity = getContext() as AppCompatActivity
            activity.onBackPressed()
        }
        toolbar.inflateMenu(R.menu.cars_search_menu)

        traveler.viewmodel = HotelTravelerPickerViewModel(getContext(), true)
        searchViewModel = PackageSearchViewModel(context)
        originSuggestionVM = PackageSuggestionAdapterViewModel(getContext(), suggestionServices, false, CurrentLocationObservable.create(getContext()))
        destinationSuggestionVM = PackageSuggestionAdapterViewModel(getContext(), suggestionServices, true, null)

        flyingFromAutoComplete.suggestionViewModel = originSuggestionVM
        flyingToAutoComplete.suggestionViewModel = destinationSuggestionVM
        flyingFromAutoComplete.suggestionAdapter = PackageSuggestionAdapter(originSuggestionVM)
        flyingToAutoComplete.suggestionAdapter = PackageSuggestionAdapter(destinationSuggestionVM)

        calendar.setYearMonthDisplayedChangedListener {
            calendar.hideToolTip()
        }

        monthView.setTextEqualDatesColor(Color.WHITE)
        monthView.setMaxTextSize(resources.getDimension(R.dimen.car_calendar_month_view_max_text_size))
        dayOfWeek.setDayOfWeekRenderer { dayOfWeek: LocalDate.Property ->
            if (Build.VERSION.SDK_INT >= 18) {
                val sdf = SimpleDateFormat("EEEEE", Locale.getDefault())
                sdf.format(dayOfWeek.localDate.toDate())
            } else if (Locale.getDefault().language == "en") {
                dayOfWeek.asShortText.toUpperCase(Locale.getDefault()).substring(0, 1)
            } else {
                DaysOfWeekView.DayOfWeekRenderer.DEFAULT.renderDayOfWeek(dayOfWeek)
            }
        }

        selectDate.typeface = FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR)
        selectTraveler.typeface = FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR)
        calendar.setMonthHeaderTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR))
        dayOfWeek.setTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR))
        monthView.setDaysTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_LIGHT))
        monthView.setTodayTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_MEDIUM))

        flyingFromAutoComplete.locationEditText.onFocusChangeListener = makeFocusChangedListener(flyingFromAutoComplete)
        flyingToAutoComplete.locationEditText.onFocusChangeListener =  makeFocusChangedListener(flyingToAutoComplete)

    }

    private fun makeFocusChangedListener(autoCompleteView: SearchAutoCompleteView): View.OnFocusChangeListener {
        return View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                show(PackageParamsDefault(), FLAG_CLEAR_BACKSTACK)
                val expandedContainer = if (v ==  flyingFromAutoComplete.locationEditText) flyingFromAutoComplete else flyingToAutoComplete
                val unexpandedContainer = if (v ==  flyingFromAutoComplete.locationEditText) flyingToAutoComplete else flyingFromAutoComplete
                val expandedLp = expandedContainer.layoutParams as LinearLayout.LayoutParams
                val unexpandedLp = unexpandedContainer.layoutParams as LinearLayout.LayoutParams
                expandedLp.weight = 2f
                unexpandedLp.weight = 1f
                expandedContainer.layoutParams = expandedLp
                unexpandedContainer.layoutParams = unexpandedLp
                expandedContainer.invalidate()
                unexpandedContainer.invalidate()
                resetSuggestion(v ==  flyingFromAutoComplete.locationEditText)
            }

            autoCompleteView.focusChanged(hasFocus)
        }
    }

    private val defaultToCal = object : Presenter.Transition(PackageParamsDefault::class.java, PackageParamsCalendar::class.java) {
        private var calendarHeight: Int = 0

        override fun startTransition(forward: Boolean) {
            com.mobiata.android.util.Ui.hideKeyboard(this@PackageSearchPresenter)
            calendarHeight = calendar.height
            val pos = (if (forward) height + calendarHeight else calendarHeight).toFloat()
            calendar.translationY = pos
            calendar.visibility = View.VISIBLE
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            val pos = if (forward) calendarHeight + (-f * calendarHeight) else (f * calendarHeight)
            calendar.translationY = pos
        }

        override fun endTransition(forward: Boolean) {
            calendar.translationY = if (forward) 0f else calendarHeight.toFloat()
            if (forward) {
                flyingFromAutoComplete.locationEditText.clearFocus()
                flyingToAutoComplete.locationEditText.clearFocus()
                calendar.hideToolTip()
            }
            calendar.visibility = View.VISIBLE
        }
    }

    private val default = object : Presenter.DefaultTransition(PackageParamsDefault::class.java.name) {
        override fun endTransition(forward: Boolean) {
            calendar.visibility =  View.INVISIBLE
            traveler.visibility =  View.GONE
        }
    }

    private fun resetSuggestion(isDestination: Boolean) {
        searchViewModel.suggestionTextChangedObserver.onNext(isDestination)
        selectDate.isChecked = false
        selectTraveler.isChecked = false
        calendar.visibility = View.GONE
        traveler.visibility = View.GONE
        calendar.hideToolTip()
    }

    override fun back(): Boolean {
        if (flyingFromAutoComplete.dismissSuggestions() || flyingToAutoComplete.dismissSuggestions()) {
            resetAutoCompleteWeights()
            return true;
        }

        calendar.hideToolTip()
        return super.back()
    }

    // Classes for state
    class PackageParamsDefault

    class PackageParamsCalendar
}
