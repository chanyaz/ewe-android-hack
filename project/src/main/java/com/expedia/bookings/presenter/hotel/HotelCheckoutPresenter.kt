package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.User
import com.expedia.bookings.data.hotels.HotelCheckoutParams
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.VisibilityTransition
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.tracking.HotelV2Tracking
import com.expedia.bookings.utils.BookingSuppressionUtils
import com.expedia.bookings.utils.JodaUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.CVVEntryWidget
import com.expedia.util.endlessObserver
import com.expedia.vm.HotelCheckoutViewModel
import com.expedia.vm.HotelCreateTripViewModel
import org.joda.time.format.ISODateTimeFormat
import java.util.Locale
import javax.inject.Inject
import kotlin.properties.Delegates

public class HotelCheckoutPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs), CVVEntryWidget.CVVEntryFragmentListener {
    var hotelServices: HotelServices by Delegates.notNull()
        @Inject set

    val hotelCheckoutWidget: HotelCheckoutMainViewPresenter by bindView(R.id.checkout)
    val cvv: CVVEntryWidget by bindView(R.id.cvv)

    var hotelCheckoutViewModel: HotelCheckoutViewModel by Delegates.notNull()
        @Inject set

    init {
        Ui.getApplication(getContext()).hotelComponent().inject(this)
        View.inflate(getContext(), R.layout.widget_hotel_checkout, this)
    }

    override fun onFinishInflate() {
        addTransition(checkoutToCvv)
        addDefaultTransition(defaultCheckoutTransition)
        hotelCheckoutWidget.slideAllTheWayObservable.subscribe(checkoutSliderSlidObserver)
        hotelCheckoutWidget.emailOptInStatus.subscribe { status ->
            hotelCheckoutWidget.mainContactInfoCardView.setUPEMailOptCheckBox(status)
        }
        hotelCheckoutWidget.viewmodel = HotelCreateTripViewModel(hotelServices)
        cvv.setCVVEntryListener(this)
    }

    fun showCheckout(offer: HotelOffersResponse.HotelRoomResponse) {
        Db.getTripBucket().clearHotelV2()
        show(hotelCheckoutWidget)
        hotelCheckoutWidget.showCheckout(offer)
    }

    private val defaultCheckoutTransition = object : Presenter.DefaultTransition(HotelCheckoutMainViewPresenter::class.java.name) {
        override fun finalizeTransition(forward: Boolean) {
            hotelCheckoutWidget.setVisibility(View.VISIBLE)
            if (User.isLoggedIn(getContext()) && hotelCheckoutWidget.paymentInfoCardView.sectionBillingInfo.billingInfo == null && Db.getUser().getStoredCreditCards().size() == 1) {
                hotelCheckoutWidget.paymentInfoCardView.sectionBillingInfo.bind(Db.getBillingInfo())
                hotelCheckoutWidget.paymentInfoCardView.selectFirstAvailableCard()
            }
            cvv.setVisibility(View.GONE)
        }
    }

    private val checkoutToCvv = object : VisibilityTransition(this, HotelCheckoutMainViewPresenter::class.java, CVVEntryWidget::class.java) {
        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            if (!forward) {
                hotelCheckoutWidget.slideWidget.resetSlider()
                hotelCheckoutWidget.checkoutFormWasUpdated()
            }
        }
    }

    val checkoutSliderSlidObserver = endlessObserver<Unit> {
        val billingInfo = hotelCheckoutWidget.paymentInfoCardView.sectionBillingInfo.getBillingInfo()
        if (billingInfo.getStoredCard() != null && billingInfo.getStoredCard().isGoogleWallet()) {
            onBook(billingInfo.getSecurityCode())
        } else {
            cvv.bind(billingInfo)
            show(cvv)
            HotelV2Tracking().trackHotelV2CheckoutPaymentCid()
        }
    }

    override fun onBook(cvv: String) {
        val hotelCheckoutParams = HotelCheckoutParams()
        val hotelRate = Db.getTripBucket().getHotelV2().mHotelTripResponse.newHotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo
        val dtf = ISODateTimeFormat.date()

        hotelCheckoutParams.tripId = Db.getTripBucket().getHotelV2().mHotelTripResponse.tripId
        hotelCheckoutParams.expectedTotalFare = java.lang.String.format(Locale.ENGLISH, "%.2f", hotelRate.total)
        hotelCheckoutParams.expectedFareCurrencyCode = "" + hotelRate.currencyCode
        val primaryTraveler = hotelCheckoutWidget.mainContactInfoCardView.sectionTravelerInfo.getTraveler()
        val billingInfo = hotelCheckoutWidget.paymentInfoCardView.sectionBillingInfo.getBillingInfo()
        hotelCheckoutParams.firstName = primaryTraveler.getFirstName()
        hotelCheckoutParams.phone = primaryTraveler.getPhoneNumber()
        hotelCheckoutParams.phoneCountryCode = primaryTraveler.getPhoneCountryCode()
        hotelCheckoutParams.email = primaryTraveler.getEmail()
        hotelCheckoutParams.lastName = primaryTraveler.getLastName()
        hotelCheckoutParams.sendEmailConfirmation = true
        hotelCheckoutParams.abacusUserGuid = Db.getAbacusGuid()
        hotelCheckoutParams.checkInDate = dtf.print(Db.getHotelSearch().getSearchParams().getCheckInDate())
        hotelCheckoutParams.checkOutDate = dtf.print(Db.getHotelSearch().getSearchParams().getCheckOutDate())
        hotelCheckoutParams.cvv = cvv
        hotelCheckoutParams.emailOptIn = hotelCheckoutWidget.mainContactInfoCardView.emailOptIn?.toString() ?: ""

        if (billingInfo.getStoredCard() == null || billingInfo.getStoredCard().isGoogleWallet()) {
            hotelCheckoutParams.creditCardNumber = billingInfo.getNumber()
            hotelCheckoutParams.expirationDateYear = JodaUtils.format(billingInfo.getExpirationDate(), "yyyy")
            hotelCheckoutParams.expirationDateMonth = JodaUtils.format(billingInfo.getExpirationDate(), "MM")
            hotelCheckoutParams.nameOnCard = billingInfo.getNameOnCard()
            hotelCheckoutParams.postalCode = billingInfo.getLocation().getPostalCode()
            hotelCheckoutParams.storeCreditCardInUserProfile = billingInfo.saveCardToExpediaAccount
        } else {
            hotelCheckoutParams.storedCreditCardId = billingInfo.getStoredCard().getId()
            hotelCheckoutParams.nameOnCard = billingInfo.getFirstName() + " " + billingInfo.getLastName()
        }

        hotelCheckoutParams.suppressFinalBooking = BookingSuppressionUtils.shouldSuppressFinalBooking(context, R.string.preference_suppress_hotel_bookings)
        val tealeafTransactionId = Db.getTripBucket().hotelV2.mHotelTripResponse.tealeafTransactionId;
        hotelCheckoutParams.tealeafTransactionId = tealeafTransactionId

        hotelCheckoutViewModel.checkoutParams.onNext(hotelCheckoutParams)
    }
}
