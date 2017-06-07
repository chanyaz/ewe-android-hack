package com.expedia.bookings.presenter

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Space
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.SlideToWidgetLL
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.TotalPriceWidget
import com.expedia.util.notNullAndObservable
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
    val totalPriceWidget: TotalPriceWidget by bindView(R.id.total_price_widget)
    val checkoutButtonContainer: View by bindView(R.id.button_container)
    val checkoutButton: Button by bindView(R.id.checkout_button)
    val bottomContainer: LinearLayout by bindView(R.id.bottom_container)
    val bottomContainerDropShadow: View by bindView(R.id.bottom_container_drop_shadow)
    val acceptTermsWidget: ViewStub by bindView(R.id.accept_terms_view_stub)


    var viewModel: BottomCheckoutContainerViewModel by notNullAndObservable { vm ->
        vm.sliderPurchaseTotalText.subscribeText(slideTotalText)
        vm.accessiblePurchaseButtonContentDescription.subscribe { accessiblePurchaseButton.contentDescription = it }
        vm.checkoutPriceChangeObservable.subscribe { response ->
            slideToPurchase.resetSlider()
        }
        vm.noNetworkObservable.subscribe {
            slideToPurchase.resetSlider()
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