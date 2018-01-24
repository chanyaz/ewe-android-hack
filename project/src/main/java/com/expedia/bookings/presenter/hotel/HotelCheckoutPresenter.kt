package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotel.HotelBookingData
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.payment.PaymentSplits
import com.expedia.bookings.enums.MerchandiseSpam
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.VisibilityTransition
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.widget.CVVEntryWidget
import com.expedia.bookings.widget.FreeCancellationWidget
import com.expedia.bookings.withLatestFrom
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.vm.HotelCheckoutViewModel
import kotlin.properties.Delegates
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isHotelMaterialForms
import com.expedia.bookings.widget.HotelTravelerEntryWidget
import com.expedia.vm.traveler.HotelTravelersViewModel

class HotelCheckoutPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs), CVVEntryWidget.CVVEntryFragmentListener {

    val isFreeCancellationTooltipEnabled by lazy {
        AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppFreeCancellationTooltip)
    }

    val isMaterialHotelEnabled by lazy {
        isHotelMaterialForms(context)
    }

    private val hotelCheckoutWidgetHeight by lazy {
        hotelCheckoutWidget.height
    }

    val freeCancellationWidget: FreeCancellationWidget by bindView(R.id.free_cancellation_view)
    val hotelCheckoutWidget: HotelCheckoutMainViewPresenter by bindView(R.id.checkout)
    val cvv: CVVEntryWidget by bindView(R.id.cvv)

    var hotelSearchParams: HotelSearchParams by Delegates.notNull()

    var hotelCheckoutViewModel: HotelCheckoutViewModel by notNullAndObservable { vm ->
        vm.bookedWithCVVSubject.withLatestFrom(vm.paymentModel.paymentSplits, { cvv, paymentSplits ->
            object {
                val cvv = cvv
                val paymentSplits = paymentSplits
            }
        }).subscribe {
            hotelCheckoutViewModel.hotelBookingDataObservable.onNext(getHotelBookingData(it.cvv, it.paymentSplits))
        }

        vm.bookedWithoutCVVSubject.withLatestFrom(vm.paymentModel.paymentSplits, { unit, paymentSplits -> paymentSplits }).subscribe {
            hotelCheckoutViewModel.hotelBookingDataObservable.onNext(getHotelBookingData(null, it))
        }

        hotelCheckoutWidget.slideAllTheWayObservable.withLatestFrom(vm.paymentModel.paymentSplitsWithLatestTripTotalPayableAndTripResponse) { unit, paymentSplitsAndLatestTripResponse ->
            paymentSplitsAndLatestTripResponse.isCardRequired()
        }.subscribe(checkoutSliderSlidObserver)

        if (isFreeCancellationTooltipEnabled) {
            vm.paymentModel.paymentSplitsWithLatestTripTotalPayableAndTripResponse.map {
                HtmlCompat.fromHtml(it.tripResponse.newHotelProductResponse.hotelRoomResponse.cancellationPolicy)
            }.subscribe(freeCancellationWidget.viewModel.freeCancellationTextObservable)
        }
    }

    init {
        View.inflate(getContext(), R.layout.widget_hotel_checkout, this)
    }

    override fun onFinishInflate() {
        addTransition(checkoutToCvv)
        addDefaultTransition(defaultCheckoutTransition)
        hotelCheckoutWidget.hotelCheckoutMainViewModel.emailOptInStatus.subscribe { status ->
            if (isHotelMaterialForms(context)) {
                (hotelCheckoutWidget.travelersPresenter.viewModel as HotelTravelersViewModel).createTripOptInStatus.onNext(status)
            } else {
                hotelCheckoutWidget.mainContactInfoCardView.setUPEMailOptCheckBox(status)
            }
        }
        cvv.setCVVEntryListener(this)

        if (isFreeCancellationTooltipEnabled) {
            addTransition(checkoutToFreeCancellation)

            freeCancellationWidget.viewModel.closeFreeCancellationObservable.subscribe {
                back()
            }
            hotelCheckoutWidget.hotelCheckoutSummaryWidget.freeCancellationTooltipView.setOnClickListener({
                show(freeCancellationWidget)
            })
        }
    }

    fun setSearchParams(params: HotelSearchParams) {
        hotelSearchParams = params
    }

    fun showCheckout(offer: HotelOffersResponse.HotelRoomResponse) {
        show(hotelCheckoutWidget)
        hotelCheckoutWidget.showCheckout(offer)
    }

    private val defaultCheckoutTransition = object : Presenter.DefaultTransition(HotelCheckoutMainViewPresenter::class.java.name) {
        override fun endTransition(forward: Boolean) {
            hotelCheckoutWidget.visibility = View.VISIBLE
            cvv.visibility = View.GONE
        }
    }

    private val checkoutToFreeCancellation = object : Transition(HotelCheckoutMainViewPresenter::class.java, FreeCancellationWidget::class.java) {
        override fun startTransition(forward: Boolean) {
            hotelCheckoutWidget.visibility = View.VISIBLE
            freeCancellationWidget.visibility = View.VISIBLE
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            val pos = if (forward) (hotelCheckoutWidgetHeight - (f * hotelCheckoutWidgetHeight)) else (f * hotelCheckoutWidgetHeight)
            freeCancellationWidget.translationY = pos
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            if (forward) {
                hotelCheckoutWidget.visibility = View.GONE
            } else {
                freeCancellationWidget.visibility = View.GONE
            }
        }
    }

    private val checkoutToCvv = object : VisibilityTransition(this, HotelCheckoutMainViewPresenter::class.java, CVVEntryWidget::class.java) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            if (!forward) {
                hotelCheckoutWidget.slideWidget.resetSlider()
                hotelCheckoutWidget.checkoutFormWasUpdated()
            }
        }
    }

    val checkoutSliderSlidObserver = endlessObserver<Boolean> {
        val billingInfo = if (Db.sharedInstance.temporarilySavedCard != null && Db.sharedInstance.temporarilySavedCard.saveCardToExpediaAccount)
            Db.sharedInstance.temporarilySavedCard
        else hotelCheckoutWidget.paymentInfoCardView.sectionBillingInfo.billingInfo

        if (!it) {
            hotelCheckoutViewModel.bookedWithoutCVVSubject.onNext(Unit)
        } else if (billingInfo.storedCard != null && billingInfo.storedCard.isGoogleWallet) {
            onBook(billingInfo.securityCode)
        } else {
            cvv.bind(billingInfo)
            show(cvv)
            HotelTracking.trackHotelCheckoutPaymentCid()
        }
    }

    override fun onBook(cvv: String) {
        hotelCheckoutViewModel.bookedWithCVVSubject.onNext(cvv)
    }

    fun getHotelBookingData(cvv: String?, paymentSplits: PaymentSplits): HotelBookingData {
        val primaryTraveler = if (isMaterialHotelEnabled) {
            (hotelCheckoutWidget.travelersPresenter.viewModel as HotelTravelersViewModel).getTravelers()[0]
        } else {
            hotelCheckoutWidget.mainContactInfoCardView.sectionTravelerInfo.traveler
        }
        val billingInfo = if (Db.sharedInstance.temporarilySavedCard != null && Db.sharedInstance.temporarilySavedCard.saveCardToExpediaAccount)
            Db.sharedInstance.temporarilySavedCard
        else hotelCheckoutWidget.paymentInfoCardView.sectionBillingInfo.billingInfo

        val isEmailOptedIn = if (isMaterialHotelEnabled) {
            getMaterialEmailOptInStatus(Db.getTripBucket().hotelV2.mHotelTripResponse.guestUserPromoEmailOptInStatus)
        } else {
            hotelCheckoutWidget.mainContactInfoCardView.emailOptIn ?: false
        }

        return HotelBookingData(cvv, paymentSplits, hotelSearchParams.checkIn, hotelSearchParams.checkOut, primaryTraveler, billingInfo, isEmailOptedIn)
    }

    private fun getMaterialEmailOptInStatus(createTripOptInStatus: String?): Boolean {
        createTripOptInStatus?.let { status ->
            val isOptInChecked = (hotelCheckoutWidget.travelersPresenter.travelerEntryWidget as HotelTravelerEntryWidget)
                    .merchandiseOptCheckBox.isChecked
            if (MerchandiseSpam.valueOf(status) === MerchandiseSpam.CONSENT_TO_OPT_OUT) {
                return !isOptInChecked
            } else {
                return isOptInChecked
            }
        }
        return false
    }
}
