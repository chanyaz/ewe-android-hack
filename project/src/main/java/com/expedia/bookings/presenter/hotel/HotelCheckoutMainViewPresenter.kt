package com.expedia.bookings.presenter.hotel

import android.app.AlertDialog
import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.activity.HotelRulesActivity
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.TripBucketItemHotelV2
import com.expedia.bookings.data.User
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.HotelApplyCouponParameters
import com.expedia.bookings.data.hotels.HotelCreateTripParams
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.enums.MerchandiseSpam
import com.expedia.bookings.otto.Events
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.tracking.HotelV2Tracking
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.CheckoutBasePresenter
import com.expedia.bookings.widget.CouponWidget
import com.expedia.bookings.widget.HotelCheckoutSummaryWidget
import com.expedia.bookings.widget.PaymentWidget
import com.expedia.util.endlessObserver
import com.expedia.util.getCheckoutToolbarTitle
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.vm.HotelCheckoutMainViewModel
import com.expedia.vm.HotelCheckoutOverviewViewModel
import com.expedia.vm.HotelCheckoutSummaryViewModel
import com.expedia.vm.HotelCouponViewModel
import com.expedia.vm.HotelCreateTripViewModel
import com.expedia.vm.ShopWithPointsViewModel
import com.squareup.otto.Subscribe
import rx.Observer
import rx.subjects.PublishSubject
import javax.inject.Inject
import kotlin.properties.Delegates

class HotelCheckoutMainViewPresenter(context: Context, attr: AttributeSet) : CheckoutBasePresenter(context, attr) {

    var slideAllTheWayObservable = PublishSubject.create<Unit>()
    var emailOptInStatus = PublishSubject.create<MerchandiseSpam>()
    var hotelCheckoutSummaryWidget: HotelCheckoutSummaryWidget by Delegates.notNull()
    var offer: HotelOffersResponse.HotelRoomResponse by Delegates.notNull()
    var hotelSearchParams: HotelSearchParams by Delegates.notNull()
    val couponCardView = CouponWidget(context, attr)
    var hasDiscount = false
    val backPressedAfterUserWithEffectiveSwPAvailableSignedOut = PublishSubject.create<Unit>()

    var createTripViewmodel: HotelCreateTripViewModel by notNullAndObservable {
        createTripViewmodel.tripResponseObservable.subscribe(createTripResponseListener)
        couponCardView.viewmodel.couponObservable.subscribe(createTripViewmodel.tripResponseObservable)
        couponCardView.viewmodel.errorShowDialogObservable.subscribe {

            val builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.coupon_error_dialog_title)
            builder.setMessage(R.string.coupon_error_dialog_message)
            builder.setPositiveButton(context.getString(R.string.DONE), { dialog, which -> dialog.dismiss() })
            val alertDialog = builder.create()
            alertDialog.show()
            doCreateTrip()
        }

        couponCardView.viewmodel.errorRemoveCouponShowDialogObservable.subscribe {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.coupon_error_remove_dialog_title)
            builder.setMessage(R.string.coupon_error_fallback)
            builder.setPositiveButton(context.getString(R.string.cancel), { dialog, which ->
                createTripResponseListener.onNext(createTripViewmodel.tripResponseObservable.value)
                dialog.dismiss()
            })
            builder.setNegativeButton(context.getString(R.string.retry), { dialog, which -> couponCardView.viewmodel.removeObservable.onNext(true) })
            val alertDialog = builder.create()
            alertDialog.show()
        }
    }

    var checkoutOverviewViewModel: HotelCheckoutOverviewViewModel by notNullAndObservable {
        checkoutOverviewViewModel.slideToText.subscribe { slideWidget.setText(it) }
        checkoutOverviewViewModel.legalTextInformation.subscribeText(legalInformationText)
        checkoutOverviewViewModel.disclaimerText.subscribeText(disclaimerText)
        checkoutOverviewViewModel.depositPolicyText.subscribeTextAndVisibility(depositPolicyText)
        checkoutOverviewViewModel.priceAboveSlider.subscribeText(sliderTotalText)
        checkoutOverviewViewModel.resetMenuButton.subscribe { resetMenuButton() }
    }

    lateinit var hotelServices: HotelServices
        @Inject set

    lateinit var paymentModel: PaymentModel<HotelCreateTripResponse>
        @Inject set

    lateinit var shopWithPointsViewModel: ShopWithPointsViewModel
        @Inject set

    var hotelCheckoutMainViewModel: HotelCheckoutMainViewModel by notNullAndObservable { vm ->
        vm.updateEarnedRewards.subscribe { it ->
            Db.getTripBucket().hotelV2.updateTotalPointsAndCurrencyToEarn(it)
            loginWidget.updateRewardsText(lineOfBusiness)
        }
        vm.animateSlideToPurchaseWithPaymentSplits.subscribe {
            HotelV2Tracking().trackHotelV2SlideToPurchase(paymentInfoCardView.getCardType(), it)
        }
    }

    init {
        Ui.getApplication(getContext()).hotelComponent().inject(this)
        couponCardView.viewmodel = HotelCouponViewModel(getContext(), hotelServices, paymentModel)
        hotelCheckoutMainViewModel = HotelCheckoutMainViewModel(paymentModel, shopWithPointsViewModel)
    }

    override fun getToolbarTitle(): String {
        return getCheckoutToolbarTitle(resources,
                Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelSecureCheckoutMessaging))
    }

    override fun getLineOfBusiness(): LineOfBusiness {
        return LineOfBusiness.HOTELSV2
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        hotelCheckoutSummaryWidget = HotelCheckoutSummaryWidget(context, null, HotelCheckoutSummaryViewModel(context, paymentModel))
        summaryContainer.addView(hotelCheckoutSummaryWidget)

        mainContactInfoCardView.setLineOfBusiness(LineOfBusiness.HOTELSV2)

        couponContainer.addView(couponCardView)
        couponCardView.setToolbarListener(toolbar)

        couponCardView.viewmodel.removeObservable.subscribe {
            if (it) {
                showProgress(true)
                showCheckout()
            }
        }

        val params = couponCardView.layoutParams as LayoutParams
        params.setMargins(0, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, resources.displayMetrics).toInt(), 0, 0);
    }

    fun bind() {
        mainContactInfoCardView.setEnterDetailsText(resources.getString(R.string.enter_driver_details))
        mainContactInfoCardView.isExpanded = false
        paymentInfoCardView.show(PaymentWidget.PaymentDefault(), Presenter.FLAG_CLEAR_BACKSTACK)

        if (!hasDiscount) {
            clearCCNumber()
        }

        couponCardView.setExpanded(false)

        slideWidget.resetSlider()
        slideToContainer.visibility = View.INVISIBLE
        legalInformationText.setOnClickListener({ context.startActivity(HotelRulesActivity.createIntent(context, LineOfBusiness.HOTELSV2)) })
        updateLoginWidget()
        selectFirstAvailableCardIfOnlyOneAvailable()
    }

    fun showCheckout(offer: HotelOffersResponse.HotelRoomResponse) {
        this.offer = offer
        show(CheckoutBasePresenter.CheckoutDefault())
        userAccountRefresher.ensureAccountIsRefreshed()
    }

    override fun doCreateTrip() {
        val numberOfAdults = hotelSearchParams.adults
        val childAges = hotelSearchParams.children
        val qualifyAirAttach = false
        val createTrip = createTripViewmodel.tripResponseObservable.value

        val tripHasCoupon = createTrip != null && createTrip.coupon != null
        val isRemoveCoupon = couponCardView.viewmodel.removeObservable.value != null && couponCardView.viewmodel.removeObservable.value

        if (isRemoveCoupon) {
            couponCardView.viewmodel.couponRemoveObservable.onNext(Db.getTripBucket().hotelV2.mHotelTripResponse.tripId)
        }
        else {
            val shouldTryToApplyCouponAfterLogin = couponCardView.viewmodel.hasDiscountObservable.value != null && couponCardView.viewmodel.hasDiscountObservable.value
            if (User.isLoggedIn(context) && tripHasCoupon && shouldTryToApplyCouponAfterLogin) {
                // This is to apply a coupon in case user signs in after applying a coupon. So there is no user preference.
                var couponParams = HotelApplyCouponParameters.Builder()
                        .tripId(Db.getTripBucket().getHotelV2().mHotelTripResponse.tripId)
                        .couponCode(createTrip.coupon.code)
                        .isFromNotSignedInToSignedIn(true)
                        .userPreferencePointsDetails(emptyList())
                        .build()
                couponCardView.viewmodel.couponParamsObservable.onNext(couponParams)
            } else {
                createTripViewmodel.tripParams.onNext(HotelCreateTripParams(offer.productKey, qualifyAirAttach, numberOfAdults, childAges))
            }
        }
    }

    val createTripResponseListener: Observer<HotelCreateTripResponse> = endlessObserver { trip ->
        Db.getTripBucket().clearHotelV2()
        Db.getTripBucket().add(TripBucketItemHotelV2(trip))
        hotelCheckoutSummaryWidget.viewModel.guestCountObserver.onNext(hotelSearchParams.adults + hotelSearchParams.children.size)
        val couponRate = trip.newHotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo.getPriceAdjustments()
        hasDiscount = couponRate != null && !couponRate.isZero
        couponCardView.viewmodel.hasDiscountObservable.onNext(hasDiscount)
        checkoutOverviewViewModel = HotelCheckoutOverviewViewModel(getContext(), paymentModel)
        checkoutOverviewViewModel.newRateObserver.onNext(trip.newHotelProductResponse)
        checkoutOverviewViewModel.resetMenuButton.onNext(Unit)
        bind()
        show(CheckoutBasePresenter.Ready(), Presenter.FLAG_CLEAR_BACKSTACK)
        acceptTermsWidget.vm.resetAcceptedTerms()
        HotelV2Tracking().trackPageLoadHotelV2CheckoutInfo(trip, hotelSearchParams)

        if (trip.guestUserPromoEmailOptInStatus != null) {
            emailOptInStatus.onNext(MerchandiseSpam.valueOf(trip.guestUserPromoEmailOptInStatus!!))
        }
    }

    override fun showProgress(show: Boolean) {
        hotelCheckoutSummaryWidget.visibility = if (show) View.INVISIBLE else View.VISIBLE
        mSummaryProgressLayout.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun accountLogoutClicked() {
        hotelCheckoutMainViewModel.onLogoutButtonClicked.onNext(Unit)
        super.accountLogoutClicked()

    }

    override fun back(): Boolean {
        if ((CheckoutBasePresenter.Ready ::class.java.name == currentState ||
                CheckoutBasePresenter.CheckoutFailed ::class.java.name == currentState )
                && hotelCheckoutMainViewModel.userWithEffectiveSwPAvailableSignedOut.value ) {
            hotelCheckoutMainViewModel.userWithEffectiveSwPAvailableSignedOut.onNext(false)
            backPressedAfterUserWithEffectiveSwPAvailableSignedOut.onNext(Unit)
            acceptTermsWidget.visibility = View.INVISIBLE
            return true
        }
        return super.back()
    }

    override fun onSlideStart() {
    }

    override fun onSlideProgress(pixels: Float, total: Float) {
    }

    override fun onSlideAllTheWay() {
        slideAllTheWayObservable.onNext(Unit)
    }

    override fun onSlideAbort() {
    }

    fun setSearchParams(params: HotelSearchParams) {
        hotelSearchParams = params
    }

    @Subscribe fun onLogin(@Suppress("UNUSED_PARAMETER") event: Events.LoggedInSuccessful) {
        onLoginSuccessful()
    }

    override fun animateInSlideToPurchase(visible: Boolean) {
        super.animateInSlideToPurchase(visible)
        if (visible) {
            hotelCheckoutMainViewModel.animateInSlideToPurchaseSubject.onNext(Unit)
        }
    }

}
