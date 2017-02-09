package com.expedia.bookings.deeplink

import android.net.Uri
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(RobolectricRunner::class)
class UniversalDeepLinkParserTest() {

    val parser = DeepLinkParser()

    @Test
    fun hotelUniversalLinkParsing() {
        var data = Uri.parse("https://www.expedia.com/mobile/deeplink/Hotel-Search")
        var output = parser.parseDeepLink(data)
        Assert.assertTrue(output is HotelDeepLink)

        data = data.buildUpon().appendQueryParameter("startDate", "05%2F31%2F2017").build()
        output = parser.parseDeepLink(data) as HotelDeepLink
        Assert.assertTrue(output is HotelDeepLink)
        Assert.assertEquals(LocalDate(2017, 5, 31), output.checkInDate)

        data = data.buildUpon().appendQueryParameter("endDate", "06%2F06%2F2017").build()
        output = parser.parseDeepLink(data) as HotelDeepLink
        Assert.assertTrue(output is HotelDeepLink)
        Assert.assertEquals(LocalDate(2017, 5, 31), output.checkInDate)
        Assert.assertEquals(LocalDate(2017, 6, 6), output.checkOutDate)

        data = data.buildUpon().appendQueryParameter("regionId", "6059241").build()
        output = parser.parseDeepLink(data) as HotelDeepLink
        Assert.assertTrue(output is HotelDeepLink)
        Assert.assertEquals(LocalDate(2017, 5, 31), output.checkInDate)
        Assert.assertEquals(LocalDate(2017, 6, 6), output.checkOutDate)
        Assert.assertEquals("6059241", output.regionId)

        data = data.buildUpon().appendQueryParameter("sort", "deals").build()
        output = parser.parseDeepLink(data) as HotelDeepLink
        Assert.assertTrue(output is HotelDeepLink)
        Assert.assertEquals(LocalDate(2017, 5, 31), output.checkInDate)
        Assert.assertEquals(LocalDate(2017, 6, 6), output.checkOutDate)
        Assert.assertEquals("6059241", output.regionId)
        Assert.assertEquals("deals", output.sortType)

        data = data.buildUpon().appendQueryParameter("adults", "2").build()
        output = parser.parseDeepLink(data) as HotelDeepLink
        Assert.assertTrue(output is HotelDeepLink)
        Assert.assertEquals(LocalDate(2017, 5, 31), output.checkInDate)
        Assert.assertEquals(LocalDate(2017, 6, 6), output.checkOutDate)
        Assert.assertEquals("6059241", output.regionId)
        Assert.assertEquals("deals", output.sortType)
        Assert.assertEquals(2, output.numAdults)

    }

    @Test
    fun flightUniversalLinkParsing() {
        var data = Uri.parse("https://www.expedia.com/mobile/deeplink/Flights-Search")
        var output = parser.parseDeepLink(data)
        Assert.assertTrue(output is FlightDeepLink)

        data = data.buildUpon().appendQueryParameter("leg1", "from:Seattle, WA (SEA-Seattle - Tacoma Intl.),to:BKK,departure:09%2F27%2F2017TANYT").build()
        output = parser.parseDeepLink(data) as FlightDeepLink
        Assert.assertTrue(output is FlightDeepLink)
        Assert.assertEquals("SEA", output.origin)
        Assert.assertEquals("BKK", output.destination)
        Assert.assertEquals(LocalDate(2017, 9, 27), output.departureDate)

        data = data.buildUpon().appendQueryParameter("leg2", "from:BKK,to:Seattle, WA (SEA-Seattle - Tacoma Intl.),departure:10/11/2017TANYT").build()
        output = parser.parseDeepLink(data) as FlightDeepLink
        Assert.assertTrue(output is FlightDeepLink)
        Assert.assertEquals("SEA", output.origin)
        Assert.assertEquals("BKK", output.destination)
        Assert.assertEquals(LocalDate(2017, 9, 27), output.departureDate)
        Assert.assertEquals(LocalDate(2017, 10, 11), output.returnDate)

        data = data.buildUpon().appendQueryParameter("passengers", "children:2[8;10],adults:2,seniors:0,infantinlap:Y").build()
        output = parser.parseDeepLink(data) as FlightDeepLink
        Assert.assertTrue(output is FlightDeepLink)
        Assert.assertEquals("SEA", output.origin)
        Assert.assertEquals("BKK", output.destination)
        Assert.assertEquals(LocalDate(2017, 9, 27), output.departureDate)
        Assert.assertEquals(LocalDate(2017, 10, 11), output.returnDate)
        Assert.assertEquals(2, output.numAdults)
    }

    @Test
    fun carUniversalLinkParsing() {
        var data = Uri.parse("https://www.expedia.com/mobile/deeplink/carsearch")
        var output = parser.parseDeepLink(data)
        Assert.assertTrue(output is CarDeepLink)

        data = data.buildUpon().appendQueryParameter("date1", "09%2F26%2F2017").build()
        output = parser.parseDeepLink(data) as CarDeepLink
        Assert.assertEquals(null, output.pickupDateTime)

        data = data.buildUpon().appendQueryParameter("time1", "700PM").build()
        output = parser.parseDeepLink(data) as CarDeepLink
        Assert.assertEquals(DateTime(2017, 9, 26, 19, 0), output.pickupDateTime)

        data = data.buildUpon().appendQueryParameter("date2", "10%2F11%2F2017").build()
        output = parser.parseDeepLink(data) as CarDeepLink
        Assert.assertEquals(DateTime(2017, 9, 26, 19, 0), output.pickupDateTime)
        Assert.assertEquals(null, output.dropoffDateTime)

        data = data.buildUpon().appendQueryParameter("time2", "500PM").build()
        output = parser.parseDeepLink(data) as CarDeepLink
        Assert.assertEquals(DateTime(2017, 9, 26, 19, 0), output.pickupDateTime)
        Assert.assertEquals(DateTime(2017, 10, 11, 17, 0), output.dropoffDateTime)

        data = data.buildUpon().appendQueryParameter("locn", "Bangkok, Thailand (BKK-All Airports)").build()
        output = parser.parseDeepLink(data) as CarDeepLink
        Assert.assertEquals(DateTime(2017, 9, 26, 19, 0), output.pickupDateTime)
        Assert.assertEquals(DateTime(2017, 10, 11, 17, 0), output.dropoffDateTime)
        Assert.assertEquals("BKK", output.pickupLocation)
    }

    @Test
    fun activityUniversalLinkParsing() {
        var data = Uri.parse("https://www.expedia.com/mobile/deeplink/things-to-do/search")
        var output = parser.parseDeepLink(data)
        Assert.assertTrue(output is ActivityDeepLink)

        data = data.buildUpon().appendQueryParameter("startDate", "09%2F26%2F2017").build()
        output = parser.parseDeepLink(data) as ActivityDeepLink
        Assert.assertEquals(LocalDate(2017, 9, 26), output.startDate)

        data = data.buildUpon().appendQueryParameter("location", "Bangkok (and vicinity), Thailand").build()
        output = parser.parseDeepLink(data) as ActivityDeepLink
        Assert.assertEquals(LocalDate(2017, 9, 26), output.startDate)
        Assert.assertEquals("Bangkok (and vicinity)", output.location)

        data = data.buildUpon().appendQueryParameter("categories", "Nightlife|Cruises%20%26%20Water%20Tours").build()
        output = parser.parseDeepLink(data) as ActivityDeepLink
        Assert.assertEquals(LocalDate(2017, 9, 26), output.startDate)
        Assert.assertEquals("Bangkok (and vicinity)", output.location)
        Assert.assertEquals("Nightlife|Cruises%20%26%20Water%20Tours", output.filters)
    }

    @Test
    fun homeUniversalLinkParsing() {
        var data = Uri.parse("https://www.expedia.com/mobile/deeplink")
        var output = parser.parseDeepLink(data)
        Assert.assertTrue(output is HomeDeepLink)

        data = Uri.parse("http://www.expedia.com/mobile/deeplink")
        output = parser.parseDeepLink(data)
        Assert.assertTrue(output is HomeDeepLink)
    }

    @Test
    fun signInUniversalLinkParsing() {
        val data = Uri.parse("https://www.expedia.com/mobile/deeplink/user/signin")
        val output = parser.parseDeepLink(data)
        Assert.assertTrue(output is SignInDeepLink)
    }

    @Test
    fun tripUniversalLinkParsing() {
        val data = Uri.parse("https://www.expedia.com/mobile/deeplink/trips")
        val output = parser.parseDeepLink(data)
        Assert.assertTrue(output is TripDeepLink)
    }

    @Test
    fun unknownUniversalLinkParsing() {
        var data = Uri.parse("https://www.expedia.com/mobile/deeplink/user/wrongHost")
        var output = parser.parseDeepLink(data)
        Assert.assertTrue(output is HomeDeepLink)

        data = Uri.parse("https://www.expedia.com/wrongpath/user/wrongHost")
        output = parser.parseDeepLink(data)
        Assert.assertTrue(output is HomeDeepLink)
    }

    @Test
    fun sharedItineraryDeepLinkParsing() {
        var data = Uri.parse("https://www.expedia.com/m/trips/shared/0y5Ht7LVY1gqSwdrngvC0MCAdQKn-QHMK5hNDlKKtt6jwSkXTR2TnYs9xISPHASFzitz_Tty083fguArrsJbxx6j")
        var output = parser.parseDeepLink(data)
        Assert.assertTrue(output is SharedItineraryDeepLink)

        output = output as SharedItineraryDeepLink
        Assert.assertEquals("https://www.expedia.com/m/trips/shared/0y5Ht7LVY1gqSwdrngvC0MCAdQKn-QHMK5hNDlKKtt6jwSkXTR2TnYs9xISPHASFzitz_Tty083fguArrsJbxx6j", output.url)
    }

    @Test
    fun shortUrlUniversalLinkParsing() {
        val data = Uri.parse("http://e.xpda.co")
        var output = parser.parseDeepLink(data)
        Assert.assertTrue(output is ShortUrlDeepLink)
        output = output as ShortUrlDeepLink
        Assert.assertEquals("http://e.xpda.co", output.shortUrl)
    }
}