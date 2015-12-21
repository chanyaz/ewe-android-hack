package com.expedia.bookings.presenter.packages

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Rect
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ToggleButton
import com.expedia.account.graphics.ArrowXDrawable
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.SuggestionV4
import com.expedia.bookings.location.CurrentLocationObservable
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.services.SuggestionV4Services
import com.expedia.bookings.utils.*
import com.expedia.bookings.widget.HotelTravelerPickerView
import com.expedia.bookings.widget.PackageSuggestionAdapter
import com.expedia.bookings.widget.RecyclerDividerDecoration
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
import java.util.*

public class PackageSearchPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

    val suggestionServices: SuggestionV4Services by lazy {
        Ui.getApplication(getContext()).packageComponent().suggestionsService()
    }

    val destinationEditText: EditText by bindView(R.id.flying_from)
    val arrivalEditText: EditText by bindView(R.id.flying_to)
    val suggestionRecyclerView: RecyclerView by bindView(R.id.drop_down_list)
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
                selectDate.setChecked(true)
                destinationEditText.clearFocus()
                arrivalEditText.clearFocus()
                calendar.setVisibility(View.VISIBLE)
                traveler.setVisibility(View.GONE)
                if (currentState == null) {
                    show(PackageParamsDefault())
                }
                show(PackageParamsCalendar(), FLAG_CLEAR_BACKSTACK)
            } else {
                vm.errorNoOriginObservable.onNext(false)
            }
        }

        traveler.viewmodel.travelerParamsObservable.subscribe(vm.travelersObserver)
        traveler.viewmodel.guestsTextObservable.subscribeToggleButton(selectTraveler)
        selectTraveler.subscribeOnClick(vm.enableTravelerObserver)
        vm.enableTravelerObservable.subscribe { enable ->
            if (enable) {
                selectTraveler.setChecked(true)
                destinationEditText.clearFocus()
                arrivalEditText.clearFocus()
                calendar.setVisibility(View.GONE)
                traveler.setVisibility(View.VISIBLE)
                calendar.hideToolTip()
            } else {
                vm.errorNoOriginObservable.onNext(false)
            }
        }

        vm.destinationTextObservable.subscribe {
            val text = if (it.equals(context.getString(R.string.current_location))) "" else it
            isFromUser = false;
            destinationEditText.setText(text)
        }

        vm.arrivalTextObservable.subscribe {
            val text = if (it.equals(context.getString(R.string.current_location))) "" else it
            isFromUser = false;
            arrivalEditText.setText(text)
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
            if (currentState == null) {
                show(PackageParamsDefault())
            }
            show(PackageParamsSuggestion(), FLAG_CLEAR_BACKSTACK)
            val editText = if (!it) destinationEditText else arrivalEditText
            editText.requestFocus()
            com.mobiata.android.util.Ui.showKeyboard(editText, null)
            AnimUtils.doTheHarlemShake(editText)
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

    private var destinationSuggestionVM: PackageSuggestionAdapterViewModel by notNullAndObservable { vm ->
        vm.suggestionSelectedSubject.subscribe { suggestion ->
            searchViewModel.destinationObserver.onNext(suggestion)
            selectSuggestion(suggestion, destinationEditText)
        }
    }

    private var arrivalSuggestionVM: PackageSuggestionAdapterViewModel by notNullAndObservable { vm ->
        vm.suggestionSelectedSubject.subscribe { suggestion ->
            searchViewModel.arrivalObserver.onNext(suggestion)
            selectSuggestion(suggestion, arrivalEditText)
        }
    }

    private fun selectSuggestion(suggestion: SuggestionV4, editText: EditText) {
        editText.clearFocus()
        com.mobiata.android.util.Ui.hideKeyboard(this)
        selectDate.isChecked = true
        selectTraveler.isChecked = true
        SuggestionV4Utils.saveSuggestionHistory(context, suggestion, SuggestionV4Utils.RECENT_HOTEL_SUGGESTIONS_FILE)
        currentState ?: show(PackageParamsDefault())
        show(PackageParamsCalendar(), Presenter.FLAG_CLEAR_BACKSTACK)
    }

    private var destinationSuggestionAdapter: PackageSuggestionAdapter by notNullAndObservable {

    }
    private var arrivalSuggestionAdapter: PackageSuggestionAdapter by notNullAndObservable {

    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        addDefaultTransition(default)
        addTransition(defaultToCal)
        addTransition(defaultToSuggestion)
        addTransition(suggestionToCal)
        show(PackageParamsDefault())

        val mRootWindow = (context as Activity).window
        val mRootView = mRootWindow.decorView.findViewById(android.R.id.content)

        mRootView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val decorView = mRootWindow.decorView
                val windowVisibleDisplayFrameRect = Rect()
                decorView.getWindowVisibleDisplayFrame(windowVisibleDisplayFrameRect)
                var location = IntArray(2)
                destinationEditText.getLocationOnScreen(location)
                val lp = suggestionRecyclerView.layoutParams
                val newHeight = windowVisibleDisplayFrameRect.bottom - windowVisibleDisplayFrameRect.top - (location[1] + destinationEditText.height) + Ui.getStatusBarHeight(context)
                if (lp.height != newHeight) {
                    lp.height = newHeight
                    suggestionRecyclerView.layoutParams = lp
                }
                suggestionRecyclerView.translationY = (location[1] + destinationEditText.height).toFloat()
            }
        })
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

        traveler.viewmodel = HotelTravelerPickerViewModel(getContext())
        searchViewModel = PackageSearchViewModel(context)
        destinationSuggestionVM = PackageSuggestionAdapterViewModel(getContext(), suggestionServices, CurrentLocationObservable.create(getContext()))
        arrivalSuggestionVM = PackageSuggestionAdapterViewModel(getContext(), suggestionServices, null)
        destinationSuggestionAdapter = PackageSuggestionAdapter(destinationSuggestionVM)
        arrivalSuggestionAdapter = PackageSuggestionAdapter(arrivalSuggestionVM)

        calendar.setYearMonthDisplayedChangedListener {
            calendar.hideToolTip()
        }

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

        selectDate.typeface = FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR)
        selectTraveler.typeface = FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR)
        calendar.setMonthHeaderTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR))
        dayOfWeek.setTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR))
        monthView.setDaysTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_LIGHT))
        monthView.setTodayTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_MEDIUM))

        suggestionRecyclerView.layoutManager = LinearLayoutManager(context)
        suggestionRecyclerView.addItemDecoration(RecyclerDividerDecoration(getContext(), 0, 0, 0, 0, 0, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25f, resources.displayMetrics).toInt(), false))

        destinationEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {

            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (isFromUser) {
                    destinationSuggestionVM.queryObserver.onNext(s.toString())
                }
                isFromUser = true
            }
        })

        arrivalEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {

            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (isFromUser) {
                    arrivalSuggestionVM.queryObserver.onNext(s.toString())
                }
                isFromUser = true
            }
        })

        destinationEditText.onFocusChangeListener = object : OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                if (v == destinationEditText && hasFocus) {
                    suggestionFocusChanged(true)
                }
            }
        }

        arrivalEditText.onFocusChangeListener = object : OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                if (v == arrivalEditText && hasFocus) {
                    suggestionFocusChanged(false)
                }
            }
        }

        suggestionRecyclerView.adapter = destinationSuggestionAdapter
    }

    private fun suggestionFocusChanged(isDestination: Boolean) {
        val editText = if (isDestination) destinationEditText else arrivalEditText
        val adapter = if (isDestination) destinationSuggestionAdapter else arrivalSuggestionAdapter
        resetSuggestion(isDestination)
        editText.setSelection(editText.getText().length)
        suggestionRecyclerView.adapter = adapter
        show(PackageParamsSuggestion())
        adapter.notifyDataSetChanged()
    }

    private val defaultToCal = object : Presenter.Transition(PackageParamsDefault::class.java, PackageParamsCalendar::class.java) {
        private var calendarHeight: Int = 0

        override fun startTransition(forward: Boolean) {
            com.mobiata.android.util.Ui.hideKeyboard(this@PackageSearchPresenter)
            suggestionRecyclerView.visibility = View.GONE
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
        }

        override fun finalizeTransition(forward: Boolean) {
            calendar.translationY = if (forward) 0f else calendarHeight.toFloat()
            if (forward) {
                destinationEditText.clearFocus()
                arrivalEditText.clearFocus()
                calendar.hideToolTip()
            }
            calendar.visibility = View.VISIBLE
        }
    }

    private val suggestionToCal = object : Presenter.Transition(PackageParamsSuggestion::class.java, PackageParamsCalendar::class.java) {
        private var calendarHeight: Int = 0

        override fun startTransition(forward: Boolean) {
            suggestionRecyclerView.visibility = if (forward) View.GONE else View.VISIBLE
            val parentHeight = height
            calendarHeight = calendar.height
            val pos = (if (forward) parentHeight + calendarHeight else calendarHeight).toFloat()
            calendar.translationY = pos
            calendar.visibility = View.VISIBLE
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            val pos = if (forward) calendarHeight + (-f * calendarHeight) else (f * calendarHeight)
            calendar.translationY = pos
        }

        override fun endTransition(forward: Boolean) {
            calendar.translationY = if (forward) 0f else calendarHeight.toFloat()
        }

        override fun finalizeTransition(forward: Boolean) {
            calendar.translationY = if (forward) 0f else calendarHeight.toFloat()
            if (forward) {
                com.mobiata.android.util.Ui.hideKeyboard(this@PackageSearchPresenter)
                destinationEditText.clearFocus()
                arrivalEditText.clearFocus()
                calendar.hideToolTip()
            }
            calendar.visibility = View.VISIBLE
        }
    }

    private val defaultToSuggestion = object : Presenter.Transition(PackageParamsDefault::class.java, PackageParamsSuggestion::class.java) {
        override fun startTransition(forward: Boolean) {
            suggestionRecyclerView.visibility = if (forward) View.VISIBLE else View.GONE
        }
    }

    private val default = object : Presenter.DefaultTransition(PackageParamsDefault::class.java.name) {
        override fun finalizeTransition(forward: Boolean) {
            suggestionRecyclerView.visibility = View.VISIBLE
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
        calendar.hideToolTip()
        return super.back()
    }

    // Classes for state
    public class PackageParamsDefault

    public class PackageParamsCalendar

    public class PackageParamsSuggestion
}
