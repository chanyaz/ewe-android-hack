package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.hotels.HotelCheckoutParams
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.VisibilityTransition
import com.expedia.bookings.services.HotelCheckoutResponse
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.utils.JodaUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.CVVEntryWidget
import com.expedia.bookings.widget.ErrorWidget
import com.mobiata.android.Log
import org.joda.time.format.ISODateTimeFormat
import rx.Observer
import javax.inject.Inject
import kotlin.properties.Delegates

public class HotelCheckoutPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs), CVVEntryWidget.CVVEntryFragmentListener {

    val checkout: HotelCheckoutWidget by bindView(R.id.checkout)
    val cvv: CVVEntryWidget by bindView(R.id.cvv)
    val errorScreen: ErrorWidget by bindView(R.id.checkout_error_widget)
    var confirmationObserver: Observer<HotelCheckoutResponse> by Delegates.notNull()

    var hotelServices: HotelServices? = null
        @Inject set
    
    init {
        Ui.getApplication(getContext()).hotelComponent().inject(this)
        View.inflate(getContext(), R.layout.widget_hotel_checkout, this)
    }

    fun showCheckout(offer: HotelOffersResponse.HotelRoomResponse) {
        Db.getTripBucket().clearHotelV2()
        show(checkout)
        checkout.showCheckout(offer)
    }

    override fun onFinishInflate() {
        addTransition(checkoutToCvv)
        addTransition(checkoutToError)
        addTransition(cvvToError)
        addDefaultTransition(defaultCheckoutTransition)
        checkout.slideAllTheWayObservable.subscribe(checkoutSliderSlidObserver)
        cvv.setCVVEntryListener(this)
    }

    private val defaultCheckoutTransition = object : Presenter.DefaultTransition(javaClass<HotelCheckoutWidget>().getName()) {
        override fun finalizeTransition(forward: Boolean) {
            checkout.setVisibility(View.VISIBLE)
            cvv.setVisibility(View.GONE)
            errorScreen.setVisibility(View.GONE)
        }
    }
    private val checkoutToCvv = VisibilityTransition(this, javaClass<HotelCheckoutWidget>(), javaClass<CVVEntryWidget>())

    private val checkoutToError = object : VisibilityTransition(this, javaClass<HotelCheckoutWidget>(), javaClass<ErrorWidget>()) {
        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            if (!forward) {
                checkout.slideWidget.resetSlider()
                checkout.isCheckoutComplete()
            }
        }
    }

    private val cvvToError = VisibilityTransition(this, javaClass<CVVEntryWidget>(), javaClass<ErrorWidget>())

    val checkoutSliderSlidObserver: Observer<Unit> = object : Observer<Unit> {
        override fun onCompleted() {
            val billingInfo = checkout.paymentInfoCardView.sectionBillingInfo.getBillingInfo()
            cvv.bind(billingInfo)
            show(cvv)
        }

        override fun onError(e: Throwable?) {
            Log.d("Eek, there was a slip up!", e)
        }

        override fun onNext(t: Unit?) {

        }
    }

    override fun onBook(cvv: String) {
        val hotelCheckoutParams = HotelCheckoutParams()
        val hotelRate = Db.getTripBucket().getHotelV2().mHotelTripResponse.newHotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo
        val dtf = ISODateTimeFormat.date()

        hotelCheckoutParams.tripId = Db.getTripBucket().getHotelV2().mHotelTripResponse.tripId
        hotelCheckoutParams.expectedTotalFare = "" + java.lang.String.format("%.2f", hotelRate.total) // TODO - We should be using BigDecimal
        hotelCheckoutParams.expectedFareCurrencyCode = "" + hotelRate.currencyCode
        val primaryTraveler = checkout.mainContactInfoCardView.sectionTravelerInfo.getTraveler()
        val billingInfo = checkout.paymentInfoCardView.sectionBillingInfo.getBillingInfo()
        hotelCheckoutParams.firstName = primaryTraveler.getFirstName()
        hotelCheckoutParams.phone = primaryTraveler.getPhoneNumber()
        hotelCheckoutParams.phoneCountryCode = primaryTraveler.getPhoneCountryCode()
        hotelCheckoutParams.email = primaryTraveler.getEmail()
        hotelCheckoutParams.lastName = primaryTraveler.getLastName()
        hotelCheckoutParams.sendEmailConfirmation = false // TODO: when true?
        hotelCheckoutParams.abacusUserGuid = Db.getAbacusGuid()
        hotelCheckoutParams.checkInDate = dtf.print(Db.getHotelSearch().getSearchParams().getCheckInDate())
        hotelCheckoutParams.checkOutDate = dtf.print(Db.getHotelSearch().getSearchParams().getCheckOutDate())
        hotelCheckoutParams.cvv = cvv
        // TODO: Support saved credit cards
        hotelCheckoutParams.nameOnCard = billingInfo.getNameOnCard()
        hotelCheckoutParams.creditCardNumber = billingInfo.getNumber()
        hotelCheckoutParams.expirationDateYear = JodaUtils.format(billingInfo.getExpirationDate(), "yyyy")
        hotelCheckoutParams.expirationDateMonth = JodaUtils.format(billingInfo.getExpirationDate(), "MM")
        hotelCheckoutParams.postalCode = billingInfo.getLocation().getPostalCode()


        hotelServices!!.checkout(hotelCheckoutParams, confirmationObserver)
    }
}
