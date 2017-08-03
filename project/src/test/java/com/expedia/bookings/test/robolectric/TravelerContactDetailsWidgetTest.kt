package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.widget.TravelerContactDetailsWidget
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals


@RunWith(RobolectricRunner::class)
class TravelerContactDetailsWidgetTest {

    private lateinit var themedContext: Activity
    private lateinit var travelerWidget: TravelerContactDetailsWidget

    @Before
    fun setup() {
        themedContext = Robolectric.buildActivity(android.support.v4.app.FragmentActivity::class.java).create().get()
        themedContext.setTheme(R.style.Theme_Hotels_Default)
        travelerWidget = LayoutInflater.from(themedContext).inflate(R.layout.test_traveler_contact_details_widget, null) as TravelerContactDetailsWidget
    }

    @Test
    fun testOnChosenTravelerSetsTravelerInDb() {
        val emptyTraveler = Traveler()
        Db.setTravelers(listOf(emptyTraveler))
        val completeTraveler = getCompletedTraveler()
        travelerWidget.onTravelerChosen(completeTraveler)

        assertEquals(Db.getTravelers()[0].tuid, 987654321)
    }

    @Test
    fun testOnAddNewTravelerSetsEmptyTravelerInDb() {
        val completeTraveler = getCompletedTraveler()
        Db.setTravelers(listOf(completeTraveler))
        val emptyTraveler = Traveler()
        Db.getWorkingTravelerManager().setWorkingTravelerAndBase(emptyTraveler)
        travelerWidget.onAddNewTravelerSelected()

        assertEquals(Db.getTravelers()[0].tuid, 0)
    }

    @Test
    fun testBindTravelerWithNullTraveler() {
        travelerWidget.sectionTravelerInfo.bind(null)

        assertEquals(Db.getTravelers()[0].tuid, 0)
    }

    private fun getCompletedTraveler() : Traveler{
        val traveler = Traveler()
        traveler.age = 40
        traveler.email = "12345@aol.com"
        traveler.birthDate = LocalDate.now().minusYears(40)
        traveler.firstName = "Oscar"
        traveler.lastName = "Grouch"
        traveler.tuid = 987654321
        return traveler
    }

}