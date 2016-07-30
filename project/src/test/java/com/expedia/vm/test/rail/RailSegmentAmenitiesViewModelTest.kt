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
import rx.observers.TestSubscriber
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

        val noAmenitiesSubscriber = TestSubscriber<Boolean>()
        amenitiesVM.noAmenitiesObservable.subscribe(noAmenitiesSubscriber)

        amenitiesVM.segmentAmenitiesObservable.onNext(segmentFarePair)
        assertTrue(noAmenitiesSubscriber.onNextEvents[0])
    }

    @Test
    fun amenitiesForSegment() {
        val expected = "• Your own seat<br/>• WC<br/>"
        amenitiesVM = RailSegmentAmenitiesViewModel(activity)
        val segmentFarePair = createSegmentWithAmenities()

        val noAmenitiesSubscriber = TestSubscriber<Boolean>()
        amenitiesVM.noAmenitiesObservable.subscribe(noAmenitiesSubscriber)
        val formattedAmenitiesSubscriber = TestSubscriber<String>()
        amenitiesVM.formattedAmenitiesObservable.subscribe(formattedAmenitiesSubscriber)

        amenitiesVM.segmentAmenitiesObservable.onNext(segmentFarePair)

        assertFalse(noAmenitiesSubscriber.onNextEvents[0])
        assertEquals(expected, formattedAmenitiesSubscriber.onNextEvents[0])
    }

    private fun createSegmentWithNoAmenities(): Pair<RailSegment, PassengerSegmentFare?> {
        var segment = RailSegment()
        segment.departureStation = RailStation("ABC", "London", "", "")
        segment.arrivalStation = RailStation("ABC", "Manchester", "", "")

        var segmentFare = PassengerSegmentFare()
        segmentFare.carrierServiceClassDisplayName = "Greatest class ever"
        segmentFare.carrierFareClassDisplayName = "go anytime"

        return Pair(segment, segmentFare)
    }

    private fun createSegmentWithAmenities(): Pair<RailSegment, PassengerSegmentFare?> {
        var pair = createSegmentWithNoAmenities()
        var amenity1 = PassengerSegmentFare.Amenity()
        amenity1.displayName = "Your own seat"

        var amenity2 = PassengerSegmentFare.Amenity()
        amenity2.displayName = "WC"


        var amenities = ArrayList<PassengerSegmentFare.Amenity>()
        amenities.add(amenity1)
        amenities.add(amenity2)
        pair.second?.amenityList = amenities

        return pair
    }
}