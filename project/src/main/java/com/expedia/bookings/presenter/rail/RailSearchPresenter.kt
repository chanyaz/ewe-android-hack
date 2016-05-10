package com.expedia.bookings.presenter.rail

import android.content.Context
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
import com.expedia.bookings.widget.SearchInputCardView
import com.expedia.bookings.widget.TravelerWidgetV2
import com.expedia.bookings.widget.rail.PositionObservableTabLayout
import com.expedia.bookings.widget.suggestions.RailSuggestionAdapter
import com.expedia.util.notNullAndObservable
import com.expedia.vm.BaseSearchViewModel
import com.expedia.vm.SuggestionAdapterViewModel
import com.expedia.vm.rail.RailSearchViewModel
import com.expedia.vm.rail.RailSuggestionAdapterViewModel
import org.joda.time.LocalDate
import kotlin.properties.Delegates

class RailSearchPresenter(context: Context, attrs: AttributeSet) : BaseTwoLocationSearchPresenter(context, attrs) {

    lateinit private var originSuggestionAdapter: RailSuggestionAdapter
    lateinit private var destinationSuggestionAdapter: RailSuggestionAdapter

    val suggestionServices: SuggestionV4Services by lazy {
        Ui.getApplication(context).railComponent().suggestionsService()
    }

    override fun getSuggestionHistoryFileName(): String {
        return SuggestionV4Utils.RECENT_RAIL_SUGGESTIONS_FILE
    }

    override fun inflate() {
        View.inflate(context, R.layout.widget_rail_search_params, this)
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

    val tabs: PositionObservableTabLayout by bindView(R.id.tabs)
    val viewpager: ViewPager by bindView<ViewPager>(R.id.viewpager)
    override val travelerWidgetV2: TravelerWidgetV2 by lazy {
        adapter.searchWidget.travelerWidget
    }
    override val originCardView: SearchInputCardView by lazy {
        adapter.searchWidget.locationWidget.originLocationText
    }
    override val destinationCardView: SearchInputCardView by lazy {
        adapter.searchWidget.locationWidget.destinationLocationText
    }

    var adapter by Delegates.notNull<RailSearchPagerAdapter>()

    var searchViewModel: RailSearchViewModel by notNullAndObservable {
        adapter.searchViewModel = it

        originSuggestionViewModel = RailSuggestionAdapterViewModel(context, suggestionServices, false, CurrentLocationObservable.create(context))
        destinationSuggestionViewModel = RailSuggestionAdapterViewModel(context, suggestionServices, true, null)
        originSuggestionAdapter = RailSuggestionAdapter(originSuggestionViewModel)
        destinationSuggestionAdapter = RailSuggestionAdapter(destinationSuggestionViewModel)
    }

    override fun selectDates(startDate: LocalDate?, endDate: LocalDate?) {
        //no-op
    }

    init {
        Ui.getApplication(context).railComponent().inject(this)
        val statusBarHeight = Ui.getStatusBarHeight(context)
        if (statusBarHeight > 0) {
            val color = ContextCompat.getColor(context, R.color.rail_primary_color)
            val statusBar = Ui.setUpStatusBarWithTabs(context, searchContainer, color)
            addView(statusBar)
        }

        adapter = RailSearchPagerAdapter(context)
        viewpager.adapter = adapter
        viewpager.offscreenPageLimit = 2 //make sure that the first tab is always in the viewpager - we animate that one
        tabs.setupWithViewPager(viewpager)

        tabs.singleToReturnScrollObservable.subscribe({
            //todo - animate calendar card 0->1 for single date vs 2 dates
        })

        tabs.returnToOpenReturnScrollObservable.subscribe({
            //todo - animate calendar card 0->1 for 2 dates vs "open return"
        })

        searchButton.setOnClickListener({
            searchViewModel.searchObserver.onNext(Unit)
        })

        tabs.singleToReturnScrollObservable.subscribe() {
            val view = viewpager.getChildAt(0);
            if (view != null) {
                view.translationX = viewpager.scrollX.toFloat()
            }
        }

        tabs.returnToOpenReturnScrollObservable.subscribe() {
            val view = viewpager.getChildAt(0);
            if (view != null) {
                view.translationX = Math.min(viewpager.scrollX.toFloat(), (view.measuredWidth * 2).toFloat())
            }
        }
    }
}