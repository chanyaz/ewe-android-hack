package com.expedia.bookings.presenter

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Space
import com.expedia.bookings.R
import com.expedia.bookings.enums.TwoScreenOverviewState
import com.expedia.bookings.extensions.safeSubscribe
import com.expedia.bookings.extensions.setAccessibilityHoverFocus
import com.expedia.bookings.extensions.setInverseVisibility
import com.expedia.bookings.extensions.subscribeEnabled
import com.expedia.bookings.extensions.subscribeOnClick
import com.expedia.bookings.extensions.subscribeText
import com.expedia.bookings.extensions.setVisibility
import com.expedia.bookings.extensions.subscribeVisibility
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.SlideToWidgetLL
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.TotalPriceWidget
import com.expedia.util.notNullAndObservable
import com.expedia.vm.BaseCostSummaryBreakdownViewModel
import com.expedia.bookings.packages.vm.AbstractUniversalCKOTotalPriceViewModel

class BottomCheckoutContainer(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs), SlideToWidgetLL.ISlideToListener {
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

    override fun onFinishInflate() {
        super.onFinishInflate()
        checkoutButton.text = context.getString(R.string.next)
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
    val urgencyMessageContainer: RelativeLayout by bindView(R.id.urgency_message_container)
    val urgencyMessage: TextView by bindView(R.id.urgency_message)

    var viewModel: BottomCheckoutContainerViewModel by notNullAndObservable { vm ->
        vm.sliderPurchaseTotalText.subscribeText(slideTotalText)
        vm.checkoutPriceChangeObservable.subscribe {
            slideToPurchase.resetSlider()
        }
        vm.noNetworkObservable.subscribe {
            slideToPurchase.resetSlider()
        }
        accessiblePurchaseButton.subscribeOnClick(vm.slideAllTheWayObservable)
        vm.cvvToCheckoutObservable.subscribe {
            slideToPurchase.resetSlider()
            slideToPurchaseLayout.setAccessibilityHoverFocus()
        }
        vm.setSTPLayoutFocusObservable.subscribe { setFocus ->
            slideToPurchaseLayout.isFocusable = setFocus
        }
        vm.setSTPLayoutVisibilityObservable.subscribe { isVisible ->
            if (isVisible) {
                slideToPurchaseLayout.visibility = View.VISIBLE
            } else {
                slideToPurchaseLayout.visibility = View.GONE
            }
        }
        vm.resetSliderObservable.subscribe {
            onSlideAbort()
        }
        vm.toggleBundleTotalDrawableObservable.subscribe { toggle ->
            if (toggle) {
                totalPriceWidget.toggleBundleTotalCompoundDrawable(true)
            } else {
                totalPriceWidget.toggleBundleTotalCompoundDrawable(false)
            }
        }
        vm.resetPriceWidgetObservable.subscribe {
            totalPriceWidget.resetPriceWidget()
            if (totalPriceViewModel.shouldShowTotalPriceLoadingProgress()) {
                vm.checkoutButtonEnableObservable.onNext(false)
            }
        }
        vm.checkoutButtonEnableObservable.subscribeEnabled(checkoutButton)
        vm.urgencyMessageContainerVisibilityObservable.subscribeVisibility(urgencyMessageContainer)
        vm.urgencyMessageTextObservable.subscribeText(urgencyMessage)
    }

    var totalPriceViewModel: AbstractUniversalCKOTotalPriceViewModel by notNullAndObservable { vm ->
        totalPriceWidget.viewModel = vm
        if (ProductFlavorFeatureConfiguration.getInstance().shouldShowPackageIncludesView()) {
            vm.bundleTotalIncludesObservable.onNext(context.getString(R.string.includes_flights_hotel))
        }
    }

    var baseCostSummaryBreakdownViewModel: BaseCostSummaryBreakdownViewModel by notNullAndObservable { vm ->
        totalPriceWidget.breakdown.viewmodel = vm
        vm.iconVisibilityObservable.safeSubscribe { show ->
            totalPriceWidget.toggleBundleTotalCompoundDrawable(show)
            totalPriceWidget.viewModel.costBreakdownEnabledObservable.onNext(show)
        }
    }

    init {
        View.inflate(context, R.layout.bottom_checkout_container, this)
        setUpClickListeners()
        AccessibilityUtil.appendRoleContDesc(slideToPurchase, context.getString(R.string.slide_to_book_text),
                R.string.accessibility_cont_desc_role_button)
        AccessibilityUtil.appendRoleContDesc(accessiblePurchaseButton,
                context.getString(R.string.accessibility_purchase_button), R.string.accessibility_cont_desc_role_button)
    }

    private fun setUpClickListeners() {
        slideToPurchase.addSlideToListener(this)
    }

    fun toggleBottomContainerViews(state: TwoScreenOverviewState, showSlider: Boolean) {
        toggleSlideToPurchaseText(showSlider)
        toggleCheckoutButtonOrSlider(showSlider, state)
    }

    fun toggleSlideToPurchaseText(showSlider: Boolean) {
        val hasText = !viewModel.sliderPurchaseTotalText.value.isNullOrEmpty()
        slideToPurchaseSpace.setInverseVisibility(hasText && showSlider)
        slideTotalText.setVisibility(hasText && showSlider)
    }

    fun toggleCheckoutButtonOrSlider(showSlider: Boolean, state: TwoScreenOverviewState) {
        if (AccessibilityUtil.isTalkBackEnabled(context) && showSlider) {
            //hide the slider for talkback users and show a purchase button
            accessiblePurchaseButton.setText(context.getString(R.string.accessibility_purchase_button))
            accessiblePurchaseButton.visibility = View.VISIBLE
            accessiblePurchaseButton.hideTouchTarget()
            slideToPurchase.visibility = View.GONE
            checkoutButtonContainer.visibility = View.GONE
            checkoutButton.visibility = View.GONE
        } else {
            accessiblePurchaseButton.visibility = View.GONE

            if (showSlider) {
                slideToPurchase.visibility = View.VISIBLE
                checkoutButtonContainer.visibility = View.GONE
                checkoutButton.visibility = View.GONE
            } else {
                if (state == TwoScreenOverviewState.OTHER) {
                    checkoutButtonContainer.visibility = View.GONE
                    checkoutButton.visibility = View.GONE
                } else {
                    checkoutButtonContainer.visibility = View.VISIBLE
                    checkoutButton.visibility = View.VISIBLE
                    val checkoutButtonTextColor = ContextCompat.getColor(context, if (state == TwoScreenOverviewState.BUNDLE) {
                        R.color.search_dialog_background_v2
                    } else {
                        R.color.white_disabled
                    })
                    checkoutButton.setTextColor(checkoutButtonTextColor)
                }
                slideToPurchase.visibility = View.GONE
            }
        }
    }
}
