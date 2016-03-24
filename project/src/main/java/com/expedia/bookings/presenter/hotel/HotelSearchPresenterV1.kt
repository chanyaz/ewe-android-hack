package com.expedia.bookings.presenter.hotel

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Rect
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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ToggleButton
import com.expedia.account.graphics.ArrowXDrawable
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.data.TravelerParams
import com.expedia.bookings.location.CurrentLocationObservable
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.SuggestionV4Utils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.HotelSuggestionAdapter
import com.expedia.bookings.widget.HotelTravelerPickerView
import com.expedia.bookings.widget.RecentSearchesWidget
import com.expedia.bookings.widget.RecyclerDividerDecoration
import com.expedia.bookings.widget.TextView
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeOnClick
import com.expedia.util.subscribeToggleButton
import com.expedia.vm.DatedSearchViewModel
import com.expedia.vm.HotelSearchViewModel
import com.expedia.vm.HotelSuggestionAdapterViewModel
import com.expedia.vm.HotelTravelerPickerViewModel
import com.expedia.vm.RecentSearchesAdapterViewModel
import com.mobiata.android.time.util.JodaUtils
import org.joda.time.LocalDate

class HotelSearchPresenterV1(context: Context, attrs: AttributeSet) : BaseHotelSearchPresenter(context, attrs) {
    override fun getSearchViewModel(): DatedSearchViewModel {
        return searchViewModel
    }

    val searchLocationTextView: TextView by bindView(R.id.hotel_location)
    val searchLocationEditText: EditText by bindView(R.id.hotel_location_autocomplete)
    val suggestionRecyclerView: RecyclerView by bindView(R.id.drop_down_list)
    val clearLocationButton: ImageView by bindView(R.id.clear_location_button)
    val anchor: ImageView by bindView(R.id.location_image_view)
    val selectDate: ToggleButton by bindView(R.id.select_date)
    val selectTraveler: ToggleButton by bindView(R.id.select_traveler)
    val recentSearches: RecentSearchesWidget by bindView(R.id.recent_searches)
    val monthSelectionView by lazy {
        findViewById(R.id.previous_month).getParent() as LinearLayout
    }
    val traveler: HotelTravelerPickerView by bindView(R.id.traveler_view)
    var navIcon: ArrowXDrawable
    var searchParamsContainerHeight: Int = 0

    val searchContainer: ViewGroup by bindView(R.id.search_container)
    val searchParamsContainer: ViewGroup by bindView(R.id.search_params_container)

    val toolbar: Toolbar by bindView(R.id.toolbar)
    val toolbarTitle by lazy { toolbar.getChildAt(0) }
    val searchButton: Button by lazy {
        val button = LayoutInflater.from(getContext()).inflate(R.layout.toolbar_checkmark_item, null) as Button
        val navIcon = ContextCompat.getDrawable(context, R.drawable.ic_check_white_24dp).mutate()
        navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        button.setCompoundDrawablesWithIntrinsicBounds(navIcon, null, null, null)
        button.setTextColor(ContextCompat.getColor(context, android.R.color.white))
        // Disable search button initially
        button.setAlpha(0.15f)
        toolbar.getMenu().findItem(R.id.menu_check).setActionView(button)

        button
    }

    var suggestionViewModel: HotelSuggestionAdapterViewModel by notNullAndObservable { vm ->
        vm.suggestionSelectedSubject.subscribe { suggestion ->
            searchContainer.setBackgroundColor(Color.WHITE)
            searchViewModel.suggestionObserver.onNext(suggestion)
            searchLocationEditText.clearFocus()
            searchLocationEditText.visibility = View.INVISIBLE
            searchLocationTextView.visibility = View.VISIBLE
            clearLocationButton.visibility = View.INVISIBLE
            com.mobiata.android.util.Ui.hideKeyboard(this)

            selectDate.setChecked(true)
            selectTraveler.setChecked(true)

            calendar.setVisibility(View.VISIBLE)
            traveler.setVisibility(View.GONE)
            SuggestionV4Utils.saveSuggestionHistory(context, suggestion, SuggestionV4Utils.RECENT_HOTEL_SUGGESTIONS_FILE)
            if (currentState == null) {
                show(HotelParamsDefault())
            }
            show(HotelParamsCalendar(), Presenter.FLAG_CLEAR_BACKSTACK)
        }
    }

    override fun selectTravelers(params: TravelerParams) {
        traveler.viewmodel.travelerParamsObservable.onNext(params)
        selectTraveler.isChecked = true
    }

    override fun selectDates(startDate: LocalDate?, endDate: LocalDate?) {
        calendar.setSelectedDates(startDate, endDate)
    }

    private val hotelSuggestionAdapter by lazy {
        val service = Ui.getApplication(getContext()).hotelComponent().suggestionsService()
        suggestionViewModel = HotelSuggestionAdapterViewModel(getContext(), service, CurrentLocationObservable.create(getContext()), true, true)
        HotelSuggestionAdapter(suggestionViewModel)
    }

    // Classes for state
    class HotelParamsDefault

    class HotelParamsCalendar

    class HotelParamsSuggestion

    override fun onFinishInflate() {
        addTransition(defaultToCal)
        addTransition(defaultToSuggestion)
        addTransition(suggestionToCal)
        show(HotelParamsDefault())

        val mRootWindow = (context as Activity).window
        val mRootView = mRootWindow.decorView.findViewById(android.R.id.content)
        mRootView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val decorView = mRootWindow.decorView
                val windowVisibleDisplayFrameRect = Rect()
                decorView.getWindowVisibleDisplayFrame(windowVisibleDisplayFrameRect)
                var location = IntArray(2)
                anchor.getLocationOnScreen(location)
                val lp = suggestionRecyclerView.layoutParams
                val newHeight = windowVisibleDisplayFrameRect.bottom - windowVisibleDisplayFrameRect.top - (location[1] + anchor.height) + Ui.getStatusBarHeight(context)
                if (lp.height != newHeight) {
                    lp.height = newHeight
                    suggestionRecyclerView.layoutParams = lp
                }
                suggestionRecyclerView.translationY = (location[1] + anchor.height).toFloat()
            }
        })

        clearLocationButton.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                clearLocationButton.viewTreeObserver.removeOnGlobalLayoutListener(this)
                searchLocationEditText.setPadding(searchLocationEditText.paddingLeft, searchLocationEditText.paddingTop, clearLocationButton.width, searchLocationEditText.paddingBottom)
                searchLocationTextView.setPadding(searchLocationTextView.paddingLeft, searchLocationTextView.paddingTop, clearLocationButton.width, searchLocationTextView.paddingBottom)
            }
        })
    }

    var searchViewModel: HotelSearchViewModel by notNullAndObservable { vm ->
        suggestionRecyclerView.layoutManager = LinearLayoutManager(context)
        suggestionRecyclerView.addItemDecoration(RecyclerDividerDecoration(getContext(), 0, 0, 0, 0, 0, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25f, resources.displayMetrics).toInt(), false))
        val maxDate = LocalDate.now().plusDays(getResources().getInteger(R.integer.calendar_max_selectable_date_range))
        calendar.setSelectableDateRange(LocalDate.now(), maxDate)
        calendar.setMaxSelectableDateRange(resources.getInteger(R.integer.calendar_max_selectable_date_range))
        recentSearches.recentSearchesAdapterViewModel.recentSearchSelectedSubject.subscribe {
            searchViewModel.searchParamsObservable.onNext(it)
            vm.suggestionObserver.onNext(it.suggestion)
            traveler.viewmodel.travelerParamsObservable.onNext(TravelerParams(it.adults, it.children))
            for (index in 0..it.children.size -1) {
                val spinner = traveler.childSpinners[index]
                spinner.setSelection(it.children[index])
            }
            vm.datesObserver.onNext(Pair(it.checkIn, it.checkOut))
            calendar.setSelectedDates(it.checkIn, it.checkOut)
        }
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
            val showCalendarTooltip = (this.visibility == View.VISIBLE)
            val (top, bottom) = p
            calendar.setToolTipText(top, bottom, true, showCalendarTooltip)
        })

        if (vm.externalSearchParamsObservable.value) {
            postDelayed(Runnable {
                if (ExpediaBookingApp.isAutomation()) {
                    return@Runnable
                }
                searchLocationEditText.requestFocus()
                com.mobiata.android.util.Ui.showKeyboard(searchLocationEditText, null)
            }, 300)
        }

        vm.dateTextObservable.subscribeToggleButton(selectDate)

        selectDate.subscribeOnClick(vm.enableDateObserver)
        vm.enableDateObservable.subscribe { enable ->
            if (enable) {
                selectDate.setChecked(true)
                searchLocationEditText.clearFocus()
                calendar.setVisibility(View.VISIBLE)
                traveler.setVisibility(View.GONE)
                if (currentState == null) {
                    show(HotelParamsDefault())
                }
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
                selectTraveler.isChecked = true
                searchLocationEditText.clearFocus()
                calendar.visibility = View.GONE
                traveler.visibility = View.VISIBLE
                calendar.hideToolTip()
            } else {
                vm.errorNoOriginObservable.onNext(Unit)
            }
        }

        suggestionRecyclerView.adapter = hotelSuggestionAdapter

        searchLocationTextView.setOnClickListener {
            resetSuggestion()
            recentSearches.visibility = View.GONE
            searchLocationTextView.visibility = View.GONE
            searchLocationEditText.visibility = View.VISIBLE
            clearLocationButton.visibility = if (Strings.isEmpty(searchLocationEditText.text)) View.INVISIBLE else View.VISIBLE
            searchLocationEditText.requestFocus()
            searchLocationEditText.setSelection(searchLocationEditText.text.length)
            suggestionRecyclerView.visibility = View.VISIBLE
            if (currentState == null) {
                show(HotelParamsDefault())
            }
            show(HotelParamsSuggestion(), Presenter.FLAG_CLEAR_BACKSTACK)
            com.mobiata.android.util.Ui.showKeyboard(searchLocationEditText, null)
        }

        searchLocationEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {

            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                suggestionViewModel.queryObserver.onNext(s.toString())
                clearLocationButton.visibility = if (Strings.isEmpty(s) || !searchLocationEditText.hasFocus()) View.INVISIBLE else View.VISIBLE
            }
        })

        vm.locationTextObservable.subscribe {
            val text = if (it.equals(context.getString(R.string.current_location))) "" else it
            searchLocationEditText.setText(text)
            searchLocationTextView.text = it
        }

        clearLocationButton.setOnClickListener { view ->
            searchLocationEditText.text = null
            com.mobiata.android.util.Ui.showKeyboard(searchLocationEditText, null)
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
            if (currentState == null) {
                show(HotelParamsDefault())
            }
            show(HotelParamsSuggestion(), Presenter.FLAG_CLEAR_BACKSTACK)
            searchLocationEditText.requestFocus()
            com.mobiata.android.util.Ui.showKeyboard(searchLocationEditText, null)
            AnimUtils.doTheHarlemShake(searchLocationEditText)
        }
        vm.errorNoDatesObservable.subscribe {
            if (calendar.visibility == View.VISIBLE) {
                AnimUtils.doTheHarlemShake(calendar)
            } else {
                AnimUtils.doTheHarlemShake(selectDate)
            }
        }
        vm.errorMaxDatesObservable.subscribe {
            maxHotelStayDialog.show()
        }
        vm.searchParamsObservable.subscribe {
            calendar.hideToolTip()
        }

        vm.userBucketedObservable.subscribe {
            if (it) {
                recentSearches.recentSearchesAdapterViewModel.recentSearchesObserver.onNext(Unit)
            } else {
                showSuggestions()
            }
        }
    }


    init {
        View.inflate(context, R.layout.widget_hotel_search_params, this)
        traveler.viewmodel = HotelTravelerPickerViewModel(getContext(), false)
        recentSearches.recentSearchesAdapterViewModel = RecentSearchesAdapterViewModel(getContext())
        recentSearches.recentSearchesAdapterViewModel.recentSearchesObservable.subscribe { searchList ->
            if(searchList.isEmpty()) {
                showSuggestions()
            }else{
                searchLocationEditText.clearFocus()
                searchLocationEditText.visibility = View.INVISIBLE
                searchLocationTextView.visibility = View.VISIBLE
                suggestionRecyclerView.visibility = View.GONE
                recentSearches.visibility = View.VISIBLE
                calendar.visibility = View.GONE
            }
        }
        calendar.visibility = View.INVISIBLE
        traveler.visibility = View.GONE
        selectDate.isChecked = false
        selectTraveler.isChecked = false

        val statusBarHeight = Ui.getStatusBarHeight(getContext())
        if (statusBarHeight > 0) {
            val color = ContextCompat.getColor(context, R.color.hotels_primary_color)
            val statusBar = Ui.setUpStatusBar(getContext(), toolbar, searchContainer, color)
            addView(statusBar)
        }

        navIcon = ArrowXDrawableUtil.getNavigationIconDrawable(getContext(), ArrowXDrawableUtil.ArrowDrawableType.CLOSE)
        navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        toolbar.navigationIcon = navIcon
        toolbar.setNavigationOnClickListener {
            com.mobiata.android.util.Ui.hideKeyboard(this@HotelSearchPresenterV1)
            val activity = getContext() as AppCompatActivity
            activity.onBackPressed()
        }
        toolbar.inflateMenu(R.menu.cars_search_menu)

        searchContainer.getViewTreeObserver().addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                searchContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)
                searchParamsContainerHeight = searchParamsContainer.measuredHeight
            }
        })

        selectDate.typeface = FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR)
        selectTraveler.typeface = FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR)
        styleCalendar()
    }

    private fun showSuggestions() {
        searchLocationEditText.visibility = View.VISIBLE
        searchLocationTextView.visibility = View.GONE
        searchLocationEditText.requestFocus()
        com.mobiata.android.util.Ui.showKeyboard(searchLocationEditText, null)
        suggestionRecyclerView.visibility = View.VISIBLE
    }

    private val defaultToCal = object : Presenter.Transition(HotelParamsDefault::class.java, HotelParamsCalendar::class.java) {
        private var calendarHeight: Int = 0

        override fun startTransition(forward: Boolean) {
            com.mobiata.android.util.Ui.hideKeyboard(this@HotelSearchPresenterV1)
            if(forward) recentSearches.visibility = View.GONE
            suggestionRecyclerView.visibility = View.GONE
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
            calendar.translationY = (if (forward) 0 else calendarHeight).toFloat()
            if (forward) {
                searchLocationEditText.clearFocus()
                calendar.hideToolTip()
            }
            searchLocationEditText.visibility = View.INVISIBLE
            searchLocationTextView.visibility = View.VISIBLE
            calendar.visibility = View.VISIBLE
        }
    }

    private val suggestionToCal = object : Presenter.Transition(HotelParamsSuggestion::class.java, HotelParamsCalendar::class.java) {
        private var calendarHeight: Int = 0

        override fun startTransition(forward: Boolean) {
            suggestionRecyclerView.visibility = if (forward) View.GONE else View.VISIBLE
            if (forward) recentSearches.visibility = View.GONE
            val parentHeight = height
            calendarHeight = calendar.height
            val pos = (if (forward) parentHeight + calendarHeight else calendarHeight).toFloat()
            calendar.translationY = pos
            calendar.visibility = View.VISIBLE
            val hasOrigin = searchViewModel.originObservable.value
            searchLocationEditText.visibility = if (forward) View.INVISIBLE else View.VISIBLE
            searchLocationTextView.visibility = if (forward) View.VISIBLE else View.GONE
            if (forward && !hasOrigin) {
                searchViewModel.locationTextObservable.onNext("")
            }
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            val pos = if (forward) calendarHeight + (-f * calendarHeight) else (f * calendarHeight)
            calendar.translationY = pos
        }

        override fun endTransition(forward: Boolean) {
            calendar.translationY = (if (forward) 0 else calendarHeight).toFloat()
            if (forward) {
                com.mobiata.android.util.Ui.hideKeyboard(this@HotelSearchPresenterV1)
                searchLocationEditText.clearFocus()
                calendar.hideToolTip()
            }
            calendar.visibility = View.VISIBLE
        }
    }

    private val defaultToSuggestion = object : Presenter.Transition(HotelParamsDefault::class.java, HotelParamsSuggestion::class.java) {
        override fun startTransition(forward: Boolean) {
            suggestionRecyclerView.visibility = if (forward) View.VISIBLE else View.GONE
            searchLocationEditText.visibility = if (forward) View.VISIBLE else View.INVISIBLE
            searchLocationTextView.visibility = if (forward) View.GONE else View.VISIBLE
        }
    }

    var toolbarTitleTop = 0
    override fun animationStart(forward : Boolean) {
        recentSearches.translationY = (if (forward) recentSearches.height else 0).toFloat()
        calendar.translationY = (if (forward) calendar.height else 0).toFloat()
        traveler.translationY = (if (forward) traveler.height else 0).toFloat()
        searchContainer.setBackgroundColor(Color.TRANSPARENT)
        toolbarTitleTop = (toolbarTitle.bottom - toolbarTitle.top)/3
    }

    override fun animationUpdate(f : Float, forward : Boolean) {
        val translationCalendar = if (forward) calendar.height * (1 - f) else calendar.height * f
        val translationRecentSearches = if (forward) recentSearches.height * (1 - f) else recentSearches.height * f
        val layoutParams = searchParamsContainer.layoutParams
        // TODO: Fix searchParamsContainerHeight = 0 in case of two consecutive animations.
        if (searchParamsContainerHeight > 0) {
            layoutParams.height = if (forward) (f * (searchParamsContainerHeight)).toInt() else (Math.abs(f - 1) * (searchParamsContainerHeight)).toInt()
        }
        searchParamsContainer.layoutParams = layoutParams

        calendar.translationY = translationCalendar
        traveler.translationY = translationCalendar
        recentSearches.translationY = translationRecentSearches
        val factor: Float = if (forward) f else Math.abs(1 - f)
        toolbar.alpha = factor
        traveler.alpha = factor
        monthView.alpha = factor
        dayOfWeek.alpha = factor
        monthSelectionView.alpha = factor
        navIcon.parameter = factor

        toolbarTitle.translationY = (if (forward) Math.abs(1 - f) else f) * -toolbarTitleTop
    }

    override fun animationFinalize(forward: Boolean) {
        if (forward) {
            recentSearches.visibility = View.GONE
            searchViewModel.enableTravelerObservable.onNext(true)
            searchViewModel.enableDateObservable.onNext(true)
        }
        searchContainer.setBackgroundColor(Color.WHITE)
        navIcon.parameter = ArrowXDrawableUtil.ArrowDrawableType.CLOSE.getType().toFloat()
    }

    private fun resetSuggestion() {
        searchViewModel.suggestionTextChangedObserver.onNext(Unit)
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
}
