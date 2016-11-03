package com.expedia.bookings.presenter.rail

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import android.view.animation.Animation
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.rail.responses.RailCreateTripResponse
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.setFocusForView
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.CheckoutToolbar
import com.expedia.bookings.widget.PaymentWidget
import com.expedia.bookings.widget.PriceChangeWidget
import com.expedia.bookings.widget.SlideToWidgetLL
import com.expedia.bookings.widget.TotalPriceWidget
import com.expedia.bookings.widget.packages.BillingDetailsPaymentWidget
import com.expedia.bookings.widget.rail.RailTicketDeliveryEntryWidget
import com.expedia.bookings.widget.rail.RailTicketDeliveryOverviewWidget
import com.expedia.bookings.widget.rail.RailTravelerEntryWidget
import com.expedia.bookings.widget.rail.TicketDeliveryMethod
import com.expedia.bookings.widget.shared.SlideToPurchaseWidget
import com.expedia.bookings.widget.traveler.TravelerSummaryCard
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
import com.expedia.vm.traveler.RailCheckoutTravelerViewModel
import com.expedia.vm.traveler.RailTravelerSummaryViewModel
import com.expedia.vm.traveler.SimpleTravelerViewModel
import java.util.concurrent.TimeUnit

class RailCheckoutPresenter(context: Context, attr: AttributeSet?) : Presenter(context, attr),
        SlideToWidgetLL.ISlideToListener {
    val toolbar: CheckoutToolbar by bindView(R.id.rail_checkout_toolbar)
    val cardProcessingFeeTextView: TextView by bindView(R.id.card_processing_fee)

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

    private val checkoutDialog = ProgressDialog(context)

    val checkoutViewModel = RailCheckoutViewModel(context)

    private val toolbarViewModel = CheckoutToolbarViewModel(context)
    private val totalPriceViewModel = RailTotalPriceViewModel(context)
    private val priceBreakDownViewModel = RailCostSummaryBreakdownViewModel(context)
    private val priceChangeViewModel = RailPriceChangeViewModel(context)

    private val paymentViewModel = PaymentViewModel(context)
    private val cardFeeViewModel = RailCreditCardFeesViewModel()
    private val ticketDeliveryOverviewViewModel = RailTicketDeliveryOverviewViewModel(context)
    private val ticketDeliveryEntryViewModel = RailTicketDeliveryEntryViewModel(context)

    private val travelerCardViewModel = RailTravelerSummaryViewModel(context)
    private val travelerCheckoutViewModel = RailCheckoutTravelerViewModel(context)

    private var cardFeeSlideAnimation: Animation? = null

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

        paymentWidget.viewmodel = paymentViewModel
        paymentViewModel.lineOfBusiness.onNext(LineOfBusiness.RAILS)
        paymentViewModel.isCreditCardRequired.onNext(true)
        paymentViewModel.billingInfoAndStatusUpdate.map { billingInfoAndStatusPair ->
            billingInfoAndStatusPair.first
        }.subscribe(checkoutViewModel.paymentCompleteObserver)

        paymentViewModel.showingPaymentForm.subscribe(checkoutViewModel.showingPaymentForm)
        paymentViewModel.expandObserver.subscribe {
            show(paymentWidget)
        }
        paymentWidget.creditCardFeesView.viewModel = cardFeeViewModel

        ticketDeliveryEntryViewModel.ticketDeliveryOptionSubject.subscribe(cardFeeViewModel.ticketDeliveryOptionSubject)

        travelerCardWidget.setOnClickListener {
            show(travelerEntryWidget)
        }
        travelerCheckoutViewModel.travelerCompletenessStatus.subscribe(travelerCardViewModel.travelerStatusObserver)
        travelerCheckoutViewModel.travelerCompletenessStatus.subscribe { status ->
            if (status == TravelerCheckoutStatus.CLEAN || status == TravelerCheckoutStatus.DIRTY) {
                checkoutViewModel.clearTravelers.onNext(Unit)
            } else {
                checkoutViewModel.travelerCompleteObserver.onNext(travelerCheckoutViewModel.getTraveler(0))
            }
        }
        checkoutViewModel.sliderPurchaseTotalText.subscribe { total ->
            slideToPurchaseWidget.updatePricingDisplay(total.toString())
        }
        checkoutViewModel.showCheckoutDialogObservable.subscribe { show ->
            if (show) checkoutDialog.show() else checkoutDialog.dismiss()
        }
        checkoutViewModel.bookingSuccessSubject.subscribe {
            slideToPurchaseWidget.reset()
        }

        priceChangeWidget.viewmodel = priceChangeViewModel
        checkoutViewModel.priceChangeObservable.subscribe {
            checkoutViewModel.fetchCardFees(paymentViewModel.cardBIN.value)
            priceChangeViewModel.priceChangedObserver.onNext(Unit)
            priceChangeWidget.visibility = View.VISIBLE
            slideToPurchaseWidget.reset()
        }

        travelerEntryWidget.travelerCompleteSubject.subscribe {
            show(DefaultCheckout(), FLAG_CLEAR_BACKSTACK)
        }

        toolbar.viewModel = toolbarViewModel
        slideToPurchaseWidget.addSlideListener(this)

        initializePriceWidget()
        wireUpToolbarWithPayment()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addDefaultTransition(defaultTransition)
        addTransition(defaultToPayment)
        addTransition(defaultToTraveler)
        addTransition(defaultToTicketDeliveryOptions)
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
        travelerCheckoutViewModel.refresh()
        travelerCardWidget.viewModel = travelerCardViewModel
        show(DefaultCheckout())
    }

    fun openTicketDeliveryEntry() {
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
        toolbarViewModel.doneClicked.subscribe {
            if (currentState == BillingDetailsPaymentWidget::class.java.name) {
                paymentWidget.doneClicked.onNext(Unit)
            } else if (currentState == RailTicketDeliveryEntryWidget::class.java.name) {
                ticketDeliveryEntryWidget.doneClicked.onNext(Unit)
            }
        }
        paymentWidget.toolbarTitle.subscribe(toolbarViewModel.toolbarTitle)
        paymentWidget.focusedView.subscribe(toolbarViewModel.currentFocus)
        paymentWidget.filledIn.subscribe(toolbarViewModel.formFilledIn)
        paymentWidget.menuVisibility.subscribe(toolbarViewModel.menuVisibility)
        paymentWidget.enableMenuItem.subscribe(toolbarViewModel.enableMenuItem)
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
        ticketDeliveryEntryViewModel.ticketDeliveryOptions.onNext(response.railDomainProduct?.railOffer?.ticketDeliveryOptionList)

        updatePricing(response)
        showLegalInformationText(response)
    }

    private fun showLegalInformationText(response: RailCreateTripResponse) {
        val rulesAndRestrictionsURL = PointOfSale.getPointOfSale().railsRulesAndRestrictionsUrl + response.tripId
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

    private val defaultTransition = object : Presenter.DefaultTransition(DefaultCheckout::class.java.name) {
        override fun endTransition(forward: Boolean) {
            paymentWidget.show(PaymentWidget.PaymentDefault(), Presenter.FLAG_CLEAR_BACKSTACK)
            priceChangeWidget.visibility = View.GONE
            if (checkoutViewModel.isValidForBooking()) {
                slideToPurchaseWidget.show()
            } else {
                slideToPurchaseWidget.visibility = View.GONE
            }
        }
    }

    private val defaultToPayment = object : Presenter.Transition(DefaultCheckout::class.java, BillingDetailsPaymentWidget::class.java) {
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
            if (!forward) {
                transitionToCheckoutEnd()
                Ui.hideKeyboard(paymentWidget)
                paymentWidget.setFocusForView()
                checkoutViewModel.paymentTypeSelectedHasCardFee.onNext(false)
            }
        }
    }

    private val defaultToTraveler = object : Presenter.Transition(DefaultCheckout::class.java, RailTravelerEntryWidget::class.java) {
        override fun startTransition(forward: Boolean) {
            if (forward) {
                travelerEntryWidget.viewModel = SimpleTravelerViewModel(context, 0)
                paymentWidget.visibility = View.GONE
                hideCheckoutStart()
            } else {
                travelerCheckoutViewModel.updateCompletionStatus()
                transitionToCheckoutStart()
                travelerEntryWidget.visibility = View.GONE
            }
        }

        override fun endTransition(forward: Boolean) {
            if (!forward) {
                transitionToCheckoutEnd()
                Ui.hideKeyboard(paymentWidget)
                paymentWidget.setFocusForView()
            } else {
                toolbar.visibility = View.GONE
                travelerEntryWidget.visibility = View.VISIBLE
            }
        }
    }

    private val defaultToTicketDeliveryOptions = object : Presenter.Transition(DefaultCheckout::class.java, RailTicketDeliveryEntryWidget::class.java) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            if (forward) {
                hideCheckoutStart()

                toolbarViewModel.toolbarTitle.onNext(resources.getString(R.string.ticket_delivery))
                toolbarViewModel.toolbarNavIcon.onNext(ArrowXDrawableUtil.ArrowDrawableType.CLOSE)
                toolbarViewModel.enableMenuItem.onNext(ticketDeliveryEntryWidget.isComplete())
                toolbarViewModel.visibleMenuWithTitleDone.onNext(Unit)
                toolbarViewModel.menuVisibility.onNext(true)
            } else {
                transitionToCheckoutStart()
            }
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            ticketDeliveryEntryWidget.setVisibility(forward)

            if (forward) {
                paymentWidget.visibility = View.GONE
                hideCheckoutStart()
            } else {
                transitionToCheckoutEnd()
                Ui.hideKeyboard(ticketDeliveryEntryWidget)
                ticketDeliveryEntryWidget.setFocusForView()
            }
        }
    }

    private fun hideCheckoutStart() {
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
        travelerCardWidget.visibility = View.VISIBLE
        paymentWidget.visibility = View.VISIBLE
        totalPriceWidget.visibility = View.VISIBLE
        ticketDeliveryOverviewWidget.visibility = View.VISIBLE
        legalInformationText.visibility = View.VISIBLE

        if (checkoutViewModel.isValidForBooking()) {
            slideToPurchaseWidget.show()
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