package com.expedia.bookings.widget

import android.animation.ObjectAnimator
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewStub
import android.widget.Button
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.activity.AccountLibActivity
import com.expedia.bookings.activity.HotelRulesActivity
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.User
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.packages.TravelerPresenter
import com.expedia.bookings.tracking.HotelV2Tracking
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.UserAccountRefresher
import com.expedia.bookings.utils.bindOptionalView
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.BaseCheckoutViewModel
import com.expedia.vm.PaymentViewModel
import com.mobiata.android.Log
import rx.Observer
import rx.subjects.PublishSubject
import kotlin.properties.Delegates

abstract class BaseCheckoutPresenter(context: Context, attr: AttributeSet) : Presenter(context, attr), SlideToWidgetLL.ISlideToListener,
        UserAccountRefresher.IUserAccountRefreshListener, AccountButton.AccountButtonClickListener, ExpandableCardView.IExpandedListener {

    val toolbar: CheckoutToolbar? by bindOptionalView(R.id.checkout_toolbar)
    val scrollView: ScrollView by bindView(R.id.scrollView)
    val loginWidget: AccountButton by bindView(R.id.login_widget)
    var paymentWidget: PaymentWidget by Delegates.notNull()
    val paymentViewStub: ViewStub by bindView(R.id.payment_info_card_view_stub)

    //TODO TEMP
    val travelersButton: Button by bindView(R.id.travelers_button);
    val travelerPresenter: TravelerPresenter by bindView(R.id.traveler_presenter)

    val legalInformationText: TextView by bindView(R.id.legal_information_text_view)
    val hintContainer: LinearLayout by bindView(R.id.hint_container)
    val depositPolicyText: TextView by bindView(R.id.disclaimer_text)
    val acceptTermsWidget: AcceptTermsWidget by bindView(R.id.layout_confirm_tos)
    val slideContainer: LinearLayout by bindView(R.id.slide_to_purchase_layout)
    val slideToPurchase: SlideToWidgetLL by bindView(R.id.slide_to_purchase_widget)
    val slideTotalText: TextView by bindView(R.id.purchase_total_text_view)
    val handle: CardView by bindView(R.id.handle)
    val chevron: View by bindView(R.id.chevron)

    val checkoutDialog = ProgressDialog(context)
    val createTripDialog = ProgressDialog(context)

    var slideAllTheWayObservable = PublishSubject.create<Unit>()
    var userAccountRefresher: UserAccountRefresher by Delegates.notNull()

    var viewModel: BaseCheckoutViewModel by notNullAndObservable { vm ->
        vm.infoCompleted.subscribe { completed ->
            if (completed) {
                showAcceptTerms()
            }
        }
        vm.lineOfBusiness.subscribe { lob ->
            paymentWidget.viewmodel.lineOfBusiness.onNext(lob)
            userAccountRefresher = UserAccountRefresher(context, lob, this)
        }
        vm.creditCardRequired.subscribe { required ->
            paymentWidget.viewmodel.isCreditCardRequired.onNext(required)
        }

        travelerPresenter.travelersCompleteSubject.subscribe(vm.travelerCompleted)
    }

    init {
        View.inflate(context, R.layout.widget_base_checkout, this)
        paymentWidget = paymentViewStub.inflate() as PaymentWidget
        paymentWidget.viewmodel = PaymentViewModel(context)
        loginWidget.setListener(this)
        slideToPurchase.addSlideToListener(this)

        loginWidget.bind(false, Db.getUser() != null, Db.getUser(), LineOfBusiness.PACKAGES)

        travelersButton.setOnClickListener { showTravelerPresenter() }

        legalInformationText.setOnClickListener {
            context.startActivity(HotelRulesActivity.createIntent(context, LineOfBusiness.PACKAGES))
        }
        paymentWidget.viewmodel.expandObserver.subscribe { expand ->
            show(paymentWidget)
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

        createTripDialog.setMessage(resources.getString(R.string.spinner_text_hotel_create_trip))
        createTripDialog.setCancelable(false)
        createTripDialog.isIndeterminate = true

        checkoutDialog.setMessage(resources.getString(R.string.booking_loading))
        checkoutDialog.setCancelable(false)
        checkoutDialog.isIndeterminate = true
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addDefaultTransition(defaultTransition)
        slideToPurchase.addSlideToListener(this)

        addTransition(defaultToTraveler)
        addTransition(defaultToPayment)
        paymentWidget.show(PaymentWidget.PaymentDefault(), Presenter.FLAG_CLEAR_BACKSTACK)

        travelerPresenter.travelersCompleteSubject.subscribe {
                show(CheckoutDefault(), FLAG_CLEAR_BACKSTACK)
        }
    }

    private val defaultTransition = object : Presenter.DefaultTransition(CheckoutDefault::class.java.name) {
        override fun finalizeTransition(forward: Boolean) {
            loginWidget.bind(false, Db.getUser() != null, Db.getUser(), LineOfBusiness.PACKAGES)
            paymentWidget.show(PaymentWidget.PaymentDefault(), Presenter.FLAG_CLEAR_BACKSTACK)
        }
    }

    private val defaultToTraveler = object : Presenter.Transition(CheckoutDefault::class.java, TravelerPresenter::class.java) {
        override fun startTransition(forward: Boolean) {
            handle.setVisibility(forward)
            loginWidget.setVisibility(forward)
            hintContainer.visibility = if (forward || User.isLoggedIn(getContext())) View.GONE else View.VISIBLE
            paymentWidget.visibility = if (forward) View.GONE else (if (paymentWidget.isCreditCardRequired()) View.VISIBLE else View.GONE)
            legalInformationText.setVisibility(forward)
            depositPolicyText.setVisibility(forward)
            slideContainer.visibility = View.GONE

            travelersButton.visibility = if (!forward) View.VISIBLE else View.GONE
            travelerPresenter.visibility = if (forward) View.VISIBLE else View.GONE
        }
    }

    private val defaultToPayment = object : Presenter.Transition(CheckoutDefault::class.java, PackagePaymentWidget::class.java) {

        override fun startTransition(forward: Boolean) {
            handle.setVisibility(forward)
            loginWidget.setVisibility(forward)
            hintContainer.visibility = if (forward) View.GONE else if (User.isLoggedIn(getContext())) View.GONE else View.VISIBLE
            travelersButton.visibility = if (!forward) View.VISIBLE else View.GONE
            legalInformationText.setVisibility(forward)
            depositPolicyText.setVisibility(forward)
            slideContainer.visibility = View.GONE
            if (!forward) {
                paymentWidget.show(PaymentWidget.PaymentDefault(), Presenter.FLAG_CLEAR_BACKSTACK)
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
        if (viewModel.builder.hasValidParams()) {
            val checkoutParams = viewModel.builder.build()
            viewModel.checkoutInfoCompleted.onNext(checkoutParams)
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
        val args = AccountLibActivity.createArgumentsBundle(viewModel.lineOfBusiness.value, CheckoutLoginExtender());
        User.signIn(context as Activity, args);
    }

    override fun accountLogoutClicked() {
        User.signOut(context)
        loginWidget.bind(false, false, null, getLineOfBusiness())
        //travelerWidget.onLogout()
        paymentWidget.viewmodel.userLogin.onNext(false)
        hintContainer.visibility = View.VISIBLE
        acceptTermsWidget.visibility = View.INVISIBLE
        doCreateTrip()
    }

    override fun collapsed(view: ExpandableCardView?) {
        show(CheckoutDefault(), FLAG_CLEAR_BACKSTACK)
    }

    override fun expanded(view: ExpandableCardView?) {
        //show(travelerWidget)
    }

    private fun showTravelerPresenter() {
        show(travelerPresenter)
    }

    class CheckoutDefault

    abstract fun doCreateTrip()

    fun getLineOfBusiness(): LineOfBusiness {
        return viewModel.lineOfBusiness.value
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
        //travelerWidget.onLogin()
        paymentWidget.viewmodel.userLogin.onNext(true)
        hintContainer.visibility = View.GONE
        doCreateTrip()
    }

    fun showAcceptTerms() {
        if (viewModel.infoCompleted.value) {
            if (PointOfSale.getPointOfSale(context).requiresRulesRestrictionsCheckbox() && !acceptTermsWidget.vm.acceptedTermsObservable.value) {
                acceptTermsWidget.vm.acceptedTermsObservable.subscribe(object : Observer<Boolean> {
                    override fun onCompleted() {
                    }

                    override fun onError(e: Throwable) {
                    }

                    override fun onNext(b: Boolean?) {
                        animateInSlideToPurchase(true)
                    }
                })
                acceptTermsWidget.visibility = View.VISIBLE
            } else {
                animateInSlideToPurchase(true)
            }
        } else {
            acceptTermsWidget.visibility = View.INVISIBLE
            animateInSlideToPurchase(false)
        }
    }

    fun animateInSlideToPurchase(visible: Boolean) {
        // If its already in position, don't do it again
        if (slideContainer.visibility == (if (visible) View.VISIBLE else View.INVISIBLE)) {
            return
        }

        val acceptTermsRequired = PointOfSale.getPointOfSale(context).requiresRulesRestrictionsCheckbox()
        val acceptedTerms = acceptTermsWidget.vm.acceptedTermsObservable.value
        if (acceptTermsRequired && !acceptedTerms) {
            return  // don't show if terms have not ben accepted yet
        }

        slideContainer.translationY = (if (visible) slideContainer.height else 0).toFloat()
        slideContainer.visibility = View.VISIBLE
        val animator = ObjectAnimator.ofFloat(slideContainer, "translationY", if (visible) 0f else slideContainer.height.toFloat())
        animator.setDuration(300)
        animator.start()

        if (visible) {
            scrollView.postDelayed({ scrollView.fullScroll(ScrollView.FOCUS_DOWN) }, 100)
            val cardType = paymentWidget.getCardType().omnitureTrackingCode
            when (getLineOfBusiness()) {
                LineOfBusiness.HOTELSV2 -> HotelV2Tracking().trackHotelV2SlideToPurchase(paymentWidget.getCardType(), paymentWidget.viewmodel.splitsType.value)
                LineOfBusiness.LX -> OmnitureTracking.trackAppLXCheckoutSlideToPurchase(cardType)
                LineOfBusiness.CARS -> OmnitureTracking.trackAppCarCheckoutSlideToPurchase(cardType)
                else -> {
                    //we should never reach here justa dded to remove kotlin warning
                }
            }
        }
    }

    private fun View.setVisibility(forward: Boolean) {
        this.visibility = if (forward) View.GONE else View.VISIBLE
    }
}