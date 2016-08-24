package com.expedia.bookings.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
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
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Space
import com.activeandroid.util.Log
import com.expedia.bookings.R
import com.expedia.bookings.activity.AccountLibActivity
import com.expedia.bookings.activity.FlightAndPackagesRulesActivity
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.User
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.ScaleTransition
import com.expedia.bookings.presenter.packages.TravelerPresenter
import com.expedia.bookings.services.InsuranceServices
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.TravelerManager
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.UserAccountRefresher
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.setFocusForView
import com.expedia.bookings.widget.packages.BillingDetailsPaymentWidget
import com.expedia.bookings.widget.traveler.TravelerDefaultState
import com.expedia.util.getCheckoutToolbarTitle
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.vm.BaseCheckoutViewModel
import com.expedia.vm.BaseCostSummaryBreakdownViewModel
import com.expedia.vm.BaseCreateTripViewModel
import com.expedia.vm.InsuranceViewModel
import com.expedia.vm.PaymentViewModel
import com.expedia.vm.PriceChangeViewModel
import com.expedia.vm.packages.BundleTotalPriceViewModel
import com.expedia.vm.traveler.CheckoutTravelerViewModel
import com.expedia.vm.traveler.TravelerSummaryViewModel
import rx.Observable
import kotlin.properties.Delegates

abstract class BaseCheckoutPresenter(context: Context, attr: AttributeSet) : Presenter(context, attr), SlideToWidgetLL.ISlideToListener,
        UserAccountRefresher.IUserAccountRefreshListener, AccountButton.AccountButtonClickListener {
    lateinit var insuranceServices: InsuranceServices
    lateinit var travelerManager: TravelerManager

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

    var paymentWidget: PaymentWidget by Delegates.notNull()
    val travelerDefaultState: TravelerDefaultState by lazy {
        val view = findViewById(R.id.traveler_default_state) as TravelerDefaultState
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
        presenter.viewModel.travelerCompletenessStatus.subscribe(travelerDefaultState.viewModel.travelerStatusObserver)
        presenter.viewModel.allTravelersCompleteSubject.subscribe(getCheckoutViewModel().travelerCompleted)
        presenter.viewModel.invalidTravelersSubject.subscribe(getCheckoutViewModel().clearTravelers)
        presenter
    }

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
    val checkoutButton: Button by bindView(R.id.checkout_button)
    val rootWindow by lazy { (context as Activity).window }
    val decorView by lazy { rootWindow.decorView.findViewById(android.R.id.content) }
    var paymentLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null
    var travelerLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null
    var toolbarHeight = Ui.getToolbarSize(context)

    val checkoutDialog = ProgressDialog(context)
    val createTripDialog = ProgressDialog(context)

    var userAccountRefresher = UserAccountRefresher(context, getLineOfBusiness(), this)
    var sliderHeight = 0f
    var checkoutButtonHeight = 0f

    protected var ckoViewModel: BaseCheckoutViewModel by notNullAndObservable { vm ->
        vm.creditCardRequired.subscribe { required ->
            paymentWidget.viewmodel.isCreditCardRequired.onNext(required)
        }

        paymentWidget.viewmodel.billingInfoAndStatusUpdate.map { it.first }.subscribe(vm.paymentCompleted)
        vm.legalText.subscribeTextAndVisibility(legalInformationText)
        vm.depositPolicyText.subscribeTextAndVisibility(depositPolicyText)
        vm.sliderPurchaseTotalText.subscribeTextAndVisibility(slideTotalText)
        vm.accessiblePurchaseButtonContentDescription.subscribe { accessiblePurchaseButton.contentDescription = it }
        vm.checkoutParams.subscribe {
            checkoutDialog.show()
        }
        vm.bookingSuccessResponse.subscribe {
            checkoutDialog.hide()
        }
        vm.priceChangeObservable.subscribe {
            checkoutDialog.hide()
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

        insuranceServices = Ui.getApplication(context).appComponent().insurance()
        travelerManager = Ui.getApplication(context).travelerComponent().travelerManager()

        paymentWidget = paymentViewStub.inflate() as PaymentWidget

        getPaymentWidgetViewModel().paymentTypeWarningHandledByCkoView.onNext(true)
        paymentWidget.viewmodel = getPaymentWidgetViewModel()

        insuranceWidget.viewModel = InsuranceViewModel(context, insuranceServices)
        priceChangeWidget.viewmodel = PriceChangeViewModel(context, getLineOfBusiness())
        priceChangeWidget.viewmodel.priceChangeVisibility.subscribe { visible ->
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

        totalPriceWidget.breakdown.viewmodel = getCostSummaryBreakdownViewModel()

        totalPriceWidget.breakdown.viewmodel.iconVisibilityObservable.subscribe { show ->
            totalPriceWidget.toggleBundleTotalCompoundDrawable(show)
            totalPriceWidget.viewModel.costBreakdownEnabledObservable.onNext(show)
        }
        totalPriceWidget.viewModel = BundleTotalPriceViewModel(context)
        totalPriceWidget.viewModel.bundleTotalIncludesObservable.onNext(context.getString(R.string.includes_flights_hotel))
        ckoViewModel = makeCheckoutViewModel()
        tripViewModel = makeCreateTripViewModel()

        paymentWidget.viewmodel.lineOfBusiness.onNext(getLineOfBusiness())
        travelerPresenter.travelerEntryWidget.travelerButton.setLOB(getLineOfBusiness())

        loginWidget.setListener(this)
        slideToPurchase.addSlideToListener(this)

        loginWidget.bind(false, User.isLoggedIn(context), Db.getUser(), getLineOfBusiness())
        travelerPresenter.onLogin(User.isLoggedIn(context))
        hintContainer.visibility = if (User.isLoggedIn(getContext())) View.GONE else View.VISIBLE
        if (User.isLoggedIn(context)) {
            val lp = loginWidget.layoutParams as LinearLayout.LayoutParams
            lp.bottomMargin = resources.getDimension(R.dimen.card_view_container_margin).toInt()
        }


        paymentWidget.viewmodel.expandObserver.subscribe { showPaymentPresenter() }

        legalInformationText.setOnClickListener {
            context.startActivity(FlightAndPackagesRulesActivity.createIntent(context, getLineOfBusiness()))
        }

        handle.setOnTouchListener(HandleTouchListener())

        createTripDialog.setCancelable(false)
        createTripDialog.isIndeterminate = true
        checkoutDialog.setMessage(resources.getString(R.string.booking_loading))
        checkoutDialog.setCancelable(false)
        checkoutDialog.isIndeterminate = true

        getCheckoutViewModel().priceChangeObservable.subscribe {
            slideToPurchase.resetSlider()
            animateInSlideToPurchase(true)
        }

        getCheckoutViewModel().noNetworkObservable.subscribe {
            checkoutDialog.dismiss()
            slideToPurchase.resetSlider()
        }

        getCreateTripViewModel().noNetworkObservable.subscribe {
            createTripDialog.dismiss()
        }

        accessiblePurchaseButton.setOnClickListener {
            if (ckoViewModel.builder.hasValidParams()) {
                ckoViewModel.checkoutParams.onNext(ckoViewModel.builder.build())
            } else {
                ckoViewModel.slideAllTheWayObservable.onNext(Unit)
            }
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addDefaultTransition(defaultTransition)
        addTransition(defaultToTraveler)
        addTransition(defaultToPayment)
        slideToPurchaseLayout.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                sliderHeight = slideToPurchaseLayout.height.toFloat()
                if (sliderHeight != 0f) {
                    slideToPurchaseLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    bottomContainer.translationY = sliderHeight
                }
            }
        })
        checkoutButton.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                checkoutButtonHeight = checkoutButton.height.toFloat()
                if (sliderHeight != 0f) {
                    checkoutButton.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    checkoutButton.translationY = checkoutButtonHeight
                }
            }
        })
        paymentLayoutListener = makeKeyboardListener(scrollView)
        travelerLayoutListener = makeKeyboardListener(travelerPresenter.travelerEntryWidget, toolbarHeight * 2)

        Observable.combineLatest(
                paymentWidget.viewmodel.showingPaymentForm,
                paymentWidget.viewmodel.invalidPaymentTypeWarning,
                { showingGuestPaymentForm, invalidPaymentTypeWarning ->
                    val hasPaymentTypeWarning = invalidPaymentTypeWarning.isNotBlank()
                    val visibility = if (hasPaymentTypeWarning && showingGuestPaymentForm) View.VISIBLE else View.GONE
                    invalidPaymentTypeWarningTextView.text = invalidPaymentTypeWarning
                    if (visibility == View.VISIBLE) {
                        invalidPaymentTypeWarningTextView.visibility = visibility
                        AnimUtils.slideIn(invalidPaymentTypeWarningTextView)
                        toolbarDropShadow.visibility = visibility
                    } else if (invalidPaymentTypeWarningTextView.visibility == View.VISIBLE) {
                        AnimUtils.slideOut(invalidPaymentTypeWarningTextView)
                    }
                }).subscribe()

    }

    private val defaultTransition = object : Presenter.DefaultTransition(CheckoutDefault::class.java.name) {
        override fun endTransition(forward: Boolean) {
            loginWidget.bind(false, User.isLoggedIn(context), Db.getUser(), getLineOfBusiness())
            paymentWidget.show(PaymentWidget.PaymentDefault(), Presenter.FLAG_CLEAR_BACKSTACK)
            updateTravelerPresenter()
        }
    }

    private val defaultToTraveler = object : ScaleTransition(this, mainContent, travelerPresenter, CheckoutDefault::class.java, TravelerPresenter::class.java) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            bottomContainer.visibility = if (forward) GONE else VISIBLE
            if (!forward) {
                travelerPresenter.toolbarNavIconContDescSubject.onNext(resources.getString(R.string.toolbar_nav_icon_cont_desc))
                travelerPresenter.viewModel.updateCompletionStatus()
                travelerPresenter.toolbarTitleSubject.onNext(getCheckoutToolbarTitle(resources, Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelSecureCheckoutMessaging)))
                decorView.viewTreeObserver.removeOnGlobalLayoutListener(travelerLayoutListener)
            } else {
                decorView.viewTreeObserver.addOnGlobalLayoutListener(travelerLayoutListener)
            }
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            if (!forward) {
                Ui.hideKeyboard(travelerPresenter)
                animateInSlideToPurchase(true)
                travelerPresenter.setFocusForView()
                travelerDefaultState.setFocusForView()
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
            travelerDefaultState.visibility = if (forward) View.GONE else View.VISIBLE
            insuranceWidget.setInverseVisibility(forward || !insuranceWidget.viewModel.hasProduct)
            legalInformationText.setInverseVisibility(forward)
            depositPolicyText.setInverseVisibility(forward)
            bottomContainer.setInverseVisibility(forward)
            cardFeeWarningTextView.setInverseVisibility(forward)
            if (!forward) {
                invalidPaymentTypeWarningTextView.visibility = View.GONE
                cardProcessingFeeTextView.visibility  = View.GONE
                debitCardsNotAcceptedTextView.visibility = View.GONE
                paymentWidget.show(PaymentWidget.PaymentDefault(), Presenter.FLAG_CLEAR_BACKSTACK)
                decorView.viewTreeObserver.removeOnGlobalLayoutListener(paymentLayoutListener)
                scrollView.layoutParams.height = height
                paymentWidget.viewmodel.showingPaymentForm.onNext(false)
            } else {
                decorView.viewTreeObserver.addOnGlobalLayoutListener(paymentLayoutListener)
            }
        }

        override fun endTransition(forward: Boolean) {
            if (!forward) {
                Ui.hideKeyboard(paymentWidget)
                animateInSlideToPurchase(true)
                paymentWidget.setFocusForView()
            } else {
                val lp = space.layoutParams
                lp.height = 0
                space.layoutParams = lp
            }
            ckoViewModel.showingPaymentWidgetSubject.onNext(forward)
        }
    }

    //Either shows the bundle overview or the checkout presenter based on distance/rotation
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
        animator.addUpdateListener(ValueAnimator.AnimatorUpdateListener { anim ->
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

    //Abstract methods
    override fun onSlideStart() {
    }

    override fun onSlideProgress(pixels: Float, total: Float) {
    }

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

    override fun onUserAccountRefreshed() {
        doCreateTrip()
    }

    override fun accountLoginClicked() {
        val args = AccountLibActivity.createArgumentsBundle(getLineOfBusiness(), CheckoutLoginExtender());
        User.signIn(context as Activity, args);
    }

    override fun accountLogoutClicked() {
        User.signOut(context)
        animateInSlideToPurchase(false)
        updateDbTravelers()
        updateTravelerPresenter()
        travelerPresenter.onLogin(false)
        loginWidget.bind(false, false, null, getLineOfBusiness())
        paymentWidget.viewmodel.userLogin.onNext(false)
        hintContainer.visibility = View.VISIBLE
        val lp = loginWidget.layoutParams as LinearLayout.LayoutParams
        lp.bottomMargin = 0
        doCreateTrip()
    }

    private fun showPaymentPresenter() {
        show(paymentWidget)
    }

    class CheckoutDefault

    fun doCreateTrip() {
        tripViewModel.performCreateTrip.onNext(Unit)
    }

    fun clearPaymentInfo() {
        paymentWidget.reset()
        paymentWidget.clearCCAndCVV()
    }

    fun onLoginSuccess() {
        loginWidget.bind(false, true, Db.getUser(), getLineOfBusiness())
        paymentWidget.viewmodel.userLogin.onNext(true)
        hintContainer.visibility = View.GONE
        val lp = loginWidget.layoutParams as LinearLayout.LayoutParams
        lp.bottomMargin = resources.getDimension(R.dimen.card_view_container_margin).toInt()
        travelerManager.onSignIn(context)
        travelerPresenter.onLogin(true)
        updateTravelerPresenter()
        animateInSlideToPurchase(true)
        doCreateTrip()
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
        val isSlideToPurchaseLayoutVisible = visible && ckoViewModel.isValid()
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

    fun toggleCheckoutButton(isEnabled: Boolean) {
        checkoutButton.translationY = if (isEnabled) 0f else checkoutButtonHeight
        val shouldShowSlider = currentState == CheckoutDefault::class.java.name && ckoViewModel.isValid()
        bottomContainer.translationY = if (isEnabled) sliderHeight - checkoutButtonHeight else if (shouldShowSlider) 0f else sliderHeight
        checkoutButton.isEnabled = isEnabled
    }

    val gestureDetector = GestureDetector(context, SingleTapUp())

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

    private fun View.setInverseVisibility(forward: Boolean) {
        this.visibility = if (forward) View.GONE else View.VISIBLE
    }

    private fun TextView.setInverseVisibility(forward: Boolean) {
        this.visibility = if (!forward && this.text.isNotEmpty()) View.VISIBLE else View.GONE
    }

    fun resetPriceChange() {
        priceChangeWidget.viewmodel.priceChangeVisibility.onNext(false)
    }

    fun resetAndShowTotalPriceWidget() {
        resetPriceChange()
        totalPriceWidget.resetPriceWidget()
    }

    fun resetTravelers() {
        ckoViewModel.clearTravelers.onNext(Unit)
    }

    fun openTravelerPresenter() {
        show(travelerPresenter)
        travelerPresenter.showSelectOrEntryState(travelerDefaultState.getStatus())
    }

    open fun updateTravelerPresenter() {
        travelerPresenter.viewModel.refresh()
    }

    abstract fun getPaymentWidgetViewModel(): PaymentViewModel
    abstract fun injectComponents()
    abstract fun getLineOfBusiness(): LineOfBusiness
    abstract fun updateDbTravelers()
    abstract fun trackShowSlideToPurchase()
    abstract fun trackShowBundleOverview()
    abstract fun makeCheckoutViewModel(): BaseCheckoutViewModel
    abstract fun makeCreateTripViewModel(): BaseCreateTripViewModel
    abstract fun getCheckoutViewModel(): BaseCheckoutViewModel
    abstract fun getCreateTripViewModel(): BaseCreateTripViewModel
    abstract fun getCostSummaryBreakdownViewModel(): BaseCostSummaryBreakdownViewModel
    abstract fun setupCreateTripViewModel(vm: BaseCreateTripViewModel)
    abstract fun isPassportRequired(response: TripResponse)
    abstract fun showMainTravelerMinimumAgeMessaging(): Boolean

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

        }, 50L)
    }
}
