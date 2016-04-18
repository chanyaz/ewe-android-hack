package com.expedia.bookings.presenter.packages

import android.animation.ArgbEvaluator
import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import com.expedia.bookings.R
import com.expedia.bookings.animation.TransitionElement
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.packages.PackageCheckoutResponse
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.presenter.BaseOverviewPresenter
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.ScaleTransition
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.CurrencyUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.vm.packages.BundleOverviewViewModel
import com.expedia.vm.packages.PackageCheckoutOverviewViewModel
import com.expedia.vm.packages.PackageCheckoutViewModel
import com.expedia.vm.packages.PackageConfirmationViewModel
import com.expedia.vm.packages.PackageCreateTripViewModel
import com.expedia.vm.packages.PackageErrorViewModel
import com.expedia.vm.packages.PackageSearchViewModel
import com.squareup.phrase.Phrase
import rx.android.schedulers.AndroidSchedulers
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PackagePresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    lateinit var packageServices: PackageServices
        @Inject set

    val searchPresenter: PackageSearchPresenter by bindView(R.id.widget_package_search_presenter)
    val bundlePresenter: PackageOverviewPresenter by bindView(R.id.widget_bundle_overview)
    val confirmationPresenter: PackageConfirmationPresenter by bindView(R.id.widget_package_confirmation)
    val errorPresenter: PackageErrorPresenter by bindView(R.id.widget_package_hotel_errors)

    private val DELAY_INVOKING_ERROR_OBSERVABLES_DOING_SHOW = 100L
    private val ANIMATION_DURATION = 400

    var expediaRewards: String? = null

    init {
        Ui.getApplication(getContext()).packageComponent().inject(this)
        View.inflate(context, R.layout.package_presenter, this)
        val checkoutPresenter = bundlePresenter.getCheckoutPresenter()

        searchPresenter.searchViewModel = PackageSearchViewModel(context)
        bundlePresenter.bundleWidget.viewModel = BundleOverviewViewModel(context, packageServices)
        checkoutPresenter.createTripViewModel = PackageCreateTripViewModel(packageServices)
        checkoutPresenter.checkoutViewModel = PackageCheckoutViewModel(context, packageServices)
        confirmationPresenter.viewModel = PackageConfirmationViewModel(context)
        errorPresenter.viewmodel = PackageErrorViewModel(context)

        checkoutPresenter.createTripViewModel.tripResponseObservable.subscribe { trip ->
            bundlePresenter.bundleOverviewHeader.toolbar.viewModel.showChangePackageMenuObservable.onNext(true)
            bundlePresenter.bundleWidget.outboundFlightWidget.toggleFlightWidget(1f, true)
            bundlePresenter.bundleWidget.inboundFlightWidget.toggleFlightWidget(1f, true)
            bundlePresenter.bundleWidget.bundleHotelWidget.toggleHotelWidget(1f, true)
            bundlePresenter.bundleWidget.toggleMenuObservable.onNext(true)
        }
        bundlePresenter.bundleWidget.viewModel.showBundleTotalObservable.subscribe { visible ->
            var packagePrice = Db.getPackageResponse().packageResult.currentSelectedOffer.price

            var packageSavings = Phrase.from(context, R.string.bundle_total_savings_TEMPLATE)
                    .put("savings", Money(BigDecimal(packagePrice.tripSavings.amount.toDouble()),
                            packagePrice.tripSavings.currencyCode).formattedMoney)
                    .format().toString()
            checkoutPresenter.totalPriceWidget.visibility = if (visible) View.VISIBLE else View.GONE
            checkoutPresenter.totalPriceWidget.viewModel.setTextObservable.onNext(Pair(Money(BigDecimal(packagePrice.packageTotalPrice.amount.toDouble()),
                    packagePrice.packageTotalPrice.currencyCode).formattedMoney, packageSavings))
        }
        //TODO:Move this checkout stuff into a common place not specific to package presenter
        checkoutPresenter.createTripViewModel.tripResponseObservable.subscribe { bundlePresenter.bundleOverviewHeader.toggleOverviewHeader(true) }
        checkoutPresenter.createTripViewModel.tripResponseObservable.subscribe {
            if (BaseOverviewPresenter.BundleDefault::class.java.name.equals(bundlePresenter.currentState)) {
                checkoutPresenter.toggleCheckoutButton(true)
            }
        }
        checkoutPresenter.createTripViewModel.tripResponseObservable.subscribe(checkoutPresenter.checkoutViewModel.tripResponseObservable)
        checkoutPresenter.paymentWidget.viewmodel.billingInfoAndStatusUpdate.map { it.first }.subscribe(checkoutPresenter.viewModel.paymentCompleted)
        checkoutPresenter.createTripViewModel.tripResponseObservable.subscribe(bundlePresenter.bundleWidget.viewModel.createTripObservable)
        checkoutPresenter.createTripViewModel.tripResponseObservable.subscribe((bundlePresenter.bundleOverviewHeader.checkoutOverviewFloatingToolbar.viewmodel as PackageCheckoutOverviewViewModel).tripResponse)
        checkoutPresenter.createTripViewModel.tripResponseObservable.subscribe((bundlePresenter.bundleOverviewHeader.checkoutOverviewHeaderToolbar.viewmodel as PackageCheckoutOverviewViewModel).tripResponse)
        checkoutPresenter.createTripViewModel.tripResponseObservable.subscribe { trip ->
            bundlePresenter.bundleWidget.setPadding(0, 0, 0, 0)
            expediaRewards = trip.rewards?.totalPointsToEarn?.toString() ?: "0.0"
        }
        checkoutPresenter.checkoutViewModel.checkoutResponse.subscribe { pair: Pair<PackageCheckoutResponse, String> ->
            show(confirmationPresenter)
            confirmationPresenter.viewModel.showConfirmation.onNext(Pair(pair.first.newTrip?.itineraryNumber, pair.second))
            confirmationPresenter.viewModel.setExpediaRewardsPoints.onNext(expediaRewards)
            PackagesTracking().trackCheckoutPaymentConfirmation()
        }

        // TODO - can we move this up to a common "base" presenter? (common between Package and Flight presenter)
        searchPresenter.searchViewModel.searchParamsObservable.subscribe {
            // Starting a new search clear previous selection
            Db.clearPackageSelection()
            errorPresenter.viewmodel.paramsSubject.onNext(it)
            show(bundlePresenter)
            bundlePresenter.show(BaseOverviewPresenter.BundleDefault(), FLAG_CLEAR_BACKSTACK)
        }
        searchPresenter.searchViewModel.searchParamsObservable.subscribe(bundlePresenter.bundleWidget.viewModel.hotelParamsObservable)
        bundlePresenter.bundleWidget.viewModel.toolbarTitleObservable.subscribe(bundlePresenter.bundleOverviewHeader.toolbar.viewModel.toolbarTitle)
        bundlePresenter.bundleWidget.viewModel.toolbarSubtitleObservable.subscribe(bundlePresenter.bundleOverviewHeader.toolbar.viewModel.toolbarSubtitle)
        bundlePresenter.bundleWidget.viewModel.errorObservable.subscribe(errorPresenter.viewmodel.searchApiErrorObserver)
        bundlePresenter.bundleWidget.viewModel.errorObservable.delay(DELAY_INVOKING_ERROR_OBSERVABLES_DOING_SHOW, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe { show(errorPresenter) }
        errorPresenter.viewmodel.defaultErrorObservable.delay(DELAY_INVOKING_ERROR_OBSERVABLES_DOING_SHOW, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe {
            show(searchPresenter, FLAG_CLEAR_TOP)
        }

        checkoutPresenter.createTripViewModel.createTripErrorObservable.subscribe(errorPresenter.viewmodel.checkoutApiErrorObserver)
        checkoutPresenter.checkoutViewModel.checkoutErrorObservable.subscribe(errorPresenter.viewmodel.checkoutApiErrorObserver)
        checkoutPresenter.createTripViewModel.createTripErrorObservable.delay(DELAY_INVOKING_ERROR_OBSERVABLES_DOING_SHOW, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe { show(errorPresenter) }
        checkoutPresenter.checkoutViewModel.checkoutErrorObservable.delay(DELAY_INVOKING_ERROR_OBSERVABLES_DOING_SHOW, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe { show(errorPresenter) }
        errorPresenter.viewmodel.checkoutUnknownErrorObservable.delay(DELAY_INVOKING_ERROR_OBSERVABLES_DOING_SHOW, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe {
            show(bundlePresenter, Presenter.FLAG_CLEAR_TOP)
            checkoutPresenter.slideToPurchase.resetSlider()
        }

        errorPresenter.viewmodel.checkoutTravelerErrorObservable.delay(DELAY_INVOKING_ERROR_OBSERVABLES_DOING_SHOW, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe {
            show(bundlePresenter, Presenter.FLAG_CLEAR_TOP)
            checkoutPresenter.slideToPurchase.resetSlider()
            checkoutPresenter.travelerPresenter.expandedSubject.onNext(true)
            checkoutPresenter.show(checkoutPresenter, Presenter.FLAG_CLEAR_TOP)
        }

        errorPresenter.viewmodel.checkoutCardErrorObservable.delay(DELAY_INVOKING_ERROR_OBSERVABLES_DOING_SHOW, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe {
            show(bundlePresenter, Presenter.FLAG_CLEAR_TOP)
            checkoutPresenter.slideToPurchase.resetSlider()
            checkoutPresenter.clearCCNumber()
            checkoutPresenter.paymentWidget.cardInfoContainer.performClick()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addDefaultTransition(defaultSearchTransition)
        addTransition(searchToBundle)
        addTransition(bundleToConfirmation)
        show(searchPresenter)
        addTransition(bundleOverviewToError)
        addTransition(errorToSearch)
    }

    private val defaultSearchTransition = object : Presenter.DefaultTransition(PackageSearchPresenter::class.java.name) {
        override fun endTransition(forward: Boolean) {
            searchPresenter.visibility = View.VISIBLE
            trackSearchPageLoad()
        }
    }

    val searchArgbEvaluator = ArgbEvaluator()
    val searchBackgroundColor = TransitionElement(ContextCompat.getColor(context, R.color.search_anim_background), Color.TRANSPARENT)

    private val searchToBundle = object : Transition(PackageSearchPresenter::class.java, PackageOverviewPresenter::class.java, AccelerateDecelerateInterpolator(), 500) {

        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            searchPresenter.visibility = View.VISIBLE
            bundlePresenter.visibility = View.VISIBLE
            searchPresenter.animationStart(!forward)
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

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
            searchPresenter.animationUpdate(f, !forward)
            if(forward) {
                searchPresenter.setBackgroundColor(searchArgbEvaluator.evaluate(f, searchBackgroundColor.start, searchBackgroundColor.end) as Int)
            } else {
                searchPresenter.setBackgroundColor(searchArgbEvaluator.evaluate(f, searchBackgroundColor.end, searchBackgroundColor.start) as Int)
            }
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            searchPresenter.setBackgroundColor(if (forward) searchBackgroundColor.end else searchBackgroundColor.start)
            searchPresenter.animationFinalize(forward)
            searchPresenter.visibility = if (forward) View.GONE else View.VISIBLE
            bundlePresenter.visibility = if (forward) View.VISIBLE else View.GONE
            if (!forward) {
                trackSearchPageLoad()
            }
        }
    }

    private val bundleToConfirmation = ScaleTransition(this, PackageOverviewPresenter::class.java, PackageConfirmationPresenter::class.java)

    private val bundleOverviewToError = object : Presenter.Transition(PackageOverviewPresenter::class.java, PackageErrorPresenter::class.java, DecelerateInterpolator(), ANIMATION_DURATION) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            errorPresenter.visibility = View.VISIBLE
            bundlePresenter.getCheckoutPresenter().checkoutDialog.hide()
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
            errorPresenter.animationUpdate(f, !forward)
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            bundlePresenter.visibility = if (forward) View.GONE else View.VISIBLE
            errorPresenter.visibility = if (forward) View.VISIBLE else View.GONE
            errorPresenter.animationFinalize()
        }
    }

    private val errorToSearch = object : Presenter.Transition(PackageErrorPresenter::class.java, PackageSearchPresenter::class.java, DecelerateInterpolator(), ANIMATION_DURATION) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            searchPresenter.visibility = View.VISIBLE
            searchPresenter.animationStart(forward)
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
            searchPresenter.animationUpdate(f, forward)
            if(forward) {
                searchPresenter.setBackgroundColor(searchArgbEvaluator.evaluate(f, searchBackgroundColor.start, searchBackgroundColor.end) as Int)
            } else {
                searchPresenter.setBackgroundColor(searchArgbEvaluator.evaluate(f, searchBackgroundColor.end, searchBackgroundColor.start) as Int)
            }
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            searchPresenter.setBackgroundColor(if (!forward) searchBackgroundColor.end else searchBackgroundColor.start)
            searchPresenter.animationFinalize(!forward)
            errorPresenter.visibility = if (forward) View.GONE else View.VISIBLE
            searchPresenter.visibility = if (forward) View.VISIBLE else View.GONE
            if (forward) {
                trackSearchPageLoad()
            }
        }
    }

    fun trackSearchPageLoad() {
        PackagesTracking().trackDestinationSearchInit()
    }
}
