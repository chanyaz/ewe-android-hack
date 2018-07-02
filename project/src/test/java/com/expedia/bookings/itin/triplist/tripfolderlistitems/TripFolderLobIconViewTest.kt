package com.expedia.bookings.itin.triplist.tripfolderlistitems

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.TripFolderProduct
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class TripFolderLobIconViewTest {
    private lateinit var context: Context
    private lateinit var view: TripFolderLobIconView
    private lateinit var testObserver: TestObserver<TripFolderProduct>

    @Before
    fun setup() {
        context = RuntimeEnvironment.application
        view = TripFolderLobIconView(context, null)
        view.viewModel = TripFolderLobIconViewModel()
        testObserver = TestObserver()
        view.viewModel.tripFolderProductSubject.subscribe(testObserver)

    }

    @After
    fun tearDown() {
        testObserver.dispose()
    }

    @Test
    fun testHotelIconSet() {
        setLOBAndAssert(TripFolderProduct.HOTEL, R.drawable.trip_folders_hotel_lob_icon)
    }

    @Test
    fun testFlightIconSet() {
        setLOBAndAssert(TripFolderProduct.FLIGHT, R.drawable.trip_folders_flight_lob_icon)
    }

    @Test
    fun testCarIconSet() {
        setLOBAndAssert(TripFolderProduct.CAR, R.drawable.trip_folders_car_lob_icon)
    }

    @Test
    fun testActivityIconSet() {
        setLOBAndAssert(TripFolderProduct.ACTIVITY, R.drawable.trip_folders_activity_lob_icon)
    }

    @Test
    fun testRailIconSet() {
        setLOBAndAssert(TripFolderProduct.RAIL, R.drawable.trip_folders_rail_lob_icon)
    }

    @Test
    fun testCruiseIconSet() {
        setLOBAndAssert(TripFolderProduct.CRUISE, R.drawable.trip_folders_cruise_lob_icon)
    }

    private fun setLOBAndAssert(lob: TripFolderProduct, icon: Int) {
        testObserver.assertEmpty()

        view.viewModel.tripFolderProductSubject.onNext(lob)

        testObserver.assertValueCount(1)
        testObserver.assertValue(lob)
        assertEquals(icon, Shadows.shadowOf(view.lobIconImageView.drawable).createdFromResId)
    }
}
