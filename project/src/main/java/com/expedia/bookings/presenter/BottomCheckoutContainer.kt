package com.expedia.bookings.presenter

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.Space
import com.expedia.bookings.R
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.PriceChangeWidget
import com.expedia.bookings.widget.SlideToWidgetLL
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.TotalPriceWidget
import com.expedia.util.*
import com.expedia.vm.BaseCostSummaryBreakdownViewModel
import com.expedia.vm.packages.AbstractUniversalCKOTotalPriceViewModel

class BottomCheckoutContainer(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs), SlideToWidgetLL.ISlideToListener {
    /** Slide to purchase **/
    override fun onSlideStart() {
    }

    override fun onSlideProgress(pixels: Float, total: Float) {
    }

    override fun onSlideAllTheWay() {
        viewModel.slideAllTheWayObservable.onNext(Unit)
    }

    override fun onSlideAbort() {
        slideToPurchase.resetSlider()
    }

    val slideTotalText: TextView by bindView(R.id.purchase_total_text_view)
    val slideToPurchaseSpace: Space by bindView(R.id.slide_to_purchase_space)
    val slideToPurchaseLayout: LinearLayout by bindView(R.id.slide_to_purchase_layout)
    val slideToPurchase: SlideToWidgetLL by bindView(R.id.slide_to_purchase_widget)
    val accessiblePurchaseButton: SlideToWidgetLL by bindView(R.id.purchase_button_widget)
    val priceChangeWidget: PriceChangeWidget by bindView(R.id.price_change)
    val totalPriceWidget: TotalPriceWidget by bindView(R.id.total_price_widget)

    protected var totalPriceViewModel: AbstractUniversalCKOTotalPriceViewModel by notNullAndObservable { vm ->
        totalPriceWidget.viewModel = vm
        if (ProductFlavorFeatureConfiguration.getInstance().shouldShowPackageIncludesView())
            vm.bundleTotalIncludesObservable.onNext(context.getString(R.string.includes_flights_hotel))
    }

    protected var baseCostSummaryBreakdownViewModel: BaseCostSummaryBreakdownViewModel by notNullAndObservable { vm ->
        totalPriceWidget.breakdown.viewmodel = vm
        vm.iconVisibilityObservable.safeSubscribe { show ->
            totalPriceWidget.toggleBundleTotalCompoundDrawable(show)
            totalPriceWidget.viewModel.costBreakdownEnabledObservable.onNext(show)
        }
    }

    var viewModel: BottomCheckoutContainerViewModel by notNullAndObservable { vm ->
        vm.sliderPurchaseTotalText.subscribeText(slideTotalText)
        vm.accessiblePurchaseButtonContentDescription.subscribe { accessiblePurchaseButton.contentDescription = it }
        vm.checkoutPriceChangeObservable.subscribe { response ->
            slideToPurchase.resetSlider()
        }
        vm.noNetworkObservable.subscribe {
            slideToPurchase.resetSlider()
        }
        vm.animateInSlideToPurchaseObservable.subscribe {
            val hasText = !vm.sliderPurchaseTotalText.value.isNullOrEmpty()
            slideToPurchaseSpace.setInverseVisibility(hasText)
            slideTotalText.setInverseVisibility(!hasText)
        }
        accessiblePurchaseButton.subscribeOnClick(vm.accessiblePurchaseButtonClicked)

    }

    init {
        View.inflate(context, R.layout.bottom_checkout_container, this)
        setUpClickListeners()
    }

    private fun setUpClickListeners() {
        slideToPurchase.addSlideToListener(this)
        accessiblePurchaseButton.setOnClickListener {
            //            if (ckoViewModel.builder.hasValidCVV()) {
//                val params = ckoViewModel.builder.build()
//                if (!ExpediaBookingApp.isAutomation() && !ckoViewModel.builder.hasValidCheckoutParams()) {
//                    Crashlytics.logException(Exception(("User slid to purchase, see params: ${params.toValidParamsMap()}, hasValidParams: ${ckoViewModel.builder.hasValidParams()}")))
//                }
//                ckoViewModel.checkoutParams.onNext(params)
//            } else {
//                ckoViewModel.slideAllTheWayObservable.onNext(Unit)
//            }
        }
    }
}