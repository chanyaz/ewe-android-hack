package com.expedia.bookings.widget

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewStub
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.activity.AccountLibActivity
import com.expedia.bookings.activity.HotelRulesActivity
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.User
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.UserAccountRefresher
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.util.subscribeVisibility
import com.expedia.vm.BaseCheckoutViewModel
import kotlin.properties.Delegates

open class BaseCheckoutPresenter(context: Context, attr: AttributeSet) : Presenter(context, attr), SlideToWidgetLL.ISlideToListener,
        UserAccountRefresher.IUserAccountRefreshListener, AccountButton.AccountButtonClickListener, ExpandableCardView.IExpandedListener {

    val loginWidget: AccountButton by bindView(R.id.login_widget)
    val travelerWidget: TravelerContactDetailsWidget by bindView(R.id.traveler_widget)
    var paymentWidget: PaymentWidget by Delegates.notNull()
    val paymentViewStub: ViewStub by bindView(R.id.payment_info_card_view_stub)
    val widgetContainer: LinearLayout by bindView(R.id.checkout_widget_container)
    val sliderContainer: LinearLayout by bindView(R.id.slide_to_purchase_layout)
    val sliderPurchaseTotalText: TextView by bindView(R.id.purchase_total_text_view)
    val slideToPurchase: SlideToWidgetLL by bindView(R.id.slide_to_purchase_widget)
    val legalInformationText: TextView by bindView(R.id.legal_information_text_view)
    val depositPolicyText: TextView by bindView(R.id.disclaimer_text)
    val handle: CardView by bindView(R.id.handle)
    val chevron: View by bindView(R.id.chevron)

    var expandedView: ExpandableCardView? = null
    val checkoutDialog = ProgressDialog(context)

    var viewModel: BaseCheckoutViewModel by notNullAndObservable { vm ->
        vm.infoCompleted.subscribeVisibility(sliderContainer)
        vm.lineOfBusiness.subscribe { lob ->
            travelerWidget.setLineOfBusiness(lob)
            paymentWidget.setLineOfBusiness(lob)
            vm.creditCardRequired.onNext(lob == LineOfBusiness.PACKAGES)
        }
        vm.creditCardRequired.subscribe { required ->
            paymentWidget.isCreditCardRequired = required
        }
        vm.legalText.subscribeTextAndVisibility(legalInformationText)
        vm.depositPolicyText.subscribeTextAndVisibility(depositPolicyText)
        vm.checkoutInfoCompleted.subscribe {
            checkoutDialog.show()
        }
        vm.checkoutResponse.subscribe {
            checkoutDialog.hide()
        }
        vm.sliderPurchaseTotalText.subscribeText(sliderPurchaseTotalText)
    }

    init {
        View.inflate(context, R.layout.widget_base_checkout, this)
        paymentWidget = paymentViewStub.inflate() as PaymentWidget
        loginWidget.setListener(this)
        travelerWidget.addExpandedListener(this)
        paymentWidget.addExpandedListener(this)
        slideToPurchase.addSlideToListener(this)

        if (User.isLoggedIn(getContext())) {
            loginWidget.bind(false, true, Db.getUser(), LineOfBusiness.PACKAGES)
        } else {
            loginWidget.bind(false, false, null, LineOfBusiness.PACKAGES)
        }

        legalInformationText.setOnClickListener {
            context.startActivity(HotelRulesActivity.createIntent(context, LineOfBusiness.PACKAGES))
        }

        //calculates the difference for rotating the chevron and translating the checkout presenter
        handle.setOnTouchListener(object : View.OnTouchListener {
            internal var originY: Float = 0.toFloat()
            internal var doneForNow: Boolean = false
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    (MotionEvent.ACTION_DOWN) -> {
                        // this could probs break it cause multitouch
                        doneForNow = false
                        originY = event.rawY
                    }
                    (MotionEvent.ACTION_UP) -> {
                        originY = 0f
                        doneForNow = false
                    }
                    (MotionEvent.ACTION_MOVE) -> if (!doneForNow) {
                        val diff = event.rawY - originY
                        if (rotateChevron(Math.max(diff, 0f))) {
                            doneForNow = true
                        }
                    }
                }
                return true
            }
        })

        checkoutDialog.setMessage(resources.getString(R.string.booking_loading))
        checkoutDialog.setCancelable(false)
        checkoutDialog.isIndeterminate = true
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
            handle.visibility = if (forward) View.GONE else View.VISIBLE
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
            sliderContainer.visibility = View.GONE
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

    //Either shows the bundle overview or the checkout presenter based on distance/rotation
    private fun rotateChevron(distance: Float): Boolean {
        val distanceGoal = 300f
        if (distance > distanceGoal) {
            (context as AppCompatActivity).onBackPressed()
            return true
        } else {
            translationY = distance
            chevron.rotation = distance / distanceGoal * (-90)
            return false
        }
    }

    //Abstract methods
    override fun onSlideStart() {

    }

    override fun onSlideProgress(pixels: Float, total: Float) {

    }

    override fun onSlideAllTheWay() {
        viewModel.cvvCompleted.onNext("123")
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