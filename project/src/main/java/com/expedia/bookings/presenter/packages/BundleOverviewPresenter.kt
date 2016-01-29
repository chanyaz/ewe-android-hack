package com.expedia.bookings.presenter.packages

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.VisibilityTransition
import com.expedia.bookings.utils.CurrencyUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.BaseCheckoutPresenter
import com.expedia.bookings.widget.CVVEntryWidget
import com.expedia.bookings.widget.CheckoutToolbar
import com.expedia.bookings.widget.PackageBundleFlightWidget
import com.expedia.bookings.widget.PackageBundleHotelWidget
import com.expedia.bookings.widget.PackageBundlePriceWidget
import com.expedia.bookings.widget.PackageCheckoutPresenter
import com.expedia.bookings.widget.TravelerContactDetailsWidget
import com.expedia.bookings.widget.packages.CheckoutOverviewHeader
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.vm.BundleFlightViewModel
import com.expedia.vm.BundleHotelViewModel
import com.expedia.vm.BundleOverviewViewModel
import com.expedia.vm.BundlePriceViewModel
import com.expedia.vm.CheckoutToolbarViewModel
import com.expedia.vm.PackageSearchType
import com.squareup.phrase.Phrase
import java.math.BigDecimal
import kotlin.properties.Delegates

public class BundleOverviewPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs), CVVEntryWidget.CVVEntryFragmentListener {
    val ANIMATION_DURATION = 450L

    val toolbar: CheckoutToolbar by bindView(R.id.checkout_toolbar)
    val bundleContainer: ScrollView by bindView(R.id.bundle_container)
    val checkoutPresenter: PackageCheckoutPresenter by bindView(R.id.checkout_presenter)
    val checkoutButton: Button by bindView(R.id.checkout_button)
    val bundleTotalPriceWidget: PackageBundlePriceWidget by bindView(R.id.bundle_total)
    val cvv: CVVEntryWidget by bindView(R.id.cvv)
    val stepOneText: TextView by bindView(R.id.step_one_text)
    val stepTwoText: TextView by bindView(R.id.step_two_text)
    val bundleHotelWidget: PackageBundleHotelWidget by bindView(R.id.package_bundle_hotel_widget)
    val outboundFlightWidget: PackageBundleFlightWidget by bindView(R.id.package_bundle_outbound_flight_widget)
    val inboundFlightWidget: PackageBundleFlightWidget by bindView(R.id.package_bundle_inbound_flight_widget)
    var statusBar: View by Delegates.notNull()
    val checkoutOverviewHeader: CheckoutOverviewHeader by bindView(R.id.checkout_overview_header)
    var toolbarHeight: Int by Delegates.notNull()
    val scrollViewtopPadding = resources.getDimensionPixelSize(R.dimen.package_bundle_scroll_view_padding)

    var viewModel: BundleOverviewViewModel by notNullAndObservable { vm ->
        vm.hotelParamsObservable.subscribe { param ->
            bundleHotelWidget.viewModel.showLoadingStateObservable.onNext(true)
            outboundFlightWidget.viewModel.hotelLoadingStateObservable.onNext(PackageSearchType.OUTBOUND_FLIGHT)
            inboundFlightWidget.viewModel.hotelLoadingStateObservable.onNext(PackageSearchType.INBOUND_FLIGHT)
        }
        vm.hotelResultsObservable.subscribe {
            bundleHotelWidget.viewModel.showLoadingStateObservable.onNext(false)
        }
        vm.flightParamsObservable.subscribe { param ->
            if (param.isOutboundSearch()) {
                outboundFlightWidget.viewModel.showLoadingStateObservable.onNext(true)
                outboundFlightWidget.viewModel.flightTextObservable.onNext(context.getString(R.string.searching_flight_to, StrUtils.formatCityName(Db.getPackageParams().destination.regionNames.shortName)))
            } else {
                inboundFlightWidget.viewModel.showLoadingStateObservable.onNext(true)
                inboundFlightWidget.viewModel.flightTextObservable.onNext(context.getString(R.string.searching_flight_to, StrUtils.formatCityName(Db.getPackageParams().origin.regionNames.shortName)))
            }
        }
        vm.flightResultsObservable.subscribe { searchType ->
            if (searchType == PackageSearchType.OUTBOUND_FLIGHT) {
                outboundFlightWidget.viewModel.showLoadingStateObservable.onNext(false)
                outboundFlightWidget.viewModel.flightTextObservable.onNext(context.getString(R.string.select_flight_to, StrUtils.formatCityName(Db.getPackageParams().destination.regionNames.shortName)))
            } else {
                inboundFlightWidget.viewModel.showLoadingStateObservable.onNext(false)
                inboundFlightWidget.viewModel.flightTextObservable.onNext(context.getString(R.string.select_flight_to, StrUtils.formatCityName(Db.getPackageParams().origin.regionNames.shortName)))
            }
        }

        vm.showBundleTotalObservable.subscribe { visible ->
            var packagePrice = Db.getPackageResponse().packageResult.currentSelectedOffer.price

            var packageSavings = Phrase.from(context, R.string.bundle_total_savings_TEMPLATE)
                    .put("savings", Money(BigDecimal(packagePrice.tripSavings.amount.toDouble()),
                            packagePrice.tripSavings.currencyCode).formattedMoney)
                    .format().toString()
            bundleTotalPriceWidget.visibility = if (visible) View.VISIBLE else View.GONE
            bundleTotalPriceWidget.viewModel.setTextObservable.onNext(Pair(Money(BigDecimal(packagePrice.packageTotalPrice.amount.toDouble()),
                    packagePrice.packageTotalPrice.currencyCode).formattedMoney, packageSavings))
        }
    }

    init {
        View.inflate(context, R.layout.bundle_overview, this)
        bundleHotelWidget.viewModel = BundleHotelViewModel(context)

        outboundFlightWidget.isOutbound = true
        inboundFlightWidget.isOutbound = false
        outboundFlightWidget.viewModel = BundleFlightViewModel(context)
        inboundFlightWidget.viewModel = BundleFlightViewModel(context)
        outboundFlightWidget.flightIcon.setImageResource(R.drawable.packages_overview_flight1)
        inboundFlightWidget.flightIcon.setImageResource(R.drawable.packages_overview_flight2)

        bundleTotalPriceWidget.viewModel = BundlePriceViewModel(context)
        checkoutPresenter.travelerWidget.mToolbarListener = toolbar

        toolbar.viewModel = CheckoutToolbarViewModel(context)
        toolbar.viewModel.nextClicked.subscribe {
            if (checkoutPresenter.currentState == TravelerContactDetailsWidget::class.java.name) {
                checkoutPresenter.travelerWidget.setNextFocus()
            }
        }
        toolbar.viewModel.doneClicked.subscribe {
            if (checkoutPresenter.currentState == TravelerContactDetailsWidget::class.java.name) {
                checkoutPresenter.travelerWidget.onMenuButtonPressed()
            }
            Ui.hideKeyboard(this)
        }

        checkoutPresenter.paymentWidget.viewmodel.toolbarTitle.subscribe(toolbar.viewModel.toolbarTitle)
        checkoutPresenter.paymentWidget.viewmodel.editText.subscribe(toolbar.viewModel.editText)
        checkoutPresenter.paymentWidget.viewmodel.enableMenu.subscribe(toolbar.viewModel.enableMenu)
        checkoutPresenter.paymentWidget.viewmodel.enableMenuDone.subscribe(toolbar.viewModel.enableMenuDone)
        toolbar.viewModel.doneClicked.subscribe(checkoutPresenter.paymentWidget.viewmodel.doneClicked)
        val statusBarHeight = Ui.getStatusBarHeight(getContext())
        if (statusBarHeight > 0) {
            val color = ContextCompat.getColor(context, R.color.packages_primary_color)
            statusBar = Ui.setUpStatusBar(getContext(), toolbar, null, color)
            addView(statusBar)
        }
        val padding = Ui.getToolbarSize(context) + statusBarHeight
        checkoutPresenter.setPadding(0, padding, 0, 0)

        checkoutButton.setOnClickListener {
            show(checkoutPresenter)
            checkoutPresenter.show(BaseCheckoutPresenter.CheckoutDefault(), FLAG_CLEAR_BACKSTACK)
        }

        bundleTotalPriceWidget.visibility = View.VISIBLE
        var countryCode = PointOfSale.getPointOfSale().threeLetterCountryCode
        var currencyCode = CurrencyUtils.currencyForLocale(countryCode)
        bundleTotalPriceWidget.viewModel.setTextObservable.onNext(Pair(Money(BigDecimal("0.00"), currencyCode).formattedMoney,
                Phrase.from(context, R.string.bundle_total_savings_TEMPLATE)
                        .put("savings", Money(BigDecimal("0.00"), currencyCode).formattedMoney)
                        .format().toString()))
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addDefaultTransition(defaultTransition)
        addTransition(checkoutTransition)
        addTransition(checkoutToCvv)
        show(BundleDefault())
        cvv.setCVVEntryListener(this)
        checkoutPresenter.slideAllTheWayObservable.subscribe(checkoutSliderSlidObserver)
        toolbarHeight = Ui.getStatusBarHeight(context) + Ui.getToolbarSize(context)
        bundleContainer.setPadding(0, toolbarHeight, 0, 0)
    }

    public fun hideCheckoutHeaderImage() {
        toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.packages_primary_color))
        statusBar.setBackgroundColor(ContextCompat.getColor(context, R.color.packages_primary_color))
        toolbar.viewModel.toolbarTitle.onNext(resources.getString(R.string.Checkout))
        checkoutOverviewHeader.visibility = GONE
        bundleContainer.setPadding(0, toolbarHeight, 0, 0)
    }

    public fun showCheckoutHeaderImage() {
        toolbar.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
        statusBar.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
        toolbar.viewModel.toolbarTitle.onNext("")
        checkoutOverviewHeader.visibility = VISIBLE
        bundleContainer.setPadding(0, scrollViewtopPadding, 0, 0)
    }

    val defaultTransition = object : Presenter.DefaultTransition(BundleDefault::class.java.name) {
        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
        }
    }

    val checkoutTransition = object : Presenter.Transition(BundleDefault::class.java, PackageCheckoutPresenter::class.java) {
        override fun startTransition(forward: Boolean) {
            checkoutButton.visibility = if (forward) View.GONE else View.VISIBLE
            bundleContainer.visibility = View.VISIBLE
            checkoutPresenter.visibility = View.VISIBLE
            bundleTotalPriceWidget.visibility = if (forward) View.GONE else View.VISIBLE
            if (forward) {
                hideCheckoutHeaderImage()
            } else {
                showCheckoutHeaderImage()
            }
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            bundleContainer.translationY = if (forward) f * -bundleContainer.height.toFloat() else (1 - f) * bundleContainer.height.toFloat()
            checkoutPresenter.translationY = if (forward) (f - 1) * -checkoutPresenter.height.toFloat() else f * checkoutPresenter.height.toFloat()
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
        }

        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
        }
    }

    private val checkoutToCvv = object : VisibilityTransition(this, PackageCheckoutPresenter::class.java, CVVEntryWidget::class.java) {
        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            if (!forward) {
                checkoutPresenter.slideToPurchase.resetSlider()
            } else {
                cvv.visibility = View.VISIBLE
            }
        }
    }

    val checkoutSliderSlidObserver = endlessObserver<Unit> {
        val billingInfo = checkoutPresenter.paymentWidget.sectionBillingInfo.billingInfo
        cvv.bind(billingInfo)
        show(cvv)
    }

    override fun onBook(cvv: String?) {
        checkoutPresenter.viewModel.cvvCompleted.onNext(cvv)
    }

    override fun back(): Boolean {
        bundleHotelWidget.backButtonPressed()
        hideCheckoutHeaderImage()
        return super.back()
    }

    class BundleDefault
}