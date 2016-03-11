package com.expedia.bookings.presenter.hotel

import android.animation.ArgbEvaluator
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Rect
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.text.Html
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
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
import com.expedia.bookings.location.CurrentLocationObservable
import com.expedia.bookings.tracking.HotelV2Tracking
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.SuggestionV4Utils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.CalendarWidgetV2
import com.expedia.bookings.widget.HotelSuggestionAdapter
import com.expedia.bookings.widget.RecyclerDividerDecoration
import com.expedia.bookings.widget.SearchInputCardView
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.TravelerWidgetV2
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeOnClick
import com.expedia.vm.HotelSearchViewModel
import com.expedia.vm.HotelSuggestionAdapterViewModel
import com.expedia.vm.HotelTravelerParams
import org.joda.time.LocalDate

class HotelSearchPresenterV2(context: Context, attrs: AttributeSet) : BaseHotelSearchPresenter(context, attrs) {
    val ANIMATION_DURATION = 200L
    val toolbar: Toolbar by bindView(R.id.search_v2_toolbar)
    val scrollView: ScrollView by bindView(R.id.scrollView)
    val searchContainer: ViewGroup by bindView(R.id.search_v2_container)

    val calendarWidgetV2: CalendarWidgetV2 by bindView(R.id.calendar_card)

    val suggestionContainer: View by bindView(R.id.suggestions_container)
    val suggestionRecyclerView: RecyclerView by bindView(R.id.suggestion_list)
    var navIcon: ArrowXDrawable
    val destinationCardView: SearchInputCardView by bindView(R.id.destination_card)
    val travelerWidgetV2: TravelerWidgetV2 by bindView(R.id.traveler_card)
    val searchButton: Button by bindView(R.id.search_button_v2)
    var searchLocationEditText: SearchView? = null
    val toolBarTitle: TextView by bindView(R.id.title)
    val statusBarHeight by lazy { Ui.getStatusBarHeight(context) }
    val mRootWindow by lazy { (context as Activity).window }
    val mRootView by lazy { mRootWindow.decorView.findViewById(android.R.id.content) }
    var firstLaunch = true

    override var searchViewModel: HotelSearchViewModel by notNullAndObservable { vm ->
        calendarWidgetV2.viewModel = vm
        travelerWidgetV2.hotelSearchViewModelSubject.onNext(vm)
        vm.searchButtonObservable.subscribe { enable ->
            searchButton.setTextColor(if (enable) ContextCompat.getColor(context, R.color.hotel_filter_spinner_dropdown_color) else ContextCompat.getColor(context, R.color.white_disabled))
        }

        vm.locationTextObservable.subscribe { locationText ->
            firstLaunch = false
            destinationCardView.setText(locationText)
            if (this.visibility == VISIBLE && vm.startDate() == null) {
                calendarWidgetV2.showCalendarDialog()
            }
        }
        vm.errorNoOriginObservable.subscribe {
            AnimUtils.doTheHarlemShake(destinationCardView)
        }

        vm.errorNoDatesObservable.subscribe {
            AnimUtils.doTheHarlemShake(calendarWidgetV2)
        }

        vm.errorMaxDatesObservable.subscribe {
            maxHotelStayDialog.show()
        }

        searchButton.subscribeOnClick(vm.searchObserver)

        calendarWidgetV2.setOnClickListener {
            calendarWidgetV2.showCalendarDialog()
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
    }

    override fun showSuggestionState() {
        navIcon = ArrowXDrawableUtil.getNavigationIconDrawable(context, ArrowXDrawableUtil.ArrowDrawableType.BACK)
        navIcon.setColorFilter(ContextCompat.getColor(context, R.color.search_suggestion_v2), PorterDuff.Mode.SRC_IN)
        toolbar.navigationIcon = navIcon
        show(SuggestionSelectionState())
    }

    val globalLayoutListener = (ViewTreeObserver.OnGlobalLayoutListener {
        val decorView = mRootWindow.decorView
        val windowVisibleDisplayFrameRect = Rect()
        decorView.getWindowVisibleDisplayFrame(windowVisibleDisplayFrameRect)
        var location = IntArray(2)
        searchLocationEditText?.getLocationOnScreen(location)
        val lp = suggestionRecyclerView.layoutParams
        val newHeight = windowVisibleDisplayFrameRect.bottom - windowVisibleDisplayFrameRect.top + statusBarHeight
        if (lp.height != newHeight) {
            lp.height = newHeight
            suggestionRecyclerView.layoutParams = lp
        }
    })

    override fun selectDates(startDate: LocalDate?, endDate: LocalDate?) {
        calendarWidgetV2.hideCalendarDialog()
    }

    override fun selectTravelers(hotelTravelerParams: HotelTravelerParams) {
        travelerWidgetV2.traveler.viewmodel.travelerParamsObservable.onNext(hotelTravelerParams)
        travelerWidgetV2.travelerDialog.dismiss()
    }

    internal var listener: SearchView.OnQueryTextListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String): Boolean {
            return false
        }

        override fun onQueryTextChange(query: String): Boolean {
            suggestionViewModel.queryObserver.onNext(query)
            return false
        }
    }

    init {
        View.inflate(context, R.layout.widget_hotel_search_params_v2, this)
        HotelV2Tracking().trackHotelV2SearchBox()
        val statusBarHeight = Ui.getStatusBarHeight(getContext())
        if (statusBarHeight > 0) {
            val color = ContextCompat.getColor(context, R.color.hotels_primary_color)
            val statusBar = Ui.setUpStatusBar(getContext(), toolbar, searchContainer, color)
            addView(statusBar)
        }
        val totalTopPadding = suggestionRecyclerView.paddingTop + statusBarHeight
        suggestionRecyclerView.setPadding(0, totalTopPadding, 0, 0)

        navIcon = ArrowXDrawableUtil.getNavigationIconDrawable(getContext(), ArrowXDrawableUtil.ArrowDrawableType.CLOSE)
        navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        toolbar.navigationIcon = navIcon
        toolbar.setNavigationOnClickListener {
            if (navIcon.parameter.toInt() == ArrowXDrawableUtil.ArrowDrawableType.BACK.type) {
                firstLaunch = false
                com.mobiata.android.util.Ui.hideKeyboard(this@HotelSearchPresenterV2)
                super.back()
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

        destinationCardView.setOnClickListener {
            show(SuggestionSelectionState())
        }

        travelerWidgetV2.setOnClickListener {
            travelerWidgetV2.travelerDialog.show()
        }
    }

    //TODO try to style search view in xml
    fun styleSearchView() {
        if (searchLocationEditText != null) {
            val searchEditText = searchLocationEditText?.findViewById(android.support.v7.appcompat.R.id.search_src_text) as EditText?
            searchEditText?.setTextColor(ContextCompat.getColor(context, R.color.search_suggestion_v2))
            searchEditText?.setHintTextColor(ContextCompat.getColor(context, R.color.search_suggestion_hint_v2))

            val searchPlate = searchLocationEditText?.findViewById(android.support.v7.appcompat.R.id.search_plate)
            searchPlate?.setBackgroundColor(android.R.color.transparent)

            val imgViewSearchView = searchLocationEditText?.findViewById(android.support.v7.appcompat.R.id.search_mag_icon) as ImageView?
            imgViewSearchView?.setImageResource(0)
            searchEditText?.hint = context.resources.getString(R.string.enter_destination_hint)

            val close = searchLocationEditText?.findViewById(android.support.v7.appcompat.R.id.search_close_btn) as ImageView?
            val drawable = ContextCompat.getDrawable(context, R.drawable.ic_close_white_24dp).mutate()
            drawable.setColorFilter(R.color.hotels_primary_color, PorterDuff.Mode.SRC_IN)
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

    private val selectionToSuggestionTransition = object : Transition(InputSelectionState::class.java, SuggestionSelectionState::class.java, AccelerateDecelerateInterpolator(), 300) {

        val bgFade = TransitionElement(0f, 1f)
        // Start with a large dummy value, and adjust it once we have an actual height
        var recyclerY = TransitionElement(-2000f, 0f)

        val recyclerStartTime = .33f
        // This is probably not even
        val eval: ArgbEvaluator = ArgbEvaluator()
        val decelInterp: DecelerateInterpolator = DecelerateInterpolator()
        val toolbarTextColor = TransitionElement(Color.WHITE, ContextCompat.getColor(context, R.color.search_suggestion_v2))
        val toolbarBgColor = TransitionElement(ContextCompat.getColor(context, R.color.hotels_primary_color), Color.WHITE)

        override fun startTransition(forward: Boolean) {

            recyclerY = TransitionElement(-(suggestionContainer.height.toFloat()), 0f)

            searchLocationEditText?.visibility = VISIBLE
            toolBarTitle.visibility = VISIBLE
            suggestionRecyclerView.visibility = VISIBLE
            suggestionContainer.visibility = VISIBLE

            // Toolbar color
            toolbar.setBackgroundColor(if (forward) toolbarBgColor.start else toolbarBgColor.end)
            toolBarTitle.alpha = TransitionElement.calculateStep(bgFade.end, bgFade.start, 0f)

            // Suggestion Fade In
            suggestionContainer.alpha = TransitionElement.calculateStep(bgFade.start, bgFade.end, 0f, forward)

            // Edit text fade in
            searchLocationEditText?.alpha = TransitionElement.calculateStep(bgFade.start, bgFade.end, 0f, forward)

            // RecyclerView vertical transition
            suggestionRecyclerView.translationY = recyclerY.start;

            if (forward) {
                applyAdapter()
            }

            searchButton.visibility = if (forward) GONE else VISIBLE
            if (!firstLaunch) {
                searchContainer.visibility = if (forward) VISIBLE else GONE
            }

            if (!firstLaunch && forward) {
                navIcon = ArrowXDrawableUtil.getNavigationIconDrawable(context, ArrowXDrawableUtil.ArrowDrawableType.CLOSE)
                navIcon.setColorFilter(toolbarTextColor.start, PorterDuff.Mode.SRC_IN)
            } else {
                navIcon = ArrowXDrawableUtil.getNavigationIconDrawable(context, ArrowXDrawableUtil.ArrowDrawableType.BACK)
                navIcon.setColorFilter(toolbarTextColor.end, PorterDuff.Mode.SRC_IN)
            }
            toolbar.navigationIcon = navIcon

            if (firstLaunch) {
                endTransition(forward)
            }
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            if (!firstLaunch) {
                super.updateTransition(f, forward)
                val progress = if (forward) f else 1f - f
                val currentToolbarTextColor = eval.evaluate(progress, toolbarTextColor.start, toolbarTextColor.end) as Int
                navIcon.setColorFilter(currentToolbarTextColor, PorterDuff.Mode.SRC_IN)

                // toolbar fading
                toolbar.setBackgroundColor(eval.evaluate(progress, toolbarBgColor.start, toolbarBgColor.end) as Int)

                //NavIcon
                navIcon.setColorFilter(currentToolbarTextColor, PorterDuff.Mode.SRC_IN)
                navIcon.parameter = 1f - progress

                //recycler bg
                suggestionContainer.alpha = TransitionElement.calculateStep(bgFade.start, bgFade.end, progress)
                searchLocationEditText?.alpha = TransitionElement.calculateStep(bgFade.start, bgFade.end, progress)

                toolBarTitle.alpha = TransitionElement.calculateStep(bgFade.end, bgFade.start, progress)

                // recycler movement - only moves during its portion of the animation
                if (forward && f > recyclerStartTime) {
                    suggestionRecyclerView.translationY = TransitionElement.calculateStep(recyclerY.start, recyclerY.end, decelInterp.getInterpolation(com.expedia.util.scaleValueToRange(recyclerStartTime, 1f, 0f, 1f, f)))
                } else if (!forward && progress > recyclerStartTime) {
                    suggestionRecyclerView.translationY = TransitionElement.calculateStep(recyclerY.start, recyclerY.end, com.expedia.util.scaleValueToRange(recyclerStartTime, 1f, 0f, 1f, progress))
                }
            }
        }

        override fun endTransition(forward: Boolean) {
            // Toolbar bg color
            toolbar.setBackgroundColor(if (forward) toolbarBgColor.end else toolbarBgColor.start)
            suggestionContainer.alpha = if (forward) bgFade.end else bgFade.start
            searchLocationEditText?.alpha = if (forward) bgFade.end else bgFade.start
            suggestionRecyclerView.translationY = if (forward) recyclerY.end else recyclerY.start
            suggestionRecyclerView.visibility = if (forward) VISIBLE else GONE

            searchContainer.visibility = if (forward) GONE else VISIBLE
            if (!forward) {
                searchLocationEditText?.visibility = GONE
                mRootView.viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
            } else {
                searchLocationEditText?.visibility = VISIBLE
                mRootView.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
            }

            toolBarTitle.visibility = if (forward) GONE else VISIBLE
        }
    }

    fun applyAdapter() {
        suggestionRecyclerView.adapter = hotelSuggestionAdapter
        searchLocationEditText?.requestFocus()
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).
                toggleSoftInput(InputMethodManager.SHOW_FORCED,
                        InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    private val hotelSuggestionAdapter by lazy {
        val service = Ui.getApplication(getContext()).hotelComponent().suggestionsService()
        suggestionViewModel = HotelSuggestionAdapterViewModel(getContext(), service, CurrentLocationObservable.create(getContext()), true, true)
        HotelSuggestionAdapter(suggestionViewModel)
    }

    var suggestionViewModel: HotelSuggestionAdapterViewModel by notNullAndObservable { vm ->
        searchLocationEditText?.setOnQueryTextListener(listener)
        vm.suggestionSelectedSubject.subscribe { suggestion ->
            com.mobiata.android.util.Ui.hideKeyboard(this)
            searchViewModel.suggestionObserver.onNext(suggestion)
            val suggestionName = Html.fromHtml(suggestion.regionNames.displayName).toString()
            destinationCardView.setText(suggestionName)
            searchLocationEditText?.setQuery(suggestionName, false)
            SuggestionV4Utils.saveSuggestionHistory(context, suggestion, SuggestionV4Utils.RECENT_HOTEL_SUGGESTIONS_FILE)
            showDefault()
        }
    }

    var toolbarTitleTop = 0

    override fun animationStart(forward: Boolean) {
        super.animationStart(forward)
        searchContainer.translationY = (if (forward) 0 else -searchContainer.height).toFloat()
        searchButton.translationY = (if (forward) 0 else searchButton.height).toFloat()
        toolbarTitleTop = (toolBarTitle.bottom - toolBarTitle.top) / 3
    }

    override fun animationUpdate(f: Float, forward: Boolean) {
        super.animationUpdate(f, forward)
        val translationSearchesCardContainer = (-searchContainer.height) * if (forward) (1 - f) else f
        searchContainer.translationY = translationSearchesCardContainer
        searchButton.translationY = (if (forward) searchButton.height * (1 - f) else searchButton.height * f).toFloat()
        val factor: Float = if (forward) f else Math.abs(1 - f)
        toolBarTitle.alpha = factor
        navIcon.parameter = factor
        this.alpha = factor
        toolBarTitle.translationY = (if (forward) Math.abs(1 - f) else f) * -toolbarTitleTop
    }

    override fun animationFinalize(forward: Boolean) {
        super.animationFinalize(forward)
        if (forward) {
            searchViewModel.enableTravelerObservable.onNext(true)
            searchViewModel.enableDateObservable.onNext(true)
        }
        navIcon.parameter = ArrowXDrawableUtil.ArrowDrawableType.CLOSE.type.toFloat()
    }

    override fun back(): Boolean {
        if (SuggestionSelectionState().javaClass.name == currentState) {
            firstLaunch = false
            return super.back()
        }
        return false
    }
}
