package com.expedia.bookings.presenter

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.Space
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.PriceChangeWidget
import com.expedia.bookings.widget.SlideToWidgetLL
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.TotalPriceWidget
import com.expedia.util.notNullAndObservable
import com.expedia.util.setInverseVisibility
import com.expedia.util.subscribeOnClick
import com.expedia.util.subscribeText

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
        accessiblePurchaseButton.subscribeOnClick(vm.slideAllTheWayObservable)

    }

    init {
        View.inflate(context, R.layout.bottom_checkout_container, this)
        setUpClickListeners()
    }

    private fun setUpClickListeners() {
        slideToPurchase.addSlideToListener(this)
    }
}