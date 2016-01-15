package com.expedia.bookings.widget

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.activity.AccountLibActivity
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.User
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.UserAccountRefresher
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeVisibility
import com.expedia.vm.BaseCheckoutViewModel
import kotlin.properties.Delegates

public class BaseCheckoutPresenter(context: Context, attr: AttributeSet) : Presenter(context, attr), SlideToWidgetLL.ISlideToListener,
        UserAccountRefresher.IUserAccountRefreshListener, AccountButton.AccountButtonClickListener,  ExpandableCardView.IExpandedListener {

    val loginWidget: AccountButton by bindView(R.id.login_widget)
    val travelerWidget: TravelerContactDetailsWidget by bindView(R.id.traveler_widget)
    var paymentWidget: PaymentWidget by Delegates.notNull()
    val paymentViewStub: ViewStub by bindView(R.id.payment_info_card_view_stub)
    val widgetContainer: LinearLayout by bindView(R.id.checkout_widget_container)
    val sliderContainer: LinearLayout by bindView(R.id.slide_to_purchase_layout)
    val slideToPurchase: SlideToWidgetLL by bindView(R.id.slide_to_purchase_widget)

    var expandedView: ExpandableCardView? = null

    var viewModel: BaseCheckoutViewModel by notNullAndObservable { vm ->
        vm.infoCompleted.subscribeVisibility(sliderContainer)
        vm.lineOfBusiness.subscribe { lob ->
            travelerWidget.setLineOfBusiness(lob)
            paymentWidget.setLineOfBusiness(lob)
        }
        vm.creditCardRequired.subscribe { required ->
            paymentWidget.isCreditCardRequired = required
        }
    }

    init {
        View.inflate(context, R.layout.widget_base_checkout, this)
        paymentWidget = paymentViewStub.inflate() as PaymentWidget
        loginWidget.setListener(this)
        travelerWidget.addExpandedListener(this)
        paymentWidget.addExpandedListener(this)

        if (User.isLoggedIn(getContext())) {
            loginWidget.bind(false, true, Db.getUser(), LineOfBusiness.HOTELSV2)
        } else {
            loginWidget.bind(false, false, null, LineOfBusiness.HOTELSV2)
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addDefaultTransition(defaultTransition)
        addTransition(checkoutExpanded)
    }

    private val defaultTransition = object : Presenter.DefaultTransition(CheckoutDefault::class.java.name) {
        override fun finalizeTransition(forward: Boolean) {
            travelerWidget.isExpanded = false
            paymentWidget.isExpanded = false
        }
    }

    private val checkoutExpanded = object : Presenter.Transition(CheckoutDefault::class.java, CheckoutExpanded::class.java) {

        override fun startTransition(forward: Boolean) {
            for (i in 0..widgetContainer.childCount - 1) {
                val child = widgetContainer.getChildAt(i)
                if (forward && child != expandedView) {
                    child.visibility = View.GONE
                } else if (!forward) {
                    if (child is ExpandableCardView) {
                        child.isExpanded = false
                    }
                    child.visibility = View.VISIBLE
                }
            }
        }

        override fun finalizeTransition(forward: Boolean) {
            if (!forward) {
                if (travelerWidget.isComplete) {
                    viewModel.travelerCompleted.onNext(travelerWidget.sectionTravelerInfo.traveler)
                }
                if (paymentWidget.isComplete) {
                    viewModel.paymentCompleted.onNext(paymentWidget.sectionBillingInfo.billingInfo)
                }
            }
        }
    }

    //Abstract methods
    override fun onSlideStart() {

    }

    override fun onSlideProgress(pixels: Float, total: Float) {

    }

    override fun onSlideAllTheWay() {

    }

    override fun onSlideAbort() {
        slideToPurchase.resetSlider()
    }

    override fun onUserAccountRefreshed() {

    }

    override fun accountLoginClicked() {
        val args = AccountLibActivity.createArgumentsBundle(viewModel.lineOfBusiness.value, CheckoutLoginExtender());
        User.signIn(context as Activity, args);
    }

    override fun accountLogoutClicked() {

    }

    override fun collapsed(view: ExpandableCardView?) {
        expandedView = null
        show(CheckoutDefault(), FLAG_CLEAR_BACKSTACK)
    }

    override fun expanded(view: ExpandableCardView?) {
        expandedView = view
        show(CheckoutExpanded())
    }

    class CheckoutDefault
    class CheckoutExpanded
}