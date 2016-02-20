package com.expedia.bookings.presenter.packages

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Money
import com.expedia.bookings.presenter.LeftToRightTransition
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.ScaleTransition
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.vm.BaseCheckoutViewModel
import com.expedia.vm.BundleOverviewViewModel
import com.expedia.vm.PackageCheckoutViewModel
import com.expedia.vm.PackageCreateTripViewModel
import com.squareup.phrase.Phrase
import java.math.BigDecimal
import javax.inject.Inject

public class PackagePresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    lateinit var packageServices: PackageServices
        @Inject set

    val searchPresenter: PackageSearchPresenter by bindView(R.id.widget_package_search_presenter)
    val bundlePresenter: BundleOverviewPresenter by bindView(R.id.widget_bundle_overview)
    val confirmationPresenter: PackageConfirmationPresenter by bindView(R.id.widget_package_confirmation)

    init {
        Ui.getApplication(getContext()).packageComponent().inject(this)
        View.inflate(context, R.layout.package_presenter, this)
        bundlePresenter.bundleWidget.viewModel = BundleOverviewViewModel(context, packageServices)
        bundlePresenter.checkoutPresenter.viewModel = BaseCheckoutViewModel(context)
        bundlePresenter.checkoutPresenter.createTripViewModel = PackageCreateTripViewModel(packageServices)
        bundlePresenter.checkoutPresenter.packageCheckoutViewModel = PackageCheckoutViewModel(context, packageServices)
        bundlePresenter.checkoutPresenter.createTripViewModel.bundleTotalPrice.subscribe(bundlePresenter.bundleWidget.bundleTotalPriceWidget.viewModel.setTextObservable)
        bundlePresenter.checkoutPresenter.createTripViewModel.tripResponseObservable.subscribe { trip ->
            bundlePresenter.bundleWidget.priceChangeWidget.viewmodel.originalPackagePrice.onNext(trip.oldPackageDetails?.pricing?.packageTotal)
            bundlePresenter.bundleWidget.priceChangeWidget.viewmodel.packagePrice.onNext(trip.packageDetails.pricing.packageTotal)
            bundlePresenter.bundleWidget.checkoutButton.visibility = VISIBLE
        }
        bundlePresenter.checkoutPresenter.createTripViewModel.tripResponseObservable.subscribe( bundlePresenter.checkoutPresenter.packageCheckoutViewModel.tripResponseObservable)
        bundlePresenter.checkoutPresenter.viewModel.lineOfBusiness.onNext(LineOfBusiness.PACKAGES)
        bundlePresenter.checkoutPresenter.paymentWidget.viewmodel.completeBillingInfo.subscribe(bundlePresenter.checkoutPresenter.viewModel.paymentCompleted)
        bundlePresenter.checkoutPresenter.createTripViewModel.tripResponseObservable.subscribe {
            bundlePresenter.showCheckoutHeaderImage()
        }
        bundlePresenter.checkoutPresenter.createTripViewModel.createTripBundleTotalObservable.subscribe(bundlePresenter.bundleWidget.viewModel.createTripObservable)
        bundlePresenter.checkoutPresenter.createTripViewModel.createTripBundleTotalObservable.subscribe { trip ->
            bundlePresenter.checkoutOverviewHeader.update(trip.packageDetails.hotel, width)
            bundlePresenter.bundleWidget.bundleTotalPriceWidget.packagebreakdown.viewmodel.newDataObservable.onNext(trip.packageDetails)
        }
        bundlePresenter.checkoutPresenter.packageCheckoutViewModel.checkoutResponse.subscribe {
            show(confirmationPresenter)
            confirmationPresenter.itinNumber.text = it.newTrip?.itineraryNumber
        }

        searchPresenter.searchViewModel.searchParamsObservable.subscribe {
            // Starting a new search clear previous selection
            Db.clearPackageSelection()
            show(bundlePresenter)
            bundlePresenter.show(BundleOverviewPresenter.BundleDefault(), FLAG_CLEAR_BACKSTACK)
        }
        searchPresenter.searchViewModel.searchParamsObservable.subscribe(bundlePresenter.bundleWidget.viewModel.hotelParamsObservable)
        bundlePresenter.bundleWidget.viewModel.toolbarTitleObservable.subscribe(bundlePresenter.toolbar.viewModel.toolbarTitle)
        bundlePresenter.bundleWidget.viewModel.toolbarSubtitleObservable.subscribe(bundlePresenter.toolbar.viewModel.toolbarSubtitle)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addDefaultTransition(defaultSearchTransition)
        addTransition(searchToBundle)
        addTransition(bundleToConfirmation)
        show(searchPresenter)
    }

    private val defaultSearchTransition = object : Presenter.DefaultTransition(PackageSearchPresenter::class.java.name) {
        override fun finalizeTransition(forward: Boolean) {
            searchPresenter.visibility = View.VISIBLE
        }
    }

    private val searchToBundle = LeftToRightTransition(this, PackageSearchPresenter::class.java, BundleOverviewPresenter::class.java)

    private val bundleToConfirmation = ScaleTransition(this, BundleOverviewPresenter::class.java, PackageConfirmationPresenter::class.java)
}
