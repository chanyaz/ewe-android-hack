package com.expedia.bookings.hotel.util

import android.content.Context
import android.support.annotation.VisibleForTesting
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.shortlist.HotelShortlistResponse
import com.expedia.bookings.data.hotels.shortlist.ShortlistItem
import com.expedia.bookings.services.HotelShortlistServices
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject
import okhttp3.ResponseBody

class HotelFavoritesManager(private val shortlistService: HotelShortlistServices) {
    @VisibleForTesting
    val saveSuccessSubject = PublishSubject.create<Unit>()
    @VisibleForTesting
    val removeSuccessSubject = PublishSubject.create<Unit>()

    private var saveRequestSubscription: Disposable? = null
    private var deleteRequestSubscription: Disposable? = null

    fun saveFavorite(context: Context, hotelId: String, searchParams: HotelSearchParams) {
        saveRequestSubscription?.dispose()
        saveRequestSubscription = shortlistService.saveFavoriteHotel(hotelId, searchParams.checkIn,
                searchParams.checkOut, searchParams.adults, searchParams.children,
                getSaveFavoriteObserver(context, hotelId))
    }

    fun removeFavorite(context: Context, hotelId: String) {
        deleteRequestSubscription?.dispose()
        deleteRequestSubscription = shortlistService.removeFavoriteHotel(hotelId, createRemoveFavoriteObserver(context, hotelId))
    }

    private fun getSaveFavoriteObserver(context: Context, hotelId: String): Observer<HotelShortlistResponse<ShortlistItem>> {
        return object : DisposableObserver<HotelShortlistResponse<ShortlistItem>>() {
            override fun onNext(response: HotelShortlistResponse<ShortlistItem>) {
                HotelFavoritesCache.saveFavoriteId(context, hotelId)
                saveSuccessSubject.onNext(Unit)
            }

            //TODO Unhappy path
            override fun onError(e: Throwable) {}
            override fun onComplete() {}
        }
    }

    private fun createRemoveFavoriteObserver(context: Context, hotelId: String): Observer<ResponseBody> {
        return object : DisposableObserver<ResponseBody>() {
            override fun onNext(response: ResponseBody) {
                HotelFavoritesCache.removeFavoriteId(context, hotelId)
                removeSuccessSubject.onNext(Unit)
            }

            //TODO Unhappy path
            override fun onError(e: Throwable) {}
            override fun onComplete() {}
        }
    }
}
