package com.expedia.bookings.rail.presenter

import android.content.Context
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.extensions.setAccessibilityHoverFocus
import com.expedia.bookings.extensions.subscribeOnClick
import com.expedia.bookings.extensions.subscribeText
import com.expedia.bookings.presenter.BaseTwoLocationSearchPresenter
import com.expedia.bookings.rail.widget.PositionObservableTabLayout
import com.expedia.bookings.rail.widget.RailSearchPagerAdapter
import com.expedia.bookings.rail.widget.RailSearchWidget
import com.expedia.bookings.rail.widget.RailTravelerWidgetV2
import com.expedia.bookings.services.SuggestionV4Services
import com.expedia.bookings.rail.widget.RailSuggestionAdapter
import com.expedia.bookings.utils.SuggestionV4Utils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.shared.SearchInputTextView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.BaseSearchViewModel
import com.expedia.vm.BaseSuggestionAdapterViewModel
import com.expedia.vm.rail.RailSearchViewModel
import com.expedia.vm.rail.RailSuggestionAdapterViewModel
import com.squareup.phrase.Phrase
import io.reactivex.Observable
import kotlin.properties.Delegates

class RailSearchPresenter(context: Context, attrs: AttributeSet) : BaseTwoLocationSearchPresenter(context, attrs) {
    override fun setUpStatusBar() {
        // Rail is smart and lets the system handle the status bar. Do nothing.
    }

    override fun getToolbarsHeight(): Int {
        return Ui.getToolbarSize(context)
    }

    private lateinit var originRailSuggestionAdapter: RailSuggestionAdapter
    private lateinit var destinationRailSuggestionAdapter: RailSuggestionAdapter
    private val searchWidget by bindView<RailSearchWidget>(R.id.rail_search_widget)

    override val tabs: PositionObservableTabLayout by bindView(R.id.tabs)
    override val viewpager: ViewPager by bindView<ViewPager>(R.id.viewpager)

    val suggestionServices: SuggestionV4Services by lazy {
        Ui.getApplication(context).railComponent().suggestionsService()
    }

    override fun getSuggestionHistoryFileName(): String {
        return SuggestionV4Utils.RECENT_RAIL_SUGGESTIONS_FILE
    }

    override fun getSuggestionViewModel(): BaseSuggestionAdapterViewModel {
        return if (isCustomerSelectingOrigin) originSuggestionViewModel else destinationSuggestionViewModel
    }

    override fun getSuggestionAdapter(): RecyclerView.Adapter<RecyclerView.ViewHolder> {
        return if (isCustomerSelectingOrigin) originRailSuggestionAdapter else destinationRailSuggestionAdapter
    }

    override fun getSearchViewModel(): BaseSearchViewModel {
        return searchViewModel
    }

    override val travelerWidgetV2 by lazy {
        findViewById<RailTravelerWidgetV2>(R.id.traveler_card)
    }
    override val originCardView: SearchInputTextView by lazy {
        searchWidget.locationWidget.originLocationText
    }
    override val destinationCardView: SearchInputTextView by lazy {
        searchWidget.locationWidget.destinationLocationText
    }

    var adapter by Delegates.notNull<RailSearchPagerAdapter>()

    var searchViewModel: RailSearchViewModel by notNullAndObservable { vm ->
        searchWidget.searchViewModel = vm
        searchViewModel.resetDatesAndTimes()
        travelerWidgetV2.travelersSubject.subscribe(vm.travelersObservable)
        travelerWidgetV2.traveler.getViewModel().travelerParamsObservable.subscribe { travelers ->
            val noOfTravelers = travelers.getTravelerCount()
            travelerWidgetV2.contentDescription = Phrase.from(context.resources.getQuantityString(R.plurals.search_travelers_cont_desc_TEMPLATE, noOfTravelers))
                    .put("travelers", noOfTravelers).format().toString()
        }

        // we dont want to do current location now - TODO future enhancement
        originSuggestionViewModel = RailSuggestionAdapterViewModel(context, suggestionServices, false, null)
        destinationSuggestionViewModel = RailSuggestionAdapterViewModel(context, suggestionServices, true, null)
        originRailSuggestionAdapter = RailSuggestionAdapter(originSuggestionViewModel)
        destinationRailSuggestionAdapter = RailSuggestionAdapter(destinationSuggestionViewModel)

        vm.formattedOriginObservable.subscribeText(originCardView)
        vm.formattedDestinationObservable.subscribeText(destinationCardView)
        vm.a11yFocusSelectDatesObservable.subscribe {
            searchWidget.calendarWidget.setAccessibilityHoverFocus()
        }

        searchViewModel.searchButtonObservable.subscribe { enable ->
            searchButton.setTextColor(if (enable) ContextCompat.getColor(context, R.color.white) else ContextCompat.getColor(context, R.color.white_disabled))
        }
        searchButton.subscribeOnClick(vm.searchObserver)

        Observable.merge(vm.errorMaxDurationObservable,
                vm.errorMaxRangeObservable,
                vm.errorOriginSameAsDestinationObservable,
                vm.errorInvalidCardsCountObservable)
                .subscribe { message ->
                    showErrorDialog(message)
                }
    }

    override fun inflate() {
        View.inflate(context, R.layout.widget_rail_search_params, this)
        toolBarTitle.text = context.resources.getText(R.string.trains)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        initializeToolbarTabs()
    }

    override fun getOriginSearchBoxPlaceholderText(): String {
        return context.resources.getString(R.string.rail_location_hint)
    }

    override fun getDestinationSearchBoxPlaceholderText(): String {
        return context.resources.getString(R.string.rail_location_hint)
    }

    override fun getLineOfBusiness(): LineOfBusiness {
        return LineOfBusiness.RAILS
    }

    private val railTabListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabReselected(tab: TabLayout.Tab?) {
            // do nothing
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
            // do nothing
        }

        override fun onTabSelected(tab: TabLayout.Tab) {
            val isRoundTripSearch = tab.position == 1
            handleRoundTripChanged(isRoundTripSearch)
        }
    }

    private fun initializeToolbarTabs() {
        adapter = RailSearchPagerAdapter(context)
        viewpager.adapter = adapter
        viewpager.overScrollMode = ViewPager.OVER_SCROLL_NEVER

        tabs.setupWithViewPager(viewpager)

        tabs.singleToReturnScrollObservable.subscribe {
            val view = viewpager.getChildAt(0)
            if (view != null) {
                view.translationX = Math.min(viewpager.scrollX.toFloat(), (view.measuredWidth * 1).toFloat())
            }
        }

        tabs.addOnTabSelectedListener(railTabListener)
    }

    fun cleanup() {
        tabs.removeOnTabSelectedListener(railTabListener)
    }

    private fun handleRoundTripChanged(roundTrip: Boolean) {
        searchViewModel.isRoundTripSearchObservable.onNext(roundTrip)
        searchViewModel.resetDatesAndTimes()
        if (roundTrip) {
            announceForAccessibility(context.getString(R.string.rail_tab_roundtrip_selected_announcement))
        } else {
            announceForAccessibility(context.getString(R.string.rail_tab_oneway_selected_announcement))
        }
    }
}
