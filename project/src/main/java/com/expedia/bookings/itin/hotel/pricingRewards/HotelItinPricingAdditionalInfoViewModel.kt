package com.expedia.bookings.itin.hotel.pricingRewards

import com.expedia.bookings.R
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.common.ItinAdditionalInfoItem
import com.expedia.bookings.itin.common.ItinPricingAdditionInfoViewModelInterface
import com.expedia.bookings.itin.scopes.HasHotelRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.tripstore.data.ItinHotel
import io.reactivex.subjects.PublishSubject

class HotelItinPricingAdditionalInfoViewModel<out S>(val scope: S) : ItinPricingAdditionInfoViewModelInterface where S : HasLifecycleOwner, S : HasStringProvider, S : HasHotelRepo {
    var hotelObserver: LiveDataObserver<ItinHotel>

    override val toolbarTitleSubject: PublishSubject<String> = PublishSubject.create()
    override val additionalInfoItemSubject: PublishSubject<List<ItinAdditionalInfoItem>> = PublishSubject.create()

    init {
        hotelObserver = LiveDataObserver { hotel ->
            toolbarTitleSubject.onNext(scope.strings.fetch(R.string.itin_hotel_details_price_summary_additional_info_button_text))

            val listOfInfoItems = mutableListOf<ItinAdditionalInfoItem>()

            //additional hotel fees
            var stringBuilder = StringBuilder()
            val paymentsAndCreditFees = hotel?.paymentsAndCreditFees
            val hotelPropertyInfo = hotel?.hotelPropertyInfo

            val paymentFeeDisclaimer = paymentsAndCreditFees?.paymentsHotelFeesAndDepositsDisclaimer
            paymentFeeDisclaimer?.let {
                stringBuilder.append(it)
            }
            val noFeesStaticText = paymentsAndCreditFees?.noFeesStaticText
            noFeesStaticText?.let {
                stringBuilder.append(it)
            }
            val fees = hotelPropertyInfo?.fees
            fees?.forEach {
                stringBuilder.append(it)
            }
            val mandatoryFees = hotelPropertyInfo?.mandatoryFees
            mandatoryFees?.forEach {
                stringBuilder.append(it)
            }

            if (!stringBuilder.isBlank()) {
                val additionalHotelFees = ItinAdditionalInfoItem(
                        scope.strings.fetch(R.string.itin_hotel_details_price_summary_additional_info_additional_hotel_fees),
                        stringBuilder.toString()
                )
                listOfInfoItems.add(additionalHotelFees)
            }

            val rules = hotel?.rules

            //taxes and fees
            stringBuilder = StringBuilder()

            val taxesAndFeesInfo = rules?.taxesAndFeesInfo
            taxesAndFeesInfo?.let {
                stringBuilder.append(it)
            }
            if (!stringBuilder.isBlank()) {
                val taxesAndFees = ItinAdditionalInfoItem(
                        scope.strings.fetch(R.string.itin_hotel_details_price_summary_additional_info_taxes_and_fees),
                        stringBuilder.toString()
                )
                listOfInfoItems.add(taxesAndFees)
            }


            //guest charges and room capacity
            stringBuilder = StringBuilder()

            val occupancyPolicies = rules?.occupancyPolicies
            occupancyPolicies?.forEach {
                stringBuilder.append(it)
            }
            val extraGuestPolicies = rules?.extraGuestPolicies
            extraGuestPolicies?.forEach {
                stringBuilder.append(it)
            }

            if (!stringBuilder.isBlank()) {
                val guestCharges = ItinAdditionalInfoItem(
                        scope.strings.fetch(R.string.itin_hotel_details_price_summary_additional_info_guest_charges_room_capacity),
                        stringBuilder.toString()
                )
                listOfInfoItems.add(guestCharges)
            }

            if (listOfInfoItems.isNotEmpty()) {
                additionalInfoItemSubject.onNext(listOfInfoItems)
            }
        }

        scope.itinHotelRepo.liveDataHotel.observe(scope.lifecycleOwner, hotelObserver)
    }
}
