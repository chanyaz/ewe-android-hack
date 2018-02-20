package com.expedia.bookings.test.phone.flights

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.expedia.bookings.data.AppDatabase
import com.expedia.bookings.data.flights.RecentSearch
import com.expedia.bookings.data.flights.RecentSearchDAO
import junit.framework.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecentSearchDAOTest {

    private lateinit var testDb: AppDatabase
    private lateinit var recentSearchDAO: RecentSearchDAO
    val recentSearch1 = RecentSearch("SFO", "LAS", "{\"coordinates\"}".toByteArray(),
            "{\"coordinates\"}".toByteArray(), "2018-05-03", "2018-05-31", "COACH",
            1519277785754, 668, "USD", 1, "",
            false, true)
    val recentSearch2 = RecentSearch("DEL", "BLR", "{\"coordinates\"}".toByteArray(),
            "{\"coordinates\"}".toByteArray(), "2018-05-05", "", "PREMIUM_ECONOMY", 1519277785754, 200, "USD", 1, "",
            false, false)
    val recentSearch3 = RecentSearch("LHR", "LAS", "{\"coordinates\"}".toByteArray(),
            "{\"coordinates\"}".toByteArray() , "2018-05-07", "2018-05-29", "BUSINESS",
            1519277785754, 500, "USD", 1, "10",
            false, true)
    val recentSearch4 = RecentSearch("BLR", "LAS", "{\"coordinates\"}".toByteArray(),
            "{\"coordinates\"}".toByteArray() , "2018-05-09", "", "COACH",
            1519277785754, 400, "USD", 1, "10,12",
            false, false)

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getTargetContext()
        testDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        recentSearchDAO = testDb.recentSearchDAO()
    }

    @Test
    fun testInsertion() {
        recentSearchDAO.insert(recentSearch1)
        recentSearchDAO.insert(recentSearch2)
        val numberOfRecords = recentSearchDAO.count()
        Assert.assertEquals(numberOfRecords, 2)
    }

    @Test
    fun testUpdateExistingRecentSearch() {
        recentSearchDAO.insert(recentSearch1)
        recentSearchDAO.insert(recentSearch2)

        val recentSearch1Modified = RecentSearch("SFO", "LAS", "{\"coordinates\"}".toByteArray(),
                "{\"coordinates\"}".toByteArray() , "2018-07-03", "2018-07-31", "COACH",
                1519277985766, 400, "USD", 1, "",
                false, true)
        recentSearchDAO.insert(recentSearch1Modified)

        val numberOfRecords = recentSearchDAO.count()
        Assert.assertEquals(2, numberOfRecords)
    }

    @Test
    fun testRecentSearchExists() {
        recentSearchDAO.insert(recentSearch1)
        recentSearchDAO.insert(recentSearch2)
        recentSearchDAO.insert(recentSearch3)

        val numberOfRecords = recentSearchDAO.count()
        Assert.assertEquals(3, numberOfRecords)

        Assert.assertEquals(1, recentSearchDAO.checkIfExist(recentSearch1.sourceAirportCode, recentSearch1.destinationAirportCode,
                recentSearch1.isRoundTrip))
        Assert.assertEquals(1, recentSearchDAO.checkIfExist(recentSearch2.sourceAirportCode, recentSearch2.destinationAirportCode,
                recentSearch2.isRoundTrip))
        Assert.assertEquals(1, recentSearchDAO.checkIfExist(recentSearch3.sourceAirportCode, recentSearch3.destinationAirportCode,
                recentSearch3.isRoundTrip))
        Assert.assertEquals(0, recentSearchDAO.checkIfExist(recentSearch4.sourceAirportCode, recentSearch4.destinationAirportCode,
                recentSearch4.isRoundTrip))
    }

    @Test
    fun testDeleteRecentSearch() {
        recentSearchDAO.insert(recentSearch1)
        recentSearchDAO.insert(recentSearch2)
        recentSearchDAO.insert(recentSearch3)

        recentSearchDAO.delete(recentSearch2)

        Assert.assertEquals(1, recentSearchDAO.checkIfExist(recentSearch1.sourceAirportCode, recentSearch1.destinationAirportCode,
                recentSearch1.isRoundTrip))
        Assert.assertEquals(0, recentSearchDAO.checkIfExist(recentSearch2.sourceAirportCode, recentSearch2.destinationAirportCode,
                recentSearch2.isRoundTrip))
        Assert.assertEquals(1, recentSearchDAO.checkIfExist(recentSearch3.sourceAirportCode, recentSearch3.destinationAirportCode,
                recentSearch3.isRoundTrip))
        Assert.assertEquals(2, recentSearchDAO.count())

        recentSearchDAO.delete(recentSearch1)

        Assert.assertEquals(0, recentSearchDAO.checkIfExist(recentSearch1.sourceAirportCode, recentSearch1.destinationAirportCode,
                recentSearch1.isRoundTrip))
        Assert.assertEquals(1, recentSearchDAO.count())
    }
}
