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
import android.view.MotionEvent
import android.view.View
import android.view.ViewStub
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.activity.AccountLibActivity
import com.expedia.bookings.activity.FlightRulesActivity
import com.expedia.bookings.activity.HotelRulesActivity
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.User
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.packages.TravelerPresenter
import com.expedia.bookings.utils.CurrencyUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.UserAccountRefresher
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.packages.PackagePaymentWidget
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.vm.BaseCheckoutViewModel
import com.expedia.vm.FlightCostSummaryBreakdownViewModel
import com.expedia.vm.PackageCostSummaryBreakdownViewModel
import com.expedia.vm.PaymentViewModel
import com.expedia.vm.PriceChangeViewModel
import com.expedia.vm.packages.BaseCreateTripViewModel
import com.expedia.vm.packages.BundlePriceViewModel
import com.expedia.vm.traveler.CheckoutTravelerViewModel
import com.mobiata.android.Log
import rx.subjects.PublishSubject
import java.math.BigDecimal
import kotlin.properties.Delegates

abstract class BaseCheckoutPresenter(context: Context, attr: AttributeSet) : Presenter(context, attr), SlideToWidgetLL.ISlideToListener,
        UserAccountRefresher.IUserAccountRefreshListener, AccountButton.AccountButtonClickListener {

    val handle: FrameLayout by bindView(R.id.handle)
    val toolbarDropShadow: View by bindView(R.id.drop_shadow)
    val chevron: View by bindView(R.id.chevron)
    val mainContent: LinearLayout by bindView(R.id.main_content)
    val scrollView: ScrollView by bindView(R.id.scrollView)
    val loginWidget: AccountButton by bindView(R.id.login_widget)

    val cardProcessingFeeTextView: TextView by bindView(R.id.card_processing_fee)
    val cardFeeWarningTextView: TextView by bindView(R.id.card_fee_warning_text)
    var paymentWidget: PaymentWidget by Delegates.notNull()
    val paymentViewStub: ViewStub by bindView(R.id.payment_info_card_view_stub)

    val travelerPresenter: TravelerPresenter by bindView(R.id.traveler_presenter)

    val legalInformationText: TextView by bindView(R.id.legal_information_text_view)
    val hintContainer: LinearLayout by bindView(R.id.hint_container)
    val depositPolicyText: TextView by bindView(R.id.disclaimer_text)

    val bottomContainer: LinearLayout by bindView(R.id.bottom_container)
    val priceChangeWidget: PriceChangeWidget by bindView(R.id.price_change)
    val totalPriceWidget: TotalPriceWidget by bindView(R.id.total_price_widget)
    val slideToPurchaseLayout: LinearLayout by bindView(R.id.slide_to_purchase_layout)
    val slideToPurchase: SlideToWidgetLL by bindView(R.id.slide_to_purchase_widget)
    val slideTotalText: TextView by bindView(R.id.purchase_total_text_view)
    val checkoutButton: Button by bindView(R.id.checkout_button)

    val paymentWidgetRootWindow by lazy { (context as Activity).window }
    val paymentWidgetRootView by lazy { paymentWidgetRootWindow.decorView.findViewById(android.R.id.content) }
    var globalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null
    var toolbarHeight = Ui.getToolbarSize(context)

    val checkoutDialog = ProgressDialog(context)
    val createTripDialog = ProgressDialog(context)

    var slideAllTheWayObservable = PublishSubject.create<Unit>()
    var checkoutTranslationObserver = PublishSubject.create<Float>()
    var userAccountRefresher = UserAccountRefresher(context, getLineOfBusiness(), this)

    var sliderHeight = 0f
    var checkoutButtonHeight = 0f

    val paymentWidgetViewModel = PaymentViewModel(context)

    protected var ckoViewModel: BaseCheckoutViewModel by notNullAndObservable { vm ->
        vm.creditCardRequired.subscribe { required ->
            paymentWidget.viewmodel.isCreditCardRequired.onNext(required)
        }
        travelerPresenter.allTravelersCompleteSubject.subscribe(vm.travelerCompleted)
        travelerPresenter.travelersIncompleteSubject.subscribe(vm.clearTravelers)
        paymentWidget.viewmodel.billingInfoAndStatusUpdate.map { it.first }.subscribe(vm.paymentCompleted)
        vm.legalText.subscribeTextAndVisibility(legalInformationText)
        vm.depositPolicyText.subscribeTextAndVisibility(depositPolicyText)
        vm.sliderPurchaseTotalText.subscribeTextAndVisibility(slideTotalText)
        vm.checkoutParams.subscribe {
            checkoutDialog.show()
        }
        vm.checkoutResponse.subscribe {
            checkoutDialog.hide()
        }
    }

    protected var tripViewModel: BaseCreateTripViewModel by notNullAndObservable { vm ->
        vm.showCreateTripDialogObservable.subscribe { show ->
            if (show) {
                createTripDialog.show();
                createTripDialog.setContentView(R.layout.process_dialog_layout);
            } else {
                createTripDialog.hide()
            }
        }
        setupCreateTripViewModel(vm)
    }

    init {
        View.inflate(context, R.layout.widget_base_checkout, this)
        paymentWidget = paymentViewStub.inflate() as PaymentWidget
        paymentWidget.viewmodel = paymentWidgetViewModel
        travelerPresenter.viewModel = CheckoutTravelerViewModel()
        priceChangeWidget.viewmodel = PriceChangeViewModel(context, getLineOfBusiness())
        if (getLineOfBusiness() == LineOfBusiness.FLIGHTS_V2) {
            totalPriceWidget.packagebreakdown.viewmodel = FlightCostSummaryBreakdownViewModel(context)
        } else {
            totalPriceWidget.packagebreakdown.viewmodel = PackageCostSummaryBreakdownViewModel(context)
        }
        totalPriceWidget.packagebreakdown.viewmodel.iconVisibilityObservable.subscribe { show ->
            totalPriceWidget.toggleBundleTotalCompoundDrawable(show)
            totalPriceWidget.viewModel.costBreakdownEnabledObservable.onNext(show)
        }
        totalPriceWidget.viewModel = BundlePriceViewModel(context)
        ckoViewModel = makeCheckoutViewModel()
        tripViewModel = makeCreateTripViewModel()
        globalLayoutListener = (ViewTreeObserver.OnGlobalLayoutListener {
            val decorView = paymentWidgetRootWindow.decorView
            val windowVisibleDisplayFrameRect = Rect()
            decorView.getWindowVisibleDisplayFrame(windowVisibleDisplayFrameRect)
            var location = IntArray(2)
            scrollView.getLocationOnScreen(location)
            val lp = scrollView.layoutParams
            val newHeight = windowVisibleDisplayFrameRect.bottom - windowVisibleDisplayFrameRect.top - toolbarHeight

            if (lp.height != newHeight) {
                lp.height = newHeight
                scrollView.layoutParams = lp
            }
        })

        toolbarHeight = Ui.getToolbarSize(context)

        paymentWidget.viewmodel.lineOfBusiness.onNext(getLineOfBusiness())
        travelerPresenter.travelerEntryWidget.travelerButton.setLOB(getLineOfBusiness())

        loginWidget.setListener(this)
        slideToPurchase.addSlideToListener(this)

        loginWidget.bind(false, User.isLoggedIn(context), Db.getUser(), LineOfBusiness.PACKAGES)
        hintContainer.visibility = if (User.isLoggedIn(getContext())) View.GONE else View.VISIBLE
        if (User.isLoggedIn(context)) {
            val lp = loginWidget.layoutParams as LinearLayout.LayoutParams
            lp.bottomMargin = resources.getDimension(R.dimen.card_view_container_margin).toInt()
        }

        paymentWidget.viewmodel.expandObserver.subscribe { showPaymentPresenter() }

        travelerPresenter.expandedSubject.subscribe { expanded ->
            if (expanded) {
                toolbarDropShadow.visibility = View.GONE
                show(travelerPresenter)
            } else {
                toolbarDropShadow.visibility = View.VISIBLE
            }
        }

        legalInformationText.setOnClickListener {
            if (getLineOfBusiness() == LineOfBusiness.PACKAGES) {
                context.startActivity(HotelRulesActivity.createIntent(context, getLineOfBusiness()))
            } else {
                context.startActivity(FlightRulesActivity.createIntent(context, getLineOfBusiness()))
            }
        }

        handle.setOnTouchListener(HandleTouchListner())

        createTripDialog.setCancelable(false)
        createTripDialog.isIndeterminate = true
        checkoutDialog.setMessage(resources.getString(R.string.booking_loading))
        checkoutDialog.setCancelable(false)
        checkoutDialog.isIndeterminate = true
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addDefaultTransition(defaultTransition)
        addTransition(defaultToTraveler)
        addTransition(defaultToPayment)
        travelerPresenter.closeSubject.subscribe {
            show(CheckoutDefault(), FLAG_CLEAR_BACKSTACK)
        }
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
    }

    private val defaultTransition = object : Presenter.DefaultTransition(CheckoutDefault::class.java.name) {
        override fun endTransition(forward: Boolean) {
            loginWidget.bind(false, User.isLoggedIn(context), Db.getUser(), LineOfBusiness.PACKAGES)
            travelerPresenter.show(travelerPresenter.travelerDefaultState, Presenter.FLAG_CLEAR_BACKSTACK)
            paymentWidget.show(PaymentWidget.PaymentDefault(), Presenter.FLAG_CLEAR_BACKSTACK)
            updateTravelerPresenter()
        }
    }

    private val defaultToTraveler = object : Presenter.Transition(CheckoutDefault::class.java, TravelerPresenter::class.java) {
        override fun startTransition(forward: Boolean) {
            handle.setInverseVisibility(forward)
            loginWidget.setInverseVisibility(forward)
            hintContainer.visibility = if (forward) View.GONE else if (User.isLoggedIn(getContext())) View.GONE else View.VISIBLE
            paymentWidget.visibility = if (forward) View.GONE else (if (paymentWidget.isCreditCardRequired()) View.VISIBLE else View.GONE)
            legalInformationText.setInverseVisibility(forward)
            depositPolicyText.setInverseVisibility(forward)
            bottomContainer.setInverseVisibility(forward)
        }

        override fun endTransition(forward: Boolean) {
            if (!forward) {
                travelerPresenter.show(travelerPresenter.travelerDefaultState, Presenter.FLAG_CLEAR_BACKSTACK)
                Ui.hideKeyboard(travelerPresenter)
                toolbarDropShadow.visibility = View.GONE
                animateInSlideToPurchase(true)
            }
        }
    }

    private val defaultToPayment = object : Presenter.Transition(CheckoutDefault::class.java, PackagePaymentWidget::class.java) {

        override fun startTransition(forward: Boolean) {
            handle.setInverseVisibility(forward)
            loginWidget.setInverseVisibility(forward)
            hintContainer.visibility = if (forward) View.GONE else if (User.isLoggedIn(getContext())) View.GONE else View.VISIBLE
            travelerPresenter.visibility = if (!forward) View.VISIBLE else View.GONE
            legalInformationText.setInverseVisibility(forward)
            depositPolicyText.setInverseVisibility(forward)
            bottomContainer.setInverseVisibility(forward)
            cardFeeWarningTextView.setInverseVisibility(forward)
            cardProcessingFeeTextView.visibility = if (forward && cardProcessingFeeTextView.text.isNotEmpty()) VISIBLE else GONE
            if (!forward) {
                paymentWidget.show(PaymentWidget.PaymentDefault(), Presenter.FLAG_CLEAR_BACKSTACK)
                paymentWidgetRootView.viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
                scrollView.layoutParams.height = height
                toolbarDropShadow.visibility = View.GONE
            } else {
                paymentWidgetRootView.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
            }
        }

        override fun endTransition(forward: Boolean) {
            if (!forward) {
                Ui.hideKeyboard(paymentWidget)
                animateInSlideToPurchase(true)
            }
        }
    }

    //Either shows the bundle overview or the checkout presenter based on distance/rotation
    private fun rotateChevron(distance: Float) {
        val distanceGoal = height
        mainContent.translationY = distance
        chevron.rotation = Math.min(1f, distance / distanceGoal) * (180)
        checkoutTranslationObserver.onNext(distance)
    }

    private fun animCheckoutToTop() {
        val distanceGoal = height
        val animator = ObjectAnimator.ofFloat(mainContent, "translationY", mainContent.translationY, 0f)
        animator.duration = 400L
        animator.addUpdateListener(ValueAnimator.AnimatorUpdateListener { anim ->
            chevron.rotation = Math.min(1f, mainContent.translationY / distanceGoal) * (180)
            checkoutTranslationObserver.onNext(mainContent.translationY)
        })
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                checkoutTranslationObserver.onNext(mainContent.translationY)
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
            slideAllTheWayObservable.onNext(Unit)
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

    fun clearCCNumber() {
        try {
            paymentWidget.creditCardNumber.setText("")
            Db.getWorkingBillingInfoManager().workingBillingInfo.number = null
            Db.getBillingInfo().number = null
            paymentWidget.validateAndBind()
        } catch (ex: Exception) {
            Log.e("Error clearing billingInfo card number", ex)
        }
    }

    fun onLoginSuccess() {
        loginWidget.bind(false, true, Db.getUser(), getLineOfBusiness())
        paymentWidget.viewmodel.userLogin.onNext(true)
        hintContainer.visibility = View.GONE
        val lp = loginWidget.layoutParams as LinearLayout.LayoutParams
        lp.bottomMargin = resources.getDimension(R.dimen.card_view_container_margin).toInt()
        doCreateTrip()
    }

    fun animateInSlideToPurchase(visible: Boolean) {
        var isSlideToPurchaseLayoutVisible = visible && ckoViewModel.isValid()
        if (isSlideToPurchaseLayoutVisible) {
            trackShowSlideToPurchase()
        }
        slideToPurchaseLayout.isFocusable = isSlideToPurchaseLayoutVisible
        var distance = if (!isSlideToPurchaseLayoutVisible) slideToPurchaseLayout.height.toFloat() else 0f
        if (bottomContainer.translationY == distance) {
            return
        }
        val animator = ObjectAnimator.ofFloat(bottomContainer, "translationY", distance)
        animator.duration = 300
        animator.start()
    }

    fun toggleCheckoutButton(isEnabled: Boolean) {
        checkoutButton.translationY = if (isEnabled) 0f else checkoutButtonHeight
        val shouldShowSlider = currentState == CheckoutDefault::class.java.name && ckoViewModel.isValid()
        bottomContainer.translationY = if (isEnabled) sliderHeight - checkoutButtonHeight else if (shouldShowSlider) 0f else sliderHeight
        checkoutButton.isEnabled = isEnabled
    }

    inner class HandleTouchListner() : View.OnTouchListener {
        internal var originY: Float = 0.toFloat()
        var isClicked = false
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                (MotionEvent.ACTION_DOWN) -> {
                    isClicked = true
                    originY = event.rawY
                    toolbarDropShadow.visibility = View.VISIBLE
                }
                (MotionEvent.ACTION_UP) -> {
                    if (isClicked) {
                        (context as AppCompatActivity).onBackPressed()
                        isClicked = false
                    } else {
                        val diff = event.rawY - originY
                        val distance = Math.max(diff, 0f)
                        val distanceGoal = height / 3f
                        if (distance > distanceGoal) {
                            (context as AppCompatActivity).onBackPressed()
                        } else {
                            animCheckoutToTop()
                        }
                        originY = 0f
                        toolbarDropShadow.visibility = View.GONE
                    }
                }
                (MotionEvent.ACTION_MOVE) -> {
                    isClicked = false
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

    fun resetAndShowTotalPriceWidget() {
        var countryCode = PointOfSale.getPointOfSale().threeLetterCountryCode
        var currencyCode = CurrencyUtils.currencyForLocale(countryCode)
        totalPriceWidget.visibility = View.VISIBLE
        totalPriceWidget.viewModel.total.onNext(Money(BigDecimal("0.00"), currencyCode))
        totalPriceWidget.viewModel.savings.onNext(Money(BigDecimal("0.00"), currencyCode))
    }

    abstract fun getLineOfBusiness(): LineOfBusiness
    abstract fun updateTravelerPresenter()
    abstract fun updateDbTravelers()
    abstract fun trackShowSlideToPurchase()
    abstract fun trackShowBundleOverview()
    abstract fun makeCheckoutViewModel(): BaseCheckoutViewModel
    abstract fun makeCreateTripViewModel(): BaseCreateTripViewModel
    abstract fun getCheckoutViewModel(): BaseCheckoutViewModel
    abstract fun getCreateTripViewModel(): BaseCreateTripViewModel
    abstract fun setupCreateTripViewModel(vm: BaseCreateTripViewModel)
}