package com.expedia.bookings.hotel.vm

import android.content.Context
import android.content.Intent
import android.support.annotation.VisibleForTesting
import com.expedia.bookings.data.ChildTraveler
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.hotels.shortlist.HotelShortlistItem
import com.expedia.bookings.data.hotels.shortlist.HotelShortlistResponse
import com.expedia.bookings.data.user.UserStateManager
import com.expedia.bookings.deeplink.HotelDeepLink
import com.expedia.bookings.hotel.deeplink.HotelIntentBuilder
import com.expedia.bookings.services.HotelShortlistServices
import io.reactivex.Observer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.ReplaySubject

class HotelFavoritesViewModel(private val context: Context,
                              private val userStateManager: UserStateManager, hotelShortlistServices: HotelShortlistServices) {
    var receivedResponseSubject = ReplaySubject.create<Unit>()

    var response: HotelShortlistResponse<HotelShortlistItem>? = null
        @VisibleForTesting set(value) {
            field = value
            value?.results?.forEach { result -> favoritesList.addAll(result.items) }
        }

    var favoritesList: ArrayList<HotelShortlistItem> = arrayListOf()
        private set

    @VisibleForTesting val compositeDisposable = CompositeDisposable()

    var useShopWithPoints: Boolean? = null

    private val favoritesObserver = object : Observer<HotelShortlistResponse<HotelShortlistItem>> {
        override fun onError(e: Throwable) {
            // TODO
        }

        override fun onNext(fetchResponse: HotelShortlistResponse<HotelShortlistItem>) {
            response = fetchResponse
            receivedResponseSubject.onNext(Unit)
        }

        override fun onSubscribe(d: Disposable) {
            compositeDisposable.add(d)
        }

        override fun onComplete() {
            // Not needed
        }
    }

    init {
        if (userStateManager.isUserAuthenticated()) {
            hotelShortlistServices.fetchFavoriteHotels(favoritesObserver)
        }
    }

    fun onClear() {
        compositeDisposable.clear()
    }

    fun shouldShowList(): Boolean = favoritesList.isNotEmpty()

    fun createHotelIntent(hotelShortlistItem: HotelShortlistItem): Intent? {
        val hotelDeepLink = createHotelDeepLink(hotelShortlistItem)

        return if (hotelDeepLink != null) {
            val intent = HotelIntentBuilder().from(context, hotelDeepLink, false).build(context)
            intent.putExtra(Codes.INFOSITE_DEEPLINK_DONT_BACK_TO_SEARCH, true)
            intent.putExtra(Codes.KEEP_HOTEL_MODULE_ON_DESTROY, true)
            intent
        } else {
            null
        }
    }

    private fun createHotelDeepLink(hotelShortlistItem: HotelShortlistItem): HotelDeepLink? {
        val metadata = hotelShortlistItem.shortlistItem?.metaData
        val hotelId = if (metadata?.hotelId.isNullOrBlank()) hotelShortlistItem.shortlistItem?.itemId else metadata?.hotelId
        if (hotelId.isNullOrBlank()) {
            return null
        }

        return HotelDeepLink().apply {
            this.hotelId = hotelId
            regionId = hotelShortlistItem.regionId
            checkInDate = metadata?.getCheckInLocalDate()
            checkOutDate = metadata?.getCheckOutLocalDate()
            numAdults = metadata?.getNumberOfAdults() ?: 1
            children = (metadata?.getChildrenAges() ?: emptyList()).map { age ->
                ChildTraveler(age, false)
            }
            shopWithPoints = useShopWithPoints
        }
    }
}
