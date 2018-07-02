package com.expedia.bookings.itin.triplist.tripfolderlistitems

import com.expedia.bookings.data.trips.TripFolder
import com.expedia.bookings.data.trips.TripFolderProduct
import com.expedia.bookings.itin.helpers.MockStringProvider
import com.expedia.bookings.services.TestObserver
import com.google.gson.Gson
import com.mobiata.mocke3.getJsonStringFromMock
import org.junit.Test
import kotlin.test.assertTrue

class TripFolderItemViewModelTest {

    private val mockStringProvider = MockStringProvider()
    private val viewModel = TripFolderItemViewModel(mockStringProvider)
    private val foldersJson = getJsonStringFromMock("api/trips/tripfolders/tripfolders_three_hotels_one_cruise.json", null)
    private val folders = Gson().fromJson(foldersJson, Array<TripFolder>::class.java).toList()
    private val folder = folders[0]

    @Test
    fun testTripFolderBinding() {
        val testObserver = TestObserver<TripFolder>()
        viewModel.bindTripFolderSubject.subscribe(testObserver)
        testObserver.assertEmpty()

        viewModel.bindTripFolderSubject.onNext(folder)
        testObserver.assertValueCount(1)
        testObserver.assertValue(folder)
    }

    @Test
    fun testTitle() {
        val testObserver = TestObserver<String>()
        viewModel.titleSubject.subscribe(testObserver)
        testObserver.assertEmpty()

        viewModel.bindTripFolderSubject.onNext(folder)
        testObserver.assertValueCount(1)
        testObserver.assertValue("7-night Alaska Cruise from Seattle (Roundtrip)")
    }

    @Test
    fun testTiming() {
        val testObserver = TestObserver<String>()
        viewModel.timingSubject.subscribe(testObserver)
        testObserver.assertEmpty()

        viewModel.bindTripFolderSubject.onNext(folder)
        testObserver.assertValueCount(1)
        val actual = testObserver.values()[0]
        assertTrue(mockStringProvider.fetchWithPhraseCalled)
        assertTrue(actual.contains("Sep 25"))
        assertTrue(actual.contains("Oct 2"))
    }

    @Test
    fun testLobIcons() {
        val testObserver = TestObserver<List<TripFolderProduct>>()
        viewModel.lobIconSubject.subscribe(testObserver)
        testObserver.assertEmpty()

        viewModel.bindTripFolderSubject.onNext(folder)
        testObserver.assertValueCount(1)
        assertTrue(testObserver.values()[0].containsAll(listOf(TripFolderProduct.CRUISE)))
    }
}
