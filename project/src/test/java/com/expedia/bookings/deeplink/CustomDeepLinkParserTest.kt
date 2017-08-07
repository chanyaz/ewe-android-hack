package com.expedia.bookings.deeplink

import android.net.Uri
import com.expedia.bookings.data.ChildTraveler
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.util.ArrayList

@RunWith(RobolectricRunner::class)
class CustomDeepLinkParserTest {

    val parser = DeepLinkParser(RuntimeEnvironment.application.assets)

    @Test
    fun hotelDeepLinkParsing() {

        var data = Uri.parse("expda://hotelSearch")
        var output = parser.parseDeepLink(data)
        Assert.assertTrue(output is HotelDeepLink)

        data = data.buildUpon().appendQueryParameter("location", "ID6059241").build()
        output = parser.parseDeepLink(data) as HotelDeepLink
        Assert.assertEquals("6059241", output.regionId)

        data = Uri.parse("expda://hotelSearch").buildUpon().appendQueryParameter("location", "Las%20Vegas,%20NV").build()
        output = parser.parseDeepLink(data) as HotelDeepLink
        Assert.assertEquals("Las%20Vegas,%20NV", output.location)

        data = data.buildUpon().appendQueryParameter("hotelId", "11562190").build()
        output = parser.parseDeepLink(data) as HotelDeepLink
        Assert.assertEquals("Las%20Vegas,%20NV", output.location)
        Assert.assertEquals("11562190", output.hotelId)

        data = data.buildUpon().appendQueryParameter("checkInDate", "2017-05-31").build()
        output = parser.parseDeepLink(data) as HotelDeepLink
        Assert.assertEquals("Las%20Vegas,%20NV", output.location)
        Assert.assertEquals("11562190", output.hotelId)
        Assert.assertEquals(LocalDate(2017, 5, 31), output.checkInDate)

        data = data.buildUpon().appendQueryParameter("checkOutDate", "2017-06-06").build()
        output = parser.parseDeepLink(data) as HotelDeepLink
        Assert.assertEquals("Las%20Vegas,%20NV", output.location)
        Assert.assertEquals("11562190", output.hotelId)
        Assert.assertEquals(LocalDate(2017, 5, 31), output.checkInDate)
        Assert.assertEquals(LocalDate(2017, 6, 6), output.checkOutDate)

        data = data.buildUpon().appendQueryParameter("sortType", "Discounts").build()
        output = parser.parseDeepLink(data) as HotelDeepLink
        Assert.assertEquals("Las%20Vegas,%20NV", output.location)
        Assert.assertEquals("11562190", output.hotelId)
        Assert.assertEquals(LocalDate(2017, 5, 31), output.checkInDate)
        Assert.assertEquals(LocalDate(2017, 6, 6), output.checkOutDate)
        Assert.assertEquals("Discounts", output.sortType)

        data = data.buildUpon().appendQueryParameter("numAdults", "3").build()
        output = parser.parseDeepLink(data) as HotelDeepLink
        Assert.assertEquals("Las%20Vegas,%20NV", output.location)
        Assert.assertEquals("11562190", output.hotelId)
        Assert.assertEquals(LocalDate(2017, 5, 31), output.checkInDate)
        Assert.assertEquals(LocalDate(2017, 6, 6), output.checkOutDate)
        Assert.assertEquals("Discounts", output.sortType)
        Assert.assertEquals(3, output.numAdults)

        data = data.buildUpon().appendQueryParameter("childAges", "1,7,17").build()
        output = parser.parseDeepLink(data) as HotelDeepLink
        val children = arrayOf(ChildTraveler(1, false), ChildTraveler(7, false), ChildTraveler(17, false))
        Assert.assertEquals("Las%20Vegas,%20NV", output.location)
        Assert.assertEquals("11562190", output.hotelId)
        Assert.assertEquals(LocalDate(2017, 5, 31), output.checkInDate)
        Assert.assertEquals(LocalDate(2017, 6, 6), output.checkOutDate)
        Assert.assertEquals("Discounts", output.sortType)
        Assert.assertEquals(3, output.numAdults)
        assertChildTravelersEquals(children,  (output.children as ArrayList).toTypedArray())
    }

    @Test
    fun flightDeepLinkParsing() {
        var data = Uri.parse("expda://flightSearch")
        var output = parser.parseDeepLink(data)
        Assert.assertTrue(output is FlightDeepLink)

        data = data.buildUpon().appendQueryParameter("origin", "SFO").build()
        output = parser.parseDeepLink(data) as FlightDeepLink
        Assert.assertEquals("SFO", output.origin)

        data = data.buildUpon().appendQueryParameter("destination", "DTW").build()
        output = parser.parseDeepLink(data) as FlightDeepLink
        Assert.assertEquals("SFO", output.origin)
        Assert.assertEquals("DTW", output.destination)

        data = data.buildUpon().appendQueryParameter("departureDate", "2017-05-27").build()
        output = parser.parseDeepLink(data) as FlightDeepLink
        Assert.assertEquals("SFO", output.origin)
        Assert.assertEquals("DTW", output.destination)
        Assert.assertEquals(LocalDate(2017, 5, 27), output.departureDate)

        data = data.buildUpon().appendQueryParameter("returnDate", "2017-06-21").build()
        output = parser.parseDeepLink(data) as FlightDeepLink
        Assert.assertEquals("SFO", output.origin)
        Assert.assertEquals("DTW", output.destination)
        Assert.assertEquals(LocalDate(2017, 5, 27), output.departureDate)
        Assert.assertEquals(LocalDate(2017, 6, 21), output.returnDate)

        data = data.buildUpon().appendQueryParameter("numAdults", "3").build()
        output = parser.parseDeepLink(data) as FlightDeepLink
        Assert.assertEquals("SFO", output.origin)
        Assert.assertEquals("DTW", output.destination)
        Assert.assertEquals(LocalDate(2017, 5, 27), output.departureDate)
        Assert.assertEquals(LocalDate(2017, 6, 21), output.returnDate)
        Assert.assertEquals(3, output.numAdults)
    }

    @Test
    fun carDeepLinkParsing() {
        var data = Uri.parse("expda://carSearch")
        var output = parser.parseDeepLink(data)
        Assert.assertTrue(output is CarDeepLink)

        data = data.buildUpon().appendQueryParameter("pickupDateTime", "2017-05-27T14:30:00").build()
        output = parser.parseDeepLink(data) as CarDeepLink
        Assert.assertEquals(DateTime(2017, 5, 27, 14, 30, 0), output.pickupDateTime)

        data = data.buildUpon().appendQueryParameter("dropoffDateTime", "2017-06-13T09:30:00").build()
        output = parser.parseDeepLink(data) as CarDeepLink
        Assert.assertEquals(DateTime(2017, 5, 27, 14, 30, 0), output.pickupDateTime)
        Assert.assertEquals(DateTime(2017, 6, 13, 9, 30, 0), output.dropoffDateTime)

        data = data.buildUpon().appendQueryParameter("pickupLocation", "SFO").build()
        output = parser.parseDeepLink(data) as CarDeepLink
        Assert.assertEquals(DateTime(2017, 5, 27, 14, 30, 0), output.pickupDateTime)
        Assert.assertEquals(DateTime(2017, 6, 13, 9, 30, 0), output.dropoffDateTime)
        Assert.assertEquals("SFO", output.pickupLocation)

        data = data.buildUpon().appendQueryParameter("originDescription", "SFO-San Francisco International Airport").build()
        output = parser.parseDeepLink(data) as CarDeepLink
        Assert.assertEquals(DateTime(2017, 5, 27, 14, 30, 0), output.pickupDateTime)
        Assert.assertEquals(DateTime(2017, 6, 13, 9, 30, 0), output.dropoffDateTime)
        Assert.assertEquals("SFO", output.pickupLocation)
        Assert.assertEquals("SFO-San Francisco International Airport", output.originDescription)

        data = data.buildUpon().appendQueryParameter("pickupLocationLat", "42.2162").build()
        output = parser.parseDeepLink(data) as CarDeepLink
        Assert.assertEquals(DateTime(2017, 5, 27, 14, 30, 0), output.pickupDateTime)
        Assert.assertEquals(DateTime(2017, 6, 13, 9, 30, 0), output.dropoffDateTime)
        Assert.assertEquals("SFO", output.pickupLocation)
        Assert.assertEquals("SFO-San Francisco International Airport", output.originDescription)
        Assert.assertEquals("42.2162", output.pickupLocationLat)

        data = data.buildUpon().appendQueryParameter("pickupLocationLng", "83.3554").build()
        output = parser.parseDeepLink(data) as CarDeepLink
        Assert.assertEquals(DateTime(2017, 5, 27, 14, 30, 0), output.pickupDateTime)
        Assert.assertEquals(DateTime(2017, 6, 13, 9, 30, 0), output.dropoffDateTime)
        Assert.assertEquals("SFO", output.pickupLocation)
        Assert.assertEquals("SFO-San Francisco International Airport", output.originDescription)
        Assert.assertEquals("42.2162", output.pickupLocationLat)
        Assert.assertEquals("83.3554", output.pickupLocationLng)
    }

    @Test
    fun activityDeepLinkParsing() {
        var data = Uri.parse("expda://activitySearch")
        var output = parser.parseDeepLink(data)
        Assert.assertTrue(output is ActivityDeepLink)

        data = data.buildUpon().appendQueryParameter("startDate", "2017-05-27").build()
        output = parser.parseDeepLink(data) as ActivityDeepLink
        Assert.assertEquals(LocalDate(2017, 5, 27), output.startDate)

        data = data.buildUpon().appendQueryParameter("location", "Las%20Vegas,%20NV").build()
        output = parser.parseDeepLink(data) as ActivityDeepLink
        Assert.assertEquals(LocalDate(2017, 5, 27), output.startDate)
        Assert.assertEquals("Las%20Vegas,%20NV", output.location)

        data = data.buildUpon().appendQueryParameter("activityId", "346110").build()
        output = parser.parseDeepLink(data) as ActivityDeepLink
        Assert.assertEquals(LocalDate(2017, 5, 27), output.startDate)
        Assert.assertEquals("Las%20Vegas,%20NV", output.location)
        Assert.assertEquals("346110", output.activityID)

        data = data.buildUpon().appendQueryParameter("filters", "Spa|Adventures").build()
        output = parser.parseDeepLink(data) as ActivityDeepLink
        Assert.assertEquals(LocalDate(2017, 5, 27), output.startDate)
        Assert.assertEquals("Las%20Vegas,%20NV", output.location)
        Assert.assertEquals("346110", output.activityID)
        Assert.assertEquals("Spa|Adventures", output.filters)
    }

    @Test
    fun packagesDeepLinkParsing() {
        var data = Uri.parse("expda://packageSearch")
        var output = parser.parseDeepLink(data)
        Assert.assertTrue(output is PackageDeepLink)
    }

    @Test
    fun railsDeepLinkParsing() {
        var data = Uri.parse("expda://railSearch")
        var output = parser.parseDeepLink(data)
        Assert.assertTrue(output is RailDeepLink)
    }

    @Test
    fun sharedItineraryDeepLinkParsing() {
        var data = Uri.parse("expda://addSharedItinerary")
        var output = parser.parseDeepLink(data)
        Assert.assertTrue(output is SharedItineraryDeepLink)

        data = Uri.parse("expda://addSharedItinerary?url=https://www.expedia.com/m/trips/shared/0y5Ht7LVY1gqSwdrngvC0MCAdQKn-QHMK5hNDlKKtt6jwSkXTR2TnYs9xISPHASFzitz_Tty083fguArrsJbxx6j")
        output = parser.parseDeepLink(data) as SharedItineraryDeepLink
        Assert.assertEquals("https://www.expedia.com/m/trips/shared/0y5Ht7LVY1gqSwdrngvC0MCAdQKn-QHMK5hNDlKKtt6jwSkXTR2TnYs9xISPHASFzitz_Tty083fguArrsJbxx6j", output.url)
    }

    @Test
    fun homeDeepLinkParsing() {
        val data = Uri.parse("expda://home")
        val output = parser.parseDeepLink(data)
        Assert.assertTrue(output is HomeDeepLink)
    }

    @Test
    fun signInDeepLinkParsing() {
        val data = Uri.parse("expda://signIn")
        val output = parser.parseDeepLink(data)
        Assert.assertTrue(output is SignInDeepLink)

    }

    @Test
    fun tripDeepLinkParsing() {
        var data = Uri.parse("expda://trips")
        var output = parser.parseDeepLink(data)
        Assert.assertTrue(output is TripDeepLink)

        data = Uri.parse("expda://showtrips")
        output = parser.parseDeepLink(data)
        Assert.assertTrue(output is TripDeepLink)

        data = Uri.parse("expda://trips?itinNum=7238447666975")
        output = parser.parseDeepLink(data) as TripDeepLink
        Assert.assertTrue(output is TripDeepLink)
        Assert.assertEquals("7238447666975", output.itinNum)

    }

    @Test
    fun supportEmailDeepLinkParsing() {
        val data = Uri.parse("expda://supportEmail")
        val output = parser.parseDeepLink(data)
        Assert.assertTrue(output is SupportEmailDeepLink)
    }

    @Test
    fun reviewFeedbackEmailDeepLinkParsing() {
        val data = Uri.parse("expda://reviewFeedbackEmail")
        val output = parser.parseDeepLink(data)
        Assert.assertTrue(output is ReviewFeedbackEmailDeeplink)
    }

    @Test
    fun forceBucketDeepLinkParsing() {
        var data = Uri.parse("expda://forceBucket")
        var output = parser.parseDeepLink(data)
        Assert.assertTrue(output is ForceBucketDeepLink)

        data = data.buildUpon().appendQueryParameter("key", "1111").build()
        output = parser.parseDeepLink(data) as ForceBucketDeepLink
        Assert.assertTrue(output is ForceBucketDeepLink)
        Assert.assertEquals("1111", output.key)

        data = data.buildUpon().appendQueryParameter("value", "0").build()
        output = parser.parseDeepLink(data) as ForceBucketDeepLink
        Assert.assertTrue(output is ForceBucketDeepLink)
        Assert.assertEquals("1111", output.key)
        Assert.assertEquals("0", output.value)
    }

    @Test
    fun unknownDeepLinkParsing() {
        val data = Uri.parse("expda://wrongHost")
        val output = parser.parseDeepLink(data)
        Assert.assertTrue(output is HomeDeepLink)
    }

    @Test
    fun flightShareDeepLinkParsing() {
        var data = Uri.parse("expda://flightShare")
        var output = parser.parseDeepLink(data)
        Assert.assertTrue(output is FlightShareDeepLink)
    }

    private fun assertChildTravelersEquals(childrenExpected: Array<ChildTraveler>, childrenActual: Array<ChildTraveler>) {
        Assert.assertEquals(childrenExpected.size, childrenActual.size)
        for (i in childrenExpected.indices) {
            Assert.assertEquals(childrenExpected[i].age, childrenActual[i].age)
            Assert.assertEquals(childrenExpected[i].usingSeat(), childrenActual[i].usingSeat())
        }
    }
}