package com.expedia.bookings.presenter

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import com.expedia.bookings.R
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.setAccessibilityHoverFocus
import com.expedia.bookings.widget.BaseCheckoutPresenter
import com.expedia.bookings.widget.CVVEntryWidget
import com.expedia.util.endlessObserver
import com.expedia.util.safeSubscribe

abstract class BaseOverviewPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs), CVVEntryWidget.CVVEntryFragmentListener {

    val ANIMATION_DURATION = 400
    protected val checkoutPresenter: BaseCheckoutPresenter by lazy  { findViewById(R.id.checkout_presenter) as BaseCheckoutPresenter }
    val cvv: CVVEntryWidget by bindView(R.id.cvv)
    val toolbarHeight = Ui.getStatusBarHeight(context) + Ui.getToolbarSize(context)

    var scrollSpaceView: View? = null
//    var overviewLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    init {
        inflate()
        checkoutPresenter.getCreateTripViewModel().createTripResponseObservable.safeSubscribe { trip ->
//            this is where we would reset the bottom container & slider, do we need anything here?
        }
        checkoutPresenter.getCheckoutViewModel().checkoutPriceChangeObservable.subscribe {
            if (currentState == CVVEntryWidget::class.java.name) {
                show(checkoutPresenter, FLAG_CLEAR_TOP)
            }
        }


//        overviewLayoutListener = OverviewLayoutListener()
    }

    fun showCheckout() {
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
        val checkoutPresenterLayoutParams = checkoutPresenter.layoutParams as MarginLayoutParams
        checkoutPresenterLayoutParams.setMargins(0, toolbarHeight, 0, 0)
    }

    val defaultTransition = object : DefaultTransition(BundleDefault::class.java.name) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            checkoutPresenter.toolbarDropShadow.visibility = View.GONE
            checkoutPresenter.mainContent.visibility = View.GONE
            checkoutPresenter.trackShowBundleOverview()
        }
    }

    val checkoutTransition = object : Transition(BundleDefault::class.java, checkoutPresenter.javaClass, AccelerateDecelerateInterpolator(), ANIMATION_DURATION) {
        var translationDistance = 0f
        var range = 0f
        override fun startTransition(forward: Boolean) {
            if (!forward) {
                checkoutPresenter.toolbarDropShadow.visibility = View.GONE
                resetScrollSpaceHeight()
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
//            setBundleWidgetAndToolbar(forward)
            checkoutPresenter.mainContent.visibility = if (forward) View.VISIBLE else View.GONE
            checkoutPresenter.mainContent.translationY = 0f
            if (forward) checkoutPresenter.toolbarDropShadow.visibility = View.VISIBLE
            if (forward) {
                checkoutPresenter.adjustScrollingSpace()
                checkoutPresenter.travelersPresenter.updateAllTravelerStatuses()
                if (checkoutPresenter.getCheckoutViewModel().isValidForBooking()) {
                    checkoutPresenter.trackShowSlideToPurchase()
                }
            } else {
                checkoutPresenter.trackShowBundleOverview()
            }
        }

        private fun translateCheckout(f: Float, forward: Boolean) {
            val distance = height - translationDistance - Ui.getStatusBarHeight(context)
            checkoutPresenter.mainContent.translationY = if (forward) translationDistance + ((1 - f) * distance) else translationDistance + (f * distance)
        }

    }


    private val checkoutToCvv = object : VisibilityTransition(this, checkoutPresenter.javaClass, CVVEntryWidget::class.java) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            if (!forward) {
                checkoutPresenter.slideToPurchase.resetSlider()
                checkoutPresenter.slideToPurchaseLayout.setAccessibilityHoverFocus()
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
        checkoutPresenter.getCheckoutViewModel().cvvCompleted.onNext(cvv)
    }

    override fun back(): Boolean {
        val didHandleBack = super.back()
        if (!didHandleBack) {
            checkoutPresenter.resetPriceChange()
            checkoutPresenter.totalPriceWidget.toggleBundleTotalCompoundDrawable(false)
        }
        return didHandleBack
    }

    class BundleDefault

//    open fun setBundleWidgetAndToolbar(forward: Boolean) { }
    open fun setToolbarMenu(forward: Boolean) { }
    open fun setToolbarNavIcon(forward: Boolean) { }
    abstract fun trackCheckoutPageLoad()
    abstract fun trackPaymentCIDLoad()
    abstract fun inflate()


//    inner class OverviewLayoutListener: ViewTreeObserver.OnGlobalLayoutListener {
//        override fun onGlobalLayout () {
////            updateScrollingSpace(scrollSpaceView)
//        }
//    }

//    private fun updateScrollingSpace(scrollSpaceView: View?) {
//        val scrollSpaceViewLp = scrollSpaceView?.layoutParams
//        var scrollspaceheight = checkoutPresenter.bottomContainer.height + checkoutPresenter.checkoutButtonContainer.height
//        if (scrollSpaceViewLp?.height != scrollspaceheight) {
//            scrollSpaceViewLp?.height = scrollspaceheight
//            scrollSpaceView?.layoutParams = scrollSpaceViewLp
//            scrollSpaceView?.requestLayout()
//        }
//    }

    fun resetScrollSpaceHeight() {
        val layoutParams = scrollSpaceView?.layoutParams
        layoutParams?.height = 0
        scrollSpaceView?.layoutParams = layoutParams
    }
}