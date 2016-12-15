package com.expedia.bookings.presenter

import android.content.Context
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import com.expedia.bookings.R
import com.expedia.bookings.presenter.packages.TravelersPresenter
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.setAccessibilityHoverFocus
import com.expedia.bookings.widget.BaseCheckoutPresenter
import com.expedia.bookings.widget.BundleOverviewHeader
import com.expedia.bookings.widget.CVVEntryWidget
import com.expedia.bookings.widget.flights.PaymentFeeInfoWebView
import com.expedia.bookings.widget.packages.BillingDetailsPaymentWidget
import com.expedia.util.endlessObserver
import com.expedia.util.safeSubscribe
import com.expedia.vm.AbstractCardFeeEnabledCheckoutViewModel
import com.expedia.vm.WebViewViewModel

abstract class BaseTwoScreenOverviewPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs), CVVEntryWidget.CVVEntryFragmentListener {

    val ANIMATION_DURATION = 400

    val bundleOverviewHeader: BundleOverviewHeader by bindView(R.id.coordinator_layout)
    protected val checkoutPresenter: BaseCheckoutPresenter by lazy  { findViewById(R.id.checkout_presenter) as BaseCheckoutPresenter }
    val cvv: CVVEntryWidget by bindView(R.id.cvv)
    val toolbarHeight = Ui.getStatusBarHeight(context) + Ui.getToolbarSize(context)

    var scrollSpaceView: View? = null
    var overviewLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    val paymentFeeInfoWebView: PaymentFeeInfoWebView by lazy {
        val viewStub = findViewById(R.id.payment_fee_info_webview_stub) as ViewStub
        val airlineFeeWebview = viewStub.inflate() as PaymentFeeInfoWebView
        airlineFeeWebview.setExitButtonOnClickListener(View.OnClickListener { this.back() })
        airlineFeeWebview.viewModel = WebViewViewModel()
        (checkoutPresenter.getCheckoutViewModel() as AbstractCardFeeEnabledCheckoutViewModel).obFeeDetailsUrlSubject.subscribe(airlineFeeWebview.viewModel.webViewURLObservable)
        airlineFeeWebview
    }

    init {
        inflate()
        checkoutPresenter.getCreateTripViewModel().createTripResponseObservable.safeSubscribe { trip ->
            resetCheckoutState()
        }
        checkoutPresenter.getCheckoutViewModel().priceChangeObservable.subscribe {
            resetCheckoutState()
            if (currentState == CVVEntryWidget::class.java.name) {
                show(checkoutPresenter, FLAG_CLEAR_TOP)
            }
        }
        bundleOverviewHeader.toolbar.overflowIcon = ContextCompat.getDrawable(context, R.drawable.ic_create_white_24dp)

        checkoutPresenter.paymentWidget.toolbarTitle.subscribe(bundleOverviewHeader.toolbar.viewModel.toolbarTitle)
        checkoutPresenter.paymentWidget.focusedView.subscribe(bundleOverviewHeader.toolbar.viewModel.currentFocus)
        if (!checkoutPresenter.paymentWidget.viewmodel.newCheckoutIsEnabled.value) {
            checkoutPresenter.paymentWidget.filledIn.subscribe(bundleOverviewHeader.toolbar.viewModel.formFilledIn)
            checkoutPresenter.travelersPresenter.travelerEntryWidget.filledIn.subscribe(bundleOverviewHeader.toolbar.viewModel.formFilledIn)
        }
        checkoutPresenter.paymentWidget.viewmodel.menuVisibility.subscribe(bundleOverviewHeader.toolbar.viewModel.menuVisibility)
        checkoutPresenter.paymentWidget.viewmodel.enableMenuItem.subscribe(bundleOverviewHeader.toolbar.viewModel.enableMenuItem)
        checkoutPresenter.paymentWidget.visibleMenuWithTitleDone.subscribe(bundleOverviewHeader.toolbar.viewModel.visibleMenuWithTitleDone)
        checkoutPresenter.paymentWidget.toolbarNavIcon.subscribe(bundleOverviewHeader.toolbar.viewModel.toolbarNavIcon)

        checkoutPresenter.travelersPresenter.toolbarTitleSubject.subscribe(bundleOverviewHeader.toolbar.viewModel.toolbarTitle)
        checkoutPresenter.travelersPresenter.toolbarNavIconContDescSubject.subscribe(bundleOverviewHeader.toolbar.viewModel.toolbarNavIconContentDesc)
        checkoutPresenter.travelersPresenter.travelerEntryWidget.focusedView.subscribe(bundleOverviewHeader.toolbar.viewModel.currentFocus)
        checkoutPresenter.travelersPresenter.menuVisibility.subscribe(bundleOverviewHeader.toolbar.viewModel.menuVisibility)
        checkoutPresenter.travelersPresenter.toolbarNavIcon.subscribe(bundleOverviewHeader.toolbar.viewModel.toolbarNavIcon)

        bundleOverviewHeader.toolbar.viewModel.doneClicked.subscribe {
            if (checkoutPresenter.currentState == BillingDetailsPaymentWidget::class.java.name) {
                checkoutPresenter.paymentWidget.doneClicked.onNext(Unit)
            } else if (checkoutPresenter.currentState == TravelersPresenter::class.java.name) {
                checkoutPresenter.travelersPresenter.doneClicked.onNext(Unit)
            }
        }

        checkoutPresenter.checkoutButton.setOnClickListener {
            showCheckout()
            checkoutPresenter.slideToPurchaseLayout.visibility = View.VISIBLE
        }

        bundleOverviewHeader.setUpCollapsingToolbar()

        overviewLayoutListener = OverviewLayoutListener()
    }

    private val overviewToAirlineFeeWebView = object : Transition(checkoutPresenter.javaClass, PaymentFeeInfoWebView::class.java, DecelerateInterpolator(), ANIMATION_DURATION) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            checkoutPresenter.visibility = if (forward) View.GONE else View.VISIBLE
            bundleOverviewHeader.visibility = if (forward) View.GONE else View.VISIBLE
            paymentFeeInfoWebView.visibility = if (!forward) View.GONE else View.VISIBLE
        }
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
        addTransition(overviewToAirlineFeeWebView)
        show(BundleDefault())
        cvv.setCVVEntryListener(this)
        checkoutPresenter.getCheckoutViewModel().slideAllTheWayObservable.subscribe(checkoutSliderSlidObserver)
        checkoutPresenter.getCheckoutViewModel().checkoutParams.subscribe { cvv.enableBookButton(false) }

        val checkoutPresenterLayoutParams = checkoutPresenter.layoutParams as MarginLayoutParams
        checkoutPresenterLayoutParams.setMargins(0, toolbarHeight, 0, 0)

        checkoutPresenter.cardFeeWarningTextView.setOnClickListener {
            show(paymentFeeInfoWebView)
        }
    }

    val defaultTransition = object : DefaultTransition(BundleDefault::class.java.name) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            bundleOverviewHeader.toolbar.menu.setGroupVisible(R.id.package_change_menu, false)
            bundleOverviewHeader.toggleCollapsingToolBar(!forward)
            checkoutPresenter.toolbarDropShadow.visibility = View.GONE
            checkoutPresenter.mainContent.visibility = View.GONE
            bundleOverviewHeader.nestedScrollView.visibility = VISIBLE
            bundleOverviewHeader.nestedScrollView.foreground?.alpha = 0
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
                scrollSpaceView?.viewTreeObserver?.addOnGlobalLayoutListener(overviewLayoutListener)
            } else {
                scrollSpaceView?.viewTreeObserver?.removeOnGlobalLayoutListener(overviewLayoutListener)
            }
            bundleOverviewHeader.nestedScrollView.visibility = VISIBLE
            setToolbarMenu(forward)
            setToolbarNavIcon(forward)
            bundleOverviewHeader.checkoutOverviewHeaderToolbar.visibility = View.VISIBLE
            bundleOverviewHeader.toggleCollapsingToolBar(true)
            translationDistance = checkoutPresenter.mainContent.translationY
            val params = bundleOverviewHeader.appBarLayout.layoutParams as CoordinatorLayout.LayoutParams
            val behavior = params.behavior as AppBarLayout.Behavior
            range = if (forward) 0f else (bundleOverviewHeader.appBarLayout.totalScrollRange - Math.abs(behavior.topAndBottomOffset)) / bundleOverviewHeader.appBarLayout.totalScrollRange.toFloat()
            checkoutPresenter.mainContent.visibility = View.VISIBLE
            bundleOverviewHeader.nestedScrollView.foreground = ContextCompat.getDrawable(context, R.drawable.dim_background)
            behavior.setDragCallback(object: AppBarLayout.Behavior.DragCallback() {
                override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                    return bundleOverviewHeader.isExpandable && currentState == BundleDefault::class.java.name
                }
            });
            AccessibilityUtil.setFocusToToolbarNavigationIcon(bundleOverviewHeader.toolbar)
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            val progress = Math.min(1f, range + f)
            translateHeader(progress, forward)
            translateCheckout(progress, forward)
            translateBottomContainer(progress, forward)
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            setBundleWidgetAndToolbar(forward)
            bundleOverviewHeader.checkoutOverviewHeaderToolbar.visibility = if (forward) View.GONE else View.VISIBLE
            bundleOverviewHeader.toggleCollapsingToolBar(!forward)
            checkoutPresenter.toggleCheckoutButton(!forward)

            checkoutPresenter.mainContent.visibility = if (forward) View.VISIBLE else View.GONE
            checkoutPresenter.mainContent.translationY = 0f
            if (forward) checkoutPresenter.toolbarDropShadow.visibility = View.VISIBLE
            bundleOverviewHeader.isDisabled = forward
            bundleOverviewHeader.nestedScrollView.foreground.alpha = if (forward) 255 else 0
            bundleOverviewHeader.nestedScrollView.visibility =  if (forward) GONE else VISIBLE
            bundleOverviewHeader.toolbar.subtitle = ""
            if (forward) {
                checkoutPresenter.adjustScrollingSpace()
                checkoutPresenter.travelersPresenter.updateAllTravelerStatuses()
            } else {
                checkoutPresenter.trackShowBundleOverview()
            }
        }

        private fun translateCheckout(f: Float, forward: Boolean) {
            val distance = height - translationDistance - Ui.getStatusBarHeight(context)
            checkoutPresenter.mainContent.translationY = if (forward) translationDistance + ((1 - f) * distance) else translationDistance + (f * distance)
            bundleOverviewHeader.nestedScrollView.foreground.alpha = (255 * if (forward) f else (1 - f)).toInt()
        }

    }

    open protected fun translateHeader(f: Float, forward: Boolean) {
        val params = bundleOverviewHeader.appBarLayout.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = params.behavior as AppBarLayout.Behavior
        val userStoppedScrollingAt = behavior.topAndBottomOffset
        val scrollY = if (forward) Math.min(userStoppedScrollingAt.toFloat(), (f * -bundleOverviewHeader.appBarLayout.totalScrollRange)) else (f - 1) * (bundleOverviewHeader.appBarLayout.totalScrollRange)
        behavior.topAndBottomOffset = scrollY.toInt()
    }

    private fun translateBottomContainer(f: Float, forward: Boolean) {
        checkoutPresenter.sliderHeight = checkoutPresenter.slideToPurchaseLayout.height.toFloat()
        val hasCompleteInfo = checkoutPresenter.getCheckoutViewModel().isValidForBooking()
        val bottomDistance = checkoutPresenter.sliderHeight - checkoutPresenter.checkoutButtonHeight
        val slideIn = if (hasCompleteInfo) {
            bottomDistance - (f * (bottomDistance))
        } else {
            checkoutPresenter.sliderHeight - ((1 - f) * checkoutPresenter.checkoutButtonHeight)
        }
        val slideOut = if (hasCompleteInfo) {
            f * (bottomDistance)
        } else {
            checkoutPresenter.sliderHeight - (f * checkoutPresenter.checkoutButtonHeight)
        }
        checkoutPresenter.bottomContainer.translationY = if (forward) slideIn else slideOut
        checkoutPresenter.checkoutButtonContainer.translationY = if (forward) f * checkoutPresenter.checkoutButtonHeight else (1 - f) * checkoutPresenter.checkoutButtonHeight
    }

    open protected fun resetCheckoutState() {
        checkoutPresenter.slideToPurchase.resetSlider()
        if (currentState == BundleDefault::class.java.name) {
            bundleOverviewHeader.toggleOverviewHeader(true)
            checkoutPresenter.toggleCheckoutButton(true)
        }
    }

    private val checkoutToCvv = object : VisibilityTransition(this, checkoutPresenter.javaClass, CVVEntryWidget::class.java) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            bundleOverviewHeader.visibility = if (forward) View.GONE else View.VISIBLE
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
        var scrollspaceheight = checkoutPresenter.bottomContainer.height + checkoutPresenter.checkoutButtonContainer.height
        if (checkoutPresenter.slideToPurchaseLayout.height > 0) {
            scrollspaceheight -= checkoutPresenter.slideToPurchaseLayout.height
        }
        if (checkoutPresenter.priceChangeWidget.height > 0) {
            scrollspaceheight -= checkoutPresenter.priceChangeWidget.height
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