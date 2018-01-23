package com.expedia.bookings.rail.presenter

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import android.view.animation.Animation
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.activity.AccountLibActivity
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.rail.responses.RailCreateTripResponse
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.bookings.otto.Events
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.rail.widget.AccessibleProgressDialog
import com.expedia.bookings.rail.widget.RailTicketDeliveryEntryWidget
import com.expedia.bookings.rail.widget.RailTicketDeliveryOverviewWidget
import com.expedia.bookings.rail.widget.RailTravelerEntryWidget
import com.expedia.bookings.rail.widget.TicketDeliveryMethod
import com.expedia.bookings.tracking.RailTracking
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.setFocusForView
import com.expedia.bookings.widget.AccountButton
import com.expedia.bookings.widget.CheckoutLoginExtender
import com.expedia.bookings.widget.CheckoutToolbar
import com.expedia.bookings.widget.PaymentWidget
import com.expedia.bookings.widget.PriceChangeWidget
import com.expedia.bookings.widget.SlideToWidgetLL
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.TotalPriceWidget
import com.expedia.bookings.widget.packages.BillingDetailsPaymentWidget
import com.expedia.bookings.widget.shared.SlideToPurchaseWidget
import com.expedia.bookings.widget.traveler.TravelerSummaryCard
import com.expedia.util.Optional
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.vm.CheckoutToolbarViewModel
import com.expedia.vm.PaymentViewModel
import com.expedia.vm.rail.RailCheckoutViewModel
import com.expedia.vm.rail.RailCostSummaryBreakdownViewModel
import com.expedia.vm.rail.RailCreateTripViewModel
import com.expedia.vm.rail.RailCreditCardFeesViewModel
import com.expedia.vm.rail.RailPriceChangeViewModel
import com.expedia.vm.rail.RailTicketDeliveryEntryViewModel
import com.expedia.vm.rail.RailTicketDeliveryOverviewViewModel
import com.expedia.vm.rail.RailTotalPriceViewModel
import com.expedia.vm.traveler.RailTravelerSummaryViewModel
import com.expedia.vm.traveler.RailTravelersViewModel
import com.expedia.vm.traveler.SimpleTravelerEntryWidgetViewModel
import com.squareup.otto.Subscribe
import java.util.concurrent.TimeUnit

class RailCheckoutPresenter(context: Context, attr: AttributeSet?) : Presenter(context, attr),
        SlideToWidgetLL.ISlideToListener, AccountButton.AccountButtonClickListener {

    private val travelerManager = Ui.getApplication(getContext()).travelerComponent().travelerManager()
    private val userStateManager = Ui.getApplication(getContext()).appComponent().userStateManager()

    val toolbar: CheckoutToolbar by bindView(R.id.rail_checkout_toolbar)
    val cardProcessingFeeTextView: TextView by bindView(R.id.card_processing_fee)

    val loginContainer: LinearLayout by bindView(R.id.login_container)
    val loginWidget: AccountButton by bindView(R.id.login_button)
    val hintContainer: LinearLayout by bindView(R.id.hint_container)

    val legalInformationText: TextView by bindView(R.id.legal_information_text_view)
    val travelerCardWidget: TravelerSummaryCard by bindView(R.id.rail_traveler_card_view)
    val travelerEntryWidget: RailTravelerEntryWidget by bindView(R.id.rail_traveler_entry_widget)

    val paymentViewStub: ViewStub by bindView(R.id.rail_payment_info_card_view_stub)
    val paymentWidget: BillingDetailsPaymentWidget

    val ticketDeliveryEntryWidget: RailTicketDeliveryEntryWidget by bindView(R.id.ticket_delivery_entry_widget)
    val ticketDeliveryOverviewWidget: RailTicketDeliveryOverviewWidget by bindView(R.id.ticket_delivery_overview_widget)

    val priceChangeWidget: PriceChangeWidget by bindView(R.id.rail_price_change_widget)
    val totalPriceWidget: TotalPriceWidget by bindView(R.id.rail_total_price_widget)
    val slideToPurchaseWidget: SlideToPurchaseWidget by bindView(R.id.rail_slide_to_purchase_widget)

    val checkoutDialog = AccessibleProgressDialog(context)

    val checkoutViewModel = RailCheckoutViewModel(context)

    private val toolbarViewModel = CheckoutToolbarViewModel(context)
    private val totalPriceViewModel = RailTotalPriceViewModel(context)
    private val priceBreakDownViewModel = RailCostSummaryBreakdownViewModel(context, true)
    private val priceChangeViewModel = RailPriceChangeViewModel(context)

    private val paymentViewModel = PaymentViewModel(context)
    private val cardFeeViewModel = RailCreditCardFeesViewModel()
    private val ticketDeliveryOverviewViewModel = RailTicketDeliveryOverviewViewModel(context)
    private val ticketDeliveryEntryViewModel = RailTicketDeliveryEntryViewModel(context)

    private val travelerCardViewModel = RailTravelerSummaryViewModel(context)
    val travelersViewModel = RailTravelersViewModel(context)

    private var cardFeeSlideAnimation: Animation? = null

    val logoutDialog: AlertDialog by lazy {
        createLogoutDialog()
    }

    var createTripViewModel: RailCreateTripViewModel by notNullAndObservable { vm ->
        vm.tripResponseObservable.subscribe { response ->
            initCreateTrip(response)
        }
    }

    init {

        View.inflate(context, R.layout.rail_checkout_presenter, this)
        toolbar.setNavigationOnClickListener {
            val activity = context as AppCompatActivity
            activity.onBackPressed()
        }

        paymentWidget = paymentViewStub.inflate() as BillingDetailsPaymentWidget
        setupPayment()
        setupTraveler()

        ticketDeliveryEntryViewModel.ticketDeliveryOptionSubject.subscribe(cardFeeViewModel.ticketDeliveryOptionSubject)
        priceChangeWidget.viewmodel = priceChangeViewModel

        setupCheckoutViewModel(context)

        travelerEntryWidget.travelerCompleteSubject.subscribe {
            show(DefaultCheckout(), FLAG_CLEAR_BACKSTACK)
        }

        toolbar.viewModel = toolbarViewModel

        setClickListeners()
        initializePriceWidget()
        wireUpToolbarWithPayment()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addDefaultTransition(defaultTransition)
        addTransition(defaultToPayment)
        addTransition(defaultToTraveler)
        addTransition(defaultToTicketDeliveryOptions)
        initLoggedInState(userStateManager.isUserAuthenticated())
    }

    override fun accountLoginClicked() {
        val args = AccountLibActivity.createArgumentsBundle(LineOfBusiness.RAILS, CheckoutLoginExtender())
        userStateManager.signIn(context as Activity, args)
    }

    override fun accountLogoutClicked() {
        logoutDialog.show()
    }

    @Subscribe fun onUserLoggedIn(@Suppress("UNUSED_PARAMETER") event: Events.LoggedInSuccessful) {
        onLoginSuccess()
    }

    private fun setupPayment() {
        paymentWidget.viewmodel = paymentViewModel
        paymentViewModel.lineOfBusiness.onNext(LineOfBusiness.RAILS)
        paymentViewModel.isCreditCardRequired.onNext(true)
        paymentViewModel.billingInfoAndStatusUpdate.map { billingInfoAndStatusPair ->
            Optional(billingInfoAndStatusPair.first)
        }.subscribe(checkoutViewModel.paymentCompleteObserver)

        paymentViewModel.showingPaymentForm.subscribe(checkoutViewModel.showingPaymentForm)
        paymentViewModel.expandObserver.subscribe {
            show(paymentWidget)
            RailTracking().trackRailEditPaymentInfo()
        }
        paymentWidget.creditCardFeesView.viewModel = cardFeeViewModel
    }

    private fun setupCheckoutViewModel(context: Context) {
        checkoutViewModel.sliderPurchaseTotalText.subscribe { total ->
            slideToPurchaseWidget.updatePricingDisplay(total.toString())
        }
        checkoutViewModel.showCheckoutDialogObservable.subscribe { show ->
            if (show) checkoutDialog.show(context.getString(R.string.booking_loading)) else checkoutDialog.dismiss()
        }
        checkoutViewModel.bookingSuccessSubject.subscribe {
            slideToPurchaseWidget.reset()
        }

        checkoutViewModel.priceChangeObservable.subscribe { pair ->
            checkoutViewModel.trackPriceChange(pair.first, pair.second)
            checkoutViewModel.fetchCardFees(paymentViewModel.cardBIN.value)
            priceChangeViewModel.priceChangedObserver.onNext(Unit)
            priceChangeWidget.visibility = View.VISIBLE
            slideToPurchaseWidget.reset()
        }

        checkoutViewModel.showNoInternetRetryDialog.subscribe {
            checkoutDialog.dismiss()
            val retryFun = fun() {
                onSlideAllTheWay()
            }
            val cancelFun = fun() {
                onSlideAbort()
                back()
            }
            DialogFactory.showNoInternetRetryDialog(context, retryFun, cancelFun)
        }
    }

    private fun setClickListeners() {
        slideToPurchaseWidget.addSlideListener(this)
        loginWidget.setListener(this)
    }

    private fun setupTraveler() {
        travelerCardWidget.setOnClickListener {
            show(travelerEntryWidget)
        }
        travelersViewModel.travelersCompletenessStatus.subscribe(travelerCardViewModel.travelerStatusObserver)
        travelersViewModel.travelersCompletenessStatus.subscribe { status ->
            if (status == TravelerCheckoutStatus.CLEAN || status == TravelerCheckoutStatus.DIRTY) {
                checkoutViewModel.clearTravelers.onNext(Unit)
            } else {
                checkoutViewModel.travelerCompleteObserver.onNext(travelersViewModel.getTraveler(0))
            }
        }
    }

    private fun createLogoutDialog(): AlertDialog {
        val logoutUser = fun() {
            userStateManager.signOut()
            resetTravelers()
            initLoggedInState(false)
        }
        return DialogFactory.createLogoutDialog(context, logoutUser)
    }

    private fun onLoginSuccess() {
        resetTravelers()
        initLoggedInState(true)
    }

    private fun initLoggedInState(userLoggedIn: Boolean) {
        val user = userStateManager.userSource.user

        loginWidget.bind(false, userLoggedIn, user, LineOfBusiness.RAILS)
        hintContainer.setVisibility(!userLoggedIn)
        travelersViewModel.refresh()
        updateSlideToPurchase()
    }

    private fun setupCardFeesModal() {
        checkoutViewModel.cardFeeTextSubject.subscribeText(cardProcessingFeeTextView)

        paymentViewModel.cardBIN
                .debounce(1, TimeUnit.SECONDS)
                .subscribe { checkoutViewModel.fetchCardFees(cardId = it) }

        paymentViewModel.resetCardFees.subscribe {
            checkoutViewModel.resetCardFees()
        }

        checkoutViewModel.displayCardFeesObservable.subscribe { displayCardFee ->
            if (displayCardFee && cardProcessingFeeTextView.visibility == View.GONE) {
                cardProcessingFeeTextView.visibility = View.VISIBLE
                cardFeeSlideAnimation = AnimUtils.slideInAbove(cardProcessingFeeTextView, paymentWidget)
            } else if (!displayCardFee && cardProcessingFeeTextView.visibility == View.VISIBLE) {
                cardProcessingFeeTextView.startAnimation(cardFeeSlideAnimation)
            }
        }

        checkoutViewModel.updatePricingSubject.subscribe { response ->
            updatePricing(response)
        }

        checkoutViewModel.cardFeeErrorObservable.subscribe {
            checkoutViewModel.updateTotalPriceWithTdoFees()
            showErrorDialog(R.string.rail_cardfee_error_title, R.string.rail_cardfee_error_message)
        }
    }

    private fun showErrorDialog(title: Int, message: Int) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(context.getString(R.string.DONE)) { dialog, which -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.show()
    }

    fun onCheckoutOpened() {
        travelersViewModel.refresh()
        travelerCardWidget.viewModel = travelerCardViewModel
        show(DefaultCheckout())
    }

    private fun resetTravelers() {
        travelerManager.updateRailTravelers()
        travelersViewModel.refresh()
        checkoutViewModel.clearTravelers.onNext(Unit)
    }

    private fun openTicketDeliveryEntry() {
        ticketDeliveryEntryWidget.entryStatus(ticketDeliveryOverviewViewModel.ticketDeliverySelectedObserver.value)
        show(ticketDeliveryEntryWidget)
    }

    private fun initializePriceWidget() {
        priceBreakDownViewModel.iconVisibilityObservable.subscribe { show ->
            totalPriceWidget.toggleBundleTotalCompoundDrawable(show)
            totalPriceViewModel.costBreakdownEnabledObservable.onNext(show)
        }

        totalPriceWidget.viewModel = totalPriceViewModel
        totalPriceWidget.breakdown.viewmodel = priceBreakDownViewModel
    }

    private fun wireUpToolbarWithPayment() {
        paymentWidget.viewmodel.doneClickedMethod.subscribe(toolbarViewModel.doneClickedMethod)
        paymentWidget.toolbarTitle.subscribe(toolbarViewModel.toolbarTitle)
        paymentWidget.focusedView.subscribe(toolbarViewModel.currentFocus)
        paymentWidget.filledIn.subscribe(toolbarViewModel.showDone)
        paymentWidget.viewmodel.menuVisibility.subscribe(toolbarViewModel.menuVisibility)
        paymentWidget.viewmodel.enableMenuItem.subscribe(toolbarViewModel.enableMenuItem)
        paymentWidget.visibleMenuWithTitleDone.subscribe(toolbarViewModel.visibleMenuWithTitleDone)
        paymentWidget.toolbarNavIcon.subscribe(toolbarViewModel.toolbarNavIcon)
    }

    private fun initializeTicketDelivery() {
        ticketDeliveryOverviewWidget.viewModel = ticketDeliveryOverviewViewModel
        ticketDeliveryOverviewWidget.setOnClickListener {
            openTicketDeliveryEntry()
        }

        ticketDeliveryEntryWidget.viewModel = ticketDeliveryEntryViewModel
        ticketDeliveryEntryViewModel.ticketDeliveryMethodSelected.subscribe(ticketDeliveryOverviewViewModel.ticketDeliverySelectedObserver)

        ticketDeliveryEntryViewModel.ticketDeliveryOptionSubject.onNext(ticketDeliveryEntryWidget.getTicketDeliveryOption())
        checkoutViewModel.ticketDeliveryCompleteObserver.onNext(ticketDeliveryEntryWidget.getTicketDeliveryOption())
        ticketDeliveryOverviewViewModel.ticketDeliverySelectedObserver.onNext(TicketDeliveryMethod.PICKUP_AT_STATION)

        ticketDeliveryEntryWidget.closeSubject.subscribe {
            show(DefaultCheckout(), FLAG_CLEAR_BACKSTACK)
            checkoutViewModel.ticketDeliveryCompleteObserver.onNext(ticketDeliveryEntryWidget.getTicketDeliveryOption())
        }

        ticketDeliveryEntryViewModel.ticketDeliveryOptionSubject.subscribe { tdo ->
            checkoutViewModel.updateTicketDeliveryToken(tdo.deliveryOptionToken.name)
            checkoutViewModel.fetchCardFees(paymentViewModel.cardBIN.value)
        }
    }

    private fun initCreateTrip(response: RailCreateTripResponse) {
        paymentWidget.clearCCAndCVV()
        checkoutViewModel.createTripObserver.onNext(response)

        initializeTicketDelivery()
        setupCardFeesModal()
        response.railDomainProduct?.railOffer?.ticketDeliveryOptionList?.let {
            ticketDeliveryEntryViewModel.ticketDeliveryOptions.onNext(it)
        }

        updatePricing(response)
        showLegalInformationText(response)
    }

    private fun showLegalInformationText(response: RailCreateTripResponse) {
        val rulesAndRestrictionsURL = PointOfSale.getPointOfSale().railsRulesAndRestrictionsUrl + response.offerToken
        legalInformationText.text = StrUtils.generateRailLegalClickableLink(context, rulesAndRestrictionsURL)
        legalInformationText.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun updatePricing(response: RailCreateTripResponse) {
        response.updateOfferWithTDOAndCCFees()

        checkoutViewModel.totalPriceObserver.onNext(response.totalPayablePrice)
        totalPriceViewModel.total.onNext(response.totalPayablePrice)
        totalPriceViewModel.costBreakdownEnabledObservable.onNext(true)
        priceBreakDownViewModel.railCostSummaryBreakdownObservable.onNext(response)
        cardFeeViewModel.validFormsOfPaymentSubject.onNext(response.validFormsOfPayment)
    }

    class DefaultCheckout

    private val defaultTransition = object : DefaultTransition(DefaultCheckout::class.java.name) {
        override fun endTransition(forward: Boolean) {
            paymentWidget.show(PaymentWidget.PaymentDefault(), Presenter.FLAG_CLEAR_BACKSTACK)
            priceChangeWidget.visibility = View.GONE
            updateSlideToPurchase()
            AccessibilityUtil.setFocusToToolbarNavigationIcon(toolbar)
        }
    }

    private val defaultToPayment = object : Transition(DefaultCheckout::class.java, BillingDetailsPaymentWidget::class.java) {
        override fun startTransition(forward: Boolean) {
            if (forward) {
                hideCheckoutStart()
            } else {
                paymentViewModel.showingPaymentForm.onNext(false)
                paymentWidget.show(PaymentWidget.PaymentDefault(), Presenter.FLAG_CLEAR_BACKSTACK)
                transitionToCheckoutStart()
            }
        }

        override fun endTransition(forward: Boolean) {
            if (forward) {
                resetFocusToToolbarNavigationIcon()
            } else {
                transitionToCheckoutEnd()
                Ui.hideKeyboard(paymentWidget)
                paymentWidget.setFocusForView()
                checkoutViewModel.paymentTypeSelectedHasCardFee.onNext(false)
            }
        }
    }

    private val defaultToTraveler = object : Transition(DefaultCheckout::class.java, RailTravelerEntryWidget::class.java) {
        override fun startTransition(forward: Boolean) {
            if (forward) {
                travelerEntryWidget.viewModel = SimpleTravelerEntryWidgetViewModel(context, 0)
                val userLoggedIn = userStateManager.isUserAuthenticated()
                travelerEntryWidget.viewModel.showEmailSubject.onNext(!userLoggedIn)
                travelerEntryWidget.viewModel.showTravelerButtonObservable.onNext(userLoggedIn)
                paymentWidget.visibility = View.GONE
                hideCheckoutStart()
                RailTracking().trackRailEditTravelerInfo()
            } else {
                travelersViewModel.updateCompletionStatus()
                transitionToCheckoutStart()
                travelerEntryWidget.viewModel.clearPopupsSubject.onNext(Unit)
                travelerEntryWidget.visibility = View.GONE
            }
        }

        override fun endTransition(forward: Boolean) {
            if (forward) {
                toolbar.visibility = View.GONE
                travelerEntryWidget.visibility = View.VISIBLE
                travelerEntryWidget.resetFocusToToolbarNavigationIcon()
            } else {
                transitionToCheckoutEnd()
                Ui.hideKeyboard(travelerEntryWidget)
                travelerCardWidget.setFocusForView()
            }
        }
    }

    private val defaultToTicketDeliveryOptions = object : Transition(DefaultCheckout::class.java, RailTicketDeliveryEntryWidget::class.java) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            if (forward) {
                hideCheckoutStart()
            } else {
                transitionToCheckoutStart()
            }
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            ticketDeliveryEntryWidget.setVisibility(forward)

            if (forward) {
                toolbar.visibility = View.GONE
                paymentWidget.visibility = View.GONE
                hideCheckoutStart()
                ticketDeliveryEntryWidget.resetFocusToToolbarNavigationIcon()
            } else {
                transitionToCheckoutEnd()
                Ui.hideKeyboard(ticketDeliveryEntryWidget)
                ticketDeliveryOverviewWidget.setFocusForView()
            }
        }
    }

    private fun resetFocusToToolbarNavigationIcon() {
        AccessibilityUtil.setFocusToToolbarNavigationIcon(toolbar)
    }

    private fun hideCheckoutStart() {
        loginContainer.visibility = View.GONE
        travelerCardWidget.visibility = View.GONE
        ticketDeliveryOverviewWidget.visibility = View.GONE
        totalPriceWidget.visibility = View.GONE
        legalInformationText.visibility = View.GONE
        priceChangeWidget.visibility = View.GONE
        slideToPurchaseWidget.visibility = View.GONE
    }

    private fun transitionToCheckoutStart() {
        toolbar.visibility = View.VISIBLE
        toolbarViewModel.toolbarNavIcon.onNext(ArrowXDrawableUtil.ArrowDrawableType.BACK)
        toolbarViewModel.toolbarTitle.onNext(context.getString(R.string.checkout_text))
        toolbarViewModel.enableMenuItem.onNext(true)
        toolbarViewModel.menuVisibility.onNext(false)
    }

    private fun transitionToCheckoutEnd() {
        loginContainer.visibility = View.VISIBLE
        travelerCardWidget.visibility = View.VISIBLE
        paymentWidget.visibility = View.VISIBLE
        totalPriceWidget.visibility = View.VISIBLE
        ticketDeliveryOverviewWidget.visibility = View.VISIBLE
        legalInformationText.visibility = View.VISIBLE
        updateSlideToPurchase()
    }

    private fun updateSlideToPurchase() {
        if (checkoutViewModel.isValidForBooking()) {
            slideToPurchaseWidget.show()
            RailTracking().trackRailCheckoutSlideToPurchase(paymentWidget.getCardType())
        } else {
            slideToPurchaseWidget.visibility = View.GONE
        }
    }

    private fun View.setVisibility(visible: Boolean) {
        this.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun onSlideStart() {
    }

    override fun onSlideProgress(pixels: Float, total: Float) {
    }

    override fun onSlideAllTheWay() {
        if (checkoutViewModel.builder.isValid()) {
            checkoutViewModel.checkoutParams.onNext(checkoutViewModel.builder.build())
        }
    }

    override fun onSlideAbort() {
        slideToPurchaseWidget.reset()
    }
}
