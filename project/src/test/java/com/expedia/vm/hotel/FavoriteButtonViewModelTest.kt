package com.expedia.vm.hotel

import com.expedia.bookings.data.Db
import com.expedia.bookings.data.HotelFavoriteHelper
import com.expedia.bookings.data.abacus.AbacusResponse
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.tracking.hotel.HotelTracking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber

@RunWith(RobolectricRunner::class)
class FavoriteButtonViewModelTest {

    val context = RuntimeEnvironment.application
    val hotelIdBeingTested: String = "1234"
    val otherHotelId: String = "2345"
    lateinit var vm: FavoriteButtonViewModel
    val testSubscriber = TestSubscriber.create<Pair<String, Boolean>>()

    @Before
    fun setUp() {
        val abacusResponse = AbacusResponse()
        abacusResponse.updateABTestForDebug(AbacusUtils.EBAndroidAppHotelFavoriteTest,
                AbacusUtils.DefaultVariate.BUCKETED.ordinal)
        Db.setAbacusResponse(abacusResponse)
    }

    @Test
    fun testUnfavoriteHotelIsFavorite() {
        givenViewModelForSearchResult()

        givenHotelIsNotFavorite(hotelIdBeingTested)
        givenHotelIsNotFavorite(otherHotelId)

        whenFavoriteButtonIsClicked()

        thenHotelIsFavorite(hotelIdBeingTested)
        thenHotelIsNotFavorite(otherHotelId)
    }

    @Test
    fun testFavoritedHotelIsUnfavorited() {
        givenViewModelForSearchResult()

        givenHotelIsFavorite(hotelIdBeingTested)
        givenHotelIsFavorite(otherHotelId)

        whenFavoriteButtonIsClicked()

        thenHotelIsNotFavorite(hotelIdBeingTested)
        thenHotelIsFavorite(otherHotelId)
    }

    @Test
    fun testFavoritingHotel() {
        givenViewModelForSearchResult()

        givenFavoriteSubjectSubscription()

        givenHotelIsNotFavorite(hotelIdBeingTested)

        whenFavoriteButtonIsClicked()

        thenHotelIsFavorite(hotelIdBeingTested)

        thenHotelFavoriteChangeFired(hotelIdBeingTested, true)
    }

    @Test
    fun testUnfavoritingHotel() {
        givenViewModelForSearchResult()

        givenFavoriteSubjectSubscription()

        givenHotelIsFavorite(hotelIdBeingTested)

        whenFavoriteButtonIsClicked()

        thenHotelIsNotFavorite(hotelIdBeingTested)

        thenHotelFavoriteChangeFired(hotelIdBeingTested, false)
    }

    @Test
    fun testFavoritingIsTrackedOnSearchResult() {
        givenViewModelForSearchResult()

        givenHotelIsNotFavorite(hotelIdBeingTested)

        whenFavoriteButtonIsClicked()

        thenHotelIsFavorite(hotelIdBeingTested)

    }

    @Test
    fun testUnfavoritingIsTrackedOnSearchResult() {
        givenViewModelForSearchResult()

        givenHotelIsFavorite(hotelIdBeingTested)

        whenFavoriteButtonIsClicked()

        thenHotelIsNotFavorite(hotelIdBeingTested)
    }

    @Test
    fun testFavoritingIsTrackedOnInfosite() {
        givenViewModelForInfosite()

        givenHotelIsNotFavorite(hotelIdBeingTested)

        whenFavoriteButtonIsClicked()

        thenHotelIsFavorite(hotelIdBeingTested)
    }

    @Test
    fun testUnfavoritingIsTrackedOnInfosite() {
        givenViewModelForInfosite()

        givenHotelIsFavorite(hotelIdBeingTested)

        whenFavoriteButtonIsClicked()

        thenHotelIsNotFavorite(hotelIdBeingTested)
    }

    @Test
    fun testFirstTimeFavoriting() {
        givenViewModelForInfosite()

        thenIsFirstTimeFavoriting(true)

        whenFavoriteButtonIsClicked()

        thenIsFirstTimeFavoriting(true)

        whenFavoriteButtonIsClicked()
        thenIsFirstTimeFavoriting(false)

        whenFavoriteButtonIsClicked()
        thenIsFirstTimeFavoriting(false)
    }

    private fun givenViewModelForSearchResult() {
        vm = FavoriteButtonViewModel(context, hotelIdBeingTested, HotelTracking.PageName.SEARCH_RESULT)
    }

    private fun givenViewModelForInfosite() {
        vm = FavoriteButtonViewModel(context, hotelIdBeingTested, HotelTracking.PageName.INFOSITE)
    }

    private fun givenFavoriteSubjectSubscription() {
        vm.favoriteChangeSubject.subscribe(testSubscriber)
    }

    private fun givenHotelIsFavorite(hotelId: String) {
        HotelFavoriteHelper.toggleHotelFavoriteState(context, hotelId)
        assertEquals(true, HotelFavoriteHelper.isHotelFavorite(context, hotelId))
    }

    private fun givenHotelIsNotFavorite(hotelId: String) {
        assertEquals(false, HotelFavoriteHelper.isHotelFavorite(context, hotelId))
    }

    private fun whenFavoriteButtonIsClicked() {
        vm.clickSubject.onNext(Unit)
    }

    private fun thenIsFirstTimeFavoriting(isFirstTime: Boolean) {
        assertEquals(isFirstTime, vm.firstTimeFavoritingSubject.value)
    }

    private fun thenHotelIsFavorite(hotelId: String) {
        assertEquals(true, HotelFavoriteHelper.isHotelFavorite(context, hotelId))
    }

    private fun thenHotelIsNotFavorite(hotelId: String) {
        assertEquals(false, HotelFavoriteHelper.isHotelFavorite(context, hotelId))
    }

    private fun thenHotelFavoriteChangeFired(hotelId: String, isFavorite: Boolean) {
        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue(Pair(hotelId, isFavorite))
    }
}