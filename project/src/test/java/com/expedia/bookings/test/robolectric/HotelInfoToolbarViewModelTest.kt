package com.expedia.bookings.test.robolectric

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.hotel.util.HotelFavoritesCache
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.utils.HotelsV2DataUtil
import com.expedia.vm.HotelInfoToolbarViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.properties.Delegates
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelInfoToolbarViewModelTest {
    private var vm: HotelInfoToolbarViewModel by Delegates.notNull()
    private var hotelOffer: HotelOffersResponse by Delegates.notNull()
    private var zeroRatingHotel: HotelOffersResponse by Delegates.notNull()
    private var soldOutHotelOffer: HotelOffersResponse by Delegates.notNull()

    private var context: Context by Delegates.notNull()

    @Before
    fun before() {
        context = RuntimeEnvironment.application
        vm = HotelInfoToolbarViewModel(context)

        hotelOffer = HotelOffersResponse()
        hotelOffer.hotelName = "hotel1"
        hotelOffer.hotelId = "123"
        hotelOffer.hotelStarRating = 5.0
        hotelOffer.hotelRoomResponse = listOf(HotelOffersResponse.HotelRoomResponse())

        soldOutHotelOffer = HotelOffersResponse()
        soldOutHotelOffer.hotelName = "soldOutHotel"
        soldOutHotelOffer.hotelId = "345"
        soldOutHotelOffer.hotelStarRating = 1.0
        soldOutHotelOffer.hotelRoomResponse = emptyList()

        zeroRatingHotel = HotelOffersResponse()
        zeroRatingHotel.hotelName = "zeroRatingHotel"
        zeroRatingHotel.hotelId = "567"
        zeroRatingHotel.hotelStarRating = 0.0
        zeroRatingHotel.hotelRoomResponse = listOf(HotelOffersResponse.HotelRoomResponse())
    }

    @Test
    fun testBindhotelOffersResponseForSoldOut() {
        val viewModelUnderTest = HotelInfoToolbarViewModel(context)
        val hotelNameSubscriber = TestObserver.create<String>()
        val hotelRatingSubscriber = TestObserver.create<Float>()
        val hotelRatingContentDescriptionSubscriber = TestObserver.create<String>()

        viewModelUnderTest.hotelNameObservable.subscribe(hotelNameSubscriber)
        viewModelUnderTest.hotelRatingObservable.subscribe(hotelRatingSubscriber)
        viewModelUnderTest.hotelRatingContentDescriptionObservable.subscribe(hotelRatingContentDescriptionSubscriber)

        viewModelUnderTest.bind(soldOutHotelOffer)

        assertTrue(viewModelUnderTest.hotelSoldOut.value == true)
        assertTrue(viewModelUnderTest.hotelRatingObservableVisibility.value == true)

        hotelNameSubscriber.assertValue(soldOutHotelOffer.hotelName)
        hotelRatingSubscriber.assertValue(1.0f)
        hotelRatingContentDescriptionSubscriber.assertValue(HotelsV2DataUtil.getHotelDetailRatingContentDescription(context, soldOutHotelOffer.hotelStarRating))
    }

    @Test
    fun testBindhotelOffersResponseForNotSoldOut() {
        val viewModelUnderTest = HotelInfoToolbarViewModel(context)
        val hotelNameSubscriber = TestObserver.create<String>()
        val hotelRatingSubscriber = TestObserver.create<Float>()
        val hotelRatingContentDescriptionSubscriber = TestObserver.create<String>()
        val hotelFavoriteVisibiltySubscriber = TestObserver.create<Boolean>()

        viewModelUnderTest.hotelNameObservable.subscribe(hotelNameSubscriber)
        viewModelUnderTest.hotelRatingObservable.subscribe(hotelRatingSubscriber)
        viewModelUnderTest.hotelRatingContentDescriptionObservable.subscribe(hotelRatingContentDescriptionSubscriber)
        viewModelUnderTest.hotelFavoriteIconVisibilityObserver.subscribe(hotelFavoriteVisibiltySubscriber)

        viewModelUnderTest.bind(hotelOffer)

        assertTrue(viewModelUnderTest.hotelSoldOut.value == false)
        assertTrue(viewModelUnderTest.hotelRatingObservableVisibility.value == true)
        hotelFavoriteVisibiltySubscriber.assertValues(false, false)

        hotelNameSubscriber.assertValue(hotelOffer.hotelName)
        hotelRatingSubscriber.assertValue(5.0f)
        hotelRatingContentDescriptionSubscriber.assertValue(HotelsV2DataUtil.getHotelDetailRatingContentDescription(context, hotelOffer.hotelStarRating))
    }

    @Test
    fun testBindhotelOffersResponseForZeroRatingHotel() {
        val viewModelUnderTest = HotelInfoToolbarViewModel(context)
        viewModelUnderTest.bind(zeroRatingHotel)

        assertTrue(viewModelUnderTest.hotelRatingObservableVisibility.value == false)
    }

    @Test
    fun testBindWithFavoriteVisible() {
        HotelFavoritesCache.clearFavorites(context)
        val viewModelUnderTest = HotelInfoToolbarViewModel(context)
        val hotelFavoriteVisibilitySubscriber = TestObserver.create<Boolean>()
        val hotelFavoriteToggleSubscriber = TestObserver.create<Boolean>()
        val hotelFavoriteResourceSubscriber = TestObserver.create<Int>()

        viewModelUnderTest.hotelFavoriteIconVisibilityObserver.subscribe(hotelFavoriteVisibilitySubscriber)
        viewModelUnderTest.hotelFavoriteIconResIdObserver.subscribe(hotelFavoriteResourceSubscriber)
        viewModelUnderTest.favoriteToggledObserver.subscribe(hotelFavoriteToggleSubscriber)
        viewModelUnderTest.bind(hotelOffer, true)

        viewModelUnderTest.favoriteClickObserver.onNext(Unit)
        HotelFavoritesCache.saveFavoriteId(context, hotelOffer.hotelId)
        viewModelUnderTest.favoriteClickObserver.onNext(Unit)

        hotelFavoriteVisibilitySubscriber.assertValues(false, true)
        hotelFavoriteResourceSubscriber.assertValues(R.drawable.ic_favorite_inactive, R.drawable.ic_favorite_active, R.drawable.ic_favorite_active, R.drawable.ic_favorite_inactive)
        hotelFavoriteToggleSubscriber.assertValues(true, false)
    }

    @Test
    fun testBindWithInitiallyFavorite() {
        HotelFavoritesCache.saveFavoriteId(context, hotelOffer.hotelId)
        val viewModelUnderTest = HotelInfoToolbarViewModel(context)
        val hotelFavoriteResourceSubscriber = TestObserver.create<Int>()
        viewModelUnderTest.hotelFavoriteIconResIdObserver.subscribe(hotelFavoriteResourceSubscriber)

        viewModelUnderTest.bind(hotelOffer, true)

        hotelFavoriteResourceSubscriber.assertValues(R.drawable.ic_favorite_active)
    }

    @Test
    fun testFavoriteIconNoOffer() {
        val viewModelUnderTest = HotelInfoToolbarViewModel(context)
        val hotelFavoriteResourceSubscriber = TestObserver.create<Int>()
        val hotelFavoriteToggleSubscriber = TestObserver.create<Boolean>()

        viewModelUnderTest.favoriteToggledObserver.subscribe(hotelFavoriteToggleSubscriber)
        viewModelUnderTest.favoriteClickObserver.onNext(Unit)

        hotelFavoriteResourceSubscriber.assertValueCount(0)
        hotelFavoriteToggleSubscriber.assertValueCount(0)
    }

    @Test
    fun testCacheChangedSubject() {
        val viewModelUnderTest = HotelInfoToolbarViewModel(context)
        val hotelFavoriteResourceSubscriber = TestObserver.create<Int>()
        viewModelUnderTest.hotelFavoriteIconResIdObserver.subscribe(hotelFavoriteResourceSubscriber)

        viewModelUnderTest.bind(hotelOffer, true)

        HotelFavoritesCache.saveFavoriteId(context, hotelOffer.hotelId)

        hotelFavoriteResourceSubscriber.assertValueCount(2)
        hotelFavoriteResourceSubscriber.assertValues(R.drawable.ic_favorite_inactive, R.drawable.ic_favorite_active)
    }

    @Test
    fun testCacheChangedSubjectNotInCache() {
        HotelFavoritesCache.saveFavoriteId(context, hotelOffer.hotelId)
        val viewModelUnderTest = HotelInfoToolbarViewModel(context)
        val hotelFavoriteResourceSubscriber = TestObserver.create<Int>()
        viewModelUnderTest.hotelFavoriteIconResIdObserver.subscribe(hotelFavoriteResourceSubscriber)

        viewModelUnderTest.bind(hotelOffer, true)

        HotelFavoritesCache.clearFavorites(context)

        hotelFavoriteResourceSubscriber.assertValueCount(2)
        hotelFavoriteResourceSubscriber.assertValuesAndClear(R.drawable.ic_favorite_active, R.drawable.ic_favorite_inactive)
    }

    @Test
    fun testCacheChangedSubjectNullOfferResponse() {
        val viewModelUnderTest = HotelInfoToolbarViewModel(context)
        val hotelFavoriteResourceSubscriber = TestObserver.create<Int>()
        viewModelUnderTest.hotelFavoriteIconResIdObserver.subscribe(hotelFavoriteResourceSubscriber)

        HotelFavoritesCache.cacheChangedSubject.onNext(setOf(hotelOffer.hotelId))

        hotelFavoriteResourceSubscriber.assertEmpty()
    }
}
