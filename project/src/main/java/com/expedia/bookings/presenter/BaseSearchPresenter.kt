package com.expedia.bookings.presenter

import android.animation.ArgbEvaluator
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Rect
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.view.ViewTreeObserver
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ScrollView
import com.expedia.account.graphics.ArrowXDrawable
import com.expedia.bookings.R
import com.expedia.bookings.animation.TransitionElement
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.SearchSuggestion
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.TravelerParams
import com.expedia.bookings.launch.widget.LobToolbarWidget
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.CalendarShortDateRenderer
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.utils.SuggestionV4Utils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.CalendarWidgetV2
import com.expedia.bookings.widget.RecyclerDividerDecoration
import com.expedia.bookings.widget.ShopWithPointsWidget
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.TravelerWidgetV2
import com.expedia.bookings.widget.shared.SearchInputTextView
import com.expedia.util.notNullAndObservable
import com.expedia.util.updateVisibility
import com.expedia.vm.BaseSearchViewModel
import com.expedia.vm.SuggestionAdapterViewModel
import com.expedia.vm.launch.LobToolbarViewModel
import com.mobiata.android.time.widget.CalendarPicker
import com.mobiata.android.time.widget.DaysOfWeekView
import com.mobiata.android.time.widget.MonthView
import org.joda.time.LocalDate
import rx.Observer
import rx.subjects.PublishSubject
import java.util.concurrent.TimeUnit

abstract class BaseSearchPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

    private val SUGGESTION_TRANSITION_DURATION = 300
    private val STARTING_TRANSLATIONY = -2000f

    protected val travelerCardViewStub: ViewStub by bindView(R.id.traveler_stub)
    val swpWidgetStub: ViewStub by bindView(R.id.swp_stub)
    val ANIMATION_DURATION = 200L
    val toolbar: Toolbar by bindView(R.id.search_toolbar)
    val scrollView: ScrollView by bindView(R.id.scrollView)
    val searchContainer: ViewGroup by bindView(R.id.search_container)

    val calendarWidgetV2: CalendarWidgetV2 by bindView(R.id.calendar_card)

    val suggestionRecyclerView: RecyclerView by bindView(R.id.suggestion_list)
    var navIcon: ArrowXDrawable
    open val destinationCardView: SearchInputTextView by bindView(R.id.destination_card)
    open val travelerCardView: CardView by bindView(R.id.traveler_card_view)
    open val travelerWidgetV2 by lazy {
        travelerCardViewStub.inflate().findViewById(R.id.traveler_card) as TravelerWidgetV2
    }

    val searchButton: Button by bindView(R.id.search_btn)

    open var searchLocationEditText: SearchView? = null
    val toolBarTitle: TextView by bindView(R.id.title)
    lateinit var shopWithPointsWidget: ShopWithPointsWidget

    val statusBarHeight by lazy { Ui.getStatusBarHeight(context) }
    val mRootWindow by lazy { (context as Activity).window }
    val mRootView by lazy { mRootWindow.decorView.findViewById(android.R.id.content) }
    val primaryColor by lazy {
        val typedValue = TypedValue()
        val theme = context.theme
        theme.resolveAttribute(R.attr.primary_color, typedValue, true)
        typedValue.data
    }
    open val tabs: TabLayout by bindView(R.id.tabs)
    open val viewpager: ViewPager by bindView(R.id.viewpager)

    protected val lobToolbar: LobToolbarWidget by bindView(R.id.lob_toolbar)

    var firstLaunch = true
    var transitioningFromOriginToDestination = true
    var showFlightOneWayRoundTripOptions = false
    protected var isCustomerSelectingOrigin = false

    protected val suggestionListShownSubject = PublishSubject.create<Unit>()

    fun showErrorDialog(message: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.search_error)
        builder.setMessage(message)
        builder.setPositiveButton(context.getString(R.string.DONE)) { dialog, which -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.show()
    }

    protected var destinationSuggestionViewModel: SuggestionAdapterViewModel by notNullAndObservable { vm ->
        val suggestionSelectedObserver = suggestionSelectedObserver(getSearchViewModel().destinationLocationObserver)
        vm.suggestionSelectedSubject.subscribe(suggestionSelectedObserver)
        getSearchViewModel().formattedDestinationObservable
                .debounce(SUGGESTION_TRANSITION_DURATION.toLong() + 100L, TimeUnit.MILLISECONDS)
                .subscribe({ transitioningFromOriginToDestination = false })
    }

    protected fun suggestionSelectedObserver(observer: Observer<SuggestionV4>): (SearchSuggestion) -> Unit {
        return { searchSuggestion ->
            com.mobiata.android.util.Ui.hideKeyboard(this)
            val suggestion = searchSuggestion.suggestionV4
            observer.onNext(suggestion)
            SuggestionV4Utils.saveSuggestionHistory(context, suggestion, getSuggestionHistoryFileName(), shouldSaveSuggestionHierarchyChildInfo())
            val isOriginSelected = (observer == getSearchViewModel().originLocationObserver)
            if (isOriginSelected) {
                firstLaunch = false
            }
            showDefault()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addTransition(selectionToSuggestionTransition)
        addDefaultTransition(defaultTransition)
        showDefault()
        com.mobiata.android.util.Ui.hideKeyboard(this)
        scrollView.scrollTo(0, scrollView.top)
        searchButton.setTextColor(ContextCompat.getColor(context, R.color.white_disabled))
        destinationCardView.setOnClickListener(locationClickListener(isCustomerSelectingOrigin = false))
        searchLocationEditText?.setOnQueryTextListener(listener)
        AccessibilityUtil.delayFocusToToolbarNavigationIcon(toolbar, 300)
        if (AccessibilityUtil.isTalkBackEnabled(context)) {
            searchButton.isEnabled = false
        }

        setupLobToolbar()
    }

    protected fun locationClickListener(isCustomerSelectingOrigin: Boolean): (View) -> Unit {
        return {
            performLocationClick(isCustomerSelectingOrigin)
        }
    }

    open fun performLocationClick(isCustomerSelectingOrigin: Boolean) {
        suggestionRecyclerView.adapter = getSuggestionAdapter()
        searchLocationEditText?.setQuery("", true)
        this.isCustomerSelectingOrigin = isCustomerSelectingOrigin
        show(SuggestionSelectionState())
    }

    fun setNavIconContentDescription(isBack: Boolean) {
        if (isBack) {
            toolbar.setNavigationContentDescription(R.string.package_toolbar_back_to_search_cont_desc)
        } else {
            toolbar.setNavigationContentDescription(R.string.package_toolbar_close_cont_desc)
        }
    }

    open fun showSuggestionState(selectOrigin: Boolean) {
        searchLocationEditText?.queryHint = if (selectOrigin) getOriginSearchBoxPlaceholderText() else getDestinationSearchBoxPlaceholderText()
        searchLocationEditText?.setQuery("", true)
        this.isCustomerSelectingOrigin = selectOrigin
        navIcon = ArrowXDrawableUtil.getNavigationIconDrawable(context, ArrowXDrawableUtil.ArrowDrawableType.BACK)
        navIcon.setColorFilter(ContextCompat.getColor(context, R.color.search_suggestion_v2), PorterDuff.Mode.SRC_IN)
        toolbar.navigationIcon = navIcon
        setNavIconContentDescription(true)
        if (currentState == null) show(InputSelectionState())
        show(SuggestionSelectionState())
    }

    val globalLayoutListener = (ViewTreeObserver.OnPreDrawListener {
        val decorView = mRootWindow.decorView
        val windowVisibleDisplayFrameRect = Rect()
        decorView.getWindowVisibleDisplayFrame(windowVisibleDisplayFrameRect)
        val lp = suggestionRecyclerView.layoutParams
        val newHeight = windowVisibleDisplayFrameRect.bottom - getToolbarsHeight()
        if (lp.height != newHeight) {
            lp.height = newHeight
            suggestionRecyclerView.layoutParams = lp
        }
        true
    })

    open fun selectDates(startDate: LocalDate?, endDate: LocalDate?) {
        calendarWidgetV2.hideCalendarDialog()
    }

    fun selectTravelers(params: TravelerParams) {
        travelerWidgetV2.traveler.getViewModel().travelerParamsObservable.onNext(params)
        travelerWidgetV2.travelerDialog.dismiss()
    }

    internal var listener: SearchView.OnQueryTextListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String): Boolean {
            return false
        }

        override fun onQueryTextChange(query: String): Boolean {
            getSuggestionViewModel().queryObserver.onNext(query)
            return false
        }
    }

    init {
        inflate()
        setUpStatusBar()

        navIcon = ArrowXDrawableUtil.getNavigationIconDrawable(getContext(), ArrowXDrawableUtil.ArrowDrawableType.CLOSE)
        navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        toolbar.navigationIcon = navIcon
        setNavIconContentDescription(false)
        toolbar.setNavigationOnClickListener {
            if (navIcon.parameter.toInt() == ArrowXDrawableUtil.ArrowDrawableType.BACK.type) {
                firstLaunch = false
                com.mobiata.android.util.Ui.hideKeyboard(this@BaseSearchPresenter)
                if (AccessibilityUtil.isTalkBackEnabled(context)) {
                    toolbar.postDelayed(Runnable {
                        showDefault()
                    }, 900)
                } else {
                    showDefault()
                }
            } else {
                val activity = getContext() as AppCompatActivity
                activity.onBackPressed()
            }
        }

        searchLocationEditText = findViewById(R.id.toolbar_searchView) as SearchView?
        searchLocationEditText?.setIconifiedByDefault(false)
        searchLocationEditText?.visibility = GONE
        searchLocationEditText?.alpha = 0f
        styleSearchView()

        suggestionRecyclerView.layoutManager = LinearLayoutManager(context)
        suggestionRecyclerView.addItemDecoration(RecyclerDividerDecoration(getContext(), 0, 0, 0, 0, 0, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25f, resources.displayMetrics).toInt(), false))
    }

    //TODO try to style search view in xml
    fun styleSearchView() {
        if (searchLocationEditText != null) {
            val searchEditText = searchLocationEditText?.findViewById(android.support.v7.appcompat.R.id.search_src_text) as EditText?
            searchEditText?.setTextColor(ContextCompat.getColor(context, R.color.search_suggestion_v2))
            searchEditText?.setHintTextColor(ContextCompat.getColor(context, R.color.search_suggestion_hint_v2))
            searchEditText?.setAccessibilityDelegate(object : AccessibilityDelegate() {
                override fun onInitializeAccessibilityNodeInfo(host: View?, info: AccessibilityNodeInfo?) {
                    super.onInitializeAccessibilityNodeInfo(host, info)
                    info?.text = "${searchEditText.hint}, ${searchEditText.text}"
                }
            })

            val searchPlate = searchLocationEditText?.findViewById(android.support.v7.appcompat.R.id.search_plate)
            searchPlate?.setBackgroundColor(android.R.color.transparent)

            val imgViewSearchView = searchLocationEditText?.findViewById(android.support.v7.appcompat.R.id.search_mag_icon) as ImageView?
            imgViewSearchView?.setImageResource(0)

            val close = searchLocationEditText?.findViewById(android.support.v7.appcompat.R.id.search_close_btn) as ImageView?
            val drawable = ContextCompat.getDrawable(context, R.drawable.ic_close_white_24dp).mutate()
            drawable.setColorFilter(primaryColor, PorterDuff.Mode.SRC_IN)
            close?.setImageDrawable(drawable)
        }
    }

    class InputSelectionState
    class SuggestionSelectionState

    fun showDefault() {
        show(InputSelectionState(), FLAG_CLEAR_BACKSTACK)
    }

    private val defaultTransition = object : DefaultTransition(InputSelectionState::class.java.name) {

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
        }

        override fun endTransition(forward: Boolean) {
        }
    }

    /**
     * This is a watered down version of a higher ideal where instead of calculating each step, we define the start and
     * end, and allow the transition to do the work for us. Right now it's just a tuple that stores start and end
     * for us, and we run calculatestep on it.
     */

    private val selectionToSuggestionTransition = object : Transition(InputSelectionState::class.java, SuggestionSelectionState::class.java, AccelerateDecelerateInterpolator(), SUGGESTION_TRANSITION_DURATION) {

        val bgFade = TransitionElement(0f, 1f)
        // Start with a large dummy value, and adjust it once we have an actual height
        var recyclerY = TransitionElement(STARTING_TRANSLATIONY, Ui.toolbarSizeWithStatusBar(context).toFloat())
        val xScale = 0.25f
        val yScale = 0.25f

        val recyclerStartTime = .33f
        // This is probably not even
        val eval: ArgbEvaluator = ArgbEvaluator()
        val decelInterp: DecelerateInterpolator = DecelerateInterpolator()
        val toolbarTextColor = TransitionElement(Color.WHITE, ContextCompat.getColor(context, R.color.search_suggestion_v2))
        val toolbarBgColor = TransitionElement(primaryColor, Color.WHITE)

        override fun startTransition(forward: Boolean) {

            recyclerY = TransitionElement(-(suggestionRecyclerView.height.toFloat()), getToolbarsHeight().toFloat())

            searchLocationEditText?.visibility = VISIBLE
            toolBarTitle.visibility = VISIBLE

            suggestionRecyclerView.visibility = VISIBLE

            // Toolbar color
            toolbar.setBackgroundColor(if (forward) toolbarBgColor.start else toolbarBgColor.end)
            toolBarTitle.alpha = TransitionElement.calculateStep(bgFade.end, bgFade.start, 0f)

            // Suggestion Fade In
            suggestionRecyclerView.alpha = TransitionElement.calculateStep(bgFade.start, bgFade.end, 0f, forward)
            if (transitioningFromOriginToDestination && !firstLaunch) {
                // scale for origin to destination transition
                suggestionRecyclerView.scaleX = (if (forward) xScale else 1f)
                suggestionRecyclerView.scaleY = (if (forward) yScale else 1f)
            }

            // Edit text fade in
            searchLocationEditText?.alpha = TransitionElement.calculateStep(bgFade.start, bgFade.end, 0f, forward)

            // RecyclerView vertical transition
            if (!firstLaunch && !transitioningFromOriginToDestination) {
                recyclerY = TransitionElement(-(suggestionRecyclerView.height.toFloat()), getToolbarsHeight().toFloat())
                suggestionRecyclerView.translationY = recyclerY.start
            } else {
                suggestionRecyclerView.translationY = recyclerY.end
            }

            if (forward) {
                applyAdapter()
            }

            searchButton.visibility = if (forward) GONE else VISIBLE
            if (!firstLaunch) {
                searchContainer.visibility = if (forward || transitioningFromOriginToDestination) VISIBLE else GONE
            }

            if (!firstLaunch && forward) {
                navIcon = ArrowXDrawableUtil.getNavigationIconDrawable(context, ArrowXDrawableUtil.ArrowDrawableType.CLOSE)
                navIcon.setColorFilter(toolbarTextColor.start, PorterDuff.Mode.SRC_IN)
                setNavIconContentDescription(true)
            } else {
                navIcon = ArrowXDrawableUtil.getNavigationIconDrawable(context, ArrowXDrawableUtil.ArrowDrawableType.BACK)
                navIcon.setColorFilter(toolbarTextColor.end, PorterDuff.Mode.SRC_IN)
                if (firstLaunch) {
                    setNavIconContentDescription(true)
                } else {
                    setNavIconContentDescription(false)
                }
            }
            toolbar.navigationIcon = navIcon

            if (showFlightOneWayRoundTripOptions) {
                tabs.visibility = VISIBLE
                tabs.alpha = TransitionElement.calculateStep(bgFade.end, bgFade.start, 0f)
            }

            endTransition(forward)
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            if (!firstLaunch) {
                super.updateTransition(f, forward)

                val progress = if (forward) f else 1f - f
                val currentToolbarTextColor = eval.evaluate(progress, toolbarTextColor.start, toolbarTextColor.end) as Int

                // toolbar fading
                toolbar.setBackgroundColor(eval.evaluate(progress, toolbarBgColor.start, toolbarBgColor.end) as Int)

                //NavIcon
                navIcon.setColorFilter(currentToolbarTextColor, PorterDuff.Mode.SRC_IN)
                navIcon.parameter = 1f - progress

                searchLocationEditText?.alpha = TransitionElement.calculateStep(bgFade.start, bgFade.end, progress)
                toolBarTitle.alpha = TransitionElement.calculateStep(bgFade.end, bgFade.start, progress)

                suggestionRecyclerView.alpha = TransitionElement.calculateStep(bgFade.start, bgFade.end, progress)
                // recycler movement - only moves during its portion of the animation
                if (!transitioningFromOriginToDestination) {
                    if (forward && f > recyclerStartTime) {
                        suggestionRecyclerView.translationY = TransitionElement.calculateStep(recyclerY.start, recyclerY.end, decelInterp.getInterpolation(com.expedia.util.scaleValueToRange(recyclerStartTime, 1f, 0f, 1f, f)))
                    } else if (!forward && progress > recyclerStartTime) {
                        suggestionRecyclerView.translationY = TransitionElement.calculateStep(recyclerY.start, recyclerY.end, com.expedia.util.scaleValueToRange(recyclerStartTime, 1f, 0f, 1f, progress))
                    }
                } else {
                    // scale suggestion container between origin and destination suggestion views
                    suggestionRecyclerView.scaleX = (if (forward) (1 - (1 - xScale) * -(f - 1)) else (xScale + (1 - xScale) * -(f - 1)))
                    suggestionRecyclerView.scaleY = (if (forward) (1 - (1 - yScale) * -(f - 1)) else (yScale + (1 - yScale) * -(f - 1)))
                }

                if (showFlightOneWayRoundTripOptions) {
                    tabs.alpha = TransitionElement.calculateStep(bgFade.end, bgFade.start, progress)
                }
            }
        }

        override fun endTransition(forward: Boolean) {
            // Toolbar bg color
            toolbar.setBackgroundColor(if (forward) toolbarBgColor.end else toolbarBgColor.start)
            navIcon.setColorFilter(if (forward) toolbarTextColor.end else toolbarTextColor.start, PorterDuff.Mode.SRC_IN)
            suggestionRecyclerView.alpha = if (forward) bgFade.end else bgFade.start
            suggestionRecyclerView.scaleX = 1f
            suggestionRecyclerView.scaleY = 1f

            searchLocationEditText?.alpha = if (forward) bgFade.end else bgFade.start

            suggestionRecyclerView.translationY = if (forward) recyclerY.end else recyclerY.start
            suggestionRecyclerView.visibility = if (forward) VISIBLE else GONE

            searchContainer.visibility = if (forward) GONE else VISIBLE
            if (!forward) {
                navIcon.parameter = 1f
                toolBarTitle.alpha = TransitionElement.calculateStep(bgFade.end, bgFade.start, 0f)
                searchLocationEditText?.visibility = GONE
                if (showFlightOneWayRoundTripOptions) {
                    tabs.alpha = TransitionElement.calculateStep(bgFade.end, bgFade.start, 0f)
                }
                mRootView.viewTreeObserver.removeOnPreDrawListener(globalLayoutListener)
            } else {
                navIcon.parameter = 0f
                toolBarTitle.alpha = TransitionElement.calculateStep(bgFade.end, bgFade.start, 1f)
                searchLocationEditText?.visibility = VISIBLE
                mRootView.viewTreeObserver.addOnPreDrawListener(globalLayoutListener)
            }

            toolBarTitle.visibility = if (forward) GONE else VISIBLE
            if (showFlightOneWayRoundTripOptions) {
                tabs.visibility = if (forward) GONE else VISIBLE
            }
            if (!forward) {
                requestA11yFocus(isCustomerSelectingOrigin)
            } else if (forward || firstLaunch) {
                AccessibilityUtil.setFocusToToolbarNavigationIcon(toolbar)
                if (AccessibilityUtil.isTalkBackEnabled(context)) {
                    com.mobiata.android.util.Ui.hideKeyboard(this@BaseSearchPresenter)
                }
            }
            if (forward) suggestionListShownSubject.onNext(Unit)
        }
    }

    fun applyAdapter() {
        suggestionRecyclerView.adapter = getSuggestionAdapter()
        searchLocationEditText?.requestFocus()
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).
                toggleSoftInput(InputMethodManager.SHOW_FORCED,
                        InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    var toolbarTitleTop = 0

    fun animationStart(forward: Boolean) {
        searchContainer.translationY = (if (forward) 0 else -searchContainer.height).toFloat()
        searchButton.translationY = (if (forward) 0 else searchButton.height).toFloat()
        toolbarTitleTop = (toolBarTitle.bottom - toolBarTitle.top) / 3
    }

    fun animationUpdate(f: Float, forward: Boolean) {
        val translationSearchesCardContainer = (-searchContainer.height) * if (forward) (1 - f) else f
        searchContainer.translationY = translationSearchesCardContainer
        searchButton.translationY = (if (forward) searchButton.height * (1 - f) else searchButton.height * f).toFloat()
        val factor: Float = if (forward) f else Math.abs(1 - f)
        toolBarTitle.alpha = factor
        navIcon.parameter = factor
        this.alpha = factor
        toolBarTitle.translationY = (if (forward) Math.abs(1 - f) else f) * -toolbarTitleTop
    }

    fun animationFinalize(forward: Boolean) {
        navIcon.parameter = ArrowXDrawableUtil.ArrowDrawableType.CLOSE.type.toFloat()
    }

    open fun requestA11yFocus(isOrigin: Boolean) {
        destinationCardView.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED)
    }

    override fun back(): Boolean {
        if (SuggestionSelectionState().javaClass.name == currentState) {
            firstLaunch = false
            return super.back()
        }
        return false
    }

    companion object {
        @JvmStatic fun styleCalendar(context: Context, calendar: CalendarPicker, monthView: MonthView, dayOfWeek: DaysOfWeekView) {
            monthView.setTextEqualDatesColor(Color.WHITE)
            monthView.setMaxTextSize(context.resources.getDimension(R.dimen.car_calendar_month_view_max_text_size))
            dayOfWeek.setDayOfWeekRenderer(CalendarShortDateRenderer())

            calendar.setMonthHeaderTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR))
            dayOfWeek.setTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR))
            monthView.setDaysTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_LIGHT))
            monthView.setTodayTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_MEDIUM))
        }
    }

    open fun getToolbarsHeight(): Int {
        return Ui.toolbarSizeWithStatusBar(context);
    }

    open fun setUpStatusBar() {
        val statusBarHeight = Ui.getStatusBarHeight(getContext())
        if (statusBarHeight > 0) {
            val statusBar = Ui.setUpStatusBar(getContext(), toolbar, null, primaryColor)
            addView(statusBar)
        }
    }

    open fun shouldSaveSuggestionHierarchyChildInfo(): Boolean {
        return false
    }

    private fun setupLobToolbar() {
        lobToolbar.viewModel = LobToolbarViewModel(context, getLineOfBusiness())
        val newLaunchscreenEnabled = FeatureToggleUtil.isFeatureEnabled(context, R.string.preference_new_launchscreen_nav)
        lobToolbar.updateVisibility(newLaunchscreenEnabled)
        if (newLaunchscreenEnabled) {
            toolBarTitle.text = context.resources.getText(R.string.search_title)
        }
    }

    abstract fun inflate()
    abstract fun getSuggestionHistoryFileName(): String
    abstract fun getSuggestionViewModel(): SuggestionAdapterViewModel
    abstract fun getSearchViewModel(): BaseSearchViewModel
    abstract fun getSuggestionAdapter(): RecyclerView.Adapter<RecyclerView.ViewHolder>
    abstract fun getOriginSearchBoxPlaceholderText(): String
    abstract fun getDestinationSearchBoxPlaceholderText(): String
    abstract fun getLineOfBusiness(): LineOfBusiness
}
