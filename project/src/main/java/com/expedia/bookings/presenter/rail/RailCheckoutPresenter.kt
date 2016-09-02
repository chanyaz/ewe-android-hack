package com.expedia.bookings.presenter.rail

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.rail.responses.RailCreateTripResponse
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
import com.expedia.bookings.widget.rail.RailTravelerEntryWidget
import com.expedia.bookings.widget.traveler.TravelerSummaryCard
import com.expedia.util.notNullAndObservable
import com.expedia.vm.CheckoutToolbarViewModel
import com.expedia.vm.PaymentViewModel
import com.expedia.vm.rail.RailCostSummaryBreakdownViewModel
import com.expedia.vm.rail.RailCreateTripViewModel
import com.expedia.vm.rail.RailTotalPriceViewModel
import com.expedia.vm.traveler.RailCheckoutTravelerViewModel
import com.expedia.vm.traveler.RailTravelerSummaryViewModel
import com.expedia.vm.traveler.SimpleTravelerViewModel

class RailCheckoutPresenter(context: Context, attr: AttributeSet?) : Presenter(context, attr),
        SlideToWidgetLL.ISlideToListener {
    val toolbar: CheckoutToolbar by bindView(R.id.checkout_toolbar)
    val checkoutContainer: ViewGroup by bindView(R.id.rail_checkout_container)

    val travelerCardWidget: TravelerSummaryCard by bindView(R.id.rail_traveler_card_view)
    val travelerEntryWidget: RailTravelerEntryWidget by bindView(R.id.rail_traveler_entry_widget)

    val paymentViewStub: ViewStub by bindView(R.id.rail_payment_info_card_view_stub)
    val paymentWidget: BillingDetailsPaymentWidget

    val totalPriceWidget: TotalPriceWidget by bindView(R.id.rail_total_price_widget)

    val travelerCardViewModel = RailTravelerSummaryViewModel(context)
    val travelerCheckoutViewModel = RailCheckoutTravelerViewModel(context)

    var createTripViewModel: RailCreateTripViewModel by notNullAndObservable { vm ->
        vm.offerCodeSelectedObservable.subscribe {
            createTripDialog.show()
        }
        vm.tripResponseObservable.subscribe { response -> response as RailCreateTripResponse
            updateCreateTrip(response)
        }

    }
    val createTripDialog = CreateTripProgressDialog(context)

    init {
        View.inflate(context, R.layout.rail_checkout_presenter, this)
        val statusBarHeight = Ui.getStatusBarHeight(context)
        if (statusBarHeight > 0) {
            val color = ContextCompat.getColor(context, R.color.rail_primary_color)
            val statusBar = Ui.setUpStatusBar(context, toolbar, checkoutContainer, color)
            addView(statusBar)
        }
        toolbar.setNavigationOnClickListener {
            val activity = context as AppCompatActivity
            activity.onBackPressed()
        }

        paymentWidget = paymentViewStub.inflate() as BillingDetailsPaymentWidget
        paymentWidget.viewmodel = PaymentViewModel(context)
        paymentWidget.viewmodel.lineOfBusiness.onNext(LineOfBusiness.RAILS)
        paymentWidget.viewmodel.isCreditCardRequired.onNext(true)

        paymentWidget.viewmodel.expandObserver.subscribe {
            show(paymentWidget)
        }

        travelerCardWidget.setOnClickListener {
            show(travelerEntryWidget)
        }
        travelerCheckoutViewModel.travelerCompletenessStatus.subscribe(travelerCardViewModel.travelerStatusObserver)
        travelerEntryWidget.travelerCompleteSubject.subscribe {
            show(DefaultCheckout(), FLAG_CLEAR_BACKSTACK)
        }

        toolbar.viewModel = CheckoutToolbarViewModel(context)
        toolbar.viewModel.doneClicked.subscribe(travelerEntryWidget.doneSelectedObserver)

        initializePriceWidget()
        wireUpToolbarWithPayment()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addDefaultTransition(defaultTransition)
        addTransition(defaultToPayment)
        addTransition(defaultToTraveler)
    }

    fun onCheckoutOpened() {
        travelerCheckoutViewModel.refresh()
        travelerCardWidget.viewModel = travelerCardViewModel
        show(DefaultCheckout())
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

    private fun updateCreateTrip(response : RailCreateTripResponse) {
        createTripDialog.hide()
        paymentWidget.clearCCAndCVV()
        updatePricing(response)
    }

    private fun updatePricing(response: RailCreateTripResponse) {
        totalPriceWidget.viewModel.total.onNext(response.totalPrice)
        totalPriceWidget.viewModel.costBreakdownEnabledObservable.onNext(true)
        (totalPriceWidget.breakdown.viewmodel as RailCostSummaryBreakdownViewModel)
                .railCostSummaryBreakdownObservable.onNext(response.railDomainProduct.railOffer)
    }

    class DefaultCheckout

    private val defaultTransition = object : Presenter.DefaultTransition(DefaultCheckout::class.java.name) {
        override fun endTransition(forward: Boolean) {
            paymentWidget.show(PaymentWidget.PaymentDefault(), Presenter.FLAG_CLEAR_BACKSTACK)
        }
    }

    private val defaultToPayment = object : Presenter.Transition(DefaultCheckout::class.java, BillingDetailsPaymentWidget::class.java) {
        override fun startTransition(forward: Boolean) {
            if (forward) {
                totalPriceWidget.visibility = View.GONE
                travelerCardWidget.visibility = View.GONE
            } else {
                paymentWidget.show(PaymentWidget.PaymentDefault(), Presenter.FLAG_CLEAR_BACKSTACK)
            }
        }

        override fun endTransition(forward: Boolean) {
            if (!forward) {
                travelerCardWidget.visibility = View.VISIBLE
                totalPriceWidget.visibility = View.VISIBLE

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
            } else {
                travelerCheckoutViewModel.updateCompletionStatus()
                toolbar.viewModel.menuVisibility.onNext(false)
                travelerEntryWidget.visibility = View.GONE
            }
        }

        override fun endTransition(forward: Boolean) {
            if (!forward) {
                travelerCardWidget.visibility = View.VISIBLE
                paymentWidget.visibility = View.VISIBLE
                totalPriceWidget.visibility = View.VISIBLE
                toolbar.viewModel.toolbarTitle.onNext(context.getString(R.string.checkout_text))
                toolbar.viewModel.toolbarNavIcon.onNext(ArrowXDrawableUtil.ArrowDrawableType.BACK)

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

    override fun onSlideStart() {
        throw UnsupportedOperationException()
    }

    override fun onSlideProgress(pixels: Float, total: Float) {
        throw UnsupportedOperationException()
    }

    override fun onSlideAllTheWay() {
        throw UnsupportedOperationException()
    }

    override fun onSlideAbort() {
        throw UnsupportedOperationException()
    }
}