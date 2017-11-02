package com.expedia.vm.test.rail

import android.app.Activity
import com.expedia.bookings.data.rail.responses.PassengerSegmentFare
import com.expedia.bookings.data.rail.responses.RailSegment
import com.expedia.bookings.data.rail.responses.RailStation
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.rail.RailSegmentAmenitiesViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import com.expedia.bookings.services.TestObserver
import java.util.ArrayList
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class RailSegmentAmenitiesViewModelTest {
    lateinit var amenitiesVM: RailSegmentAmenitiesViewModel
    private var activity: Activity by Delegates.notNull()

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
    }

    @Test
    fun noAmenitiesForSegment() {
        amenitiesVM = RailSegmentAmenitiesViewModel(activity)
        val segmentFarePair = createSegmentWithNoAmenities()

        val noAmenitiesSubscriber = TestObserver<Boolean>()
        amenitiesVM.noAmenitiesObservable.subscribe(noAmenitiesSubscriber)

        amenitiesVM.segmentAmenitiesObservable.onNext(segmentFarePair)
        assertTrue(noAmenitiesSubscriber.values()[0])
    }

    @Test
    fun amenitiesForSegment() {
        val expected = "• Your own seat<br/>• WC<br/>"
        amenitiesVM = RailSegmentAmenitiesViewModel(activity)
        val segmentFarePair = createSegmentWithAmenities()

        val noAmenitiesSubscriber = TestObserver<Boolean>()
        amenitiesVM.noAmenitiesObservable.subscribe(noAmenitiesSubscriber)
        val formattedAmenitiesSubscriber = TestObserver<String>()
        amenitiesVM.formattedAmenitiesObservable.subscribe(formattedAmenitiesSubscriber)

        amenitiesVM.segmentAmenitiesObservable.onNext(segmentFarePair)

        assertFalse(noAmenitiesSubscriber.values()[0])
        assertEquals(expected, formattedAmenitiesSubscriber.values()[0])
    }

    private fun createSegmentWithNoAmenities(): Pair<RailSegment, PassengerSegmentFare?> {
        val segment = RailSegment()
        segment.departureStation = RailStation("ABC", "London", "", "")
        segment.arrivalStation = RailStation("ABC", "Manchester", "", "")

        val segmentFare = PassengerSegmentFare()
        segmentFare.carrierServiceClassDisplayName = "Greatest class ever"
        segmentFare.carrierFareClassDisplayName = "go anytime"

        return Pair(segment, segmentFare)
    }

    private fun createSegmentWithAmenities(): Pair<RailSegment, PassengerSegmentFare?> {
        val pair = createSegmentWithNoAmenities()
        val amenity1 = PassengerSegmentFare.Amenity()
        amenity1.displayName = "Your own seat"

        val amenity2 = PassengerSegmentFare.Amenity()
        amenity2.displayName = "WC"


        val amenities = ArrayList<PassengerSegmentFare.Amenity>()
        amenities.add(amenity1)
        amenities.add(amenity2)
        pair.second?.amenityList = amenities

        return pair
    }
}