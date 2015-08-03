package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.TripBucketItemHotelV2
import com.expedia.bookings.data.User
import com.expedia.bookings.data.hotels.HotelCreateTripParams
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.CheckoutBasePresenter
import com.expedia.bookings.widget.HotelCheckoutSummaryWidget
import com.expedia.bookings.widget.WidgetHotelSummaryHeader
import com.mobiata.android.Log
import rx.Observer
import rx.Subscription
import rx.subjects.PublishSubject
import javax.inject.Inject
import kotlin.properties.Delegates

public class HotelCheckoutWidget(context: Context, attr: AttributeSet) : CheckoutBasePresenter(context, attr) {
    var hotelServices: HotelServices? = null
        @Inject set

    var downloadSubscription: Subscription? = null

    var hotelSearchParams: HotelSearchParams by Delegates.notNull()

    var slidAllTheWayObservable = PublishSubject.create<Unit>()

    var hotelCheckoutSummaryWidget: HotelCheckoutSummaryWidget by Delegates.notNull()

    var offer : HotelOffersResponse.HotelRoomResponse? = null

    init {
        Ui.getApplication(getContext()).hotelComponent().inject(this)
    }

    override fun getLineOfBusiness(): LineOfBusiness {
        return LineOfBusiness.HOTELS
    }

    override fun onFinishInflate() {
        super<CheckoutBasePresenter>.onFinishInflate()
        hotelCheckoutSummaryWidget = com.mobiata.android.util.Ui.inflate(R.layout.hotel_checkout_summary_widget, summaryContainer, false)
        summaryContainer.addView(hotelCheckoutSummaryWidget)
        widgetHotelSummaryHeader = WidgetHotelSummaryHeader(getContext(), null)
        this.addView(widgetHotelSummaryHeader)
    }

    fun bind() {
        mainContactInfoCardView.setLineOfBusiness(LineOfBusiness.HOTELSV2)
        mainContactInfoCardView.setEnterDetailsText(getResources().getString(R.string.enter_driver_details))
        paymentInfoCardView.setLineOfBusiness(LineOfBusiness.HOTELSV2)
        paymentInfoCardView.setCreditCardRequired(true)
        mainContactInfoCardView.setExpanded(false)
        paymentInfoCardView.setExpanded(false)
        slideWidget.resetSlider()
        slideToContainer.setVisibility(View.INVISIBLE)
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
        downloadSubscription = hotelServices?.createTripHotels(HotelCreateTripParams(offer!!.productKey, qualifyAirAttach, numberOfAdults, childAges), downloadListener)
    }

    val downloadListener: Observer<HotelCreateTripResponse> = object : Observer<HotelCreateTripResponse> {
        override fun onNext(hotelCreateTripResponse: HotelCreateTripResponse) {
            Log.d("Hotel Checkout Next")
            widgetHotelSummaryHeader.setHotelImage(offer)


            Db.getTripBucket().add(TripBucketItemHotelV2(hotelCreateTripResponse))
            bind()
            show(CheckoutBasePresenter.Ready(), Presenter.FLAG_CLEAR_BACKSTACK)

        }

        override fun onCompleted() {
            Log.d("Hotel Checkout Completed")
        }

        override fun onError(e: Throwable?) {
            Log.d("Hotel Checkout Error", e)
        }
    }


    override fun showProgress(show: Boolean) {
        hotelCheckoutSummaryWidget.setVisibility(if (show) View.INVISIBLE else View.VISIBLE)
        mSummaryProgressLayout.setVisibility(if (show) View.VISIBLE else View.GONE)
    }

    //  SlideToWidget.ISlideToListener

    override fun onSlideStart() {
    }

    override fun onSlideProgress(pixels: Float, total: Float) {
    }

    override fun onSlideAllTheWay() {
        slidAllTheWayObservable.onCompleted()
    }

    override fun onSlideAbort() {
    }

    fun setSearchParams(params: HotelSearchParams) {
        hotelSearchParams = params
    }
}

