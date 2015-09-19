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
import com.expedia.bookings.otto.Events
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.CheckoutBasePresenter
import com.expedia.bookings.widget.CouponWidget
import com.expedia.bookings.widget.HotelCheckoutSummaryWidget
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
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
    var hotelCheckoutSummaryWidget: HotelCheckoutSummaryWidget by Delegates.notNull()
    var offer: HotelOffersResponse.HotelRoomResponse by Delegates.notNull()
    var hotelSearchParams: HotelSearchParams by Delegates.notNull()
    val couponCardView = CouponWidget(context, attr)
    var viewmodel: HotelCreateTripViewModel by notNullAndObservable {
        viewmodel.tripResponseObservable.subscribe(createTripResponseListener)
    }

    val hotelServices: HotelServices by Delegates.lazy() {
        Ui.getApplication(getContext()).hotelComponent().hotelServices()
    }

    init {
        couponCardView.viewmodel = HotelCouponViewModel(getContext(), hotelServices)
    }

    override fun getLineOfBusiness(): LineOfBusiness {
        return LineOfBusiness.HOTELSV2
    }

    override fun onFinishInflate() {
        super<CheckoutBasePresenter>.onFinishInflate()
        Ui.getApplication(getContext()).hotelComponent().inject(this)
        hotelCheckoutSummaryWidget = HotelCheckoutSummaryWidget(getContext(), null)
        hotelCheckoutSummaryWidget.viewmodel = HotelCheckoutSummaryViewModel(getContext())
        summaryContainer.addView(hotelCheckoutSummaryWidget)

        mainContactInfoCardView.setLineOfBusiness(LineOfBusiness.HOTELSV2)
        paymentInfoCardView.setLineOfBusiness(LineOfBusiness.HOTELSV2)

        val container = scrollView.findViewById(R.id.scroll_content) as LinearLayout
        container.addView(couponCardView, container.getChildCount() - 2)
        couponCardView.setToolbarListener(toolbarListener)
        couponCardView.viewmodel.couponObservable.subscribe(createTripResponseListener)
        val params = couponCardView.getLayoutParams() as LinearLayout.LayoutParams
        params.setMargins(0, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, getResources().getDisplayMetrics()).toInt(), 0, 0);
    }

    fun bind() {
        mainContactInfoCardView.setEnterDetailsText(getResources().getString(R.string.enter_driver_details))
        paymentInfoCardView.setCreditCardRequired(true)
        mainContactInfoCardView.setExpanded(false)
        paymentInfoCardView.setExpanded(false)
        couponCardView.setExpanded(false)
        slideWidget.resetSlider()
        slideToContainer.setVisibility(View.INVISIBLE)
        legalInformationText.setText(PointOfSale.getPointOfSale().getStylizedHotelBookingStatement())
        legalInformationText.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                val intent = Intent(getContext(), javaClass<HotelRulesActivity>())
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
        Db.getTripBucket().add(TripBucketItemHotelV2(trip))
        hotelCheckoutSummaryWidget.viewmodel.originalRateObserver.onNext(trip.originalHotelProductResponse)
        hotelCheckoutSummaryWidget.viewmodel.newRateObserver.onNext(trip.newHotelProductResponse)
        hotelCheckoutSummaryWidget.breakdown.viewmodel.tripObserver.onNext(trip)
        bind()
        show(CheckoutBasePresenter.Ready(), Presenter.FLAG_CLEAR_BACKSTACK)
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

    @Subscribe fun onLogin(@suppress("UNUSED_PARAMETER") event: Events.LoggedInSuccessful) {
        onLoginSuccessful()
    }

    override fun updateSpacerHeight() {
        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                scrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this)
                val summaryHeight = hotelCheckoutSummaryWidget.getHeight()
                val scrollViewContentHeight = scrollView.getChildAt(0).getHeight() - space.getHeight()
                var remainingHeight = scrollView.getHeight() - scrollViewContentHeight
                val params = space.getLayoutParams()
                params.height = summaryHeight + remainingHeight
                space.setLayoutParams(params)
            }
        })
    }

    override fun isCheckoutButtonEnabled(): Boolean {
        return true
    }

    override fun checkoutFormWasUpdated() {
        if (mainContactInfoCardView.isComplete() && paymentInfoCardView.isComplete()) {

            if (PointOfSale.getPointOfSale(getContext()).requiresRulesRestrictionsCheckbox()) {
                acceptTermsWidget.vm.acceptedTermsObservable.subscribe(object : Observer<Unit> {
                    override fun onCompleted() { }

                    override fun onError(e: Throwable) {
                        throw OnErrorNotImplementedException(e)
                    }

                    override fun onNext(unit: Unit) {
                        acceptTermsWidget.setVisibility(View.INVISIBLE)
                        animateInSlideToPurchase(true)
                    }
                })
                acceptTermsWidget.setVisibility(View.VISIBLE)
            } else {
                animateInSlideToPurchase(true)
            }
        } else {
            acceptTermsWidget.setVisibility(View.INVISIBLE)
            animateInSlideToPurchase(false)
        }
    }
}
