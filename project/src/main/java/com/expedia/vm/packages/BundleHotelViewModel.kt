package com.expedia.vm.packages

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.Images
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Strings
import com.squareup.phrase.Phrase
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.joda.time.format.DateTimeFormat

class BundleHotelViewModel(val context: Context) {
    val showLoadingStateObservable = PublishSubject.create<Boolean>()
    val selectedHotelObservable = PublishSubject.create<Unit>()

    //output
    val hotelTextObservable = BehaviorSubject.create<String>()
    val hotelDatesGuestObservable = BehaviorSubject.create<String>()
    val hotelRoomImageUrlObservable = BehaviorSubject.create<String>()
    val hotelRoomInfoObservable = BehaviorSubject.create<String>()
    val hotelRoomTypeObservable = BehaviorSubject.create<String>()
    val hotelAddressObservable = BehaviorSubject.create<String>()
    val hotelCityObservable = BehaviorSubject.create<String>()
    val hotelFreeCancellationObservable = BehaviorSubject.create<String>()
    val hotelNonRefundableObservable = BehaviorSubject.create<String>()
    val hotelPromoTextObservable = BehaviorSubject.create<String>()
    val hotelDetailsIconObservable = BehaviorSubject.create<Boolean>()
    val hotelSelectIconObservable = BehaviorSubject.create<Boolean>()
    val hotelIconImageObservable = BehaviorSubject.create<Int>()
    val hotelRowExpanded = PublishSubject.create<Unit>()

    init {
        showLoadingStateObservable.subscribe { isShowing ->
            if (isShowing) {
                hotelTextObservable.onNext(context.getString(R.string.progress_searching_hotels_hundreds))
                hotelIconImageObservable.onNext(R.drawable.packages_hotel_icon)
                hotelSelectIconObservable.onNext(true)
                hotelDetailsIconObservable.onNext(false)
            } else {
                hotelTextObservable.onNext(context.getString(R.string.select_hotel_template, StrUtils.formatCityName(Db.sharedInstance.packageParams.destination)))

                val packageResponse = Db.getPackageResponse()

                if (packageResponse != null) {
                    val dtf = DateTimeFormat.forPattern("yyyy-MM-dd")
                    hotelDatesGuestObservable.onNext(Phrase.from(context, R.string.start_dash_end_date_range_with_guests_TEMPLATE)
                            .put("startdate", LocaleBasedDateFormatUtils.localDateToMMMd(dtf.parseLocalDate(packageResponse.getHotelCheckInDate())))
                            .put("enddate", LocaleBasedDateFormatUtils.localDateToMMMd(dtf.parseLocalDate(packageResponse.getHotelCheckOutDate())))
                            .put("guests", StrUtils.formatGuestString(context, Db.sharedInstance.packageParams.guests))
                            .format()
                            .toString())
                }
                hotelSelectIconObservable.onNext(true)
                hotelDetailsIconObservable.onNext(false)
            }
        }
        selectedHotelObservable.subscribe {
            val selectedHotel = Db.getPackageSelectedHotel()
            val selectHotelRoom = Db.sharedInstance.packageSelectedRoom
            hotelTextObservable.onNext(selectedHotel.localizedName)
            hotelIconImageObservable.onNext(R.drawable.packages_hotels_checkmark_icon)
            hotelSelectIconObservable.onNext(false)
            hotelDetailsIconObservable.onNext(true)

            if (Strings.isNotEmpty(selectHotelRoom.roomThumbnailUrl)) {
                hotelRoomImageUrlObservable.onNext(Images.getMediaHost() + selectHotelRoom.roomThumbnailUrl)
            }
            hotelRoomInfoObservable.onNext(selectHotelRoom.roomTypeDescription)
            val bedTypes = (selectHotelRoom.bedTypes ?: emptyList()).map { it.description }.joinToString("")
            hotelRoomTypeObservable.onNext(bedTypes)
            hotelAddressObservable.onNext(selectedHotel.address)

            if (selectHotelRoom.hasFreeCancellation) {
                hotelFreeCancellationObservable.onNext(getCancellationText(selectHotelRoom) ?: "")
                hotelNonRefundableObservable.onNext("")
            } else {
                hotelFreeCancellationObservable.onNext("")
                hotelNonRefundableObservable.onNext(context.resources.getString(R.string.non_refundable))
            }

            hotelPromoTextObservable.onNext(selectHotelRoom.promoDescription ?: "")
            val cityCountry = Phrase.from(context, R.string.hotel_city_country_TEMPLATE)
                    .put("city", selectedHotel.city)
                    .put("country",
                            if (selectedHotel.stateProvinceCode.isNullOrBlank()) Db.sharedInstance.packageParams.destination?.hierarchyInfo?.country?.name else selectedHotel.stateProvinceCode)
                    .format().toString()
            hotelCityObservable.onNext(cityCountry)
        }
    }

    private fun getCancellationText(selectHotelRoom: HotelOffersResponse.HotelRoomResponse): String? {
        val cancellationDateString = selectHotelRoom.freeCancellationWindowDate
        if (cancellationDateString != null) {
            val cancellationDate = DateUtils.yyyyMMddHHmmToDateTime(cancellationDateString)
            return Phrase.from(context, R.string.hotel_free_cancellation_TEMPLATE).put("date",
                    LocaleBasedDateFormatUtils.dateTimeToMMMdhmma(cancellationDate))
                    .format()
                    .toString()
        } else {
            return context.getString(R.string.free_cancellation)
        }
    }
}
