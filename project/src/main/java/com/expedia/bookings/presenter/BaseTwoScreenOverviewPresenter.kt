package com.expedia.bookings.presenter

import android.animation.Animator
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
import android.widget.Button
import android.widget.LinearLayout
import com.crashlytics.android.Crashlytics
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.utils.*
import com.expedia.bookings.widget.BaseCheckoutPresenter
import com.expedia.bookings.widget.BundleOverviewHeader
import com.expedia.bookings.widget.CVVEntryWidget
import com.expedia.bookings.widget.flights.PaymentFeeInfoWebView
import com.expedia.bookings.widget.packages.BillingDetailsPaymentWidget
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.util.safeSubscribe
import com.expedia.util.setInverseVisibility
import com.expedia.vm.AbstractCardFeeEnabledCheckoutViewModel
import com.expedia.vm.BaseCostSummaryBreakdownViewModel
import com.expedia.vm.PriceChangeViewModel
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
    abstract fun getCostSummaryBreakdownViewModel(): BaseCostSummaryBreakdownViewModel
    abstract fun onTripResponse(response: TripResponse?)
    abstract fun getPriceViewModel(context: Context): AbstractUniversalCKOTotalPriceViewModel

    protected abstract fun fireCheckoutOverviewTracking(createTripResponse: TripResponse)

    val disabledSTPStateEnabled = isDisabledSTPStateEnabled(context)

    val totalPriceWidget by lazy{
        bottomCheckoutContainer.totalPriceWidget
    }

    val ANIMATION_DURATION = 400
    var checkoutButtonHeight = 0f

    val bundleOverviewHeader: BundleOverviewHeader by bindView(R.id.coordinator_layout)
    protected val checkoutPresenter: BaseCheckoutPresenter by lazy  { findViewById(R.id.checkout_presenter) as BaseCheckoutPresenter }
    val cvv: CVVEntryWidget by bindView(R.id.cvv)
    val toolbarHeight = Ui.getStatusBarHeight(context) + Ui.getToolbarSize(context)

    val checkoutButtonContainer: View by bindView(R.id.button_container)
    val checkoutButton: Button by bindView(R.id.checkout_button)
    val bottomContainer: LinearLayout by bindView(R.id.bottom_container)
    val bottomContainerDropShadow: View by bindView(R.id.bottom_container_drop_shadow)
    val bottomCheckoutContainer: BottomCheckoutContainer by bindView(R.id.bottom_checkout_container)

    var scrollSpaceView: View? = null
    var overviewLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null
    private var trackShowingCkoOverviewSubscription: Subscription? = null

    val paymentFeeInfoWebView: PaymentFeeInfoWebView by lazy {
        val viewStub = findViewById(R.id.payment_fee_info_webview_stub) as ViewStub
        val airlineFeeWebview = viewStub.inflate() as PaymentFeeInfoWebView
        airlineFeeWebview.setExitButtonOnClickListener(View.OnClickListener { this.back() })
        airlineFeeWebview.viewModel = WebViewViewModel()
        (checkoutPresenter.getCheckoutViewModel() as AbstractCardFeeEnabledCheckoutViewModel).obFeeDetailsUrlSubject.subscribe(airlineFeeWebview.viewModel.webViewURLObservable)
        airlineFeeWebview
    }


    protected var priceChangeViewModel: PriceChangeViewModel by notNullAndObservable { vm ->
        bottomCheckoutContainer.priceChangeWidget.viewmodel = vm
        vm.priceChangeVisibility.subscribe { visible ->
            if (bottomCheckoutContainer.priceChangeWidget.measuredHeight == 0) {
                bottomCheckoutContainer.priceChangeWidget.measure(MeasureSpec.makeMeasureSpec(this.width, MeasureSpec.AT_MOST),
                        MeasureSpec.makeMeasureSpec(this.height, MeasureSpec.UNSPECIFIED))
            }
            val height = bottomCheckoutContainer.priceChangeWidget.measuredHeight
            if (visible) {
                bottomCheckoutContainer.priceChangeWidget.priceChange.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
                AnimUtils.slideInOut(bottomCheckoutContainer.priceChangeWidget, height, object : Animator.AnimatorListener {
                    override fun onAnimationCancel(animation: Animator) {
                    }

                    override fun onAnimationRepeat(animation: Animator) {
                    }

                    override fun onAnimationStart(animation: Animator) {
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        bottomCheckoutContainer.priceChangeWidget.priceChange.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                    }
                })
                AnimUtils.slideInOut(bottomContainerDropShadow, height)
            } else {
                bottomCheckoutContainer.priceChangeWidget.translationY = height.toFloat()
            }
        }
    }

    protected var totalPriceViewModel: AbstractUniversalCKOTotalPriceViewModel by notNullAndObservable { vm ->
        bottomCheckoutContainer.totalPriceWidget.viewModel = vm
        if (ProductFlavorFeatureConfiguration.getInstance().shouldShowPackageIncludesView())
            vm.bundleTotalIncludesObservable.onNext(context.getString(R.string.includes_flights_hotel))
    }

    protected var baseCostSummaryBreakdownViewModel: BaseCostSummaryBreakdownViewModel by notNullAndObservable { vm ->
        bottomCheckoutContainer.totalPriceWidget.breakdown.viewmodel = vm
        vm.iconVisibilityObservable.safeSubscribe { show ->
            bottomCheckoutContainer.totalPriceWidget.toggleBundleTotalCompoundDrawable(show)
            bottomCheckoutContainer.totalPriceWidget.viewModel.costBreakdownEnabledObservable.onNext(show)
        }
    }

    init {
        inflate()
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

    fun showCheckout() {
        resetCheckoutState()
        show(checkoutPresenter, FLAG_CLEAR_TOP)
        checkoutPresenter.show(BaseCheckoutPresenter.CheckoutDefault(), FLAG_CLEAR_BACKSTACK)
        trackCheckoutPageLoad()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        val variateForTest = Db.getAbacusResponse().variateForTest(AbacusUtils.EBAndroidAppCheckoutButtonText)
        if (variateForTest == AbacusUtils.DefaultTwoVariant.VARIANT1.ordinal) {
            checkoutButton.text = context.getString(R.string.continue_booking)
        } else if (variateForTest == AbacusUtils.DefaultTwoVariant.VARIANT2.ordinal) {
            checkoutButton.text = context.getString(R.string.next)
        }
        addDefaultTransition(defaultTransition)
        addTransition(checkoutTransition)
        addTransition(checkoutToCvv)
        addTransition(overviewToAirlineFeeWebView)
        show(BundleDefault())
        cvv.setCVVEntryListener(this)
        checkoutPresenter.getCheckoutViewModel().slideAllTheWayObservable.subscribe(checkoutSliderSlidObserver)
        checkoutPresenter.getCheckoutViewModel().checkoutParams.subscribe { cvv.enableBookButton(false) }
        setUpLayoutListeners()

        val checkoutPresenterLayoutParams = checkoutPresenter.layoutParams as MarginLayoutParams
        checkoutPresenterLayoutParams.setMargins(0, toolbarHeight, 0, 0)

        checkoutPresenter.cardFeeWarningTextView.setOnClickListener {
            show(paymentFeeInfoWebView)
        }
        setupCreateTripViewModelSubscriptions()
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
            if (disabledSTPStateEnabled) {
                toggleCheckoutButtonAndSliderVisibility(!forward)
            }
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
            if (!disabledSTPStateEnabled) {
                toggleCheckoutButtonAndSliderVisibility(!forward)
            }

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
            val checkoutButtonTextColor = ContextCompat.getColor(context, if (forward) {
                R.color.white_disabled
            } else {
                R.color.search_dialog_background_v2
            })
            checkoutButton.setTextColor(checkoutButtonTextColor)
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
//        if (!disabledSTPStateEnabled) {
//            checkoutPresenter.sliderHeight = bottomCheckoutContainer.slideToPurchaseLayout.height.toFloat()
//            val hasCompleteInfo = checkoutPresenter.getCheckoutViewModel().isValidForBooking()
//            val bottomDistance = checkoutPresenter.sliderHeight - checkoutButtonHeight
//            val slideIn = if (hasCompleteInfo) {
//                bottomDistance - (f * (bottomDistance))
//            } else {
//                checkoutPresenter.sliderHeight - ((1 - f) * checkoutButtonHeight)
//            }
//            val slideOut = if (hasCompleteInfo) {
//                f * (bottomDistance)
//            } else {
//                checkoutPresenter.sliderHeight - (f * checkoutButtonHeight)
//            }
//            bottomContainer.translationY = if (forward) slideIn else slideOut
//            checkoutButtonContainer.translationY = if (forward) f * checkoutButtonHeight else (1 - f) * checkoutButtonHeight
//        }
    }

    open protected fun resetCheckoutState() {
        bottomCheckoutContainer.slideToPurchase.resetSlider()
        if (currentState == BundleDefault::class.java.name) {
            bundleOverviewHeader.toggleOverviewHeader(true)
            toggleCheckoutButtonAndSliderVisibility(true)
        }
    }

    private val checkoutToCvv = object : VisibilityTransition(this, checkoutPresenter.javaClass, CVVEntryWidget::class.java) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            bundleOverviewHeader.visibility = if (forward) View.GONE else View.VISIBLE
            if (!forward) {
                bottomCheckoutContainer.slideToPurchase.resetSlider()
                bottomCheckoutContainer.slideToPurchaseLayout.setAccessibilityHoverFocus()
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
            bottomCheckoutContainer.totalPriceWidget.toggleBundleTotalCompoundDrawable(false)
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
        if (bottomCheckoutContainer.slideToPurchaseLayout.height > 0) {
            scrollspaceheight -= bottomCheckoutContainer.slideToPurchaseLayout.height
        }
        if (bottomCheckoutContainer.priceChangeWidget.height > 0) {
            scrollspaceheight -= bottomCheckoutContainer.priceChangeWidget.height
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

    private fun setUpLayoutListeners() {
//        bottomCheckoutContainer.slideToPurchaseLayout.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
//            override fun onGlobalLayout() {
//                checkoutPresenter.sliderHeight = bottomCheckoutContainer.slideToPurchaseLayout.height.toFloat()
//                if (checkoutPresenter.sliderHeight != 0f) {
//                    bottomCheckoutContainer.slideToPurchaseLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
//                    bottomContainer.translationY = checkoutPresenter.sliderHeight
//                }
//            }
//        })
//        checkoutButtonContainer.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
//            override fun onGlobalLayout() {
//                checkoutButtonHeight = checkoutButtonContainer.height.toFloat()
//                if (checkoutPresenter.sliderHeight != 0f) {
//                    checkoutButtonContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)
//                    checkoutButtonContainer.translationY = checkoutButtonHeight
//                }
//            }
//        })
    }

    fun toggleCheckoutButtonAndSliderVisibility(showCheckoutButton: Boolean) {
        val shouldShowSlider = !showCheckoutButton && checkoutPresenter.getCheckoutViewModel().isValidForBooking()
                && checkoutPresenter.currentState == BaseCheckoutPresenter.CheckoutDefault::class.java.name

        setUpBottomContainerState(shouldShowSlider, showCheckoutButton)
    }

    private fun setUpBottomContainerState(shouldShowSlider: Boolean, showCheckoutButton: Boolean) {
//        if (disabledSTPStateEnabled) {
//            checkoutButtonContainer.translationY = 0f
//            if (shouldShowSlider) {
//                checkoutButtonContainer.translationY = checkoutButtonHeight
//                bottomContainer.translationY = 0f
//            } else {
//                bottomContainer.translationY = checkoutPresenter.sliderHeight - checkoutButtonHeight
//            }
//        } else {
//            checkoutButtonContainer.translationY = if (showCheckoutButton) 0f else checkoutButtonHeight
//            bottomContainer.translationY = if (showCheckoutButton) checkoutPresenter.sliderHeight - checkoutButtonHeight else if (shouldShowSlider) 0f else checkoutPresenter.sliderHeight
//            checkoutButton.isEnabled = showCheckoutButton
//        }
    }

    fun resetPriceChange() {
        bottomCheckoutContainer.priceChangeWidget.viewmodel.priceChangeVisibility.onNext(false)
        checkoutPresenter.getCreateTripViewModel().priceChangeAlertPriceObservable.onNext(null)
    }

    fun resetAndShowTotalPriceWidget() {
        resetPriceChange()
        bottomCheckoutContainer.totalPriceWidget.resetPriceWidget()
    }

    fun hasPriceChange(response: TripResponse?): Boolean {
        return response?.getOldPrice() != null
    }

    private fun animateInSlideToPurchase(visible: Boolean) {
        if (AccessibilityUtil.isTalkBackEnabled(context) && visible) {
            //hide the slider for talkback users and show a purchase button
            bottomCheckoutContainer.accessiblePurchaseButton.setText(context.getString(R.string.accessibility_purchase_button))
            bottomCheckoutContainer.accessiblePurchaseButton.visibility = View.VISIBLE
            bottomCheckoutContainer.accessiblePurchaseButton.hideTouchTarget()
            bottomCheckoutContainer.slideToPurchase.visibility = View.GONE
        } else {
            bottomCheckoutContainer.accessiblePurchaseButton.visibility = View.GONE
        }
        val isSlideToPurchaseLayoutVisible = visible && checkoutPresenter.getCheckoutViewModel().isValidForBooking()

        if (isSlideToPurchaseLayoutVisible) {
            bottomCheckoutContainer.slideToPurchase.visibility = View.VISIBLE
        } else {
            bottomCheckoutContainer.slideToPurchase.visibility = View.GONE
        }
        if (checkoutPresenter.acceptTermsRequired) {
            val termsAccepted = checkoutPresenter.acceptTermsWidget.vm.acceptedTermsObservable.value
            if (!termsAccepted && isSlideToPurchaseLayoutVisible) {
                checkoutPresenter.acceptTermsWidget.visibility = View.VISIBLE
            }
        }
        if (isSlideToPurchaseLayoutVisible) {
            checkoutPresenter.trackShowSlideToPurchase()
        }
        bottomCheckoutContainer.slideToPurchaseLayout.isFocusable = isSlideToPurchaseLayoutVisible
        checkoutPresenter.adjustScrollingSpace(bottomContainer)

        if (!disabledSTPStateEnabled) {

        } else {
            setUpBottomContainerState(isSlideToPurchaseLayoutVisible, !visible)
        }
    }

    fun trackShowBundleOverview() {
        trackShowingCkoOverviewSubscription?.unsubscribe()
        val createTripResponse = checkoutPresenter.getCreateTripViewModel().createTripResponseObservable.value
        if (createTripResponse != null) {
            fireCheckoutOverviewTracking(createTripResponse)
        } else {
            trackShowingCkoOverviewSubscription = checkoutPresenter.getCreateTripViewModel().createTripResponseObservable.safeSubscribe { tripResponse ->
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
            if (disabledSTPStateEnabled) {
                checkoutButtonContainer.setInverseVisibility(forward)
            }
        }
        checkoutPresenter.getCheckoutViewModel().animateInSlideToPurchaseObservable.subscribe { isVisible ->
            animateInSlideToPurchase(isVisible)
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
            if (currentState == BundleDefault::class.java.name) {
                showCheckout()
                bottomCheckoutContainer.slideToPurchaseLayout.visibility = View.VISIBLE
            } else {
                checkoutPresenter.doHarlemShakes()
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
        val bottomCheckoutContainerViewModel = BottomCheckoutContainerViewModel()
        bottomCheckoutContainerViewModel.slideAllTheWayObservable.subscribe{
            val checkoutViewModel = checkoutPresenter.getCheckoutViewModel()
            if (checkoutViewModel.builder.hasValidCVV()) {
                val params = checkoutViewModel.builder.build()
                if (!ExpediaBookingApp.isAutomation() && !checkoutViewModel.builder.hasValidCheckoutParams()) {
                    Crashlytics.logException(Exception(("User slid to purchase, see params: ${params.toValidParamsMap()}, hasValidParams: ${checkoutViewModel.builder.hasValidParams()}")))
                }
                checkoutViewModel.checkoutParams.onNext(params)
            } else {
                checkoutViewModel.slideAllTheWayObservable.onNext(Unit)
            }
        }
        bottomCheckoutContainer.viewModel = bottomCheckoutContainerViewModel

        priceChangeViewModel = PriceChangeViewModel(context, checkoutPresenter.getLineOfBusiness())
        totalPriceViewModel = getPriceViewModel(context)
        baseCostSummaryBreakdownViewModel = getCostSummaryBreakdownViewModel()
    }

    private fun setupCreateTripViewModelSubscriptions() {
        checkoutPresenter.getCreateTripViewModel().updatePriceChangeWidgetObservable.subscribe { response ->
            bottomCheckoutContainer.priceChangeWidget.viewmodel.originalPrice.onNext(response?.getOldPrice())
            bottomCheckoutContainer.priceChangeWidget.viewmodel.newPrice.onNext(response?.newPrice())
        }
        checkoutPresenter.getCreateTripViewModel().createTripResponseObservable.safeSubscribe { trip ->
            resetCheckoutState()
        }
        checkoutPresenter.getCreateTripViewModel().showPriceChangeWidgetObservable.subscribe(bottomCheckoutContainer.priceChangeWidget.viewmodel.priceChangeVisibility)
        checkoutPresenter.getCreateTripViewModel().performCreateTrip.map { false }.subscribe(bottomCheckoutContainer.priceChangeWidget.viewmodel.priceChangeVisibility)
    }
}