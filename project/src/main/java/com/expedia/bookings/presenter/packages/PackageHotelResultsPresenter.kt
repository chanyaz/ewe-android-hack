package com.expedia.bookings.presenter.packages

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.presenter.hotel.BaseHotelResultsPresenter
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.HotelResultsViewModel
import kotlin.properties.Delegates

class PackageHotelResultsPresenter(context: Context, attrs: AttributeSet) : BaseHotelResultsPresenter(context, attrs) {
    val filterButton: LinearLayout by bindView(R.id.filter_button)
    var mapFilterPlaceholderImageView: ImageView by Delegates.notNull()
    var filterButtonText: TextView by Delegates.notNull()

    var viewmodel: HotelResultsViewModel by notNullAndObservable { vm ->
        vm.hotelResultsObservable.subscribe(listResultsObserver)
        vm.hotelResultsObservable.subscribe(mapViewModel.hotelResultsSubject)

        vm.titleSubject.subscribe {
            toolbar.title = context.getString(R.string.package_hotel_results_toolbar_title_TEMPLATE, it)
        }

        vm.subtitleSubject.subscribe {
            toolbar.subtitle = it
        }

        vm.paramsSubject.subscribe { params ->
            setMapToInitialState(params.suggestion)
            filterView.sortByObserver.onNext(params.suggestion.isCurrentLocationSearch && !params.suggestion.isGoogleSuggestionSearch)
            filterView.viewmodel.clearObservable.onNext(Unit)
        }

        mapViewModel.mapInitializedObservable.subscribe{
            PackagesTracking().trackHotelMapLoad()
            setMapToInitialState(viewmodel.paramsSubject.value?.suggestion)
        }

    }

    override fun inflate() {
        View.inflate(context, R.layout.widget_package_hotel_results, this)
        toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.packages_primary_color))
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        inflateAndSetupToolbarMenu()
        recyclerView.viewTreeObserver.addOnGlobalLayoutListener(adapterListener)
        filterMenuItem.isVisible = true
        filterButton.setOnClickListener { view ->
            showWithTracking(ResultsFilter())
            filterView.viewmodel.sortContainerObservable.onNext(currentState == ResultsList::class.java.name)
            filterView.toolbar.title = resources.getString(R.string.filter)
        }
    }

    private fun inflateAndSetupToolbarMenu() {
        val toolbarFilterItemActionView = LayoutInflater.from(context).inflate(R.layout.package_toolbar_filter_item, null) as LinearLayout
        mapFilterPlaceholderImageView = toolbarFilterItemActionView.findViewById(R.id.map_filter_placeholder_icon) as ImageView
        mapFilterPlaceholderImageView.setImageDrawable(filterPlaceholderIcon)
        filterButtonText = toolbarFilterItemActionView.findViewById(R.id.filter_text) as TextView
        toolbar.menu.findItem(R.id.menu_filter).actionView = toolbarFilterItemActionView
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

    override fun showMenuItem(isResults: Boolean){
        filterMenuItem.isVisible = true
        searchMenuItem.isVisible = isResults
    }

    override fun hideBundlePriceOverview(hide: Boolean) {
       hideBundlePriceOverviewSubject.onNext(hide)
    }

    override fun trackSearchMap() {
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

    }

    override fun trackMapSearchAreaClick() {
        PackagesTracking().trackHotelMapSearchThisAreaClick()
    }

}
