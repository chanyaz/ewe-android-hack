package com.expedia.bookings.test

import android.content.Context
import com.expedia.bookings.data.flights.RecentSearch
import com.expedia.bookings.data.flights.RecentSearchDAO
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.flights.RecentSearchViewModel
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.stubbing.Answer
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class RecentSearchViewModelTest {

    private lateinit var recentSearchDAO: RecentSearchDAO
    val context = RuntimeEnvironment.application
    lateinit var sut: RecentSearchViewModel
    val recentSearch = RecentSearch("SFO", "LAS", "{\"coordinates\"}".toByteArray(),
            "{\"coordinates\"}".toByteArray() , "2018-05-03", "2018-05-31", "COACH",
            1519277785754, 668, "USD", 1, "",
            false, true)

    @Before
    fun setup() {
        recentSearchDAO = Mockito.mock(RecentSearchDAO::class.java)
        sut = TestRecentSearchViewModel(context, recentSearchDAO)
    }

    @Test
    fun testInsertionWhenThreeItemsInDB() {
        var isDeleted = false
        var isInserted = false
        Mockito.`when`(recentSearchDAO.count()).thenReturn(4)
        Mockito.`when`(recentSearchDAO.checkIfExist("SFO", "LAS", true)).thenReturn(0)
        Mockito.`when`(recentSearchDAO.delete(recentSearch)).thenAnswer(Answer {
            isDeleted = true
        })
        Mockito.`when`(recentSearchDAO.insert(recentSearch)).thenAnswer(Answer {
            isInserted = true
        })
        Mockito.`when`(recentSearchDAO.getOldestRecentSearch()).thenReturn(recentSearch)
        sut.insertRecentSearch(recentSearch)
        assertEquals(true, isDeleted)
        assertEquals(true, isInserted)
    }

    @Test
    fun testInsertionWhenLessThanThreeInDB() {
        var isDeleted = false
        var isInserted = false

        Mockito.`when`(recentSearchDAO.count()).thenReturn(2)
        Mockito.`when`(recentSearchDAO.checkIfExist("SFO", "LAS", true)).thenReturn(0)
        Mockito.`when`(recentSearchDAO.delete(recentSearch)).thenAnswer(Answer {
            isDeleted = true
        })
        Mockito.`when`(recentSearchDAO.insert(recentSearch)).thenAnswer(Answer {
            isInserted = true
        })
        Mockito.`when`(recentSearchDAO.getOldestRecentSearch()).thenReturn(recentSearch)
        sut.insertRecentSearch(recentSearch)
        assertEquals(false, isDeleted)
        assertEquals(true, isInserted)
    }

    @Test
    fun testRecentSearchContainerVisibility() {
        val testObserver = TestObserver<Boolean>()
        sut.recentSearchVisibilityObservable.subscribe(testObserver)
        val listOfRecentSearch = ArrayList<RecentSearch>()
        listOfRecentSearch.add(recentSearch)
        Mockito.`when`(recentSearchDAO.loadAll()).thenReturn(Flowable.just(listOfRecentSearch))
        sut.fetchRecentSearchesObservable.onNext(Unit)
        testObserver.assertValue(true)
    }

    private class TestRecentSearchViewModel(context: Context, private val testDao: RecentSearchDAO) : RecentSearchViewModel(context, testDao) {
        override val subscribeOn = Schedulers.trampoline()
    }
}
