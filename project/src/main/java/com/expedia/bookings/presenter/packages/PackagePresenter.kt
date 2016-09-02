package com.expedia.bookings.presenter.packages

import android.animation.ArgbEvaluator
import android.content.Context
import android.graphics.Color
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import com.expedia.bookings.R
import com.expedia.bookings.animation.TransitionElement
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.BaseApiResponse
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.User
import com.expedia.bookings.data.packages.PackageCheckoutResponse
import com.expedia.bookings.presenter.BaseOverviewPresenter
import com.expedia.bookings.presenter.IntentPresenter
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.ScaleTransition
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.*
import com.expedia.vm.packages.BundleOverviewViewModel
import com.expedia.vm.packages.PackageConfirmationViewModel
import com.expedia.vm.packages.PackageErrorViewModel
import com.expedia.vm.packages.PackageSearchViewModel
import rx.subjects.PublishSubject
import java.math.BigDecimal
import javax.inject.Inject

class PackagePresenter(context: Context, attrs: AttributeSet) : IntentPresenter(context, attrs) {
    lateinit var packageServices: PackageServices
        @Inject set

    lateinit var travelerManager: TravelerManager

    val searchPresenter: PackageSearchPresenter by bindView(R.id.widget_package_search_presenter)
    val bundlePresenter: PackageOverviewPresenter by bindView(R.id.widget_bundle_overview)
    val confirmationPresenter: PackageConfirmationPresenter by bindView(R.id.widget_package_confirmation)
    val errorPresenter: PackageErrorPresenter by bindView(R.id.widget_package_hotel_errors)
    val hotelOffersErrorObservable = PublishSubject.create<ApiError.Code>()

    private val ANIMATION_DURATION = 400

    var expediaRewards: String? = null

    init {
        Ui.getApplication(getContext()).packageComponent().inject(this)
        travelerManager = Ui.getApplication(getContext()).travelerComponent().travelerManager()
        View.inflate(context, R.layout.package_presenter, this)
        val checkoutPresenter = bundlePresenter.getCheckoutPresenter()

        searchPresenter.searchViewModel = PackageSearchViewModel(context)
        bundlePresenter.bundleWidget.viewModel = BundleOverviewViewModel(context, packageServices)
        confirmationPresenter.viewModel = PackageConfirmationViewModel(context)
        errorPresenter.viewmodel = PackageErrorViewModel(context)
        bundlePresenter.bundleWidget.viewModel.showSearchObservable.subscribe {
            show(searchPresenter, FLAG_CLEAR_BACKSTACK)
            searchPresenter.showDefault()
        }

        bundlePresenter.bundleWidget.viewModel.showBundleTotalObservable.subscribe { visible ->
            val packagePrice = Db.getPackageResponse().packageResult.currentSelectedOffer.price

            val packageSavings = Money(BigDecimal(packagePrice.tripSavings.amount.toDouble()),
                    packagePrice.tripSavings.currencyCode)
            checkoutPresenter.totalPriceWidget.visibility = if (visible) View.VISIBLE else View.GONE
            checkoutPresenter.totalPriceWidget.viewModel.total.onNext(Money(BigDecimal(packagePrice.packageTotalPrice.amount.toDouble()),
                    packagePrice.packageTotalPrice.currencyCode))
            checkoutPresenter.totalPriceWidget.viewModel.savings.onNext(packageSavings)
        }
        checkoutPresenter.getCreateTripViewModel().tripResponseObservable.subscribe { trip ->
            expediaRewards = trip.rewards?.totalPointsToEarn?.toString()
        }
        checkoutPresenter.getCheckoutViewModel().bookingSuccessResponse.subscribe { pair: Pair<BaseApiResponse, String> ->
            val response = pair.first as PackageCheckoutResponse
            show(confirmationPresenter)
            confirmationPresenter.viewModel.showConfirmation.onNext(Pair(response.newTrip?.itineraryNumber, pair.second))
            confirmationPresenter.viewModel.setRewardsPoints.onNext(expediaRewards)
            PackagesTracking().trackCheckoutPaymentConfirmation(response, Strings.capitalizeFirstLetter(Db.getPackageSelectedRoom().supplierType))
        }

        // TODO - can we move this up to a common "base" presenter? (common between Package and Flight presenter)
        searchPresenter.searchViewModel.searchParamsObservable.subscribe { params ->
            // Starting a new search clear previous selection
            Db.clearPackageSelection()
            travelerManager.updateDbTravelers(params, context)
            errorPresenter.getViewModel().paramsSubject.onNext(params)
            show(bundlePresenter)
            bundlePresenter.show(BaseOverviewPresenter.BundleDefault(), FLAG_CLEAR_BACKSTACK)
        }
        searchPresenter.searchViewModel.searchParamsObservable.subscribe(bundlePresenter.bundleWidget.viewModel.hotelParamsObservable)
        bundlePresenter.bundleWidget.viewModel.toolbarTitleObservable.subscribe(bundlePresenter.bundleOverviewHeader.toolbar.viewModel.toolbarTitle)
        bundlePresenter.bundleWidget.viewModel.toolbarTitleObservable.subscribe(bundlePresenter.bundleOverviewHeader.checkoutOverviewHeaderToolbar.viewmodel.cityTitle)
        bundlePresenter.bundleWidget.viewModel.toolbarSubtitleObservable.subscribe(bundlePresenter.bundleOverviewHeader.checkoutOverviewHeaderToolbar.viewmodel.datesTitle)
        bundlePresenter.bundleWidget.viewModel.toolbarSubtitleObservable.subscribe(bundlePresenter.bundleOverviewHeader.toolbar.viewModel.toolbarSubtitle)
        bundlePresenter.bundleWidget.viewModel.errorObservable.subscribe(errorPresenter.getViewModel().packageSearchApiErrorObserver)
        bundlePresenter.bundleWidget.viewModel.errorObservable.subscribe { show(errorPresenter) }
        errorPresenter.getViewModel().defaultErrorObservable.subscribe {
            bundlePresenter.bundleWidget.revertBundleViewToSelectHotel()
            bundlePresenter.bundleWidget.outboundFlightWidget.viewModel.showLoadingStateObservable.onNext(false)
            bundlePresenter.bundleWidget.inboundFlightWidget.viewModel.showLoadingStateObservable.onNext(false)
            show(searchPresenter, FLAG_CLEAR_BACKSTACK)
            searchPresenter.showDefault()
        }

        checkoutPresenter.getCreateTripViewModel().createTripErrorObservable.subscribe(errorPresenter.getViewModel().checkoutApiErrorObserver)
        checkoutPresenter.getCheckoutViewModel().checkoutErrorObservable.subscribe(errorPresenter.getViewModel().checkoutApiErrorObserver)
        checkoutPresenter.getCreateTripViewModel().createTripErrorObservable.subscribe { show(errorPresenter) }
        checkoutPresenter.getCheckoutViewModel().checkoutErrorObservable.subscribe { show(errorPresenter) }

        hotelOffersErrorObservable.subscribe(errorPresenter.getViewModel().hotelOffersApiErrorObserver)
        hotelOffersErrorObservable.subscribe { show(errorPresenter) }

        errorPresenter.getViewModel().checkoutUnknownErrorObservable.subscribe {
            show(bundlePresenter, Presenter.FLAG_CLEAR_TOP)
            checkoutPresenter.slideToPurchase.resetSlider()
        }

        errorPresenter.viewmodel.createTripUnknownErrorObservable.subscribe {
            show(searchPresenter, FLAG_CLEAR_BACKSTACK)
            searchPresenter.showDefault()
        }

        errorPresenter.viewmodel.checkoutTravelerErrorObservable.subscribe {
            show(bundlePresenter, Presenter.FLAG_CLEAR_TOP)
            checkoutPresenter.slideToPurchase.resetSlider()
            checkoutPresenter.openTravelerPresenter()
            checkoutPresenter.show(checkoutPresenter, Presenter.FLAG_CLEAR_TOP)
        }

        errorPresenter.getViewModel().checkoutCardErrorObservable.subscribe {
            show(bundlePresenter, Presenter.FLAG_CLEAR_TOP)
            checkoutPresenter.slideToPurchase.resetSlider()
            checkoutPresenter.paymentWidget.clearCCAndCVV()
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
            searchPresenter.originCardView.performClick()
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
            bundlePresenter.bundleWidget.collapseBundleWidgets()
            searchPresenter.animationStart(!forward)
            if (forward) {
                bundlePresenter.bundleWidget.collapseBundleWidgets()
                bundlePresenter.bundleOverviewHeader.checkoutOverviewHeaderToolbar.visibility = View.GONE
                bundlePresenter.bundleOverviewHeader.toggleOverviewHeader(false)
                bundlePresenter.getCheckoutPresenter().toggleCheckoutButton(false)
                bundlePresenter.getCheckoutPresenter().resetAndShowTotalPriceWidget()
                bundlePresenter.setToolbarNavIcon(true)
                bundlePresenter.scrollSpaceView?.viewTreeObserver?.addOnGlobalLayoutListener(bundlePresenter.overviewLayoutListener)
                if (!User.isLoggedIn(context)) {
                    bundlePresenter.getCheckoutPresenter().clearPaymentInfo()
                }
            } else {
                bundlePresenter.scrollSpaceView?.viewTreeObserver?.removeOnGlobalLayoutListener(bundlePresenter.overviewLayoutListener)
            }
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
            searchPresenter.animationUpdate(f, !forward)
            if (forward) {
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
                AccessibilityUtil.setFocusToToolbarNavigationIcon(searchPresenter.toolbar)
            } else {
                trackViewBundlePageLoad()
            }

            val params = bundlePresenter.bundleOverviewHeader.appBarLayout.layoutParams as CoordinatorLayout.LayoutParams
            val behavior = params.behavior as AppBarLayout.Behavior
            behavior.setDragCallback(object : AppBarLayout.Behavior.DragCallback() {
                override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                    return false
                }
            });
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
            if (!forward) {
                trackViewBundlePageLoad()
            }
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
            if (forward) {
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

    fun trackViewBundlePageLoad() {
        PackagesTracking().trackViewBundlePageLoad()
    }
}
