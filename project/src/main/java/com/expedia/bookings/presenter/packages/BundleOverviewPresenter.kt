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
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.BaseCheckoutPresenter
import com.expedia.bookings.widget.CheckoutToolbar
import com.expedia.bookings.widget.PackageBundleHotelWidget
import com.expedia.bookings.widget.PackageBundlePriceWidget
import com.expedia.util.notNullAndObservable
import com.expedia.bookings.widget.PackageBundleFlightWidget
import com.expedia.vm.BundleHotelViewModel
import com.expedia.vm.BundleOverviewViewModel
import com.expedia.vm.CheckoutToolbarViewModel
import com.expedia.vm.PackageCreateTripViewModel
import com.expedia.vm.BundlePriceViewModel
import com.expedia.vm.BundleFlightViewModel
import com.expedia.vm.PackageSearchType

public class BundleOverviewPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    val ANIMATION_DURATION = 450L

    val toolbar: CheckoutToolbar by bindView(R.id.checkout_toolbar)
    val bundleContainer: ScrollView by bindView(R.id.bundle_container)
    val checkoutPresenter: BaseCheckoutPresenter by bindView(R.id.checkout_presenter)
    val checkoutButton: Button by bindView(R.id.checkout_button)
    val createTripDialog = ProgressDialog(context)
    val bundleTotalPriceWidget: PackageBundlePriceWidget by bindView(R.id.bundle_total)

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
            bundleTotalPriceWidget.visibility = if (visible) View.VISIBLE else View.GONE
            bundleTotalPriceWidget.viewModel.setTextObservable.onNext(Pair(Db.getPackageResponse().packageResult.currentSelectedOffer.price.packageTotalPriceFormatted,
                    Db.getPackageResponse().packageResult.currentSelectedOffer.price.tripSavingsFormatted))
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
            bundleTotalPriceWidget.viewModel.setTextObservable.onNext(Pair(response.packageDetails.pricing.packageTotal.formattedWholePrice,
                    response.packageDetails.pricing.savings.formattedPrice))
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
        show(BundleDefault())
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

    override fun back(): Boolean {
        bundleHotelWidget.collapseSelectedHotel()
        return super.back()
    }

    class BundleDefault
}