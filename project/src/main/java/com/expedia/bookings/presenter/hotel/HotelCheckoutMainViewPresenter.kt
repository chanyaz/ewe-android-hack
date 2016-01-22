package com.expedia.bookings.presenter.hotel

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.activity.HotelRulesActivity
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.TripBucketItemHotelV2
import com.expedia.bookings.data.User
import com.expedia.bookings.data.hotels.HotelApplyCouponParams
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
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.CheckoutBasePresenter
import com.expedia.bookings.widget.CouponWidget
import com.expedia.bookings.widget.HotelCheckoutSummaryWidget
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.vm.HotelCheckoutOverviewViewModel
import com.expedia.vm.HotelCheckoutSummaryViewModel
import com.expedia.vm.HotelCouponViewModel
import com.expedia.vm.HotelCreateTripViewModel
import com.expedia.vm.HotelCheckoutMainViewModel
import com.squareup.otto.Subscribe
import rx.Observer
import rx.subjects.PublishSubject
import javax.inject.Inject
import kotlin.properties.Delegates

public class HotelCheckoutMainViewPresenter(context: Context, attr: AttributeSet) : CheckoutBasePresenter(context, attr) {
    val COUPON_VIEW_INDEX = 4
    var slideAllTheWayObservable = PublishSubject.create<Unit>()
    var emailOptInStatus = PublishSubject.create<MerchandiseSpam>()
    var hotelCheckoutSummaryWidget: HotelCheckoutSummaryWidget by Delegates.notNull()
    var offer: HotelOffersResponse.HotelRoomResponse by Delegates.notNull()
    var hotelSearchParams: HotelSearchParams by Delegates.notNull()
    val couponCardView = CouponWidget(context, attr)
    var hasDiscount = false

    var createTripViewmodel: HotelCreateTripViewModel by notNullAndObservable {
        createTripViewmodel.tripResponseObservable.subscribe(createTripResponseListener)
        couponCardView.viewmodel.couponObservable.subscribe(createTripViewmodel.tripResponseObservable)
        couponCardView.viewmodel.errorShowDialogObservable.subscribe {

            val builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.coupon_error_dialog_title)
            builder.setMessage(R.string.coupon_error_dialog_message)
            builder.setPositiveButton(context.getString(R.string.DONE), object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    dialog.dismiss()
                }
            })
            val alertDialog = builder.create()
            alertDialog.show()
            doCreateTrip()
        }
    }

    var checkoutOverviewViewModel: HotelCheckoutOverviewViewModel by notNullAndObservable {
        checkoutOverviewViewModel.slideToText.subscribe { slideWidget.setText(it) }
        checkoutOverviewViewModel.legalTextInformation.subscribeText(legalInformationText)
        checkoutOverviewViewModel.disclaimerText.subscribeTextAndVisibility(disclaimerText)
        checkoutOverviewViewModel.depositPolicyText.subscribeTextAndVisibility(depositPolicyText)
        checkoutOverviewViewModel.totalPriceCharged.subscribeText(sliderTotalText)
        checkoutOverviewViewModel.resetMenuButton.subscribe { resetMenuButton() }
    }

    lateinit var hotelServices: HotelServices
        @Inject set

    lateinit var paymentModel: PaymentModel<HotelCreateTripResponse>
        @Inject set

    var hotelCheckoutMainViewModel: HotelCheckoutMainViewModel by notNullAndObservable {
        it.updateEarnedRewards.subscribe { it ->
            Db.getTripBucket().hotelV2.updateTotalPointsToEarn(it)
            loginWidget.updateRewardsText(getLineOfBusiness())
        }
    }

    init {
        Ui.getApplication(getContext()).hotelComponent().inject(this)
        couponCardView.viewmodel = HotelCouponViewModel(getContext(), hotelServices, paymentModel)
    }

    override fun getLineOfBusiness(): LineOfBusiness {
        return LineOfBusiness.HOTELSV2
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        hotelCheckoutSummaryWidget = HotelCheckoutSummaryWidget(getContext(), null, HotelCheckoutSummaryViewModel(getContext()))
        summaryContainer.addView(hotelCheckoutSummaryWidget)

        mainContactInfoCardView.setLineOfBusiness(LineOfBusiness.HOTELSV2)
        paymentInfoCardView.setLineOfBusiness(LineOfBusiness.HOTELSV2)

        val container = scrollView.findViewById(R.id.scroll_content) as LinearLayout
        container.addView(couponCardView, container.getChildCount() - COUPON_VIEW_INDEX)
        couponCardView.setToolbarListener(toolbarListener)

        couponCardView.viewmodel.removeObservable.subscribe {
            showProgress(true)
            showCheckout()
        }

        val params = couponCardView.getLayoutParams() as LinearLayout.LayoutParams
        params.setMargins(0, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, getResources().getDisplayMetrics()).toInt(), 0, 0);
    }

    fun bind() {
        mainContactInfoCardView.setEnterDetailsText(getResources().getString(R.string.enter_driver_details))
        mainContactInfoCardView.setExpanded(false)

        paymentInfoCardView.setCreditCardRequired(true)
        paymentInfoCardView.setExpanded(false)

        if (!hasDiscount && !couponCardView.removingCoupon) {
            clearCCNumber()
        }

        couponCardView.setExpanded(false)

        slideWidget.resetSlider()
        slideToContainer.setVisibility(View.INVISIBLE)
        legalInformationText.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                context.startActivity(HotelRulesActivity.createIntent(context, LineOfBusiness.HOTELSV2))
            }
        })
        if (User.isLoggedIn(getContext())) {
            if (!hasSomeManuallyEnteredData(paymentInfoCardView.sectionBillingInfo.billingInfo) && Db.getUser().getStoredCreditCards().size == 1) {
                paymentInfoCardView.sectionBillingInfo.bind(Db.getBillingInfo())
                paymentInfoCardView.selectFirstAvailableCard()
            }
            loginWidget.bind(false, true, Db.getUser(), getLineOfBusiness())
        } else {
            loginWidget.bind(false, false, null, getLineOfBusiness())
        }
    }

    private fun hasSomeManuallyEnteredData(info: BillingInfo?): Boolean {
        if (info == null) {
            return false
        }

        if (info.location == null) {
            return false
        }
        //Checkout the major fields, if any of them have data, then we know some data has been manually entered
        if (!Strings.isEmpty(info.location.streetAddressString)) {
            return true
        }
        if (!Strings.isEmpty(info.location.city)) {
            return true
        }
        if (!Strings.isEmpty(info.location.postalCode)) {
            return true
        }
        if (!Strings.isEmpty(info.location.stateCode)) {
            return true
        }
        if (!Strings.isEmpty(info.nameOnCard)) {
            return true
        }
        if (!Strings.isEmpty(info.number)) {
            return true
        }
        return false
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
        val hasCoupon = couponCardView.viewmodel.hasDiscountObservable.value != null && couponCardView.viewmodel.hasDiscountObservable.value
        if (createTrip != null && !couponCardView.removingCoupon && hasCoupon && createTrip.coupon != null && User.isLoggedIn(context)) {
            couponCardView.viewmodel.couponParamsObservable.onNext(HotelApplyCouponParams(Db.getTripBucket().getHotelV2().mHotelTripResponse.tripId, createTrip.coupon.code, true))
        } else {
            createTripViewmodel.tripParams.onNext(HotelCreateTripParams(offer.productKey, qualifyAirAttach, numberOfAdults, childAges))
        }
    }

    val createTripResponseListener: Observer<HotelCreateTripResponse> = endlessObserver { trip ->
        Db.getTripBucket().clearHotelV2()
        Db.getTripBucket().add(TripBucketItemHotelV2(trip))

        hotelCheckoutSummaryWidget.viewModel.tripResponseObserver.onNext(trip)
        hotelCheckoutSummaryWidget.viewModel.guestCountObserver.onNext(hotelSearchParams.adults + hotelSearchParams.children.size)
        val couponRate = trip.newHotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo.getPriceAdjustments()
        hasDiscount = couponRate != null && !couponRate.isZero
        couponCardView.viewmodel.hasDiscountObservable.onNext(hasDiscount)
        checkoutOverviewViewModel = HotelCheckoutOverviewViewModel(getContext(), paymentModel)
        checkoutOverviewViewModel.newRateObserver.onNext(trip.newHotelProductResponse)
        hotelCheckoutMainViewModel = HotelCheckoutMainViewModel(paymentModel)
        bind()
        show(CheckoutBasePresenter.Ready(), Presenter.FLAG_CLEAR_BACKSTACK)
        acceptTermsWidget.vm.resetAcceptedTerms()
        HotelV2Tracking().trackPageLoadHotelV2CheckoutInfo(trip.newHotelProductResponse, hotelSearchParams)

        if (trip.guestUserPromoEmailOptInStatus != null) {
            emailOptInStatus.onNext(MerchandiseSpam.valueOf(trip.guestUserPromoEmailOptInStatus))
        }
    }

    override fun showProgress(show: Boolean) {
        hotelCheckoutSummaryWidget.setVisibility(if (show) View.INVISIBLE else View.VISIBLE)
        mSummaryProgressLayout.setVisibility(if (show) View.VISIBLE else View.GONE)
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
}
