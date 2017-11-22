package com.expedia.bookings.presenter.packages

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.packages.PackageResponseStore
import com.expedia.bookings.hotel.vm.PackageHotelResultsViewModel
import com.expedia.bookings.presenter.hotel.BaseHotelResultsPresenter
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.isBreadcrumbsPackagesEnabled
import com.expedia.bookings.widget.BaseHotelFilterView
import com.expedia.bookings.widget.BaseHotelListAdapter
import com.expedia.bookings.widget.HotelClientFilterView
import com.expedia.bookings.widget.HotelMapCarouselAdapter
import com.expedia.bookings.widget.packages.PackageHotelListAdapter
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeContentDescription
import com.expedia.vm.PackageFilterViewModel
import com.expedia.vm.hotel.BaseHotelFilterViewModel
import com.squareup.phrase.Phrase

class PackageHotelResultsPresenter(context: Context, attrs: AttributeSet) : BaseHotelResultsPresenter(context, attrs) {
    override val filterHeight by lazy { resources.getDimension(R.dimen.footer_button_height) }

    var viewModel: PackageHotelResultsViewModel by notNullAndObservable { vm ->
        baseViewModel = vm
        vm.hotelResultsObservable.subscribe(listResultsObserver)
        vm.hotelResultsObservable.subscribe(mapViewModel.hotelResultsSubject)

        vm.titleSubject.subscribe {
            if (shouldShowBreadcrumbsInToolbarTitle()) {
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

            setMapToInitialState(params.suggestion)
            showLoading()
            show(ResultsList())

            filterView.sortByObserver.onNext(params.suggestion.isCurrentLocationSearch && !params.suggestion.isGoogleSuggestionSearch)
            filterView.viewModel.clearObservable.onNext(Unit)
        }

        mapViewModel.mapInitializedObservable.subscribe {
            setMapToInitialState(PackageResponseStore.packageParams?.destination)
        }
    }

    private fun shouldShowBreadcrumbsInToolbarTitle(): Boolean {
        return (isBreadcrumbsPackagesEnabled(context) && !PackageResponseStore.packageParams.isChangePackageSearch())
    }

    override fun inflate() {
        View.inflate(context, R.layout.widget_package_hotel_results, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        recyclerView.viewTreeObserver.addOnGlobalLayoutListener(adapterListener)
        (mapCarouselRecycler.adapter as HotelMapCarouselAdapter).setLob(LineOfBusiness.PACKAGES)
        filterView.viewModel.priceRangeContainerVisibility.onNext(false)
    }

    override fun inflateFilterView(viewStub: ViewStub): BaseHotelFilterView {
        viewStub.layoutResource = R.layout.hotel_client_filter_stub
        return viewStub.inflate() as HotelClientFilterView
    }

    override fun doAreaSearch() {
    }

    override fun hideSearchThisArea() {
    }

    override fun showSearchThisArea() {
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
}
