package com.expedia.bookings.itin.hotel.pricingRewards

import com.expedia.bookings.R
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.common.ItinAdditionalInfoItem
import com.expedia.bookings.itin.common.ItinPricingAdditionInfoViewModelInterface
import com.expedia.bookings.itin.scopes.HasItinRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.extensions.firstHotel
import io.reactivex.subjects.PublishSubject

class HotelItinPricingAdditionalInfoViewModel<out S>(val scope: S) : ItinPricingAdditionInfoViewModelInterface where S : HasLifecycleOwner, S : HasStringProvider, S : HasItinRepo {

    override val toolbarTitleSubject: PublishSubject<String> = PublishSubject.create()
    override val additionalInfoItemSubject: PublishSubject<List<ItinAdditionalInfoItem>> = PublishSubject.create()

    val hotelObserver: LiveDataObserver<Itin> = LiveDataObserver { itin ->
        itin?.firstHotel()?.let { itinHotel ->
            toolbarTitleSubject.onNext(scope.strings.fetch(R.string.itin_hotel_details_price_summary_additional_info_button_text))

            val listOfInfoItems = mutableListOf<ItinAdditionalInfoItem>()

            //additional hotel fees
            var stringBuilder = StringBuilder()
            val paymentsAndCreditFees = itinHotel.paymentsAndCreditFees
            val hotelPropertyInfo = itinHotel.hotelPropertyInfo

            paymentsAndCreditFees?.paymentsHotelFeesAndDepositsDisclaimer?.let {
                stringBuilder.append(it)
            }
            paymentsAndCreditFees?.noFeesStaticText?.let {
                stringBuilder.append(it)
            }
            hotelPropertyInfo?.fees?.forEach {
                stringBuilder.append(it)
            }
            hotelPropertyInfo?.mandatoryFees?.forEach {
                stringBuilder.append(it)
            }

            if (!stringBuilder.isBlank()) {
                val additionalHotelFees = ItinAdditionalInfoItem(
                        scope.strings.fetch(R.string.itin_hotel_details_price_summary_additional_info_additional_hotel_fees),
                        stringBuilder.toString()
                )
                listOfInfoItems.add(additionalHotelFees)
            }

            val rules = itinHotel.rules

            //taxes and fees
            stringBuilder = StringBuilder()

            rules?.taxesAndFeesInfo?.let {
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

            rules?.occupancyPolicies?.forEach {
                stringBuilder.append(it)
            }
            rules?.extraGuestPolicies?.forEach {
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
    }

    init {
        scope.itinRepo.liveDataItin.observe(scope.lifecycleOwner, hotelObserver)
    }
}
