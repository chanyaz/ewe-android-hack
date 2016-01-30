package com.expedia.bookings.presenter.packages

import android.app.ProgressDialog
import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.presenter.VisibilityTransition
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.BaseCheckoutPresenter
import com.expedia.bookings.widget.CheckoutToolbar
import com.expedia.bookings.widget.PackageBundleHotelWidget
import com.expedia.bookings.widget.PackageBundlePriceWidget
import com.expedia.bookings.widget.CVVEntryWidget
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.bookings.widget.PackageBundleFlightWidget
import com.expedia.vm.BundleHotelViewModel
import com.expedia.vm.BundleOverviewViewModel
import com.expedia.vm.CheckoutToolbarViewModel
import com.expedia.vm.PackageCreateTripViewModel
import com.expedia.vm.BundlePriceViewModel
import com.expedia.vm.BundleFlightViewModel
import com.expedia.vm.PackageSearchType
import com.squareup.phrase.Phrase

public class BundleOverviewPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs), CVVEntryWidget.CVVEntryFragmentListener {
    val ANIMATION_DURATION = 450L

    val toolbar: CheckoutToolbar by bindView(R.id.checkout_toolbar)
    val bundleContainer: ScrollView by bindView(R.id.bundle_container)
    val checkoutPresenter: BaseCheckoutPresenter by bindView(R.id.checkout_presenter)
    val checkoutButton: Button by bindView(R.id.checkout_button)
    val createTripDialog = ProgressDialog(context)
    val bundleTotalPriceWidget: PackageBundlePriceWidget by bindView(R.id.bundle_total)
    val cvv: CVVEntryWidget by bindView(R.id.cvv)

    val bundleHotelWidget: PackageBundleHotelWidget by bindView(R.id.package_bundle_hotel_widget)
    val outboundFlightWidget: PackageBundleFlightWidget by bindView(R.id.package_bundle_outbound_flight_widget)
    val inboundFlightWidget: PackageBundleFlightWidget by bindView(R.id.package_bundle_inbound_flight_widget)

    var viewModel: BundleOverviewViewModel by notNullAndObservable { vm ->
        vm.hotelParamsObservable.subscribe { param ->
            bundleHotelWidget.viewModel.showLoadingStateObservable.onNext(true)
            outboundFlightWidget.viewModel.flightTextObservable.onNext(context.getString(R.string.flight_to, StrUtils.formatCityName(param.destination.regionNames.shortName)))
            inboundFlightWidget.viewModel.flightTextObservable.onNext(context.getString(R.string.flight_to, StrUtils.formatCityName(param.origin.regionNames.shortName)))
        }
        vm.hotelResultsObservable.subscribe {
            bundleHotelWidget.viewModel.showLoadingStateObservable.onNext(false)
        }
        vm.flightParamsObservable.subscribe { param ->
            if (param.isOutboundSearch()) {
                outboundFlightWidget.viewModel.showLoadingStateObservable.onNext(true)
            } else {
                inboundFlightWidget.viewModel.showLoadingStateObservable.onNext(true)
            }
        }
        vm.flightResultsObservable.subscribe { searchType ->
            if (searchType == PackageSearchType.OUTBOUND_FLIGHT){
                outboundFlightWidget.viewModel.showLoadingStateObservable.onNext(false)
            }
            else{
                inboundFlightWidget.viewModel.showLoadingStateObservable.onNext(false)
            }
        }

        vm.showBundleTotalObservable.subscribe { visible ->
            var packageSavings = Phrase.from(context, R.string.bundle_total_savings_TEMPLATE)
                    .put("savings", Db.getPackageResponse().packageResult.currentSelectedOffer.price.tripSavingsFormatted)
                    .format().toString()
            bundleTotalPriceWidget.visibility = if (visible) View.VISIBLE else View.GONE
            bundleTotalPriceWidget.viewModel.setTextObservable.onNext(Pair(Db.getPackageResponse().packageResult.currentSelectedOffer.price.packageTotalPriceFormatted,
                    packageSavings))
        }
    }

    var createTripViewModel: PackageCreateTripViewModel by notNullAndObservable { vm ->
        vm.tripParams.subscribe {
            createTripDialog.show()
        }
        vm.tripResponseObservable.subscribe {
            createTripDialog.hide()
            checkoutButton.visibility = VISIBLE
        }
        vm.tripResponseObservable.subscribe(checkoutPresenter.viewModel.packageTripResponse)
        createTripViewModel.createTripBundleTotalObservable.subscribe { response ->
            var packageSavings = Phrase.from(context, R.string.bundle_total_savings_TEMPLATE)
                    .put("savings", response.packageDetails.pricing.savings.formattedPrice)
                    .format().toString()
            bundleTotalPriceWidget.viewModel.setTextObservable.onNext(Pair(response.packageDetails.pricing.packageTotal.formattedWholePrice,
                    packageSavings))
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
        checkoutPresenter.paymentWidget.mToolbarListener = toolbar
        toolbar.viewModel = CheckoutToolbarViewModel(context)
        toolbar.viewModel.nextClicked.subscribe {
            checkoutPresenter.expandedView?.setNextFocus()
        }
        toolbar.viewModel.doneClicked.subscribe {
            checkoutPresenter.expandedView?.onMenuButtonPressed()
            Ui.hideKeyboard(this)
        }

        val statusBarHeight = Ui.getStatusBarHeight(getContext())
        if (statusBarHeight > 0) {
            val color = ContextCompat.getColor(context, R.color.packages_primary_color)
            val statusBar = Ui.setUpStatusBar(getContext(), toolbar, bundleContainer, color)
            addView(statusBar)
        }
        val padding = Ui.getToolbarSize(context) + statusBarHeight
        checkoutPresenter.setPadding(0, padding, 0, 0)

        checkoutButton.setOnClickListener {
            show(checkoutPresenter)
            checkoutPresenter.show(BaseCheckoutPresenter.CheckoutDefault(), FLAG_CLEAR_BACKSTACK)
        }

        createTripDialog.setMessage(resources.getString(R.string.spinner_text_hotel_create_trip))
        createTripDialog.setCancelable(false)
        createTripDialog.isIndeterminate = true
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addDefaultTransition(defaultTransition)
        addTransition(checkoutTransition)
        addTransition(checkoutToCvv)
        show(BundleDefault())
        cvv.setCVVEntryListener(this)
        checkoutPresenter.slideAllTheWayObservable.subscribe(checkoutSliderSlidObserver)
    }

    val defaultTransition = object : Presenter.DefaultTransition(BundleDefault::class.java.name) {
        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
        }
    }

    val checkoutTransition = object : Presenter.Transition(BundleDefault::class.java, BaseCheckoutPresenter::class.java) {
        override fun startTransition(forward: Boolean) {
            checkoutButton.visibility = if (forward) View.GONE else View.VISIBLE
            bundleContainer.visibility = View.VISIBLE
            checkoutPresenter.visibility = View.VISIBLE
            bundleTotalPriceWidget.visibility = if (forward) View.GONE else View.VISIBLE
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

    private val checkoutToCvv = object : VisibilityTransition(this, BaseCheckoutPresenter::class.java, CVVEntryWidget::class.java) {
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
        bundleHotelWidget.collapseSelectedHotel()
        return super.back()
    }

    class BundleDefault
}