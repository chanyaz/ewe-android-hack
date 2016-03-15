package com.expedia.bookings.presenter.packages

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.packages.PackageCheckoutResponse
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.presenter.BaseOverviewPresenter
import com.expedia.bookings.presenter.LeftToRightTransition
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.ScaleTransition
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.utils.CurrencyUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.vm.BundleOverviewViewModel
import com.expedia.vm.PackageCheckoutViewModel
import com.expedia.vm.PackageConfirmationViewModel
import com.expedia.vm.PackageCreateTripViewModel
import com.squareup.phrase.Phrase
import java.math.BigDecimal
import javax.inject.Inject

class PackagePresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    lateinit var packageServices: PackageServices
        @Inject set

    val searchPresenter: PackageSearchPresenter by bindView(R.id.widget_package_search_presenter)
    val bundlePresenter: PackageOverviewPresenter by bindView(R.id.widget_bundle_overview)
    val confirmationPresenter: PackageConfirmationPresenter by bindView(R.id.widget_package_confirmation)
    var expediaRewards: String? = null

    init {
        Ui.getApplication(getContext()).packageComponent().inject(this)
        View.inflate(context, R.layout.package_presenter, this)
        bundlePresenter.bundleWidget.viewModel = BundleOverviewViewModel(context, packageServices)
        bundlePresenter.getCheckoutPresenter().createTripViewModel = PackageCreateTripViewModel(packageServices)
        bundlePresenter.getCheckoutPresenter().checkoutViewModel = PackageCheckoutViewModel(context, packageServices)
        confirmationPresenter.viewModel = PackageConfirmationViewModel(context)
        bundlePresenter.getCheckoutPresenter().createTripViewModel.tripResponseObservable.subscribe { trip ->
            bundlePresenter.bundleOverviewHeader.toolbar.viewModel.showChangePackageMenuObservable.onNext(true)
            bundlePresenter.bundleWidget.outboundFlightWidget.toggleFlightWidget(1f, true)
            bundlePresenter.bundleWidget.inboundFlightWidget.toggleFlightWidget(1f, true)
            bundlePresenter.bundleWidget.bundleHotelWidget.toggleHotelWidget(1f, true)
            bundlePresenter.bundleWidget.toggleMenuObservable.onNext(true)
            bundlePresenter.getCheckoutPresenter().toggleCheckoutButton(true)
        }
        bundlePresenter.bundleWidget.viewModel.showBundleTotalObservable.subscribe { visible ->
            var packagePrice = Db.getPackageResponse().packageResult.currentSelectedOffer.price

            var packageSavings = Phrase.from(context, R.string.bundle_total_savings_TEMPLATE)
                    .put("savings", Money(BigDecimal(packagePrice.tripSavings.amount.toDouble()),
                            packagePrice.tripSavings.currencyCode).formattedMoney)
                    .format().toString()
            bundlePresenter.getCheckoutPresenter().totalPriceWidget.visibility = if (visible) View.VISIBLE else View.GONE
            bundlePresenter.getCheckoutPresenter().totalPriceWidget.viewModel.setTextObservable.onNext(Pair(Money(BigDecimal(packagePrice.packageTotalPrice.amount.toDouble()),
                    packagePrice.packageTotalPrice.currencyCode).formattedMoney, packageSavings))
        }
        //TODO:Move this checkout stuff into a common place not specific to package presenter
        bundlePresenter.getCheckoutPresenter().createTripViewModel.tripResponseObservable.subscribe { bundlePresenter.bundleOverviewHeader.toggleOverviewHeader(true) }
        bundlePresenter.getCheckoutPresenter().createTripViewModel.tripResponseObservable.subscribe(bundlePresenter.getCheckoutPresenter().checkoutViewModel.tripResponseObservable)
        bundlePresenter.getCheckoutPresenter().paymentWidget.viewmodel.billingInfoAndStatusUpdate.map{it.first}.subscribe(bundlePresenter.getCheckoutPresenter().viewModel.paymentCompleted)
        bundlePresenter.getCheckoutPresenter().createTripViewModel.tripResponseObservable.subscribe(bundlePresenter.bundleWidget.viewModel.createTripObservable)
        bundlePresenter.getCheckoutPresenter().createTripViewModel.tripResponseObservable.subscribe { trip ->
            bundlePresenter.bundleOverviewHeader.checkoutOverviewFloatingToolbar.update(trip.packageDetails.hotel, bundlePresenter.bundleOverviewHeader.imageHeader, width)
            bundlePresenter.bundleOverviewHeader.checkoutOverviewHeaderToolbar.update(trip.packageDetails.hotel, bundlePresenter.bundleOverviewHeader.imageHeader, width)
            bundlePresenter.bundleWidget.setPadding(0, 0, 0, 0)
            expediaRewards = trip.expediaRewards.totalPointsToEarn.toString()
        }
        bundlePresenter.getCheckoutPresenter().checkoutViewModel.checkoutResponse.subscribe { pair: Pair<PackageCheckoutResponse, String> ->
            show(confirmationPresenter)
            confirmationPresenter.viewModel.showConfirmation.onNext(Pair(pair.first.newTrip?.itineraryNumber, pair.second))
            confirmationPresenter.viewModel.setExpediaRewardsPoints.onNext(expediaRewards)

        }

        searchPresenter.searchViewModel.searchParamsObservable.subscribe {
            // Starting a new search clear previous selection
            Db.clearPackageSelection()
            show(bundlePresenter)
            bundlePresenter.show(BaseOverviewPresenter.BundleDefault(), FLAG_CLEAR_BACKSTACK)
        }
        searchPresenter.searchViewModel.searchParamsObservable.subscribe(bundlePresenter.bundleWidget.viewModel.hotelParamsObservable)
        bundlePresenter.bundleWidget.viewModel.toolbarTitleObservable.subscribe(bundlePresenter.bundleOverviewHeader.toolbar.viewModel.toolbarTitle)
        bundlePresenter.bundleWidget.viewModel.toolbarSubtitleObservable.subscribe(bundlePresenter.bundleOverviewHeader.toolbar.viewModel.toolbarSubtitle)
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

    private val searchToBundle = object : LeftToRightTransition(this, PackageSearchPresenter::class.java, PackageOverviewPresenter::class.java) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            if (forward) {
                bundlePresenter.bundleOverviewHeader.checkoutOverviewHeaderToolbar.visibility = View.GONE
                bundlePresenter.bundleOverviewHeader.toggleOverviewHeader(false)
                bundlePresenter.getCheckoutPresenter().toggleCheckoutButton(false)
                var countryCode = PointOfSale.getPointOfSale().threeLetterCountryCode
                var currencyCode = CurrencyUtils.currencyForLocale(countryCode)
                bundlePresenter.getCheckoutPresenter().totalPriceWidget.visibility = View.VISIBLE
                bundlePresenter.getCheckoutPresenter().totalPriceWidget.viewModel.setTextObservable.onNext(Pair(Money(BigDecimal("0.00"), currencyCode).formattedMoney,
                        Phrase.from(context, R.string.bundle_total_savings_TEMPLATE)
                                .put("savings", Money(BigDecimal("0.00"), currencyCode).formattedMoney)
                                .format().toString()))
            }
        }
    }

    private val bundleToConfirmation = ScaleTransition(this, PackageOverviewPresenter::class.java, PackageConfirmationPresenter::class.java)
}
