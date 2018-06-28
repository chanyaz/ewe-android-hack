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
import com.expedia.bookings.hotel.util.HotelFavoritesCache
import com.expedia.bookings.hotel.util.HotelFavoritesManager
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject

class HotelFavoritesViewModel(private val context: Context,
                              private val userStateManager: UserStateManager,
                              private val hotelFavoritesManager: HotelFavoritesManager) {
    var receivedResponseSubject = ReplaySubject.create<Unit>()
    var favoriteRemovedAtIndexSubject = BehaviorSubject.create<Int>()
    var favoriteAddedAtIndexSubject = BehaviorSubject.create<Int>()
    var favoritesEmptySubject = BehaviorSubject.create<Unit>()
    var favoriteRemovedFromCacheSubject = PublishSubject.create<Unit>()

    var response: HotelShortlistResponse<HotelShortlistItem>? = null
        @VisibleForTesting set(value) {
            field = value
            favoritesList.clear()
            value?.results?.forEach { result -> favoritesList.addAll(result.items) }
        }

    var favoritesList: ArrayList<HotelShortlistItem> = arrayListOf()
        private set

    @VisibleForTesting
    val compositeDisposable = CompositeDisposable()

    var useShopWithPoints: Boolean? = null

    private var lastRemovedShortlistItem: HotelShortlistItem? = null
    private var lastRemovedIndex = -1

    init {
        if (isUserAuthenticated()) {
            compositeDisposable.add(hotelFavoritesManager.fetchSuccessSubject.subscribe { fetchResponse ->
                response = fetchResponse
                receivedResponseSubject.onNext(Unit)
            })
            hotelFavoritesManager.fetchFavorites(context)
        }

        HotelFavoritesCache.cacheChangedSubject.subscribe { favorites ->
            val favoritesToBeRemoved = ArrayList<HotelShortlistItem>()
            for (favorite in favoritesList) {
                val hotelId = favorite.getHotelId()
                if (hotelId != null && !favorites.contains(hotelId)) {
                    favoritesToBeRemoved.add(favorite)
                }
            }

            if (favoritesToBeRemoved.isNotEmpty()) {
                favoritesList.removeAll(favoritesToBeRemoved)
                favoriteRemovedFromCacheSubject.onNext(Unit)
            }
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

    fun isUserAuthenticated(): Boolean {
        return userStateManager.isUserAuthenticated()
    }

    fun removeFavoriteHotelAtIndex(favoriteHotelIndex: Int) {
        val favoriteHotel = favoritesList.getOrNull(favoriteHotelIndex)
        val hotelId = favoriteHotel?.getHotelId()
        if (hotelId.isNullOrBlank()) {
            return
        }
        lastRemovedShortlistItem = favoriteHotel
        lastRemovedIndex = favoriteHotelIndex
        hotelFavoritesManager.removeFavorite(context, hotelId!!)
        favoritesList.remove(favoriteHotel)
        favoriteRemovedAtIndexSubject.onNext(favoriteHotelIndex)
        if (favoritesList.isEmpty()) {
            favoritesEmptySubject.onNext(Unit)
        }
    }

    fun undoLastRemove() {
        if (lastRemovedIndex >= 0 && lastRemovedIndex <= favoritesList.size && lastRemovedShortlistItem != null) {
            hotelFavoritesManager.saveFavorite(context, lastRemovedShortlistItem!!)
            favoritesList.add(lastRemovedIndex, lastRemovedShortlistItem!!)
            favoriteAddedAtIndexSubject.onNext(lastRemovedIndex)
        }
    }

    private fun createHotelDeepLink(hotelShortlistItem: HotelShortlistItem): HotelDeepLink? {
        val hotelId = hotelShortlistItem.getHotelId()
        if (hotelId.isNullOrBlank()) {
            return null
        }

        val metadata = hotelShortlistItem.shortlistItem?.metaData
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
