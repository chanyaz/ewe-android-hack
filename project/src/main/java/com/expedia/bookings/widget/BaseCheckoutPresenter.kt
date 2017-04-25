package com.expedia.bookings.widget

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Rect
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import android.view.ViewTreeObserver
import android.view.Window
import android.widget.LinearLayout
import android.widget.Space
import com.crashlytics.android.Crashlytics
import com.expedia.bookings.R
import com.expedia.bookings.activity.AccountLibActivity
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.activity.FlightAndPackagesRulesActivity
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.User
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.ScaleTransition
import com.expedia.bookings.presenter.packages.AbstractTravelersPresenter
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.isMaterialFormsEnabled
import com.expedia.bookings.utils.TravelerManager
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.UserAccountRefresher
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.setFocusForView
import com.expedia.bookings.widget.traveler.TravelerSummaryCard
import com.expedia.util.getCheckoutToolbarTitle
import com.expedia.util.notNullAndObservable
import com.expedia.util.safeSubscribe
import com.expedia.util.setInverseVisibility
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.util.unsubscribeOnClick
import com.expedia.vm.AbstractCheckoutViewModel
import com.expedia.vm.BaseCreateTripViewModel
import com.expedia.vm.PaymentViewModel
import com.expedia.vm.traveler.TravelerSummaryViewModel
import com.expedia.vm.traveler.TravelersViewModel
import com.squareup.phrase.Phrase
import io.fabric.sdk.android.Fabric
import rx.Observable
import rx.subjects.BehaviorSubject
import java.math.BigDecimal
import javax.inject.Inject
import kotlin.properties.Delegates

abstract class BaseCheckoutPresenter(context: Context, attr: AttributeSet?) : Presenter(context, attr),
        UserAccountRefresher.IUserAccountRefreshListener, AccountButton.AccountButtonClickListener {

    lateinit var paymentViewModel: PaymentViewModel
        @Inject set
    val checkoutRequestCallObservable = BehaviorSubject.create<Long>()

    /** abstract methods **/

    abstract fun getDefaultToTravelerTransition(): DefaultToTraveler
    abstract fun injectComponents()
    abstract fun getLineOfBusiness(): LineOfBusiness
    abstract fun updateDbTravelers()
    abstract fun trackShowSlideToPurchase()
    abstract fun makeCheckoutViewModel(): AbstractCheckoutViewModel
    abstract fun makeCreateTripViewModel(): BaseCreateTripViewModel
    abstract fun getCheckoutViewModel(): AbstractCheckoutViewModel
    abstract fun getCreateTripViewModel(): BaseCreateTripViewModel
    abstract fun setupCreateTripViewModel(vm: BaseCreateTripViewModel)
    abstract fun showMainTravelerMinimumAgeMessaging(): Boolean
    abstract fun trackCheckoutPriceChange(priceDiff: Int)
    abstract fun handleCheckoutPriceChange(response: TripResponse)
    abstract fun createTravelersViewModel(): TravelersViewModel
    abstract fun shouldShowAlertForCreateTripPriceChange(response: TripResponse?): Boolean
    abstract fun trackCreateTripPriceChange(priceChangeDiffPercentage: Int)
    abstract fun onCreateTripResponse(response: TripResponse?)

    /** contants **/
    private val ANIMATION_DELAY = 200L
    var sliderHeight = 0f

    protected var cardType: PaymentType? = null
    protected var userAccountRefresher = UserAccountRefresher(context, getLineOfBusiness(), this)
    private val checkoutDialog = ProgressDialog(context)
    private val createTripDialog = ProgressDialog(context)

    /** views **/
    val toolbarDropShadow: View by bindView(R.id.drop_shadow)
    val mainContent: LinearLayout by bindView(R.id.main_content)
    val scrollView: ScrollView by bindView(R.id.scrollView)
    val contentView: LinearLayout by bindView(R.id.checkout_widget_container)
    val loginWidget: AccountButton by bindView(R.id.login_widget)
    val cardProcessingFeeTextView: TextView by bindView(R.id.card_processing_fee)
    val cardFeeWarningTextView: TextView by bindView(R.id.card_fee_warning_text)
    val invalidPaymentTypeWarningTextView: TextView by bindView(R.id.invalid_payment_type_warning)
    val debitCardsNotAcceptedTextView: TextView by bindView(R.id.flights_debit_cards_not_accepted)
    val paymentViewStub: ViewStub by bindView(R.id.payment_info_card_view_stub)
    val materialPaymentViewStub: ViewStub by bindView(R.id.material_payment_view_stub)
    val travelersPresenterStub: ViewStub by bindView(R.id.traveler_presenter_stub)
    val scrollViewSpace: Space by bindView(R.id.scrollview_space)
    val legalInformationText: TextView by bindView(R.id.legal_information_text_view)
    val hintContainer: LinearLayout by bindView(R.id.hint_container)
    val depositPolicyText: TextView by bindView(R.id.disclaimer_text)

    val rootWindow: Window by lazy { (context as Activity).window }
    val decorView: View by lazy { rootWindow.decorView.findViewById(android.R.id.content) }
    var paymentLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null
    var travelerLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null
    var toolbarHeight = Ui.getToolbarSize(context)
    val travelerSummaryCardView: CardView by bindView(R.id.traveler_default_state_card_view)
    val materialFormTestEnabled = isMaterialFormsEnabled()
    val paymentWidget: PaymentWidget by lazy {
        val presenter = if (materialFormTestEnabled)  {
            materialPaymentViewStub.inflate() as PaymentWidget
        } else {
            paymentViewStub.inflate() as PaymentWidget
        }
        presenter
    }

    val logoutDialog: AlertDialog by lazy {
        val logoutFunc = fun() {
            logoutUser()
        }
        DialogFactory.createLogoutDialog(context, logoutFunc)
    }

    /** viewmodels **/
    protected fun setUpPaymentViewModel() {
        paymentWidget.viewmodel = getPaymentWidgetViewModel()
        paymentWidget.viewmodel.paymentTypeWarningHandledByCkoView.onNext(true)
        paymentWidget.viewmodel.lineOfBusiness.onNext(getLineOfBusiness())
        if (materialFormTestEnabled) {
            paymentWidget.viewmodel.updateBackgroundColor.subscribe { forward ->
                val color = ContextCompat.getColor(context, if (forward) R.color.white else R.color.gray1)
                scrollView.setBackgroundColor(color)
            }
        }
        paymentWidget.viewmodel.showingPaymentForm.subscribe { showingForm ->
            cardFeeWarningTextView.setInverseVisibility(showingForm)
        }
    }

    val travelerSummaryCard: TravelerSummaryCard by lazy {
        val view = findViewById(R.id.traveler_default_state) as TravelerSummaryCard
        view.viewModel = TravelerSummaryViewModel(context)
        view.setOnClickListener {
            openTravelerPresenter()
        }
        view
    }

    val travelersPresenter: AbstractTravelersPresenter by lazy {
        val presenter = travelersPresenterStub.inflate() as AbstractTravelersPresenter
        presenter.viewModel = createTravelersViewModel()
        presenter.travelerEntryWidget.travelerButton.setLOB(getLineOfBusiness())
        presenter.closeSubject.subscribe {
            show(CheckoutDefault(), FLAG_CLEAR_BACKSTACK)
            presenter.menuVisibility.onNext(false)
        }
        presenter.viewModel.travelersCompletenessStatus.subscribe(travelerSummaryCard.viewModel.travelerStatusObserver)
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

    protected var ckoViewModel: AbstractCheckoutViewModel by notNullAndObservable { vm ->
        vm.creditCardRequired.subscribe { required ->
            paymentWidget.viewmodel.isCreditCardRequired.onNext(required)
        }
        vm.legalText.subscribeTextAndVisibility(legalInformationText)
        vm.depositPolicyText.subscribeText(depositPolicyText)
        vm.showCheckoutDialogObservable.subscribe { show ->
            if (show) {
                checkoutDialog.show()
            } else {
                checkoutDialog.dismiss()
            }
        }
        vm.checkoutPriceChangeObservable.subscribe { response ->
            vm.bottomCheckoutContainerStateObservable.onNext(true)
            getCreateTripViewModel().updatePriceChangeWidgetObservable.onNext(response)
            getCreateTripViewModel().showPriceChangeWidgetObservable.onNext(true)
            val oldPrice = response.getOldPrice()
            if (oldPrice != null) {
                trackCheckoutPriceChange(getPriceChangeDiffPercentage(oldPrice, response.newPrice()))
            }
            handleCheckoutPriceChange(response)
        }
        vm.cardFeeTextSubject.subscribeText(cardProcessingFeeTextView)
        vm.cardFeeWarningTextSubject.subscribeText(cardFeeWarningTextView)
        vm.showCardFeeWarningText.subscribe {
            cardFeeWarningTextView.visibility = View.VISIBLE
        }
    }

    fun getPriceChangeDiffPercentage(oldPrice: Money, newPrice: Money): Int {
        val priceDiff = newPrice.amount.toInt() - oldPrice.amount.toInt()
        var diffPercentage: Int = 0
        if (priceDiff != 0) {
            diffPercentage = (priceDiff * 100) / oldPrice.amount.toInt()
        }
        return diffPercentage
    }

    protected var tripViewModel: BaseCreateTripViewModel by notNullAndObservable { vm ->
        vm.performCreateTrip.map { false }.subscribe(vm.showPriceChangeWidgetObservable)
        vm.priceChangeAlertPriceObservable.map { response ->
            Pair(response?.getOldPrice()?.formattedMoneyFromAmountAndCurrencyCode, response?.newPrice()?.formattedMoneyFromAmountAndCurrencyCode) }
                .distinctUntilChanged().map { it.first != null && it.second != null }
                .subscribe(vm.showPriceChangeAlertObservable)
        vm.showPriceChangeAlertObservable.subscribe { show ->
            if (show) {
                showAlertDialogForPriceChange(vm.createTripResponseObservable.value!!)
            }
        }
        vm.showCreateTripDialogObservable.subscribe { show ->
            if (show) {
                createTripDialog.show()
                createTripDialog.setContentView(R.layout.process_dialog_layout)
                AccessibilityUtil.delayedFocusToView(createTripDialog.findViewById(R.id.progress_dialog_container), 0)
                createTripDialog.findViewById(R.id.progress_dialog_container).contentDescription = context.getString(R.string.spinner_text_create_trip)
                announceForAccessibility(context.getString(R.string.spinner_text_create_trip))
            } else {
                createTripDialog.dismiss()
            }
        }
        vm.createTripResponseObservable.safeSubscribe { response ->
            getCreateTripViewModel().updatePriceChangeWidgetObservable.onNext(response)
            val oldPrice = response!!.getOldPrice()
            if (oldPrice != null) {
                trackCreateTripPriceChange(getPriceChangeDiffPercentage(oldPrice, response.newPrice()))
                if (shouldShowPriceChangeOnCreateTrip(response.newPrice().amount, oldPrice.amount)) {
                    if (shouldShowAlertForCreateTripPriceChange(response)) {
                        vm.priceChangeAlertPriceObservable.onNext(response)
                        return@safeSubscribe
                    } else {
                        vm.showPriceChangeWidgetObservable.onNext(true)
                    }
                }
            }
            onCreateTripResponse(response)
        }
        setupCreateTripViewModel(vm)
    }

    fun hasPriceChange(response: TripResponse?): Boolean {
        return response?.getOldPrice() != null
    }

    fun shouldShowPriceChangeOnCreateTrip(newPrice: BigDecimal, oldPrice: BigDecimal): Boolean {
        return (Math.ceil(newPrice.toDouble()) - Math.ceil(oldPrice.toDouble())) != 0.0
    }

    init {
        View.inflate(context, R.layout.base_checkout_presenter, this)
        injectComponents()
        travelerManager = Ui.getApplication(context).travelerComponent().travelerManager()
        setUpPaymentViewModel()
        setUpViewModels()
        setUpDialogs()
        setupClickListeners()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addDefaultTransition(defaultTransition)
        addTransition(getDefaultToTravelerTransition())
        addTransition(defaultToPayment)
        setupKeyboardListeners()
        setUpErrorMessaging()
        initLoggedInState(User.isLoggedIn(context))
    }

    private fun setUpViewModels() {
        ckoViewModel = makeCheckoutViewModel()
        tripViewModel = makeCreateTripViewModel()
        getCreateTripViewModel().createTripResponseObservable.safeSubscribe(getCheckoutViewModel().createTripResponseObservable)
        getCheckoutViewModel().cardFeeTripResponse.safeSubscribe(getCreateTripViewModel().createTripResponseObservable)
    }

    private fun initLoggedInState(isUserLoggedIn: Boolean) {
        loginWidget.bind(false, isUserLoggedIn, Db.getUser(), getLineOfBusiness())
        hintContainer.visibility = if (isUserLoggedIn) View.GONE else View.VISIBLE
        travelersPresenter.onLogin(isUserLoggedIn)
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

    private fun setupClickListeners() {
        loginWidget.setListener(this)
        legalInformationText.setOnClickListener {
            context.startActivity(FlightAndPackagesRulesActivity.createIntent(context, getLineOfBusiness()))
        }
    }

    private fun setupKeyboardListeners() {
        paymentLayoutListener = makeKeyboardListener(scrollView, 0)
        travelerLayoutListener = makeKeyboardListener(travelersPresenter.travelerEntryWidget, 0)
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
                        if (!ExpediaBookingApp.isAutomation()) {
                            AnimUtils.slideIn(invalidPaymentTypeWarningTextView)
                        }
                        toolbarDropShadow.visibility = visibility
                    } else if (visibility == View.GONE && invalidPaymentTypeWarningTextView.visibility == View.VISIBLE) {
                        if (!ExpediaBookingApp.isAutomation()) {
                            AnimUtils.slideOut(invalidPaymentTypeWarningTextView)
                        }
                    }
                }).subscribe()

        Observable.combineLatest(getCheckoutViewModel().paymentTypeSelectedHasCardFee,
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

    open val defaultTransition = object : Presenter.DefaultTransition(CheckoutDefault::class.java.name) {
        override fun endTransition(forward: Boolean) {
            val isLoggedIn = User.isLoggedIn(context)
            loginWidget.bind(false, isLoggedIn, Db.getUser(), getLineOfBusiness())
            paymentWidget.show(PaymentWidget.PaymentDefault(), Presenter.FLAG_CLEAR_BACKSTACK)
            paymentWidget.viewmodel.selectCorrectCardObservable.onNext(isLoggedIn)
            updateTravelerPresenter()
            if (forward) {
                setToolbarTitle()
                ckoViewModel.bottomCheckoutContainerStateObservable.onNext(true)
                ckoViewModel.transitionToObservable.onNext(CheckoutDefault::class.java.name)
            } else {
                ckoViewModel.bottomCheckoutContainerStateObservable.onNext(false)
            }
        }
    }


    open inner class DefaultToPayment(val presenter: BaseCheckoutPresenter) : Presenter.Transition(CheckoutDefault::class.java, paymentWidget.javaClass) {

        override fun startTransition(forward: Boolean) {
            presenter.startDefaultToPaymentTransition(forward)
        }

        override fun endTransition(forward: Boolean) {
            presenter.endDefaultToPaymentTransition(forward)
        }
    }

    private fun endDefaultToPaymentTransition(forward: Boolean) {
        if (!forward) {
            ckoViewModel.bottomCheckoutContainerStateObservable.onNext(true)
            ckoViewModel.transitionToObservable.onNext(CheckoutDefault::class.java.name)
            paymentWidget.setFocusForView()
            decorView.viewTreeObserver.removeOnGlobalLayoutListener(paymentLayoutListener)
            paymentWidget.viewmodel.updateBackgroundColor.onNext(forward)
        }
        ckoViewModel.showingPaymentWidgetSubject.onNext(forward)
    }

    private fun startDefaultToPaymentTransition(forward: Boolean) {
        loginWidget.setInverseVisibility(forward)
        hintContainer.visibility = if (forward) View.GONE else if (User.isLoggedIn(getContext())) View.GONE else View.VISIBLE
        travelerSummaryCardView.visibility = if (forward) View.GONE else View.VISIBLE
        legalInformationText.setInverseVisibility(forward)
        depositPolicyText.setInverseVisibility(forward)
        ckoViewModel.bottomContainerInverseVisibilityObservable.onNext(forward)
        if (!forward) {
            Ui.hideKeyboard(paymentWidget)
            invalidPaymentTypeWarningTextView.visibility = View.GONE
            cardProcessingFeeTextView.visibility = View.GONE
            debitCardsNotAcceptedTextView.visibility = View.GONE
            paymentWidget.show(PaymentWidget.PaymentDefault(), Presenter.FLAG_CLEAR_BACKSTACK)
            scrollView.layoutParams.height = height
            paymentWidget.viewmodel.showingPaymentForm.onNext(false)
        } else {
            decorView.viewTreeObserver.addOnGlobalLayoutListener(paymentLayoutListener)
        }
    }

    open val defaultToPayment = object : DefaultToPayment(this) {
    }

    fun showPaymentPresenter() {
        show(paymentWidget)
    }

    fun openTravelerPresenter() {
        show(travelersPresenter)
        travelersPresenter.showSelectOrEntryState()
    }

    open fun updateTravelerPresenter() {
        travelersPresenter.viewModel.refresh()
    }

    private fun setToolbarTitle() {
        travelersPresenter.toolbarTitleSubject.onNext(getCheckoutToolbarTitle(resources, Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelSecureCheckoutMessaging)))
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
        logoutDialog.show()
    }

    private fun logoutUser() {
        User.signOut(context)
        ckoViewModel.bottomCheckoutContainerStateObservable.onNext(false)
        ckoViewModel.transitionToObservable.onNext(CheckoutDefault::class.java.name)
        updateDbTravelers()
        initLoggedInState(false)
        updateTravelerPresenter()
        tripViewModel.performCreateTrip.onNext(Unit)
    }

    fun onLoginSuccess() {
        updateDbTravelers()
        initLoggedInState(true)
        tripViewModel.performCreateTrip.onNext(Unit)
        ckoViewModel.bottomCheckoutContainerStateObservable.onNext(true)
    }

    fun clearPaymentInfo() {
        if (!User.isLoggedIn(context)) {
            paymentWidget.clearPaymentInfo()
        }
    }

    fun resetTravelers() {
        travelersPresenter.resetTravelers()
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
            val newHeight = windowVisibleDisplayFrameRect.bottom - location[1] - offset

            if (lp.height != newHeight) {
                lp.height = newHeight
                scrollView.layoutParams = lp
            }
        })

        return layoutListener
    }

    /** Scrolling Space **/

    fun adjustScrollingSpace(bottomLayout: LinearLayout) {
        postDelayed({
            val contentViewLocation = IntArray(2)
            val bottomContainerLocation = IntArray(2)
            val scrollViewContainerLocation = IntArray(2)
            contentView.getLocationOnScreen(contentViewLocation)
            scrollView.getLocationOnScreen(scrollViewContainerLocation)
            bottomLayout.getLocationOnScreen(bottomContainerLocation)
            val contentViewBottomPosition = contentViewLocation[1] + contentView.height - scrollViewSpace.height
            val bottomContainerTopPosition = bottomContainerLocation[1]
            val spaceHeight = Math.max(0, scrollViewContainerLocation[1] + scrollView.height - contentViewBottomPosition)
            val distance = Math.max(0, contentViewBottomPosition - bottomContainerTopPosition) + spaceHeight

            val lp = scrollViewSpace.layoutParams
            lp.height = distance
            scrollViewSpace.layoutParams = lp
        }, ANIMATION_DELAY)
    }

    override fun addWindowSubscriptions() {
        super.addWindowSubscriptions()
        addWindowSubscription(travelerManager.travelersUpdated.subscribe { travelersPresenter.resetTravelers() })
        addWindowSubscription(paymentWidget.viewmodel.cardTypeSubject.subscribe { paymentType -> cardType = paymentType })
        addWindowSubscription(paymentWidget.viewmodel.expandObserver.subscribe { showPaymentPresenter() })
        addWindowSubscription(paymentWidget.viewmodel.billingInfoAndStatusUpdate.map { it.first }.subscribe(ckoViewModel.paymentCompleted))
    }

    override fun unsubscribeWindowAtTeardown() {
        super.unsubscribeWindowAtTeardown()
        getCheckoutViewModel().unsubscribeAll()
    }

    fun getPaymentWidgetViewModel(): PaymentViewModel {
        return paymentViewModel
    }

    fun showAlertDialogForPriceChange(tripResponse: TripResponse) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.price_change_text))
        builder.setMessage(Phrase.from(this, R.string.price_change_alert_TEMPLATE)
                .put("oldprice", tripResponse.getOldPrice()!!.formattedMoneyFromAmountAndCurrencyCode)
                .put("newprice", tripResponse.newPrice().formattedMoneyFromAmountAndCurrencyCode)
                .format())
        builder.setPositiveButton(context.getString(R.string.ok)) { dialog, which ->
            onCreateTripResponse(tripResponse)
            dialog.dismiss()
        }
        builder.setOnCancelListener { onCreateTripResponse(tripResponse) }
        val dialog = builder.create()
        dialog.show()
    }

    open inner class DefaultToTraveler(className: Class<*>) : ScaleTransition(this, mainContent, travelersPresenter, CheckoutDefault::class.java, className) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            ckoViewModel.bottomContainerInverseVisibilityObservable.onNext(forward)
            if (!forward) {
                Ui.hideKeyboard(travelersPresenter)
                travelersPresenter.toolbarNavIconContDescSubject.onNext(resources.getString(R.string.toolbar_nav_icon_cont_desc))
                travelersPresenter.viewModel.updateCompletionStatus()
                if (travelersPresenter.viewModel.areTravelersEmpty()) {
                    travelersPresenter.travelerEntryWidget.resetErrorState()
                }
                setToolbarTitle()
                decorView.viewTreeObserver.removeOnGlobalLayoutListener(travelerLayoutListener)
                travelersPresenter.toolbarTitleSubject.onNext(getCheckoutToolbarTitle(resources, Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelSecureCheckoutMessaging)))
            } else {
                decorView.viewTreeObserver.addOnGlobalLayoutListener(travelerLayoutListener)
            }
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            if (!forward) {
                ckoViewModel.bottomCheckoutContainerStateObservable.onNext(true)
                ckoViewModel.transitionToObservable.onNext(CheckoutDefault::class.java.name)
                travelersPresenter.setFocusForView()
                travelerSummaryCard.setFocusForView()
                decorView.viewTreeObserver.removeOnGlobalLayoutListener(travelerLayoutListener)
            }
        }
    }

    fun doHarlemShakes() {
        if (!travelersPresenter.viewModel.allTravelersValid()) {
            AnimUtils.doTheHarlemShake(travelerSummaryCardView)
        }
        if (!paymentWidget.isComplete()) {
            AnimUtils.doTheHarlemShake(paymentWidget)
        }
    }
}