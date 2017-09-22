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
import com.crashlytics.android.Crashlytics
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.enums.TwoScreenOverviewState
import com.expedia.bookings.utils.*
import com.expedia.bookings.widget.BaggageFeeInfoWebView
import com.expedia.bookings.widget.BaseCheckoutPresenter
import com.expedia.bookings.widget.BundleOverviewHeader
import com.expedia.bookings.widget.CVVEntryWidget
import com.expedia.bookings.widget.flights.PaymentFeeInfoWebView
import com.expedia.bookings.widget.packages.BillingDetailsPaymentWidget
import com.expedia.util.Optional
import com.expedia.util.endlessObserver
import com.expedia.util.safeSubscribe
import com.expedia.util.safeSubscribeOptional
import com.expedia.util.setInverseVisibility
import com.expedia.vm.AbstractCardFeeEnabledCheckoutViewModel
import com.expedia.vm.BaseCostSummaryBreakdownViewModel
import com.expedia.vm.WebViewViewModel
import com.expedia.vm.packages.AbstractUniversalCKOTotalPriceViewModel
import rx.Subscription

abstract class BaseTwoScreenOverviewPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs), CVVEntryWidget.CVVEntryFragmentListener{

    open fun setBundleWidgetAndToolbar(forward: Boolean) { }
    open fun setToolbarMenu(forward: Boolean) { }
    open fun setToolbarNavIcon(forward: Boolean) { }
    abstract fun trackCheckoutPageLoad()
    abstract fun trackPaymentCIDLoad()
    abstract fun inflate()
    abstract fun injectComponents()
    abstract fun getCostSummaryBreakdownViewModel(): BaseCostSummaryBreakdownViewModel
    abstract fun onTripResponse(response: TripResponse?)
    abstract fun getPriceViewModel(context: Context): AbstractUniversalCKOTotalPriceViewModel

    protected abstract fun fireCheckoutOverviewTracking(createTripResponse: TripResponse)

    val ANIMATION_DURATION = 400

    val bundleOverviewHeader: BundleOverviewHeader by bindView(R.id.coordinator_layout)
    protected val checkoutPresenter: BaseCheckoutPresenter by lazy  { findViewById<BaseCheckoutPresenter>(R.id.checkout_presenter) }
    val cvv: CVVEntryWidget by bindView(R.id.cvv)
    val toolbarHeight = Ui.getStatusBarHeight(context) + Ui.getToolbarSize(context)

    val bottomCheckoutContainer: BottomCheckoutContainer by bindView(R.id.bottom_checkout_container)

    var scrollSpaceView: View? = null
    var overviewLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null
    private var trackShowingCkoOverviewSubscription: Subscription? = null

    val paymentFeeInfoWebView: PaymentFeeInfoWebView by lazy {
        val viewStub = findViewById<ViewStub>(R.id.payment_fee_info_webview_stub)
        val airlineFeeWebview = viewStub.inflate() as PaymentFeeInfoWebView
        airlineFeeWebview.setExitButtonOnClickListener(View.OnClickListener { this.back() })
        airlineFeeWebview.viewModel = WebViewViewModel()
        (checkoutPresenter.getCheckoutViewModel() as AbstractCardFeeEnabledCheckoutViewModel).obFeeDetailsUrlSubject.subscribe(airlineFeeWebview.viewModel.webViewURLObservable)
        airlineFeeWebview
    }

    val baggageFeeInfoWebView: BaggageFeeInfoWebView by lazy {
        val viewStub = findViewById<ViewStub>(R.id.baggage_fee_summary_stub)
        val baggageFeeView = viewStub.inflate() as BaggageFeeInfoWebView
        baggageFeeView.setExitButtonOnClickListener(View.OnClickListener { this.back() })
        baggageFeeView.viewModel = WebViewViewModel()
        baggageFeeView
    }

    val bottomContainer by lazy {
        bottomCheckoutContainer.bottomContainer
    }

    val checkoutButtonContainer by lazy {
        bottomCheckoutContainer.checkoutButtonContainer
    }

    val slideToPurchaseLayout by lazy {
        bottomCheckoutContainer.slideToPurchaseLayout
    }

    val checkoutButton by lazy {
        bottomCheckoutContainer.checkoutButton
    }

    val totalPriceWidget by lazy {
        bottomCheckoutContainer.totalPriceWidget
    }

    init {
        inflate()
        injectComponents()
        setupCheckoutViewModelSubscriptions()
        setupPaymentWidgetSubscriptions()
        setupTravelerWidgetSubscriptions()
        setupBundleOverviewHeader()
        setupClickListeners()
        setupViewModels()
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

    private val overviewToPaymentFeeWebView = object : Transition(BaseTwoScreenOverviewPresenter.BundleDefault::class.java, PaymentFeeInfoWebView::class.java, DecelerateInterpolator(), ANIMATION_DURATION) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            checkoutPresenter.visibility = if (forward) View.GONE else View.VISIBLE
            bundleOverviewHeader.visibility = if (forward) View.GONE else View.VISIBLE
            paymentFeeInfoWebView.visibility = if (!forward) View.GONE else View.VISIBLE
        }
    }

    private val overviewToBaggageFeeWebView = object : Transition(BaseTwoScreenOverviewPresenter.BundleDefault::class.java, BaggageFeeInfoWebView::class.java, DecelerateInterpolator(), ANIMATION_DURATION) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            checkoutPresenter.visibility = if (forward) View.GONE else View.VISIBLE
            bundleOverviewHeader.visibility = if (forward) View.GONE else View.VISIBLE
            baggageFeeInfoWebView.visibility = if (!forward) View.GONE else View.VISIBLE
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
        addTransition(overviewToPaymentFeeWebView)
        addTransition(overviewToBaggageFeeWebView)
        show(BundleDefault())
        cvv.setCVVEntryListener(this)
        checkoutPresenter.getCheckoutViewModel().slideAllTheWayObservable.subscribe(checkoutSliderSlidObserver)
        checkoutPresenter.getCheckoutViewModel().checkoutParams.subscribe { cvv.enableBookButton(false) }
        checkoutPresenter.getCheckoutViewModel().noNetworkObservable.subscribe(bottomCheckoutContainer.viewModel.noNetworkObservable)
        checkoutPresenter.getCheckoutViewModel().checkoutPriceChangeObservable.subscribe(bottomCheckoutContainer.viewModel.checkoutPriceChangeObservable)

        val checkoutPresenterLayoutParams = checkoutPresenter.layoutParams as MarginLayoutParams
        checkoutPresenterLayoutParams.setMargins(0, toolbarHeight, 0, 0)

        checkoutPresenter.cardFeeWarningTextView.setOnClickListener {
            val mayChargeObFees = !(checkoutPresenter.getCheckoutViewModel() as AbstractCardFeeEnabledCheckoutViewModel).obFeeDetailsUrlSubject.value.isNullOrBlank()
            if (mayChargeObFees) {
                show(paymentFeeInfoWebView)
            }
        }
        setupCreateTripViewModelSubscriptions()
    }

    open val defaultTransition = TwoScreenOverviewDefaultTransition()

    open inner class TwoScreenOverviewDefaultTransition : DefaultTransition(BundleDefault::class.java.name) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            bundleOverviewHeader.toolbar.menu.setGroupVisible(R.id.package_change_menu, false)
            bundleOverviewHeader.toggleCollapsingToolBar(!forward)
            checkoutPresenter.toolbarDropShadow.visibility = View.GONE
            checkoutPresenter.mainContent.visibility = View.GONE
            bundleOverviewHeader.nestedScrollView.visibility = VISIBLE
            bundleOverviewHeader.nestedScrollView.foreground?.alpha = 0
            trackShowBundleOverview()
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
                if (isSecureIconEnabled(context)) {
                    bundleOverviewHeader.secureIcon.visibility = View.GONE
                    bundleOverviewHeader.customTitle?.visibility = View.GONE
                }
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
            })
            AccessibilityUtil.setFocusToToolbarNavigationIcon(bundleOverviewHeader.toolbar)
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            val progress = Math.min(1f, range + f)
            translateHeader(progress, forward)
            translateCheckout(progress, forward)
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            if (forward) {
                checkoutPresenter.getCheckoutViewModel().bottomCheckoutContainerStateObservable.onNext(TwoScreenOverviewState.CHECKOUT)
                if(isSecureIconEnabled(context)) {
                    bundleOverviewHeader.secureIcon.visibility = View.VISIBLE
                    bundleOverviewHeader.customTitle?.visibility = View.VISIBLE
                }
            } else {
                checkoutPresenter.getCheckoutViewModel().bottomCheckoutContainerStateObservable.onNext(TwoScreenOverviewState.BUNDLE)
            }
            setBundleWidgetAndToolbar(forward)
            bundleOverviewHeader.checkoutOverviewHeaderToolbar.visibility = if (forward) View.GONE else View.VISIBLE
            bundleOverviewHeader.toggleCollapsingToolBar(!forward)
            checkoutPresenter.mainContent.visibility = if (forward) View.VISIBLE else View.GONE
            checkoutPresenter.mainContent.translationY = 0f
            if (forward) checkoutPresenter.toolbarDropShadow.visibility = View.VISIBLE
            bundleOverviewHeader.isDisabled = forward
            bundleOverviewHeader.nestedScrollView.foreground.alpha = if (forward) 255 else 0
            bundleOverviewHeader.nestedScrollView.visibility =  if (forward) GONE else VISIBLE
            bundleOverviewHeader.toolbar.subtitle = ""
            if (forward) {
                checkoutPresenter.adjustScrollingSpace(bottomContainer)
                checkoutPresenter.travelersPresenter.updateAllTravelerStatuses()
            } else {
                trackShowBundleOverview()
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

    open protected fun resetCheckoutState() {
        bottomCheckoutContainer.viewModel.resetSliderObservable.onNext(Unit)
        if (currentState == BundleDefault::class.java.name) {
            bundleOverviewHeader.toggleOverviewHeader(true)
            checkoutPresenter.getCheckoutViewModel().bottomCheckoutContainerStateObservable.onNext(TwoScreenOverviewState.BUNDLE)
        }
    }

    private val checkoutToCvv = object : VisibilityTransition(this, checkoutPresenter.javaClass, CVVEntryWidget::class.java) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            bundleOverviewHeader.visibility = if (forward) View.GONE else View.VISIBLE
            if (!forward) {
                bottomCheckoutContainer.viewModel.cvvToCheckoutObservable.onNext(Unit)
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
            resetPriceChange()
            bottomCheckoutContainer.viewModel.toggleBundleTotalDrawableObservable.onNext(false)
        }
        return didHandleBack
    }

    class BundleDefault

    inner class OverviewLayoutListener: ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout () {
            updateScrollingSpace(scrollSpaceView)
        }
    }

    private fun updateScrollingSpace(scrollSpaceView: View?) {
        val scrollSpaceViewLp = scrollSpaceView?.layoutParams
        var scrollspaceheight = bottomContainer.height + checkoutButtonContainer.height
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

    fun resetPriceChange() {
        checkoutPresenter.getCreateTripViewModel().priceChangeAlertPriceObservable.onNext(Optional(null))
    }

    fun resetAndShowTotalPriceWidget() {
        resetPriceChange()
        bottomCheckoutContainer.viewModel.resetPriceWidgetObservable.onNext(Unit)
    }

    fun hasPriceChange(response: TripResponse?): Boolean {
        return response?.getOldPrice() != null
    }

    private fun toggleBottomContainerViews(state: TwoScreenOverviewState) {
        val showSlider = checkoutPresenter.getCheckoutViewModel().isValidForBooking()
                && state == TwoScreenOverviewState.CHECKOUT
        bottomCheckoutContainer.toggleBottomContainerViews(state, showSlider)
        toggleAcceptTermsWidget(showSlider, checkoutPresenter)
        if (showSlider) {
            checkoutPresenter.trackShowSlideToPurchase()
        }
        bottomCheckoutContainer.viewModel.setSTPLayoutFocusObservable.onNext(showSlider)
        checkoutPresenter.adjustScrollingSpace(bottomContainer)
    }

    fun trackShowBundleOverview() {
        trackShowingCkoOverviewSubscription?.unsubscribe()
        val createTripResponse = checkoutPresenter.getCreateTripViewModel().createTripResponseObservable.value?.value
        if (createTripResponse != null) {
            fireCheckoutOverviewTracking(createTripResponse)
        } else {
            trackShowingCkoOverviewSubscription = checkoutPresenter.getCreateTripViewModel().createTripResponseObservable.safeSubscribeOptional { tripResponse ->
                // Un-subscribe:- as we only want to track the initial load of cko overview
                trackShowingCkoOverviewSubscription?.unsubscribe()
                fireCheckoutOverviewTracking(tripResponse!!)
            }
        }
    }

    private fun setupCheckoutViewModelSubscriptions() {
        checkoutPresenter.getCheckoutViewModel().checkoutPriceChangeObservable.subscribe {
            resetCheckoutState()
            if (currentState == CVVEntryWidget::class.java.name) {
                show(checkoutPresenter, FLAG_CLEAR_TOP)
            }
        }
        checkoutPresenter.getCreateTripViewModel().updateOverviewUiObservable.subscribe { response ->
            onTripResponse(response)
        }
        checkoutPresenter.getCheckoutViewModel().bottomContainerInverseVisibilityObservable.subscribe { forward ->
            bottomContainer.setInverseVisibility(forward)
            checkoutButtonContainer.setInverseVisibility(forward)
        }
        checkoutPresenter.getCheckoutViewModel().bottomCheckoutContainerStateObservable.subscribe { currentState ->
            toggleBottomContainerViews(currentState)
        }
    }

    private fun setupPaymentWidgetSubscriptions() {
        checkoutPresenter.paymentWidget.toolbarTitle.subscribe(bundleOverviewHeader.toolbar.viewModel.toolbarTitle)
        checkoutPresenter.paymentWidget.focusedView.subscribe(bundleOverviewHeader.toolbar.viewModel.currentFocus)
        if (!checkoutPresenter.paymentWidget.viewmodel.newCheckoutIsEnabled.value) {
            checkoutPresenter.paymentWidget.filledIn.subscribe(bundleOverviewHeader.toolbar.viewModel.formFilledIn)
        }
        checkoutPresenter.paymentWidget.viewmodel.menuVisibility.subscribe(bundleOverviewHeader.toolbar.viewModel.menuVisibility)
        checkoutPresenter.paymentWidget.viewmodel.enableMenuItem.subscribe(bundleOverviewHeader.toolbar.viewModel.enableMenuItem)
        checkoutPresenter.paymentWidget.visibleMenuWithTitleDone.subscribe(bundleOverviewHeader.toolbar.viewModel.visibleMenuWithTitleDone)
        checkoutPresenter.paymentWidget.toolbarNavIcon.subscribe(bundleOverviewHeader.toolbar.viewModel.toolbarNavIcon)
    }

    private fun setupTravelerWidgetSubscriptions() {
        checkoutPresenter.travelersPresenter.toolbarTitleSubject.subscribe(bundleOverviewHeader.toolbar.viewModel.toolbarTitle)
        checkoutPresenter.travelersPresenter.toolbarNavIconContDescSubject.subscribe(bundleOverviewHeader.toolbar.viewModel.toolbarNavIconContentDesc)
        checkoutPresenter.travelersPresenter.travelerEntryWidget.focusedView.subscribe(bundleOverviewHeader.toolbar.viewModel.currentFocus)
        checkoutPresenter.travelersPresenter.menuVisibility.subscribe(bundleOverviewHeader.toolbar.viewModel.menuVisibility)
        checkoutPresenter.travelersPresenter.toolbarNavIcon.subscribe(bundleOverviewHeader.toolbar.viewModel.toolbarNavIcon)
    }

    private fun setupClickListeners() {
        checkoutButton.setOnClickListener {
            if(shouldShowWebCheckoutView()) {
                println("Inside show web view")
            } else {
                if (currentState == BundleDefault::class.java.name) {
                    showCheckout()
                    slideToPurchaseLayout.visibility = View.VISIBLE
                } else {
                    checkoutPresenter.doHarlemShakes()
                }
            }
        }
    }

    private fun setupBundleOverviewHeader() {
        bundleOverviewHeader.setUpCollapsingToolbar()
        bundleOverviewHeader.toolbar.overflowIcon = ContextCompat.getDrawable(context, R.drawable.ic_create_white_24dp)
        bundleOverviewHeader.toolbar.viewModel.doneClicked.subscribe {
            if (checkoutPresenter.currentState == BillingDetailsPaymentWidget::class.java.name) {
                checkoutPresenter.paymentWidget.doneClicked.onNext(Unit)
            } else if (checkoutPresenter.currentState == checkoutPresenter.travelersPresenter.javaClass.name) {
                checkoutPresenter.travelersPresenter.doneClicked.onNext(Unit)
            }
        }
    }

    private fun setupViewModels() {
        val bottomCheckoutContainerViewModel = BottomCheckoutContainerViewModel(context)
        bottomCheckoutContainerViewModel.slideAllTheWayObservable.subscribe {
            val checkoutViewModel = checkoutPresenter.getCheckoutViewModel()
            if (checkoutViewModel.builder.hasValidCVV()) {
                val params = checkoutViewModel.builder.build()
                if (!ExpediaBookingApp.isAutomation() && !checkoutViewModel.builder.hasValidCheckoutParams()) {
                    (context.applicationContext as ExpediaBookingApp).setCrashlyticsMetadata()
                    Crashlytics.logException(Exception(("User slid to purchase, see params: ${params.toValidParamsMap()}, hasValidParams: ${checkoutViewModel.builder.hasValidParams()}")))
                }
                checkoutViewModel.checkoutParams.onNext(params)
            } else {
                checkoutViewModel.slideAllTheWayObservable.onNext(Unit)
            }
        }
        bottomCheckoutContainer.viewModel = bottomCheckoutContainerViewModel
        bottomCheckoutContainer.totalPriceViewModel = getPriceViewModel(context)
        bottomCheckoutContainer.baseCostSummaryBreakdownViewModel = getCostSummaryBreakdownViewModel()
    }

    private fun setupCreateTripViewModelSubscriptions() {
        checkoutPresenter.getCreateTripViewModel().createTripResponseObservable.safeSubscribeOptional { trip ->
            resetCheckoutState()
        }
    }

    private fun toggleAcceptTermsWidget(showSlider: Boolean, checkoutPresenter: BaseCheckoutPresenter) {
        if (checkoutPresenter.acceptTermsRequired) {
            bottomCheckoutContainer.toggleAcceptTermsWidget(showSlider)
        }
    }

    private fun shouldShowWebCheckoutView(): Boolean {
        return PointOfSale.getPointOfSale().shouldShowWebCheckout() && isShowFlightsCheckoutWebview(context)
    }
}
