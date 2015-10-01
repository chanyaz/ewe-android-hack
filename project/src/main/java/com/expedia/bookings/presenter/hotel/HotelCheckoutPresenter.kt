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
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.utils.JodaUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.CVVEntryWidget
import com.expedia.bookings.widget.ErrorWidget
import com.expedia.util.endlessObserver
import com.expedia.vm.HotelCheckoutViewModel
import com.expedia.vm.HotelCreateTripViewModel
import org.joda.time.format.ISODateTimeFormat
import javax.inject.Inject
import kotlin.properties.Delegates

public class HotelCheckoutPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs), CVVEntryWidget.CVVEntryFragmentListener {
    var hotelServices: HotelServices by Delegates.notNull()
        @Inject set

    val hotelCheckoutWidget: HotelCheckoutMainViewPresenter by bindView(R.id.checkout)
    val cvv: CVVEntryWidget by bindView(R.id.cvv)
    val errorScreen: ErrorWidget by bindView(R.id.checkout_error_widget)
    var hotelCheckoutViewModel: HotelCheckoutViewModel by Delegates.notNull()
        @Inject set

    init {
        Ui.getApplication(getContext()).hotelComponent().inject(this)
        View.inflate(getContext(), R.layout.widget_hotel_checkout, this)
    }

    override fun onFinishInflate() {
        addTransition(checkoutToCvv)
        addTransition(checkoutToError)
        addTransition(cvvToError)
        addDefaultTransition(defaultCheckoutTransition)
        hotelCheckoutWidget.slideAllTheWayObservable.subscribe(checkoutSliderSlidObserver)
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
            cvv.setVisibility(View.GONE)
            errorScreen.setVisibility(View.GONE)
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

    private val checkoutToError = object : VisibilityTransition(this, HotelCheckoutMainViewPresenter::class.java, ErrorWidget::class.java) {
        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            if (!forward) {
                hotelCheckoutWidget.slideWidget.resetSlider()
                hotelCheckoutWidget.checkoutFormWasUpdated()
            }
        }
    }

    private val cvvToError = VisibilityTransition(this, CVVEntryWidget::class.java, ErrorWidget::class.java)

    val checkoutSliderSlidObserver = endlessObserver<Unit> {
        val billingInfo = hotelCheckoutWidget.paymentInfoCardView.sectionBillingInfo.getBillingInfo()
        if (billingInfo.getStoredCard() != null && billingInfo.getStoredCard().isGoogleWallet()) {
            onBook(billingInfo.getSecurityCode())
        } else {
            cvv.bind(billingInfo)
            show(cvv)
        }
    }

    override fun onBook(cvv: String) {
        val hotelCheckoutParams = HotelCheckoutParams()
        val hotelRate = Db.getTripBucket().getHotelV2().mHotelTripResponse.newHotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo
        val dtf = ISODateTimeFormat.date()

        hotelCheckoutParams.tripId = Db.getTripBucket().getHotelV2().mHotelTripResponse.tripId
        hotelCheckoutParams.expectedTotalFare = "" + java.lang.String.format("%.2f", hotelRate.total) // TODO - We should be using BigDecimal
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

        if (billingInfo.getStoredCard() == null || billingInfo.getStoredCard().isGoogleWallet()) {
            hotelCheckoutParams.creditCardNumber = billingInfo.getNumber()
            hotelCheckoutParams.expirationDateYear = JodaUtils.format(billingInfo.getExpirationDate(), "yyyy")
            hotelCheckoutParams.expirationDateMonth = JodaUtils.format(billingInfo.getExpirationDate(), "MM")
            hotelCheckoutParams.nameOnCard = billingInfo.getNameOnCard()
            hotelCheckoutParams.postalCode = billingInfo.getLocation().getPostalCode()
        } else {
            hotelCheckoutParams.storedCreditCardId = billingInfo.getStoredCard().getId()
            hotelCheckoutParams.nameOnCard = billingInfo.getFirstName() + " " + billingInfo.getLastName()
        }

        hotelCheckoutViewModel.checkoutParams.onNext(hotelCheckoutParams)
    }
}
