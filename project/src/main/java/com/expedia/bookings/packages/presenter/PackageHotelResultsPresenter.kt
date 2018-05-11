package com.expedia.bookings.packages.presenter

import android.content.Context
import android.support.annotation.VisibleForTesting
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.extensions.subscribeContentDescription
import com.expedia.bookings.extensions.subscribeOnClick
import com.expedia.bookings.hotel.animation.transition.VerticalTranslateTransition
import com.expedia.bookings.hotel.vm.BaseHotelFilterViewModel
import com.expedia.bookings.hotel.widget.adapter.HotelMapCarouselAdapter
import com.expedia.bookings.packages.adapter.PackageHotelListAdapter
import com.expedia.bookings.packages.vm.PackageFilterViewModel
import com.expedia.bookings.packages.vm.PackageHotelResultsViewModel
import com.expedia.bookings.packages.widget.BundleTotalPriceTopWidget
import com.expedia.bookings.presenter.hotel.BaseHotelResultsPresenter
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isBreadcrumbsMoveBundleOverviewPackagesEnabled
import com.expedia.bookings.utils.isHideMiniMapOnResultBucketed
import com.expedia.bookings.utils.isPackagesHSRPriceDisplayEnabled
import com.expedia.bookings.utils.isServerSideFilteringEnabledForPackages
import com.expedia.bookings.widget.BaseHotelFilterView
import com.expedia.bookings.widget.BaseHotelListAdapter
import com.expedia.bookings.widget.FilterButtonWithCountWidget
import com.expedia.bookings.widget.HotelClientFilterView
import com.expedia.bookings.widget.PackageHotelServerFilterView
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.squareup.phrase.Phrase
import io.reactivex.Observer

class PackageHotelResultsPresenter(context: Context, attrs: AttributeSet) : BaseHotelResultsPresenter(context, attrs) {

    override fun getRecyclerYTranslation(): Float {
        if (isBreadcrumbsMoveBundleOverviewPackagesEnabled(context) && isHideMiniMapOnResultBucketed(context)) {
            return resources.getDimension(R.dimen.package_bundle_widget_height)
        } else return 0f
    }

    override val filterHeight by lazy { resources.getDimension(R.dimen.footer_button_height) }

    @VisibleForTesting val mapPricePerPersonMessage: TextView by bindView(R.id.package_map_price_includes_text)
    @VisibleForTesting val mapPriceIncludesTaxesTopMessage: TextView by bindView(R.id.package_map_price_includes_texes_fees_text_top)
    @VisibleForTesting val mapPriceIncludesTaxesBottomMessage: TextView by bindView(R.id.package_map_price_includes_taxes_fees_text_bottom)

    var viewModel: PackageHotelResultsViewModel by notNullAndObservable { vm ->
        baseViewModel = vm
        vm.hotelResultsObservable.subscribe(listResultsObserver)
        vm.filterResultsObservable.subscribe(listResultsObserver)
        if (isBreadcrumbsMoveBundleOverviewPackagesEnabled(context)) {
            val filterStub = findViewById<ViewStub>(R.id.filter_stub)
            val filterStubInflated = filterStub.inflate()
            val filterBtnWithCountWidget = filterStubInflated.findViewById<FilterButtonWithCountWidget>(R.id.sort_filter_button_container)
            filterBtnWithCountWidget.subscribeOnClick(filterButtonOnClickObservable)
            val filterCountObserver: Observer<Int> = endlessObserver { numberOfFilters ->
                filterBtnWithCountWidget.showNumberOfFilters(numberOfFilters)
            }
            filterViewModel.filterCountObservable.subscribe(filterCountObserver)
            sortFilterButtonTransition = VerticalTranslateTransition(filterBtnWithCountWidget, 0, filterHeight.toInt())
        }

        vm.titleSubject.subscribe {
            if (!Db.sharedInstance.packageParams.isChangePackageSearch()) {
                toolbarTitle.text = Phrase.from(context, R.string.package_hotel_results_toolbar_title_with_breadcrumbs_TEMPLATE)
                        .put("destination", it)
                        .format()
                        .toString()
            } else {
                toolbarTitle.text = context.getString(R.string.package_hotel_results_toolbar_title_TEMPLATE, it)
            }
        }

        vm.subtitleSubject.subscribe {
            toolbarSubtitle.text = it
        }
        vm.subtitleContDescSubject.subscribeContentDescription(toolbarSubtitle)

        vm.paramsSubject.subscribe { params ->
            (mapCarouselRecycler.adapter as HotelMapCarouselAdapter).shopWithPoints = params.shopWithPoints

            moveMapToDestination(params.suggestion)
            resultsLoadingAnimation()
            params.filterOptions?.let { filterOptions ->
                filterViewModel.updatePresetOptions(filterOptions)
            }
            if (isNotFilterSearch(params)) {
                filterViewModel.clearObservable.onNext(Unit)
                filterView.sortByObserver.onNext(params.suggestion.isCurrentLocationSearch && !params.suggestion.isGoogleSuggestionSearch)
            }
        }
    }

    private fun isNotFilterSearch(params: HotelSearchParams): Boolean {
        return params.filterOptions!!.isEmpty()
    }

    private fun resultsLoadingAnimation() {
        if (previousWasList) {
            showLoading()
            show(ResultsList(), FLAG_CLEAR_TOP)
        } else {
            showMapLoadingOverlay()
            show(ResultsMap(), FLAG_CLEAR_TOP)
            mapWidget.clearMarkers()
        }
    }

    val bundlePriceWidgetTop: BundleTotalPriceTopWidget by lazy {
        val viewStub = findViewById<ViewStub>(R.id.bundle_total_top_stub)
        viewStub.inflate() as BundleTotalPriceTopWidget
    }

    override fun inflate() {
        View.inflate(context, R.layout.widget_package_hotel_results, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        recyclerView.viewTreeObserver.addOnGlobalLayoutListener(adapterListener)
        (mapCarouselRecycler.adapter as HotelMapCarouselAdapter).setLob(LineOfBusiness.PACKAGES)
        filterViewModel.priceRangeContainerVisibility.onNext(false)
    }

    override fun inflateFilterView(viewStub: ViewStub): BaseHotelFilterView {
        if (isServerSideFilteringEnabledForPackages(context)) {
            viewStub.layoutResource = R.layout.package_hotel_server_filter_view_stub
            return viewStub.inflate() as PackageHotelServerFilterView
        } else {
            viewStub.layoutResource = R.layout.hotel_client_filter_stub
            return viewStub.inflate() as HotelClientFilterView
        }
    }

    override fun hideSearchThisArea() {
    }

    override fun hideBundlePriceOverview(hide: Boolean) {
        hideBundlePriceOverviewSubject.onNext(hide)
    }

    override fun shouldDisplayPriceOverview(): Boolean {
        return !isPackagesHSRPriceDisplayEnabled(context)
    }

    override fun trackSearchMap() {
        PackagesTracking().trackHotelMapLoad()
    }

    override fun trackMapToList() {
        PackagesTracking().trackHotelMapToList()
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
        return PackageHotelListAdapter(hotelSelectedSubject, headerClickedSubject, pricingHeaderSelectedSubject)
    }

    override fun getLineOfBusiness(): LineOfBusiness {
        return LineOfBusiness.PACKAGES
    }

    override fun createFilterViewModel(): BaseHotelFilterViewModel {
        return PackageFilterViewModel(context)
    }

    override fun getScrollListener(): BaseHotelResultsScrollListener {
        return BaseHotelResultsScrollListener()
    }

    override fun toggleMapDetailedPriceMessaging(shouldShow: Boolean) {
        val topMessagingVisibility = if (isPackagesHSRPriceDisplayEnabled(context) && shouldShow) View.VISIBLE else View.GONE
        val bottomMessagingVisibility = if (isPackagesHSRPriceDisplayEnabled(context) && shouldShow) View.GONE else View.VISIBLE
        mapPricePerPersonMessage.visibility = topMessagingVisibility
        mapPriceIncludesTaxesTopMessage.visibility = topMessagingVisibility
        mapPriceIncludesTaxesBottomMessage.visibility = bottomMessagingVisibility
    }
}
