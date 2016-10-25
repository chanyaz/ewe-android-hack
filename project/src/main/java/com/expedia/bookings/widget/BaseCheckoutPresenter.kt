package com.expedia.bookings.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Rect
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewStub
import android.view.ViewTreeObserver
import android.view.Window
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Space
import com.expedia.bookings.R
import com.expedia.bookings.activity.AccountLibActivity
import com.expedia.bookings.activity.FlightAndPackagesRulesActivity
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.User
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.ScaleTransition
import com.expedia.bookings.presenter.packages.TravelerPresenter
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.TravelerManager
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.UserAccountRefresher
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.setFocusForView
import com.expedia.bookings.widget.packages.BillingDetailsPaymentWidget
import com.expedia.bookings.widget.traveler.TravelerSummaryCard
import com.expedia.util.getCheckoutToolbarTitle
import com.expedia.util.notNullAndObservable
import com.expedia.util.safeSubscribe
import com.expedia.util.setInverseVisibility
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.util.unsubscribeOnClick
import com.expedia.vm.BaseCheckoutViewModel
import com.expedia.vm.BaseCostSummaryBreakdownViewModel
import com.expedia.vm.BaseCreateTripViewModel
import com.expedia.vm.PaymentViewModel
import com.expedia.vm.PriceChangeViewModel
import com.expedia.vm.packages.BundleTotalPriceViewModel
import com.expedia.vm.traveler.CheckoutTravelerViewModel
import com.expedia.vm.traveler.TravelerSummaryViewModel
import rx.Observable
import rx.Subscription
import kotlin.properties.Delegates

abstract class BaseCheckoutPresenter(context: Context, attr: AttributeSet?) : Presenter(context, attr), SlideToWidgetLL.ISlideToListener,
        UserAccountRefresher.IUserAccountRefreshListener, AccountButton.AccountButtonClickListener {

    /** abstract methods **/
    protected abstract fun fireCheckoutOverviewTracking(createTripResponse: TripResponse)
    abstract fun getPaymentWidgetViewModel(): PaymentViewModel
    abstract fun injectComponents()
    abstract fun getLineOfBusiness(): LineOfBusiness
    abstract fun updateDbTravelers()
    abstract fun trackShowSlideToPurchase()
    abstract fun makeCheckoutViewModel(): BaseCheckoutViewModel
    abstract fun makeCreateTripViewModel(): BaseCreateTripViewModel
    abstract fun getCheckoutViewModel(): BaseCheckoutViewModel
    abstract fun getCreateTripViewModel(): BaseCreateTripViewModel
    abstract fun getCostSummaryBreakdownViewModel(): BaseCostSummaryBreakdownViewModel
    abstract fun setupCreateTripViewModel(vm: BaseCreateTripViewModel)
    abstract fun isPassportRequired(response: TripResponse)
    abstract fun showMainTravelerMinimumAgeMessaging(): Boolean

    /** contants **/
    private val ANIMATION_DELAY = 200L
    var sliderHeight = 0f
    var checkoutButtonHeight = 0f
    protected var cardType: PaymentType? = null
    protected var userAccountRefresher = UserAccountRefresher(context, getLineOfBusiness(), this)
    private val checkoutDialog = ProgressDialog(context)
    private val createTripDialog = ProgressDialog(context)
    private var trackShowingCkoOverviewSubscription: Subscription? = null

    /** views **/
    val handle: FrameLayout by bindView(R.id.handle)
    val toolbarDropShadow: View by bindView(R.id.drop_shadow)
    val bottomContainerDropShadow: View by bindView(R.id.bottom_container_drop_shadow)
    val chevron: View by bindView(R.id.chevron)
    val mainContent: LinearLayout by bindView(R.id.main_content)
    val scrollView: ScrollView by bindView(R.id.scrollView)
    val contentView: LinearLayout by bindView(R.id.checkout_widget_container)
    val loginWidget: AccountButton by bindView(R.id.login_widget)
    val cardProcessingFeeTextView: TextView by bindView(R.id.card_processing_fee)
    val cardFeeWarningTextView: TextView by bindView(R.id.card_fee_warning_text)
    val invalidPaymentTypeWarningTextView: TextView by bindView(R.id.invalid_payment_type_warning)
    val debitCardsNotAcceptedTextView: TextView by bindView(R.id.flights_debit_cards_not_accepted)
    val paymentViewStub: ViewStub by bindView(R.id.payment_info_card_view_stub)
    val insuranceWidget: InsuranceWidget by bindView(R.id.insurance_widget)
    val space: Space by bindView(R.id.scrollview_space)
    val legalInformationText: TextView by bindView(R.id.legal_information_text_view)
    val hintContainer: LinearLayout by bindView(R.id.hint_container)
    val depositPolicyText: TextView by bindView(R.id.disclaimer_text)
    val bottomContainer: LinearLayout by bindView(R.id.bottom_container)
    val priceChangeWidget: PriceChangeWidget by bindView(R.id.price_change)
    val totalPriceWidget: TotalPriceWidget by bindView(R.id.total_price_widget)
    val slideToPurchaseLayout: LinearLayout by bindView(R.id.slide_to_purchase_layout)
    val slideToPurchase: SlideToWidgetLL by bindView(R.id.slide_to_purchase_widget)
    val accessiblePurchaseButton: SlideToWidgetLL by bindView(R.id.purchase_button_widget)
    val slideTotalText: TextView by bindView(R.id.purchase_total_text_view)
    val checkoutButtonContainer: View by bindView(R.id.button_container)
    val checkoutButton: Button by bindView(R.id.checkout_button)
    val rootWindow: Window by lazy { (context as Activity).window }
    val decorView: View by lazy { rootWindow.decorView.findViewById(android.R.id.content) }
    var paymentLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null
    var travelerLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null
    var toolbarHeight = Ui.getToolbarSize(context)

    val paymentWidget: PaymentWidget by lazy {
        val presenter = paymentViewStub.inflate() as PaymentWidget
        presenter
    }

    /** viewmodels **/
    protected fun setUpPaymentViewModel() {
        paymentWidget.viewmodel = getPaymentWidgetViewModel()
        paymentWidget.viewmodel.paymentTypeWarningHandledByCkoView.onNext(true)
        paymentWidget.viewmodel.lineOfBusiness.onNext(getLineOfBusiness())
    }

    val travelerSummaryCard: TravelerSummaryCard by lazy {
        val view = findViewById(R.id.traveler_default_state) as TravelerSummaryCard
        view.viewModel = TravelerSummaryViewModel(context)
        view.setOnClickListener {
            openTravelerPresenter()
        }
        view
    }
    val travelerPresenter: TravelerPresenter by lazy {
        val presenter = findViewById(R.id.traveler_presenter) as TravelerPresenter
        presenter.viewModel = CheckoutTravelerViewModel(context, getLineOfBusiness(), showMainTravelerMinimumAgeMessaging())
        presenter.travelerEntryWidget.travelerButton.setLOB(getLineOfBusiness())
        presenter.closeSubject.subscribe {
            show(CheckoutDefault(), FLAG_CLEAR_BACKSTACK)
            presenter.menuVisibility.onNext(false)
        }
        presenter.viewModel.travelerCompletenessStatus.subscribe(travelerSummaryCard.viewModel.travelerStatusObserver)
        presenter.viewModel.allTravelersCompleteSubject.subscribe(getCheckoutViewModel().travelerCompleted)
        presenter.viewModel.invalidTravelersSubject.subscribe(getCheckoutViewModel().clearTravelers)
        presenter
    }

    val acceptTermsRequired = PointOfSale.getPointOfSale().requiresRulesRestrictionsCheckbox()
    val acceptTermsWidget: AcceptTermsWidget by lazy {
        val viewStub = findViewById(R.id.accept_terms_viewStub) as ViewStub
        val presenter = viewStub.inflate() as AcceptTermsWidget
        presenter.acceptButton.setOnClickListener {
            acceptTermsWidget.vm.acceptedTermsObservable.onNext(true)
            AnimUtils.slideDown(acceptTermsWidget)
            acceptTermsWidget.visibility = View.GONE
            acceptTermsWidget.acceptButton.unsubscribeOnClick()
        }
        presenter
    }

    var travelerManager: TravelerManager by Delegates.notNull()

    protected var ckoViewModel: BaseCheckoutViewModel by notNullAndObservable { vm ->
        vm.creditCardRequired.subscribe { required ->
            paymentWidget.viewmodel.isCreditCardRequired.onNext(required)
        }
        vm.legalText.subscribeTextAndVisibility(legalInformationText)
        vm.depositPolicyText.subscribeText(depositPolicyText)
        vm.sliderPurchaseTotalText.subscribeTextAndVisibility(slideTotalText)
        vm.accessiblePurchaseButtonContentDescription.subscribe { accessiblePurchaseButton.contentDescription = it }
        vm.showCheckoutDialogObservable.subscribe { show ->
            if (show) {
                checkoutDialog.show()
            } else {
                checkoutDialog.hide()
            }
        }
        vm.priceChangeObservable.subscribe {
            slideToPurchase.resetSlider()
            animateInSlideToPurchase(true)
        }
        vm.noNetworkObservable.subscribe {
            slideToPurchase.resetSlider()
        }
        vm.cardFeeTextSubject.subscribeText(cardProcessingFeeTextView)
        vm.cardFeeWarningTextSubject.subscribeTextAndVisibility(cardFeeWarningTextView)
    }

    protected var priceChangeViewModel: PriceChangeViewModel by notNullAndObservable { vm ->
        priceChangeWidget.viewmodel = vm
        vm.priceChangeVisibility.subscribe { visible ->
            if (priceChangeWidget.measuredHeight == 0) {
                priceChangeWidget.measure(View.MeasureSpec.makeMeasureSpec(this.width, View.MeasureSpec.AT_MOST),
                        View.MeasureSpec.makeMeasureSpec(this.height, View.MeasureSpec.UNSPECIFIED))
            }
            val height = priceChangeWidget.measuredHeight
            if (visible) {
                AnimUtils.slideInOut(priceChangeWidget, height)
                AnimUtils.slideInOut(bottomContainerDropShadow, height)
            } else {
                priceChangeWidget.translationY = height.toFloat()
            }
        }
    }

    protected var bundleTotalPriceViewModel: BundleTotalPriceViewModel by notNullAndObservable { vm ->
        totalPriceWidget.viewModel = vm
        vm.bundleTotalIncludesObservable.onNext(context.getString(R.string.includes_flights_hotel))
    }

    protected var baseCostSummaryBreakdownViewModel: BaseCostSummaryBreakdownViewModel by notNullAndObservable { vm ->
        totalPriceWidget.breakdown.viewmodel = vm
        vm.iconVisibilityObservable.subscribe { show ->
            totalPriceWidget.toggleBundleTotalCompoundDrawable(show)
            totalPriceWidget.viewModel.costBreakdownEnabledObservable.onNext(show)
        }
    }

    protected var tripViewModel: BaseCreateTripViewModel by notNullAndObservable { vm ->
        vm.performCreateTrip.map { false }.subscribe(priceChangeWidget.viewmodel.priceChangeVisibility)
        vm.showCreateTripDialogObservable.subscribe { show ->
            if (show) {
                createTripDialog.show()
                createTripDialog.setContentView(R.layout.process_dialog_layout)
                AccessibilityUtil.delayedFocusToView(createTripDialog.findViewById(R.id.create_trip_dialog), 0)
                createTripDialog.findViewById(R.id.create_trip_dialog).contentDescription = context.getString(R.string.spinner_text_hotel_create_trip)
            } else {
                createTripDialog.hide()
            }
        }
        setupCreateTripViewModel(vm)
    }

    init {
        View.inflate(context, R.layout.base_checkout_presenter, this)
        injectComponents()
        travelerManager = Ui.getApplication(context).travelerComponent().travelerManager()
        setUpPaymentViewModel()
        setUpViewModels()
        setUpDialogs()
        setClickListeners()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addDefaultTransition(defaultTransition)
        addTransition(defaultToTraveler)
        addTransition(defaultToPayment)
        setUpLayoutListeners()
        setUpErrorMessaging()
        initLoggedInState(User.isLoggedIn(context))
    }

    fun trackShowBundleOverview() {
        trackShowingCkoOverviewSubscription?.unsubscribe()
        val createTripResponse = getCreateTripViewModel().tripResponseObservable.value
        if (createTripResponse != null) {
            fireCheckoutOverviewTracking(createTripResponse)
        }
        else {
            trackShowingCkoOverviewSubscription = getCreateTripViewModel().tripResponseObservable.safeSubscribe { tripResponse ->
                // Un-subscribe:- as we only want to track the initial load of cko overview
                trackShowingCkoOverviewSubscription?.unsubscribe()
                fireCheckoutOverviewTracking(tripResponse!!)
            }
        }
    }

    private fun setUpViewModels() {
        priceChangeViewModel = PriceChangeViewModel(context, getLineOfBusiness())
        baseCostSummaryBreakdownViewModel =  getCostSummaryBreakdownViewModel()
        bundleTotalPriceViewModel = BundleTotalPriceViewModel(context)
        ckoViewModel = makeCheckoutViewModel()
        tripViewModel = makeCreateTripViewModel()
        getCreateTripViewModel().tripResponseObservable.safeSubscribe(getCheckoutViewModel().tripResponseObservable)
        getCheckoutViewModel().cardFeeTripResponse.subscribe(getCreateTripViewModel().tripResponseObservable)
    }

    private fun initLoggedInState(isUserLoggedIn: Boolean) {
        loginWidget.bind(false, isUserLoggedIn, Db.getUser(), getLineOfBusiness())
        hintContainer.visibility = if (isUserLoggedIn) View.GONE else View.VISIBLE
        travelerPresenter.onLogin(isUserLoggedIn)
        paymentWidget.viewmodel.userLogin.onNext(isUserLoggedIn)
        if (isUserLoggedIn) {
            val lp = loginWidget.layoutParams as LinearLayout.LayoutParams
            lp.bottomMargin = resources.getDimension(R.dimen.card_view_container_margin).toInt()
        }
    }

    private fun setUpDialogs() {
        createTripDialog.setCancelable(false)
        createTripDialog.isIndeterminate = true
        checkoutDialog.setMessage(resources.getString(R.string.booking_loading))
        checkoutDialog.setCancelable(false)
        checkoutDialog.isIndeterminate = true
    }

    private fun setClickListeners() {
        handle.setOnTouchListener(HandleTouchListener())
        loginWidget.setListener(this)
        slideToPurchase.addSlideToListener(this)

        legalInformationText.setOnClickListener {
            context.startActivity(FlightAndPackagesRulesActivity.createIntent(context, getLineOfBusiness()))
        }

        accessiblePurchaseButton.setOnClickListener {
            if (ckoViewModel.builder.hasValidParams()) {
                ckoViewModel.checkoutParams.onNext(ckoViewModel.builder.build())
            } else {
                ckoViewModel.slideAllTheWayObservable.onNext(Unit)
            }
        }
    }

    private fun setUpLayoutListeners() {
        slideToPurchaseLayout.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                sliderHeight = slideToPurchaseLayout.height.toFloat()
                if (sliderHeight != 0f) {
                    slideToPurchaseLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    bottomContainer.translationY = sliderHeight
                }
            }
        })
        checkoutButtonContainer.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                checkoutButtonHeight = checkoutButtonContainer.height.toFloat()
                if (sliderHeight != 0f) {
                    checkoutButtonContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    checkoutButtonContainer.translationY = checkoutButtonHeight
                }
            }
        })
        paymentLayoutListener = makeKeyboardListener(scrollView)
        travelerLayoutListener = makeKeyboardListener(travelerPresenter.travelerEntryWidget, toolbarHeight * 2)
    }

    private fun setUpErrorMessaging() {
        Observable.combineLatest(
                paymentWidget.viewmodel.showingPaymentForm,
                paymentWidget.viewmodel.invalidPaymentTypeWarning,
                { showingGuestPaymentForm, invalidPaymentTypeWarning ->
                    val hasPaymentTypeWarning = invalidPaymentTypeWarning.isNotBlank()
                    val visibility = if (hasPaymentTypeWarning && showingGuestPaymentForm) View.VISIBLE else View.GONE
                    invalidPaymentTypeWarningTextView.text = invalidPaymentTypeWarning
                    if (visibility == View.VISIBLE && invalidPaymentTypeWarningTextView.visibility == View.GONE) {
                        invalidPaymentTypeWarningTextView.visibility = visibility
                        AnimUtils.slideIn(invalidPaymentTypeWarningTextView)
                        toolbarDropShadow.visibility = visibility
                    } else if (visibility == View.GONE && invalidPaymentTypeWarningTextView.visibility == View.VISIBLE) {
                        AnimUtils.slideOut(invalidPaymentTypeWarningTextView)
                    }
                }).subscribe()

        Observable.combineLatest( getCheckoutViewModel().paymentTypeSelectedHasCardFee,
                paymentWidget.viewmodel.showingPaymentForm,
                { haveCardFee, showingGuestPaymentForm ->
                    val cardFeeVisibility = if (haveCardFee && showingGuestPaymentForm) View.VISIBLE else View.GONE
                    if (cardFeeVisibility == View.VISIBLE && cardProcessingFeeTextView.visibility == View.GONE) {
                        cardProcessingFeeTextView.visibility = cardFeeVisibility
                        AnimUtils.slideIn(cardProcessingFeeTextView)
                        toolbarDropShadow.visibility = cardFeeVisibility
                    } else if (cardFeeVisibility == View.GONE && cardProcessingFeeTextView.visibility == View.VISIBLE) {
                        AnimUtils.slideOut(cardProcessingFeeTextView)
                    }
                }).subscribe()
    }

    /** Presenter Transitions **/
    class CheckoutDefault

    private val defaultTransition = object : Presenter.DefaultTransition(CheckoutDefault::class.java.name) {
        override fun endTransition(forward: Boolean) {
            loginWidget.bind(false, User.isLoggedIn(context), Db.getUser(), getLineOfBusiness())
            paymentWidget.show(PaymentWidget.PaymentDefault(), Presenter.FLAG_CLEAR_BACKSTACK)
            updateTravelerPresenter()
            if (forward) {
                setToolbarTitle()
            }
        }
    }

    private val defaultToTraveler = object : ScaleTransition(this, mainContent, travelerPresenter, CheckoutDefault::class.java, TravelerPresenter::class.java) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            bottomContainer.visibility = if (forward) GONE else VISIBLE
            if (!forward) {
                Ui.hideKeyboard(travelerPresenter)
                travelerPresenter.toolbarNavIconContDescSubject.onNext(resources.getString(R.string.toolbar_nav_icon_cont_desc))
                travelerPresenter.viewModel.updateCompletionStatus()
                setToolbarTitle()
                decorView.viewTreeObserver.removeOnGlobalLayoutListener(travelerLayoutListener)
                travelerPresenter.toolbarTitleSubject.onNext(getCheckoutToolbarTitle(resources, Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelSecureCheckoutMessaging)))
            } else {
                decorView.viewTreeObserver.addOnGlobalLayoutListener(travelerLayoutListener)
            }
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            if (!forward) {
                animateInSlideToPurchase(true)
                travelerPresenter.setFocusForView()
                travelerSummaryCard.setFocusForView()
                decorView.viewTreeObserver.removeOnGlobalLayoutListener(travelerLayoutListener)
            } else {
                val lp = space.layoutParams
                lp.height = 0
                space.layoutParams = lp
            }

        }
    }

    private val defaultToPayment = object : Presenter.Transition(CheckoutDefault::class.java, BillingDetailsPaymentWidget::class.java) {

        override fun startTransition(forward: Boolean) {
            handle.setInverseVisibility(forward)
            loginWidget.setInverseVisibility(forward)
            hintContainer.visibility = if (forward) View.GONE else if (User.isLoggedIn(getContext())) View.GONE else View.VISIBLE
            travelerSummaryCard.visibility = if (forward) View.GONE else View.VISIBLE
            legalInformationText.setInverseVisibility(forward)
            depositPolicyText.setInverseVisibility(forward)
            bottomContainer.setInverseVisibility(forward)
            if (!forward) {
                Ui.hideKeyboard(paymentWidget)
                invalidPaymentTypeWarningTextView.visibility = View.GONE
                cardProcessingFeeTextView.visibility  = View.GONE
                debitCardsNotAcceptedTextView.visibility = View.GONE
                paymentWidget.show(PaymentWidget.PaymentDefault(), Presenter.FLAG_CLEAR_BACKSTACK)
                scrollView.layoutParams.height = height
                paymentWidget.viewmodel.showingPaymentForm.onNext(false)
            } else {
                decorView.viewTreeObserver.addOnGlobalLayoutListener(paymentLayoutListener)
            }
            setInsuranceWidgetVisibility(!forward)
        }

        override fun endTransition(forward: Boolean) {
            if (!forward) {
                animateInSlideToPurchase(true)
                paymentWidget.setFocusForView()
                decorView.viewTreeObserver.removeOnGlobalLayoutListener(paymentLayoutListener)
            } else {
                val lp = space.layoutParams
                lp.height = 0
                space.layoutParams = lp
            }
            ckoViewModel.showingPaymentWidgetSubject.onNext(forward)
        }
    }

    fun showPaymentPresenter() {
        show(paymentWidget)
    }

    fun openTravelerPresenter() {
        show(travelerPresenter)
        travelerPresenter.showSelectOrEntryState()
    }

    open fun updateTravelerPresenter() {
        travelerPresenter.viewModel.refresh()
    }

    private fun setToolbarTitle() {
        travelerPresenter.toolbarTitleSubject.onNext(getCheckoutToolbarTitle(resources, Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelSecureCheckoutMessaging)))
    }

    open fun setInsuranceWidgetVisibility(visible: Boolean) {
        insuranceWidget.visibility = View.GONE
    }

    /** Slide to book **/
    override fun onSlideStart() { }
    override fun onSlideProgress(pixels: Float, total: Float) { }
    override fun onSlideAllTheWay() {
        if (ckoViewModel.builder.hasValidParams()) {
            ckoViewModel.checkoutParams.onNext(ckoViewModel.builder.build())
        } else {
            ckoViewModel.slideAllTheWayObservable.onNext(Unit)
        }
    }

    override fun onSlideAbort() {
        slideToPurchase.resetSlider()
    }

    /** User account refresher **/
    override fun onUserAccountRefreshed() {
        tripViewModel.performCreateTrip.onNext(Unit)
    }

    override fun accountLoginClicked() {
        val args = AccountLibActivity.createArgumentsBundle(getLineOfBusiness(), CheckoutLoginExtender())
        User.signIn(context as Activity, args)
    }

    override fun accountLogoutClicked() {
        User.signOut(context)
        animateInSlideToPurchase(false)
        updateDbTravelers()
        initLoggedInState(false)
        updateTravelerPresenter()
        tripViewModel.performCreateTrip.onNext(Unit)
    }

    fun onLoginSuccess() {
        animateInSlideToPurchase(true)
        updateDbTravelers()
        initLoggedInState(true)
        tripViewModel.performCreateTrip.onNext(Unit)
    }

    fun animateInSlideToPurchase(visible: Boolean) {
        if (AccessibilityUtil.isTalkBackEnabled(context) && visible) {
            //hide the slider for talkback users and show a purchase button
            accessiblePurchaseButton.setText(context.getString(R.string.accessibility_purchase_button))
            accessiblePurchaseButton.visibility = View.VISIBLE
            accessiblePurchaseButton.hideTouchTarget()
            slideToPurchase.visibility = View.GONE
        }
        else {
            slideToPurchase.visibility = View.VISIBLE
            accessiblePurchaseButton.visibility = View.GONE
        }
        val isSlideToPurchaseLayoutVisible = visible && ckoViewModel.isValidForBooking()
        val termsAccepted = acceptTermsWidget.vm.acceptedTermsObservable.value

        if (acceptTermsRequired && !termsAccepted && isSlideToPurchaseLayoutVisible) {
            acceptTermsWidget.visibility = View.VISIBLE
        }
        if (isSlideToPurchaseLayoutVisible) {
            trackShowSlideToPurchase()
        }
        slideToPurchaseLayout.isFocusable = isSlideToPurchaseLayoutVisible
        val distance = if (!isSlideToPurchaseLayoutVisible) slideToPurchaseLayout.height.toFloat() else 0f
        if (bottomContainer.translationY == distance) {
            adjustScrollingSpace()
            return
        }
        val animator = ObjectAnimator.ofFloat(bottomContainer, "translationY", distance)
        animator.duration = 300
        animator.start()
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationCancel(p0: Animator?) {
            }

            override fun onAnimationStart(p0: Animator?) {
            }

            override fun onAnimationRepeat(p0: Animator?) {
            }

            override fun onAnimationEnd(p0: Animator?) {
                adjustScrollingSpace()
            }
        })

    }

    fun clearPaymentInfo() {
        if (!User.isLoggedIn(context)) {
            paymentWidget.reset()
            paymentWidget.clearCCAndCVV()
        }
    }

    fun toggleCheckoutButton(isEnabled: Boolean) {
        checkoutButtonContainer.translationY = if (isEnabled) 0f else checkoutButtonHeight
        val shouldShowSlider = currentState == CheckoutDefault::class.java.name && ckoViewModel.isValidForBooking()
        bottomContainer.translationY = if (isEnabled) sliderHeight - checkoutButtonHeight else if (shouldShowSlider) 0f else sliderHeight
        checkoutButton.isEnabled = isEnabled
    }

    /** Checkout sliding gesture detectors and animations **/
    val gestureDetector = GestureDetector(context, SingleTapUp())

    private fun rotateChevron(distance: Float) {
        val distanceGoal = height
        mainContent.translationY = distance
        chevron.rotation = Math.min(1f, distance / distanceGoal) * (180)
        ckoViewModel.checkoutTranslationObserver.onNext(distance)
    }

    private fun animCheckoutToTop() {
        val distanceGoal = height
        val animator = ObjectAnimator.ofFloat(mainContent, "translationY", mainContent.translationY, 0f)
        animator.duration = 400L
        animator.addUpdateListener({ anim ->
            chevron.rotation = Math.min(1f, mainContent.translationY / distanceGoal) * (180)
            ckoViewModel.checkoutTranslationObserver.onNext(mainContent.translationY)
        })
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                ckoViewModel.checkoutTranslationObserver.onNext(mainContent.translationY)
                toolbarDropShadow.visibility = View.VISIBLE
            }
        })
        animator.start()
    }

    private inner class SingleTapUp : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            return true
        }

        override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
            return true
        }
    }

    private inner class HandleTouchListener() : View.OnTouchListener {
        internal var originY: Float = 0.toFloat()
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            if (gestureDetector.onTouchEvent(event)) {
                (context as AppCompatActivity).onBackPressed()
                return true
            }
            when (event.action) {
                (MotionEvent.ACTION_DOWN) -> {
                    originY = event.rawY
                    toolbarDropShadow.visibility = View.GONE
                }
                (MotionEvent.ACTION_UP) -> {
                    val diff = event.rawY - originY
                    val distance = Math.max(diff, 0f)
                    val distanceGoal = height / 3f
                    if (distance > distanceGoal) {
                        (context as AppCompatActivity).onBackPressed()
                    } else {
                        animCheckoutToTop()
                    }
                    originY = 0f
                }
                (MotionEvent.ACTION_MOVE) -> {
                    val diff = event.rawY - originY
                    rotateChevron(Math.max(diff, 0f))
                }
            }
            return true
        }
    }

    fun resetPriceChange() {
        priceChangeWidget.viewmodel.priceChangeVisibility.onNext(false)
    }

    fun resetAndShowTotalPriceWidget() {
        resetPriceChange()
        totalPriceWidget.resetPriceWidget()
    }

    fun resetTravelers() {
        travelerPresenter.resetTravelers()
        if (!User.isLoggedIn(context)) {
            ckoViewModel.clearTravelers.onNext(Unit)
            updateTravelerPresenter()
        }
    }

    private fun makeKeyboardListener(scrollView: ScrollView, offset: Int = toolbarHeight): ViewTreeObserver.OnGlobalLayoutListener {
        val rootWindow = (context as Activity).window
        val layoutListener = (ViewTreeObserver.OnGlobalLayoutListener {
            val decorView = rootWindow.decorView
            val windowVisibleDisplayFrameRect = Rect()
            decorView.getWindowVisibleDisplayFrame(windowVisibleDisplayFrameRect)
            val location = IntArray(2)
            scrollView.getLocationOnScreen(location)
            val lp = scrollView.layoutParams
            val newHeight = windowVisibleDisplayFrameRect.bottom - windowVisibleDisplayFrameRect.top - offset

            if (lp.height != newHeight) {
                lp.height = newHeight
                scrollView.layoutParams = lp
            }
        })

        return layoutListener
    }

    /** Scrolling Space **/

    fun adjustScrollingSpace() {
        postDelayed({
            val contentViewLocation = IntArray(2)
            val bottomContainerLocation = IntArray(2)
            val scrollViewContainerLocation = IntArray(2)
            contentView.getLocationOnScreen(contentViewLocation)
            bottomContainer.getLocationOnScreen(bottomContainerLocation)
            scrollView.getLocationOnScreen(scrollViewContainerLocation)

            val contentViewBottomPosition = contentViewLocation[1] + contentView.height - space.height
            val bottomContainerTopPosition = bottomContainerLocation[1]
            val spaceHeight = Math.max(0, scrollViewContainerLocation[1] + scrollView.height - contentViewBottomPosition)
            val distance = Math.max(0, contentViewBottomPosition - bottomContainerTopPosition) + spaceHeight

            val lp = space.layoutParams
            lp.height = distance
            space.layoutParams = lp
        }, ANIMATION_DELAY)
    }

    override fun addWindowSubscriptions() {
        super.addWindowSubscriptions()
        addWindowSubscription(travelerManager.travelersUpdated.subscribe { travelerPresenter.resetTravelers() })
        addWindowSubscription(paymentWidget.viewmodel.cardTypeSubject.subscribe { paymentType -> cardType = paymentType })
        addWindowSubscription(paymentWidget.viewmodel.expandObserver.subscribe { showPaymentPresenter() })
        addWindowSubscription(paymentWidget.viewmodel.billingInfoAndStatusUpdate.map { it.first }.subscribe(ckoViewModel.paymentCompleted))
    }

    override fun unsubscribeWindowAtTeardown() {
        super.unsubscribeWindowAtTeardown()
        getCheckoutViewModel().unsubscribeAll()
    }
}
