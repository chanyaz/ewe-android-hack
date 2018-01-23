package com.expedia.bookings.rail.presenter

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import com.expedia.bookings.R
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.tracking.RailTracking
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.BundleOverviewHeader
import com.expedia.bookings.widget.PriceChangeWidget
import com.expedia.bookings.widget.TotalPriceWidget
import com.expedia.bookings.rail.widget.AccessibleProgressDialog
import com.expedia.bookings.rail.widget.RailTripSummaryWidget
import com.expedia.util.notNullAndObservable
import com.expedia.vm.rail.RailCheckoutOverviewViewModel
import com.expedia.vm.rail.RailCostSummaryBreakdownViewModel
import com.expedia.vm.rail.RailCreateTripViewModel
import com.expedia.vm.rail.RailTripSummaryViewModel
import com.expedia.vm.rail.RailTotalPriceViewModel
import com.expedia.vm.rail.RailPriceChangeViewModel
import io.reactivex.subjects.PublishSubject

class RailTripOverviewPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    val bundleOverviewHeader: BundleOverviewHeader by bindView(R.id.coordinator_layout)
    val railTripSummary: RailTripSummaryWidget by bindView(R.id.rail_trip_summary)
    val checkoutButton: Button by bindView(R.id.checkout_button)
    val totalPriceWidget: TotalPriceWidget by bindView(R.id.rail_total_price_widget)
    val priceChangeWidget: PriceChangeWidget by bindView(R.id.rail_price_change_widget)

    val tripSummaryViewModel = RailTripSummaryViewModel(context)
    val railPriceViewModel = RailTotalPriceViewModel(context)
    val railCostBreakDownViewModel = RailCostSummaryBreakdownViewModel(context, false)
    private val priceChangeViewModel = RailPriceChangeViewModel(context)

    val showCheckoutSubject = PublishSubject.create<Unit>()
    val createTripDialog = AccessibleProgressDialog(context)

    var createTripViewModel: RailCreateTripViewModel by notNullAndObservable { vm ->
        vm.tripResponseObservable.subscribe { response ->
            createTripDialog.dismiss()
            RailTracking().trackRailDetails(response)
            tripSummaryViewModel.railOfferObserver.onNext(response.railDomainProduct.railOffer)
            railPriceViewModel.updatePricing(response)
            railCostBreakDownViewModel.railCostSummaryBreakdownObservable.onNext(response)
        }

        vm.createTripCallTriggeredObservable.subscribe {
            createTripDialog.show(context.getString(R.string.spinner_text_create_trip))
            railTripSummary.reset()
        }

        vm.priceChangeObservable.subscribe { response ->
            vm.tripResponseObservable.onNext(response)
            priceChangeViewModel.priceChangedObserver.onNext(Unit)
            priceChangeWidget.visibility = View.VISIBLE
        }
    }

    init {
        View.inflate(context, R.layout.rail_overview_presenter, this)

        val overviewVM = RailCheckoutOverviewViewModel(context)
        bundleOverviewHeader.checkoutOverviewHeaderToolbar.viewmodel = overviewVM
        bundleOverviewHeader.checkoutOverviewFloatingToolbar.viewmodel = overviewVM

        railTripSummary.viewModel = tripSummaryViewModel
        totalPriceWidget.viewModel = railPriceViewModel

        railCostBreakDownViewModel.iconVisibilityObservable.subscribe { show ->
            totalPriceWidget.toggleBundleTotalCompoundDrawable(show)
            totalPriceWidget.viewModel.costBreakdownEnabledObservable.onNext(show)
        }
        totalPriceWidget.breakdown.viewmodel = railCostBreakDownViewModel

        bundleOverviewHeader.setUpCollapsingToolbar()

        checkoutButton.setOnClickListener {
            showCheckoutSubject.onNext(Unit)
        }

        priceChangeWidget.viewmodel = priceChangeViewModel
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addDefaultTransition(defaultTransition)
        show(BundleDefault())
        removeView(railTripSummary)
        bundleOverviewHeader.nestedScrollView.addView(railTripSummary)
    }

    val defaultTransition = object : DefaultTransition(BundleDefault::class.java.name) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            bundleOverviewHeader.toolbar.menu.setGroupVisible(R.id.package_change_menu, false)
            bundleOverviewHeader.toggleCollapsingToolBar(!forward)
            priceChangeWidget.visibility = View.GONE
        }
    }

    class BundleDefault
}
