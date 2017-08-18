package com.expedia.bookings.presenter

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Space
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.enums.TwoScreenOverviewState
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.setAccessibilityHoverFocus
import com.expedia.bookings.widget.AcceptTermsWidget
import com.expedia.bookings.widget.SlideToWidgetLL
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.TotalPriceWidget
import com.expedia.util.notNullAndObservable
import com.expedia.util.setInverseVisibility
import com.expedia.util.subscribeOnClick
import com.expedia.util.subscribeText
import com.expedia.util.safeSubscribe
import com.expedia.util.unsubscribeOnClick
import com.expedia.util.updateVisibility
import com.expedia.vm.BaseCostSummaryBreakdownViewModel
import com.expedia.vm.packages.AbstractUniversalCKOTotalPriceViewModel

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
        val variateForTest = Db.getAbacusResponse().variateForTest(AbacusUtils.EBAndroidAppCheckoutButtonText)
        if (variateForTest == AbacusUtils.DefaultTwoVariant.VARIANT1.ordinal) {
            checkoutButton.text = context.getString(R.string.continue_booking)
        } else if (variateForTest == AbacusUtils.DefaultTwoVariant.VARIANT2.ordinal) {
            checkoutButton.text = context.getString(R.string.next)
        }
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
    val acceptTermsViewStub: ViewStub by bindView(R.id.accept_terms_view_stub)

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
        }
    }

    val acceptTermsWidget: AcceptTermsWidget by lazy {
        val viewStub = acceptTermsViewStub
        val presenter = viewStub.inflate() as AcceptTermsWidget
        presenter.acceptButton.setOnClickListener {
            presenter.vm.acceptedTermsObservable.onNext(true)
            AnimUtils.slideDown(acceptTermsViewStub)
            acceptTermsViewStub.visibility = View.GONE
            presenter.acceptButton.unsubscribeOnClick()
        }
        presenter
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
        slideTotalText.updateVisibility(hasText && showSlider)
    }

    fun toggleCheckoutButtonOrSlider(showSlider: Boolean, state: TwoScreenOverviewState) {
        if (AccessibilityUtil.isTalkBackEnabled(context) && showSlider) {
            //hide the slider for talkback users and show a purchase button
            accessiblePurchaseButton.setText(context.getString(R.string.accessibility_purchase_button))
            accessiblePurchaseButton.visibility = View.VISIBLE
            accessiblePurchaseButton.hideTouchTarget()
            slideToPurchase.visibility = View.GONE
            checkoutButtonContainer.visibility = View.GONE
        } else {
            accessiblePurchaseButton.visibility = View.GONE

            if (showSlider) {
                slideToPurchase.visibility = View.VISIBLE
                checkoutButtonContainer.visibility = View.GONE
            } else {
                if (state == TwoScreenOverviewState.OTHER) {
                    checkoutButtonContainer.visibility = View.GONE
                } else {
                    checkoutButtonContainer.visibility = View.VISIBLE
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

    fun toggleAcceptTermsWidget(showSlider: Boolean) {
        val termsAccepted = acceptTermsWidget.vm.acceptedTermsObservable.value
        if (!termsAccepted && showSlider) {
            acceptTermsViewStub.visibility = View.VISIBLE
        }
    }
}