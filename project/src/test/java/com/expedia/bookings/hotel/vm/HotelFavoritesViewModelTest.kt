package com.expedia.bookings.hotel.vm

import com.expedia.bookings.data.hotelshortlist.HotelShortlistFetchResponse
import com.expedia.bookings.services.HotelShortlistServices
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.UserLoginTestUtil.Companion.setupUserAndMockLogin
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.vm.hotel.HotelFavoritesViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@Config(shadows = [(ShadowUserManager::class), (ShadowAccountManagerEB::class)])
class HotelFavoritesViewModelTest {
    var context = RuntimeEnvironment.application
    val userStateManager = UserLoginTestUtil.getUserStateManager()

    var reviewServicesRule = ServicesRule(HotelShortlistServices::class.java)
        @Rule get

    @Before
    fun setup() {
        context = RuntimeEnvironment.application
    }

    @Test
    fun testLoggedIn() {
        setupUserAndMockLogin(UserLoginTestUtil.mockUser(), userStateManager)
        val viewModel = HotelFavoritesViewModel(userStateManager, reviewServicesRule.services!!)
        val t = TestObserver<Unit>()
        viewModel.receivedResponseSubject.subscribe(t)
        t.assertValueCount(1)
        assertTrue(viewModel.shouldShowList())

        assertNotEquals(0, viewModel.compositeDisposable.size())
        viewModel.onClear()
        assertEquals(0, viewModel.compositeDisposable.size())
    }

    @Test
    fun testLoggedOut() {
        userStateManager.signOut()
        val viewModel = HotelFavoritesViewModel(userStateManager, reviewServicesRule.services!!)
        val t = TestObserver<Unit>()
        viewModel.receivedResponseSubject.subscribe(t)
        t.assertValueCount(0)
        assertFalse(viewModel.shouldShowList())
    }

    @Test
    fun testMultipleResultListsCombined() {
        val viewModel = HotelFavoritesViewModel(userStateManager, reviewServicesRule.services!!)
        val response = HotelShortlistFetchResponse()
        val resultOneItem = HotelShortlistFetchResponse.HotelShortlistResult().apply {
            items = arrayListOf(HotelShortlistFetchResponse.HotelShortlistItem())
        }
        response.results = arrayListOf(resultOneItem, resultOneItem)
        viewModel.response = response
        assertEquals(viewModel.favoritesList.size, 2)
    }
}
