package com.expedia.bookings.presenter

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.setAccessibilityHoverFocus
import com.expedia.bookings.widget.BaseCheckoutPresenter
import com.expedia.bookings.widget.CVVEntryWidget
import com.expedia.bookings.widget.SlideToWidgetLL
import com.expedia.util.endlessObserver
import com.expedia.util.safeSubscribeOptional

abstract class BaseOverviewPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs), CVVEntryWidget.CVVEntryFragmentListener {

    val ANIMATION_DURATION = 400
    protected val checkoutPresenter: BaseCheckoutPresenter by lazy  { findViewById<BaseCheckoutPresenter>(R.id.checkout_presenter) }
    val cvv: CVVEntryWidget by bindView(R.id.cvv)

    var scrollSpaceView: View? = null
    var overviewLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    /*Need to re-do this work if we get back to implementing this presenter for Universal checkout*/
    val slideToPurchaseLayout by lazy {
        findViewById<LinearLayout>(R.id.slide_to_purchase_layout)
    }

    /*Need to re-do this work if we get back to implementing this presenter for Universal checkout*/
    val slideToPurchase by lazy {
        findViewById<SlideToWidgetLL>(R.id.slide_to_purchase_widget)
    }

    init {
        inflate()
        checkoutPresenter.getCreateTripViewModel().createTripResponseObservable.safeSubscribeOptional { trip ->
            resetCheckoutState()
        }
        checkoutPresenter.getCheckoutViewModel().checkoutPriceChangeObservable.subscribe {
            resetCheckoutState()
            if (currentState == CVVEntryWidget::class.java.name) {
                show(checkoutPresenter, FLAG_CLEAR_TOP)
            }
        }
        overviewLayoutListener = OverviewLayoutListener()
    }

    fun showCheckout() {
        resetCheckoutState()
        show(checkoutPresenter, FLAG_CLEAR_TOP)
        checkoutPresenter.show(BaseCheckoutPresenter.CheckoutDefault(), FLAG_CLEAR_BACKSTACK)
        trackCheckoutPageLoad()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addDefaultTransition(defaultTransition)
        addTransition(checkoutTransition)
        addTransition(checkoutToCvv)
        show(BundleDefault())
        cvv.setCVVEntryListener(this)
        checkoutPresenter.getCheckoutViewModel().slideAllTheWayObservable.subscribe(checkoutSliderSlidObserver)
        checkoutPresenter.getCheckoutViewModel().checkoutParams.subscribe { cvv.enableBookButton(false) }
    }

    open val defaultTransition = object : DefaultTransition(BundleDefault::class.java.name) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            checkoutPresenter.toolbarDropShadow.visibility = View.GONE
            checkoutPresenter.mainContent.visibility = View.GONE
        }
    }

    val checkoutTransition = object : Transition(BundleDefault::class.java, checkoutPresenter.javaClass, AccelerateDecelerateInterpolator(), ANIMATION_DURATION) {
        var translationDistance = 0f
        var range = 0f
        override fun startTransition(forward: Boolean) {
            if (!forward) {
                checkoutPresenter.toolbarDropShadow.visibility = View.GONE
                resetScrollSpaceHeight()
                scrollSpaceView?.viewTreeObserver?.addOnGlobalLayoutListener(overviewLayoutListener)
            } else {
                scrollSpaceView?.viewTreeObserver?.removeOnGlobalLayoutListener(overviewLayoutListener)
            }
            setToolbarMenu(forward)
            setToolbarNavIcon(forward)
            translationDistance = checkoutPresenter.mainContent.translationY
            checkoutPresenter.mainContent.visibility = View.VISIBLE
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            val progress = Math.min(1f, range + f)
            translateCheckout(progress, forward)
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            setBundleWidgetAndToolbar(forward)

            checkoutPresenter.mainContent.visibility = if (forward) View.VISIBLE else View.GONE
            checkoutPresenter.mainContent.translationY = 0f
            if (forward) {
                checkoutPresenter.adjustScrollingSpace(slideToPurchaseLayout)
                checkoutPresenter.toolbarDropShadow.visibility = View.VISIBLE
                checkoutPresenter.travelersPresenter.updateAllTravelerStatuses()
                if (checkoutPresenter.getCheckoutViewModel().isValidForBooking()) {
                    checkoutPresenter.trackShowSlideToPurchase()
                }
            }
        }

        private fun translateCheckout(f: Float, forward: Boolean) {
            val distance = height - translationDistance - Ui.getStatusBarHeight(context)
            checkoutPresenter.mainContent.translationY = if (forward) translationDistance + ((1 - f) * distance) else translationDistance + (f * distance)
        }

    }

    open protected fun resetCheckoutState() {
        slideToPurchase.resetSlider()
    }

    private val checkoutToCvv = object : VisibilityTransition(this, checkoutPresenter.javaClass, CVVEntryWidget::class.java) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            if (!forward) {
                slideToPurchase.resetSlider()
                slideToPurchaseLayout.setAccessibilityHoverFocus()
            } else {
                cvv.visibility = View.VISIBLE
                trackPaymentCIDLoad()
                postDelayed({
                    AccessibilityUtil.setFocusToToolbarNavigationIcon(cvv.toolbar)
                }, 100L)
            }
        }
    }

    val checkoutSliderSlidObserver = endlessObserver<Unit> {
        val billingInfo = checkoutPresenter.paymentWidget.sectionBillingInfo.billingInfo
        cvv.bind(billingInfo)
        show(cvv)
    }

    override fun onBook(cvv: String?) {
        cvv?.let {
            checkoutPresenter.getCheckoutViewModel().cvvCompleted.onNext(it)
        }
    }

    class BundleDefault

    open fun setBundleWidgetAndToolbar(forward: Boolean) { }
    open fun setToolbarMenu(forward: Boolean) { }
    open fun setToolbarNavIcon(forward: Boolean) { }
    abstract fun trackCheckoutPageLoad()
    abstract fun trackPaymentCIDLoad()
    abstract fun inflate()


    inner class OverviewLayoutListener: ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout () {
            updateScrollingSpace(scrollSpaceView)
        }
    }

    private fun updateScrollingSpace(scrollSpaceView: View?) {
        val scrollSpaceViewLp = scrollSpaceView?.layoutParams
        var scrollspaceheight = slideToPurchaseLayout.height
        if (slideToPurchaseLayout.height > 0) {
            scrollspaceheight -= slideToPurchaseLayout.height
        }
        if (scrollSpaceViewLp?.height != scrollspaceheight) {
            scrollSpaceViewLp?.height = scrollspaceheight
            scrollSpaceView?.layoutParams = scrollSpaceViewLp
            scrollSpaceView?.requestLayout()
        }
    }

    fun resetScrollSpaceHeight() {
        val layoutParams = scrollSpaceView?.layoutParams
        layoutParams?.height = 0
        scrollSpaceView?.layoutParams = layoutParams
    }
}