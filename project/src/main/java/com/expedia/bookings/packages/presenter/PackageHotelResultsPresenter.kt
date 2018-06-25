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
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.extensions.subscribeContentDescription
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.hotel.vm.BaseHotelFilterViewModel
import com.expedia.bookings.packages.vm.PackageFilterViewModel
import com.expedia.bookings.packages.vm.PackageHotelResultsViewModel
import com.expedia.bookings.packages.widget.PackageHotelServerFilterView
import com.expedia.bookings.presenter.hotel.BaseHotelResultsPresenter
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isPackagesHSRPriceDisplayEnabled
import com.expedia.bookings.utils.isServerSideFilteringEnabledForPackages
import com.expedia.bookings.widget.BaseHotelFilterView
import com.expedia.bookings.widget.BaseHotelListAdapter
import com.expedia.bookings.widget.HotelClientFilterView
import com.expedia.bookings.hotel.widget.adapter.HotelMapCarouselAdapter
import com.expedia.bookings.packages.adapter.PackageHotelListAdapter
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.squareup.phrase.Phrase
import io.reactivex.Observer

class PackageHotelResultsPresenter(context: Context, attrs: AttributeSet) : BaseHotelResultsPresenter(context, attrs) {

    override fun getRecyclerYTranslation(): Float {
        return 0f
    }

    override val filterHeight by lazy { resources.getDimension(R.dimen.footer_button_height) }

    val filterCountObserver: Observer<Int> = endlessObserver { numberOfFilters ->
        floatingPill.setFilterCount(numberOfFilters)
    }

    @VisibleForTesting val mapPricePerPersonMessage: TextView by bindView(R.id.package_map_price_includes_text)
    @VisibleForTesting val mapPriceIncludesTaxesTopMessage: TextView by bindView(R.id.package_map_price_includes_texes_fees_text_top)
    @VisibleForTesting val mapPriceIncludesTaxesBottomMessage: TextView by bindView(R.id.package_map_price_includes_taxes_fees_text_bottom)

    var viewModel: PackageHotelResultsViewModel by notNullAndObservable { vm ->
        baseViewModel = vm
        vm.hotelResultsObservable.subscribe(listResultsObserver)
        vm.hotelResultsObservable.filter { shouldUsePill() }.subscribe {
            floatingPill.visibility = View.VISIBLE
        }
        vm.filterResultsObservable.subscribe(listResultsObserver)
        vm.filterResultsObservable.filter { shouldUsePill() }.subscribe {
            floatingPill.visibility = View.VISIBLE
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
            adapter.shopWithPoints = params.shopWithPoints

            moveMapToDestination(params.suggestion)
            resultsLoadingAnimation()
            params.filterOptions?.let { filterOptions ->
                filterViewModel.updatePresetOptions(filterOptions)
            }
            if (isNotFilterSearch(params)) {
                resetUserFilters()
                filterView.sortByObserver.onNext(params.suggestion.isCurrentLocationSearch && !params.suggestion.isGoogleSuggestionSearch)
            }
        }
        vm.filterSearchErrorResponseHandler.subscribe { resetUserFilters() }
    }

    fun resetUserFilters() {
        filterViewModel.clearObservable.onNext(Unit)
    }

    private fun isNotFilterSearch(params: HotelSearchParams): Boolean {
        return params.filterOptions?.isEmpty() == true
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

    override fun inflate() {
        View.inflate(context, R.layout.widget_package_hotel_results, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        recyclerView.viewTreeObserver.addOnGlobalLayoutListener(adapterListener)
        filterViewModel.priceRangeContainerVisibility.onNext(false)
        filterViewModel.filterCountObservable.subscribe(filterCountObserver)
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

    override fun trackMapLoad() {
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

    override fun getHotelMapCarouselAdapter(): HotelMapCarouselAdapter {
        return HotelMapCarouselAdapter(emptyList(), true)
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
        val bucketedToShowResultsCellOnMap = AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.HotelResultsCellOnMapCarousel)
        val topMessagingVisibility = if (isPackagesHSRPriceDisplayEnabled(context) && shouldShow) View.VISIBLE else View.GONE
        val bottomMessagingVisibility = if (bucketedToShowResultsCellOnMap || (isPackagesHSRPriceDisplayEnabled(context) && shouldShow)) View.GONE else View.VISIBLE
        mapPricePerPersonMessage.visibility = topMessagingVisibility
        mapPriceIncludesTaxesTopMessage.visibility = topMessagingVisibility
        mapPriceIncludesTaxesBottomMessage.visibility = bottomMessagingVisibility
    }
}
