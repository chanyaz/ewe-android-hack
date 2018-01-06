package com.expedia.bookings.section

import android.content.Context
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.user.User
import com.expedia.bookings.data.user.UserStateManager
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class TravelerAutoCompleteAdapterTests {

    val context = RuntimeEnvironment.application

    lateinit private var sut: TravelerAutoCompleteAdapter
    lateinit private var user: User
    lateinit private var userStateManager: UserStateManager

    @Before
    fun setup() {
        sut = TestTravelerAutoCompleteAdapter(context)
        userStateManager = Ui.getApplication(context).appComponent().userStateManager()

        userStateManager.userSource.user = null
    }

    @Test
    fun noAvailableTravelers() {
        val travelers = sut.availableTravelers
        assertTrue(travelers.isEmpty())
    }

    @Test
    fun availableTravelers() {
        givenDbHasUser()
        givenUserHasPrimaryTraveler(makeTraveler("Test", "Traveler"))
        givenUserHasAssociatedTravelers(makeTraveler("Another", "Traveler"))

        val travelers = sut.availableTravelers
        assertEquals(2, travelers.size)
    }

    @Test
    fun noSelectableAvailableTravelers() {
        val primaryTraveler = makeTraveler("Sam", "Smith")
        val associatedTraveler = makeTraveler("John", "Smith")

        givenDbHasUser()
        givenUserHasPrimaryTraveler(primaryTraveler)
        givenUserHasAssociatedTravelers(associatedTraveler)

        val travelers = listOf(primaryTraveler, associatedTraveler)
        givenTravelerFormTravelers(travelers)

        val availableTravelers = sut.availableTravelers
        assertFalse(availableTravelers[0].isSelectable)
        assertFalse(availableTravelers[1].isSelectable)
        assertEquals(2, availableTravelers.size)
    }

    @Test
    fun oneSelectableAvailableTraveler() {
        val primaryTraveler = makeTraveler("Sam", "Smith")
        val associatedTraveler = makeTraveler("John", "Smith")

        givenDbHasUser()
        givenUserHasPrimaryTraveler(primaryTraveler)
        givenUserHasAssociatedTravelers(associatedTraveler)

        val travelers = listOf(primaryTraveler)
        givenTravelerFormTravelers(travelers)

        val availableTravelers = sut.availableTravelers
        assertTrue(availableTravelers[0].isSelectable)
        assertFalse(availableTravelers[1].isSelectable)
        assertEquals(2, availableTravelers.size)
    }

    private fun givenTravelerFormTravelers(travelers: List<Traveler>) {
        Db.sharedInstance.setTravelers(travelers)
    }

    private fun givenUserHasAssociatedTravelers(traveler: Traveler) {
        user.addAssociatedTraveler(traveler)
    }

    private fun makeTraveler(firstName: String, lastName: String): Traveler {
        val traveler = Traveler()
        traveler.firstName = firstName
        traveler.lastName = lastName
        return traveler
    }

    private fun givenUserHasPrimaryTraveler(traveler: Traveler) {
        user.primaryTraveler = traveler
    }

    private fun givenDbHasUser() {
        user = User()
        userStateManager.userSource.user = user
    }

    class TestTravelerAutoCompleteAdapter(context: Context): TravelerAutoCompleteAdapter(context) {
        override fun isUserLoggedIn(): Boolean {
            return true
        }
    }
}
