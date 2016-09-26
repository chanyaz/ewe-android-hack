package com.expedia.bookings.presenter.rail

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.rail.responses.RailCreateTripResponse
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.setFocusForView
import com.expedia.bookings.widget.CheckoutToolbar
import com.expedia.bookings.widget.PaymentWidget
import com.expedia.bookings.widget.SlideToWidgetLL
import com.expedia.bookings.widget.TotalPriceWidget
import com.expedia.bookings.widget.packages.BillingDetailsPaymentWidget
import com.expedia.bookings.widget.rail.CreateTripProgressDialog
import com.expedia.bookings.widget.rail.RailTicketDeliveryEntryWidget
import com.expedia.bookings.widget.rail.RailTicketDeliveryOverviewWidget
import com.expedia.bookings.widget.rail.RailTravelerEntryWidget
import com.expedia.bookings.widget.rail.TicketDeliveryMethod
import com.expedia.bookings.widget.shared.SlideToPurchaseWidget
import com.expedia.bookings.widget.traveler.TravelerSummaryCard
import com.expedia.util.notNullAndObservable
import com.expedia.vm.CheckoutToolbarViewModel
import com.expedia.vm.PaymentViewModel
import com.expedia.vm.rail.RailCheckoutViewModel
import com.expedia.vm.rail.RailCostSummaryBreakdownViewModel
import com.expedia.vm.rail.RailCreateTripViewModel
import com.expedia.vm.rail.RailCreditCardFeesViewModel
import com.expedia.vm.rail.RailTicketDeliveryEntryViewModel
import com.expedia.vm.rail.RailTicketDeliveryOverviewViewModel
import com.expedia.vm.rail.RailTotalPriceViewModel
import com.expedia.vm.traveler.RailCheckoutTravelerViewModel
import com.expedia.vm.traveler.RailTravelerSummaryViewModel
import com.expedia.vm.traveler.SimpleTravelerViewModel

class RailCheckoutPresenter(context: Context, attr: AttributeSet?) : Presenter(context, attr),
        SlideToWidgetLL.ISlideToListener {
    val toolbar: CheckoutToolbar by bindView(R.id.rail_checkout_toolbar)

    val travelerCardWidget: TravelerSummaryCard by bindView(R.id.rail_traveler_card_view)
    val travelerEntryWidget: RailTravelerEntryWidget by bindView(R.id.rail_traveler_entry_widget)

    val paymentViewStub: ViewStub by bindView(R.id.rail_payment_info_card_view_stub)
    val paymentWidget: BillingDetailsPaymentWidget

    val ticketDeliveryEntryWidget: RailTicketDeliveryEntryWidget by bindView(R.id.ticket_delivery_entry_widget)
    val ticketDeliveryOverviewWidget: RailTicketDeliveryOverviewWidget by lazy {
        val view = findViewById(R.id.ticket_delivery_overview_widget) as RailTicketDeliveryOverviewWidget
        view.viewModel = RailTicketDeliveryOverviewViewModel(context)
        view.setOnClickListener {
            openTicketDeliveryEntry()
        }
        view
    }

    val totalPriceWidget: TotalPriceWidget by bindView(R.id.rail_total_price_widget)
    val slideToPurchaseWidget: SlideToPurchaseWidget by bindView(R.id.rail_slide_to_purchase_widget)

    val checkoutViewModel = RailCheckoutViewModel(context)
    val travelerCardViewModel = RailTravelerSummaryViewModel(context)
    val travelerCheckoutViewModel = RailCheckoutTravelerViewModel(context)

    var createTripViewModel: RailCreateTripViewModel by notNullAndObservable { vm ->
        vm.offerCodeSelectedObservable.subscribe {
            createTripDialog.show()
        }
        vm.tripResponseObservable.subscribe { response ->
            updateCreateTrip(response)
        }

    }
    val createTripDialog = CreateTripProgressDialog(context)

    init {
        View.inflate(context, R.layout.rail_checkout_presenter, this)
        toolbar.setNavigationOnClickListener {
            val activity = context as AppCompatActivity
            activity.onBackPressed()
        }

        paymentWidget = paymentViewStub.inflate() as BillingDetailsPaymentWidget
        paymentWidget.viewmodel = PaymentViewModel(context)
        paymentWidget.viewmodel.lineOfBusiness.onNext(LineOfBusiness.RAILS)
        paymentWidget.viewmodel.isCreditCardRequired.onNext(true)
        paymentWidget.viewmodel.billingInfoAndStatusUpdate.map { billingInfoAndStatusPair ->
            billingInfoAndStatusPair.first
        }.subscribe(checkoutViewModel.paymentCompleteObserver)


        paymentWidget.viewmodel.expandObserver.subscribe {
            show(paymentWidget)
        }

        paymentWidget.creditCardFeesView.viewModel = RailCreditCardFeesViewModel()

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

        travelerEntryWidget.travelerCompleteSubject.subscribe {
            show(DefaultCheckout(), FLAG_CLEAR_BACKSTACK)
        }
        toolbar.viewModel = CheckoutToolbarViewModel(context)

        slideToPurchaseWidget.addSlideListener(this)

        initializePriceWidget()
        wireUpToolbarWithPayment()
        initializeTicketDelivery()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addDefaultTransition(defaultTransition)
        addTransition(defaultToPayment)
        addTransition(defaultToTraveler)
        addTransition(defaultToTicketDeliveryOptions)
    }

    fun onCheckoutOpened() {
        travelerCheckoutViewModel.refresh()
        travelerCardWidget.viewModel = travelerCardViewModel
        show(DefaultCheckout())
    }

    fun openTicketDeliveryEntry() {
        ticketDeliveryEntryWidget.entryStatus(ticketDeliveryOverviewWidget.viewModel.ticketDeliverySelectedObserver.value)
        show(ticketDeliveryEntryWidget)
    }

    private fun initializePriceWidget() {
        totalPriceWidget.viewModel = RailTotalPriceViewModel(context)
        totalPriceWidget.breakdown.viewmodel = RailCostSummaryBreakdownViewModel(context)
        totalPriceWidget.breakdown.viewmodel.iconVisibilityObservable.subscribe { show ->
            totalPriceWidget.toggleBundleTotalCompoundDrawable(show)
            totalPriceWidget.viewModel.costBreakdownEnabledObservable.onNext(show)
        }
    }

    private fun wireUpToolbarWithPayment() {
        toolbar.viewModel.doneClicked.subscribe {
            if (currentState == BillingDetailsPaymentWidget::class.java.name) {
                paymentWidget.doneClicked.onNext(Unit)
            } else if (currentState == RailTicketDeliveryEntryWidget::class.java.name) {
                ticketDeliveryEntryWidget.doneClicked.onNext(Unit)
            } else if (currentState == RailTravelerEntryWidget::class.java.name) {
                travelerEntryWidget.doneSelectedObserver.onNext(Unit)
            }
        }
        paymentWidget.toolbarTitle.subscribe(toolbar.viewModel.toolbarTitle)
        paymentWidget.focusedView.subscribe(toolbar.viewModel.currentFocus)
        paymentWidget.filledIn.subscribe(toolbar.viewModel.formFilledIn)
        paymentWidget.menuVisibility.subscribe(toolbar.viewModel.menuVisibility)
        paymentWidget.enableMenuItem.subscribe(toolbar.viewModel.enableMenuItem)
        paymentWidget.visibleMenuWithTitleDone.subscribe(toolbar.viewModel.visibleMenuWithTitleDone)
        paymentWidget.toolbarNavIcon.subscribe(toolbar.viewModel.toolbarNavIcon)
    }

    private fun initializeTicketDelivery() {
        ticketDeliveryEntryWidget.viewModel = RailTicketDeliveryEntryViewModel(context)
        ticketDeliveryOverviewWidget.viewModel.ticketDeliverySelectedObserver.onNext(TicketDeliveryMethod.PICKUP_AT_STATION)
        ticketDeliveryEntryWidget.viewModel.ticketDeliveryMethodSelected.subscribe(ticketDeliveryOverviewWidget.viewModel.ticketDeliverySelectedObserver)
        ticketDeliveryEntryWidget.closeSubject.subscribe {
            show(DefaultCheckout(), FLAG_CLEAR_BACKSTACK)
        }
    }

    private fun updateCreateTrip(response: RailCreateTripResponse) {
        createTripDialog.hide()
        paymentWidget.clearCCAndCVV()
        ticketDeliveryEntryWidget.viewModel.ticketDeliveryOptions.onNext(response.railDomainProduct?.railOffer?.ticketDeliveryOptionList)
        checkoutViewModel.createTripObserver.onNext(response)
        updatePricing(response)
    }

    private fun updatePricing(response: RailCreateTripResponse) {
        checkoutViewModel.totalPriceObserver.onNext(response.totalPrice)
        totalPriceWidget.viewModel.total.onNext(response.totalPrice)
        totalPriceWidget.viewModel.costBreakdownEnabledObservable.onNext(true)
        (totalPriceWidget.breakdown.viewmodel as RailCostSummaryBreakdownViewModel)
                .railCostSummaryBreakdownObservable.onNext(response.railDomainProduct.railOffer)
        val cardFeeViewModel = paymentWidget.creditCardFeesView.viewModel as RailCreditCardFeesViewModel
        cardFeeViewModel.validFormsOfPaymentObservable.onNext(Pair(response.validFormsOfPayment, getTicketOptionToken()))
    }

    //TODO Defaulting to PICK_UP_AT_TICKETING_OFFICE_NONE, until #7828 is done.
    private fun getTicketOptionToken(): RailCreateTripResponse.RailTicketDeliveryOptionToken {
        return RailCreateTripResponse.RailTicketDeliveryOptionToken.PICK_UP_AT_TICKETING_OFFICE_NONE
    }

    class DefaultCheckout

    private val defaultTransition = object : Presenter.DefaultTransition(DefaultCheckout::class.java.name) {
        override fun endTransition(forward: Boolean) {
            paymentWidget.show(PaymentWidget.PaymentDefault(), Presenter.FLAG_CLEAR_BACKSTACK)
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
                totalPriceWidget.visibility = View.GONE
                travelerCardWidget.visibility = View.GONE
                ticketDeliveryOverviewWidget.visibility = View.GONE
                slideToPurchaseWidget.visibility = View.GONE
            } else {
                paymentWidget.show(PaymentWidget.PaymentDefault(), Presenter.FLAG_CLEAR_BACKSTACK)
                transitionToCheckoutStart()
            }
        }

        override fun endTransition(forward: Boolean) {
            if (!forward) {
                transitionToCheckoutEnd()
                Ui.hideKeyboard(paymentWidget)
                paymentWidget.setFocusForView()
            }
        }
    }

    private val defaultToTraveler = object : Presenter.Transition(DefaultCheckout::class.java, RailTravelerEntryWidget::class.java) {
        override fun startTransition(forward: Boolean) {
            if (forward) {
                travelerEntryWidget.viewModel = SimpleTravelerViewModel(context, 0)
                totalPriceWidget.visibility = View.GONE
                paymentWidget.visibility = View.GONE
                travelerCardWidget.visibility = View.GONE
                ticketDeliveryOverviewWidget.visibility = View.GONE
                slideToPurchaseWidget.visibility = View.GONE
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
                toolbar.viewModel.menuVisibility.onNext(true)
                toolbar.viewModel.visibleMenuWithTitleDone.onNext(Unit)
                toolbar.viewModel.toolbarTitle.onNext(travelerEntryWidget.getToolbarTitle())
                toolbar.viewModel.toolbarNavIcon.onNext(ArrowXDrawableUtil.ArrowDrawableType.CLOSE)
                travelerEntryWidget.visibility = View.VISIBLE
            }
        }
    }

    private val defaultToTicketDeliveryOptions = object : Presenter.Transition(DefaultCheckout::class.java, RailTicketDeliveryEntryWidget::class.java) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            if (forward) {
                slideToPurchaseWidget.visibility = View.GONE

                toolbar.viewModel.toolbarTitle.onNext(resources.getString(R.string.ticket_delivery))
                toolbar.viewModel.toolbarNavIcon.onNext(ArrowXDrawableUtil.ArrowDrawableType.CLOSE)
                toolbar.viewModel.enableMenuItem.onNext(ticketDeliveryEntryWidget.isComplete())
                toolbar.viewModel.visibleMenuWithTitleDone.onNext(Unit)
                toolbar.viewModel.menuVisibility.onNext(true)
            } else {
                transitionToCheckoutStart()
            }
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            ticketDeliveryOverviewWidget.setInverseVisibility(forward)
            ticketDeliveryEntryWidget.setVisibility(forward)

            if (forward) {
                totalPriceWidget.visibility = View.GONE
                paymentWidget.visibility = View.GONE
                travelerCardWidget.visibility = View.GONE
            } else {
                transitionToCheckoutEnd()
                Ui.hideKeyboard(ticketDeliveryEntryWidget)
                ticketDeliveryEntryWidget.setFocusForView()
            }
        }
    }

    private fun transitionToCheckoutStart() {
        toolbar.viewModel.toolbarNavIcon.onNext(ArrowXDrawableUtil.ArrowDrawableType.BACK)
        toolbar.viewModel.toolbarTitle.onNext(context.getString(R.string.checkout_text))
        toolbar.viewModel.enableMenuItem.onNext(true)
        toolbar.viewModel.menuVisibility.onNext(false)
    }

    private fun transitionToCheckoutEnd() {
        travelerCardWidget.visibility = View.VISIBLE
        paymentWidget.visibility = View.VISIBLE
        totalPriceWidget.visibility = View.VISIBLE
        ticketDeliveryOverviewWidget.visibility = View.VISIBLE
        if (checkoutViewModel.isValidForBooking()) {
            slideToPurchaseWidget.show()
        } else {
            slideToPurchaseWidget.visibility = View.GONE
        }
    }

    private fun View.setVisibility(forward: Boolean) {
        this.visibility = if (forward) View.VISIBLE else View.GONE
    }

    private fun View.setInverseVisibility(forward: Boolean) {
        this.visibility = if (forward) View.GONE else View.VISIBLE
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