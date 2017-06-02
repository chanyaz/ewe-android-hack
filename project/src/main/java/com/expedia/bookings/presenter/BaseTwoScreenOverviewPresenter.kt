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
import com.crashlytics.android.Crashlytics
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.enums.TwoScreenOverviewState
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.utils.isDisabledSTPStateEnabled
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.setAccessibilityHoverFocus
import com.expedia.bookings.widget.AcceptTermsWidget
import com.expedia.bookings.widget.BaseCheckoutPresenter
import com.expedia.bookings.widget.BundleOverviewHeader
import com.expedia.bookings.widget.CVVEntryWidget
import com.expedia.bookings.widget.flights.PaymentFeeInfoWebView
import com.expedia.bookings.widget.packages.BillingDetailsPaymentWidget
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.util.safeSubscribe
import com.expedia.util.setInverseVisibility
import com.expedia.util.unsubscribeOnClick
import com.expedia.util.updateVisibility
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
    abstract fun injectComponents()
    abstract fun getCostSummaryBreakdownViewModel(): BaseCostSummaryBreakdownViewModel
    abstract fun onTripResponse(response: TripResponse?)
    abstract fun getPriceViewModel(context: Context): AbstractUniversalCKOTotalPriceViewModel

    protected abstract fun fireCheckoutOverviewTracking(createTripResponse: TripResponse)

    val disabledSTPStateEnabled = isDisabledSTPStateEnabled()

    val slideTotalText by lazy {
        bottomCheckoutContainer.slideTotalText
    }

    val slideToPurchaseSpace by lazy {
        bottomCheckoutContainer.slideToPurchaseSpace
    }

    val totalPriceWidget by lazy {
        bottomCheckoutContainer.totalPriceWidget
    }

    val slideToPurchaseLayout by lazy {
        bottomCheckoutContainer.slideToPurchaseLayout
    }

    val priceChangeWidget by lazy {
        bottomCheckoutContainer.priceChangeWidget
    }

    val slideToPurchase by lazy {
        bottomCheckoutContainer.slideToPurchase
    }

    val accessiblePurchaseButton by lazy {
        bottomCheckoutContainer.accessiblePurchaseButton
    }

    val bottomContainerDropShadow by lazy {
        bottomCheckoutContainer.bottomContainerDropShadow
    }

    val bottomContainer by lazy {
        bottomCheckoutContainer.bottomContainer
    }

    val checkoutButton by lazy {
        bottomCheckoutContainer.checkoutButton
    }

    val checkoutButtonContainer by lazy {
        bottomCheckoutContainer.checkoutButtonContainer
    }

    val acceptTermsWidget: AcceptTermsWidget by lazy {
        val viewStub = bottomCheckoutContainer.acceptTermsWidget
        val presenter = viewStub.inflate() as AcceptTermsWidget
        presenter.acceptButton.setOnClickListener {
            acceptTermsWidget.vm.acceptedTermsObservable.onNext(true)
            AnimUtils.slideDown(acceptTermsWidget)
            acceptTermsWidget.visibility = View.GONE
            acceptTermsWidget.acceptButton.unsubscribeOnClick()
        }
        presenter
    }

    val ANIMATION_DURATION = 400

    val bundleOverviewHeader: BundleOverviewHeader by bindView(R.id.coordinator_layout)
    protected val checkoutPresenter: BaseCheckoutPresenter by lazy  { findViewById(R.id.checkout_presenter) as BaseCheckoutPresenter }
    val cvv: CVVEntryWidget by bindView(R.id.cvv)
    val toolbarHeight = Ui.getStatusBarHeight(context) + Ui.getToolbarSize(context)

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
        priceChangeWidget.viewmodel = vm
        vm.priceChangeVisibility.subscribe { visible ->
            if (priceChangeWidget.measuredHeight == 0) {
                priceChangeWidget.measure(MeasureSpec.makeMeasureSpec(this.width, MeasureSpec.AT_MOST),
                        MeasureSpec.makeMeasureSpec(this.height, MeasureSpec.UNSPECIFIED))
            }
            val height = priceChangeWidget.measuredHeight
            if (visible) {
                priceChangeWidget.priceChange.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
                AnimUtils.slideInOut(priceChangeWidget, height, object : Animator.AnimatorListener {
                    override fun onAnimationCancel(animation: Animator) {
                    }

                    override fun onAnimationRepeat(animation: Animator) {
                    }

                    override fun onAnimationStart(animation: Animator) {
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        priceChangeWidget.priceChange.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                    }
                })
                AnimUtils.slideInOut(bottomContainerDropShadow, height)
            } else {
                priceChangeWidget.translationY = height.toFloat()
            }
        }
    }

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
        checkoutPresenter.getCheckoutViewModel().sliderPurchaseTotalText.subscribe(bottomCheckoutContainer.viewModel.sliderPurchaseTotalText)
        checkoutPresenter.getCheckoutViewModel().accessiblePurchaseButtonContentDescription.subscribe(bottomCheckoutContainer.viewModel.accessiblePurchaseButtonContentDescription)

        val checkoutPresenterLayoutParams = checkoutPresenter.layoutParams as MarginLayoutParams
        checkoutPresenterLayoutParams.setMargins(0, toolbarHeight, 0, 0)

        checkoutPresenter.cardFeeWarningTextView.setOnClickListener {
            show(paymentFeeInfoWebView)
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
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            if (forward) {
                checkoutPresenter.getCheckoutViewModel().bottomCheckoutContainerStateObservable.onNext(TwoScreenOverviewState.CHECKOUT)
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
        slideToPurchase.resetSlider()
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
        checkoutPresenter.getCheckoutViewModel().cvvCompleted.onNext(cvv)
    }

    override fun back(): Boolean {
        val didHandleBack = super.back()
        if (!didHandleBack) {
            resetPriceChange()
            totalPriceWidget.toggleBundleTotalCompoundDrawable(false)
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
        if (priceChangeWidget.height > 0) {
            scrollspaceheight -= priceChangeWidget.height
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
        priceChangeWidget.viewmodel.priceChangeVisibility.onNext(false)
        checkoutPresenter.getCreateTripViewModel().priceChangeAlertPriceObservable.onNext(null)
    }

    fun resetAndShowTotalPriceWidget() {
        resetPriceChange()
        totalPriceWidget.resetPriceWidget()
    }

    fun hasPriceChange(response: TripResponse?): Boolean {
        return response?.getOldPrice() != null
    }

    private fun toggleBottomContainerViews(state: TwoScreenOverviewState) {
        val showSlider = checkoutPresenter.getCheckoutViewModel().isValidForBooking()
                && state == TwoScreenOverviewState.CHECKOUT
        toggleCheckoutButtonOrSlider(showSlider, state)
        toggleSlideToPurchaseText(showSlider)
        toggleAcceptTermsWidget(showSlider)
        if (showSlider) {
            checkoutPresenter.trackShowSlideToPurchase()
        }
        slideToPurchaseLayout.isFocusable = showSlider
        checkoutPresenter.adjustScrollingSpace(bottomContainer)
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
            if (currentState == BundleDefault::class.java.name) {
                showCheckout()
                slideToPurchaseLayout.visibility = View.VISIBLE
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
                    (context.applicationContext as ExpediaBookingApp).setCrashlyticsMetadata()
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
            priceChangeWidget.viewmodel.originalPrice.onNext(response?.getOldPrice())
            priceChangeWidget.viewmodel.newPrice.onNext(response?.newPrice())
        }
        checkoutPresenter.getCreateTripViewModel().createTripResponseObservable.safeSubscribe { trip ->
            resetCheckoutState()
        }
        checkoutPresenter.getCreateTripViewModel().showPriceChangeWidgetObservable.subscribe(priceChangeWidget.viewmodel.priceChangeVisibility)
        checkoutPresenter.getCreateTripViewModel().performCreateTrip.map { false }.subscribe(priceChangeWidget.viewmodel.priceChangeVisibility)
    }

    private fun toggleCheckoutButtonOrSlider(showSlider: Boolean, state: TwoScreenOverviewState) {
        if (AccessibilityUtil.isTalkBackEnabled(context) && showSlider) {
            //hide the slider for talkback users and show a purchase button
            accessiblePurchaseButton.setText(context.getString(R.string.accessibility_purchase_button))
            accessiblePurchaseButton.visibility = View.VISIBLE
            accessiblePurchaseButton.hideTouchTarget()
            slideToPurchase.visibility = View.GONE
        } else {
            accessiblePurchaseButton.visibility = View.GONE
            if (showSlider) {
                slideToPurchase.visibility = View.VISIBLE
                checkoutButtonContainer.visibility = View.GONE
            } else {
                if (state == TwoScreenOverviewState.BUNDLE || (disabledSTPStateEnabled && state == TwoScreenOverviewState.CHECKOUT)) {
                    checkoutButtonContainer.visibility = View.VISIBLE
                    val checkoutButtonTextColor = ContextCompat.getColor(context, if (state == TwoScreenOverviewState.BUNDLE) {
                        R.color.search_dialog_background_v2
                    } else {
                        R.color.white_disabled
                    })
                    checkoutButton.setTextColor(checkoutButtonTextColor)
                } else {
                    checkoutButtonContainer.visibility = View.GONE
                }
                slideToPurchase.visibility = View.GONE
            }
        }
    }

    private fun toggleSlideToPurchaseText(sliderIsShown: Boolean) {
        val hasText = !checkoutPresenter.getCheckoutViewModel().sliderPurchaseTotalText.value.isNullOrEmpty()
        slideToPurchaseSpace.setInverseVisibility(hasText && sliderIsShown)
        slideTotalText.updateVisibility(hasText && sliderIsShown)
    }

    private fun toggleAcceptTermsWidget(showSlider: Boolean) {
        if (checkoutPresenter.acceptTermsRequired) {
            val termsAccepted = acceptTermsWidget.vm.acceptedTermsObservable.value
            if (!termsAccepted && showSlider) {
                acceptTermsWidget.visibility = View.VISIBLE
            }
        }
    }
}