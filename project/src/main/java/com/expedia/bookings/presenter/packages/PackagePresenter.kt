package com.expedia.bookings.presenter.packages

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.presenter.LeftToRightTransition
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.ScaleTransition
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.utils.CurrencyUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.vm.BaseCheckoutViewModel
import com.expedia.vm.BundleOverviewViewModel
import com.expedia.vm.PackageCheckoutViewModel
import com.expedia.vm.PackageCreateTripViewModel
import com.squareup.phrase.Phrase
import java.math.BigDecimal
import javax.inject.Inject

class PackagePresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
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
        bundlePresenter.checkoutPresenter.createTripViewModel.tripResponseObservable.subscribe { trip ->
            bundlePresenter.toolbar.viewModel.showChangePackageMenuObservable.onNext(true)
            bundlePresenter.bundleWidget.outboundFlightWidget.toggleFlightWidget(1f, true)
            bundlePresenter.bundleWidget.inboundFlightWidget.toggleFlightWidget(1f, true)
            bundlePresenter.bundleWidget.bundleHotelWidget.toggleHotelWidget(1f, true)
            bundlePresenter.bundleWidget.toggleMenuObservable.onNext(true)
            bundlePresenter.checkoutPresenter.toggleCheckoutButton(true)
        }
        bundlePresenter.bundleWidget.viewModel.showBundleTotalObservable.subscribe { visible ->
            var packagePrice = Db.getPackageResponse().packageResult.currentSelectedOffer.price

            var packageSavings = Phrase.from(context, R.string.bundle_total_savings_TEMPLATE)
                    .put("savings", Money(BigDecimal(packagePrice.tripSavings.amount.toDouble()),
                            packagePrice.tripSavings.currencyCode).formattedMoney)
                    .format().toString()
            bundlePresenter.checkoutPresenter.totalPriceWidget.visibility = if (visible) View.VISIBLE else View.GONE
            bundlePresenter.checkoutPresenter.totalPriceWidget.viewModel.setTextObservable.onNext(Pair(Money(BigDecimal(packagePrice.packageTotalPrice.amount.toDouble()),
                    packagePrice.packageTotalPrice.currencyCode).formattedMoney, packageSavings))
        }
        bundlePresenter.checkoutPresenter.createTripViewModel.tripResponseObservable.subscribe(bundlePresenter.checkoutPresenter.packageCheckoutViewModel.tripResponseObservable)
        bundlePresenter.checkoutPresenter.viewModel.lineOfBusiness.onNext(LineOfBusiness.PACKAGES)
        bundlePresenter.checkoutPresenter.paymentWidget.viewmodel.completeBillingInfo.subscribe(bundlePresenter.checkoutPresenter.viewModel.paymentCompleted)
        bundlePresenter.checkoutPresenter.createTripViewModel.tripResponseObservable.subscribe {
            bundlePresenter.toggleOverviewHeader(true)
        }
        bundlePresenter.checkoutPresenter.createTripViewModel.createTripBundleTotalObservable.subscribe(bundlePresenter.bundleWidget.viewModel.createTripObservable)
        bundlePresenter.checkoutPresenter.createTripViewModel.createTripBundleTotalObservable.subscribe { trip ->
            bundlePresenter.checkoutOverviewFloatingToolbar.update(trip.packageDetails.hotel, bundlePresenter.imageHeader, width)
            bundlePresenter.checkoutOverviewHeaderToolbar.update(trip.packageDetails.hotel, bundlePresenter.imageHeader, width)
            bundlePresenter.bundleWidget.setPadding(0, 0, 0, 0)
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
        override fun endTransition(forward: Boolean) {
            searchPresenter.visibility = View.VISIBLE
        }
    }

    private val searchToBundle = object : LeftToRightTransition(this, PackageSearchPresenter::class.java, BundleOverviewPresenter::class.java) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            if (forward) {
                bundlePresenter.checkoutOverviewHeaderToolbar.visibility = View.GONE
                bundlePresenter.toggleOverviewHeader(false)
                bundlePresenter.checkoutPresenter.toggleCheckoutButton(false)
                var countryCode = PointOfSale.getPointOfSale().threeLetterCountryCode
                var currencyCode = CurrencyUtils.currencyForLocale(countryCode)
                bundlePresenter.checkoutPresenter.totalPriceWidget.visibility = View.VISIBLE
                bundlePresenter.checkoutPresenter.totalPriceWidget.viewModel.setTextObservable.onNext(Pair(Money(BigDecimal("0.00"), currencyCode).formattedMoney,
                        Phrase.from(context, R.string.bundle_total_savings_TEMPLATE)
                                .put("savings", Money(BigDecimal("0.00"), currencyCode).formattedMoney)
                                .format().toString()))
            }
        }
    }

    private val bundleToConfirmation = ScaleTransition(this, BundleOverviewPresenter::class.java, PackageConfirmationPresenter::class.java)
}
