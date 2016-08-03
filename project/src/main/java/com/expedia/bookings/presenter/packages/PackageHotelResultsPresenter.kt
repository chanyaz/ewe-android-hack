package com.expedia.bookings.presenter.packages

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.presenter.hotel.BaseHotelResultsPresenter
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.widget.BaseHotelListAdapter
import com.expedia.bookings.widget.HotelMapCarouselAdapter
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.packages.PackageHotelListAdapter
import com.expedia.util.notNullAndObservable
import com.expedia.vm.HotelFilterViewModel
import com.expedia.vm.hotel.HotelResultsViewModel
import kotlin.properties.Delegates

class PackageHotelResultsPresenter(context: Context, attrs: AttributeSet) : BaseHotelResultsPresenter(context, attrs) {
    override val filterHeight by lazy { resources.getDimension(R.dimen.footer_button_height) }
    override val heightOfButton = 0
    var filterButtonText: TextView by Delegates.notNull()

    var viewmodel: HotelResultsViewModel by notNullAndObservable { vm ->
        vm.hotelResultsObservable.subscribe(listResultsObserver)
        vm.hotelResultsObservable.subscribe(mapViewModel.hotelResultsSubject)

        vm.titleSubject.subscribe {
            toolbarTitle.text = context.getString(R.string.package_hotel_results_toolbar_title_TEMPLATE, it)
        }

        vm.subtitleSubject.subscribe {
            toolbarSubtitle.text = it
        }

        vm.paramsSubject.subscribe { params ->
            setMapToInitialState(params.suggestion)
            showLoading()
            show(ResultsList())
            filterView.sortByObserver.onNext(params.suggestion.isCurrentLocationSearch && !params.suggestion.isGoogleSuggestionSearch)
            filterView.viewmodel.clearObservable.onNext(Unit)
        }

        mapViewModel.mapInitializedObservable.subscribe {
            setMapToInitialState(viewmodel.paramsSubject.value?.suggestion)
        }
    }

    override fun inflate() {
        View.inflate(context, R.layout.widget_package_hotel_results, this)
        toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.packages_primary_color))
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        recyclerView.viewTreeObserver.addOnGlobalLayoutListener(adapterListener)
        filterButtonText = filterMenuItem.actionView.findViewById(R.id.filter_text) as TextView
        filterButtonText.visibility = GONE
        filterMenuItem.isVisible = true
        filterView.lob = LineOfBusiness.PACKAGES
        filterBtn?.setOnClickListener { view ->
            showWithTracking(ResultsFilter())
            val isResults = currentState == ResultsList::class.java.name
            filterView.viewmodel.sortContainerObservable.onNext(isResults)
            filterView.toolbar.title = if (isResults) resources.getString(R.string.sort_and_filter) else resources.getString(R.string.filter)
        }
        (mapCarouselRecycler.adapter as HotelMapCarouselAdapter).setLob(LineOfBusiness.PACKAGES)
    }

    override fun getFilterViewModel(): HotelFilterViewModel {
        return HotelFilterViewModel(LineOfBusiness.PACKAGES)
    }

    override fun doAreaSearch() {
    }

    override fun hideSearchThisArea() {
    }

    override fun showSearchThisArea() {
    }

    override fun updateFilterButtonText(isResults: Boolean) {
        if (isResults) {
            filterButtonText.visibility = GONE
        } else {
            filterButtonText.visibility = VISIBLE
        }
    }

    override fun showMenuItem(isResults: Boolean) {
        filterMenuItem.isVisible = true
    }

    override fun hideBundlePriceOverview(hide: Boolean) {
        hideBundlePriceOverviewSubject.onNext(hide)
    }

    override fun trackSearchMap() {
        PackagesTracking().trackHotelMapLoad()
    }

    override fun trackMapToList() {
        PackagesTracking().trackHotelMapToList()
    }

    override fun trackCarouselScroll() {
        PackagesTracking().trackHotelMapCarouselScroll()
    }

    override fun trackMapPinTap() {
        PackagesTracking().trackHotelMapPinTap()
    }

    override fun trackFilterShown() {
        PackagesTracking().trackHotelFilterLoad()
    }

    override fun trackMapSearchAreaClick() {
        PackagesTracking().trackHotelMapSearchThisAreaClick()
    }

    override fun getHotelListAdapter(): BaseHotelListAdapter {
        return PackageHotelListAdapter(hotelSelectedSubject, headerClickedSubject)
    }
}
