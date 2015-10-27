package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.activity.HotelRulesActivity
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.TripBucketItemHotelV2
import com.expedia.bookings.data.User
import com.expedia.bookings.data.hotels.HotelCreateTripParams
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.enums.MerchandiseSpam
import com.expedia.bookings.otto.Events
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.tracking.HotelV2Tracking
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.CheckoutBasePresenter
import com.expedia.bookings.widget.CouponWidget
import com.expedia.bookings.widget.HotelCheckoutSummaryWidget
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.util.subscribeVisibility
import com.expedia.vm.HotelCheckoutOverviewViewModel
import com.expedia.vm.HotelCheckoutSummaryViewModel
import com.expedia.vm.HotelCouponViewModel
import com.expedia.vm.HotelCreateTripViewModel
import com.squareup.otto.Subscribe
import rx.Observer
import rx.exceptions.OnErrorNotImplementedException
import rx.subjects.PublishSubject
import kotlin.properties.Delegates

public class HotelCheckoutMainViewPresenter(context: Context, attr: AttributeSet) : CheckoutBasePresenter(context, attr) {
    var slideAllTheWayObservable = PublishSubject.create<Unit>()
    var emailOptInStatus = PublishSubject.create<MerchandiseSpam>()
    var hotelCheckoutSummaryWidget: HotelCheckoutSummaryWidget by Delegates.notNull()
    var offer: HotelOffersResponse.HotelRoomResponse by Delegates.notNull()
    var hotelSearchParams: HotelSearchParams by Delegates.notNull()
    val couponCardView = CouponWidget(context, attr)

    var viewmodel: HotelCreateTripViewModel by notNullAndObservable {
        viewmodel.tripResponseObservable.subscribe(createTripResponseListener)
    }
    var vm: HotelCheckoutOverviewViewModel by notNullAndObservable {
        vm.slideToText.subscribe { slideWidget.setText(it) }
        vm.legalTextInformation.subscribeText(legalInformationText)
        vm.disclaimerVisibility.subscribeVisibility(disclaimerText)
        vm.disclaimerText.subscribeText(disclaimerText)
        vm.totalPriceCharged.subscribeText(sliderTotalText)
        vm.resetMenuButton.subscribe { resetMenuButton() }
    }

    val hotelServices: HotelServices by lazy() {
        Ui.getApplication(getContext()).hotelComponent().hotelServices()
    }

    init {
        couponCardView.viewmodel = HotelCouponViewModel(getContext(), hotelServices)
    }

    override fun getLineOfBusiness(): LineOfBusiness {
        return LineOfBusiness.HOTELSV2
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        Ui.getApplication(getContext()).hotelComponent().inject(this)
        hotelCheckoutSummaryWidget = HotelCheckoutSummaryWidget(getContext(), null, HotelCheckoutSummaryViewModel(getContext()))
        summaryContainer.addView(hotelCheckoutSummaryWidget)

        mainContactInfoCardView.setLineOfBusiness(LineOfBusiness.HOTELSV2)
        paymentInfoCardView.setLineOfBusiness(LineOfBusiness.HOTELSV2)

        val container = scrollView.findViewById(R.id.scroll_content) as LinearLayout
        container.addView(couponCardView, container.getChildCount() - 3)
        couponCardView.setToolbarListener(toolbarListener)
        couponCardView.viewmodel.couponObservable.subscribe(createTripResponseListener)
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
        clearCCNumber()

        couponCardView.setExpanded(false)
        slideWidget.resetSlider()
        slideToContainer.setVisibility(View.INVISIBLE)
        legalInformationText.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                val intent = Intent(getContext(), HotelRulesActivity::class.java)
                getContext().startActivity(intent)
            }
        })
        if (User.isLoggedIn(getContext())) {
            loginWidget.bind(false, true, Db.getUser(), getLineOfBusiness())
        } else {
            loginWidget.bind(false, false, null, getLineOfBusiness())
        }
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
        viewmodel.tripParams.onNext(HotelCreateTripParams(offer.productKey, qualifyAirAttach, numberOfAdults, childAges))
    }

    val createTripResponseListener: Observer<HotelCreateTripResponse> = endlessObserver { trip ->
        Db.getTripBucket().clear()
        Db.getTripBucket().add(TripBucketItemHotelV2(trip))
        hotelCheckoutSummaryWidget.viewModel.tripResponseObserver.onNext(trip)
        hotelCheckoutSummaryWidget.viewModel.guestCountObserver.onNext(hotelSearchParams.adults + hotelSearchParams.children.size())
        vm = HotelCheckoutOverviewViewModel(getContext())
        vm.newRateObserver.onNext(trip.newHotelProductResponse)
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

    override fun checkoutFormWasUpdated() {
        if (isCheckoutFormComplete) {
            if (PointOfSale.getPointOfSale(getContext()).requiresRulesRestrictionsCheckbox() && !acceptTermsWidget.vm.acceptedTermsObservable.value) {
                acceptTermsWidget.vm.acceptedTermsObservable.subscribe(object : Observer<Boolean> {
                    override fun onCompleted() {
                    }

                    override fun onError(e: Throwable) {
                        throw OnErrorNotImplementedException(e)
                    }

                    override fun onNext(accepted: Boolean) {
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
}
