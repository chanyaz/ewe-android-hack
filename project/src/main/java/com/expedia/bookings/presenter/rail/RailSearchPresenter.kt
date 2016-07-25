package com.expedia.bookings.presenter.rail

import android.content.Context
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.adapter.RailSearchPagerAdapter
import com.expedia.bookings.location.CurrentLocationObservable
import com.expedia.bookings.presenter.BaseTwoLocationSearchPresenter
import com.expedia.bookings.services.SuggestionV4Services
import com.expedia.bookings.utils.SuggestionV4Utils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TravelerWidgetV2
import com.expedia.bookings.widget.rail.PositionObservableTabLayout
import com.expedia.bookings.widget.shared.SearchInputTextView
import com.expedia.bookings.widget.suggestions.SuggestionAdapter
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeOnClick
import com.expedia.util.subscribeText
import com.expedia.vm.BaseSearchViewModel
import com.expedia.vm.SuggestionAdapterViewModel
import com.expedia.vm.rail.RailSearchViewModel
import com.expedia.vm.rail.RailSuggestionAdapterViewModel
import kotlin.properties.Delegates

class RailSearchPresenter(context: Context, attrs: AttributeSet) : BaseTwoLocationSearchPresenter(context, attrs) {

    lateinit private var originSuggestionAdapter: SuggestionAdapter
    lateinit private var destinationSuggestionAdapter: SuggestionAdapter
    private val searchWidget by bindView<RailSearchWidget>(R.id.rail_search_widget)

    override val tabs: PositionObservableTabLayout by bindView(R.id.tabs)
    override val viewpager: ViewPager by bindView<ViewPager>(R.id.viewpager)

    val suggestionServices: SuggestionV4Services by lazy {
        Ui.getApplication(context).railComponent().suggestionsService()
    }

    override fun getSuggestionHistoryFileName(): String {
        return SuggestionV4Utils.RECENT_RAIL_SUGGESTIONS_FILE
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

    override val travelerWidgetV2: TravelerWidgetV2 by lazy {
        searchWidget.travelerWidget
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

        // we dont want to do current location now - TODO future enhancement
        originSuggestionViewModel = RailSuggestionAdapterViewModel(context, suggestionServices, false, null)
        destinationSuggestionViewModel = RailSuggestionAdapterViewModel(context, suggestionServices, true, null)
        originSuggestionAdapter = SuggestionAdapter(originSuggestionViewModel)
        destinationSuggestionAdapter = SuggestionAdapter(destinationSuggestionViewModel)

        vm.formattedOriginObservable.subscribeText(originCardView)
        vm.formattedDestinationObservable.subscribeText(destinationCardView)

        searchViewModel.searchButtonObservable.subscribe { enable ->
            searchButton.setTextColor(if (enable) ContextCompat.getColor(context, R.color.white) else ContextCompat.getColor(context, R.color.white_disabled))
        }
        searchButton.subscribeOnClick(vm.searchObserver)

        vm.errorMaxDurationObservable.subscribe { message ->
            showErrorDialog(message)
        }
        vm.errorMaxRangeObservable.subscribe { message ->
            showErrorDialog(message)
        }
        vm.errorOriginSameAsDestinationObservable.subscribe { message ->
            showErrorDialog(message)
        }
    }

    init {
        Ui.getApplication(context).railComponent().inject(this)
    }

    override fun inflate() {
        View.inflate(context, R.layout.widget_rail_search_params, this)
        toolBarTitle.text = context.resources.getText(R.string.trains)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        val statusBarHeight = Ui.getStatusBarHeight(context)
        if (statusBarHeight > 0) {
            val color = ContextCompat.getColor(context, R.color.rail_primary_color)
            val statusBar = Ui.setUpStatusBarWithTabs(context, null, color)
            addView(statusBar)
        }

        adapter = RailSearchPagerAdapter(context)
        viewpager.adapter = adapter
        viewpager.overScrollMode = ViewPager.OVER_SCROLL_NEVER

        tabs.setupWithViewPager(viewpager)

        tabs.singleToReturnScrollObservable.subscribe() {
         val view = viewpager.getChildAt(0);
           if (view != null) {
               view.translationX = Math.min(viewpager.scrollX.toFloat(), (view.measuredWidth * 1).toFloat())
           }
        }

        tabs.setOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
                // do nothing
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // do nothing
            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                val isRoundTripSearch = tab.position == 1
                searchViewModel.isRoundTripSearchObservable.onNext(isRoundTripSearch)
                searchViewModel.resetDatesAndTimes()
            }
        })
    }

    override fun getOriginSearchBoxPlaceholderText(): String {
        return context.resources.getString(R.string.rail_location_hint)
    }

    override fun getDestinationSearchBoxPlaceholderText(): String {
        return context.resources.getString(R.string.rail_location_hint)
    }
}