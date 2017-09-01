package com.expedia.bookings.deeplink

import android.net.Uri
import com.expedia.bookings.data.ChildTraveler
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.util.ArrayList

@RunWith(RobolectricRunner::class)
class UniversalDeepLinkParserTest {

    val parser = UniversalDeepLinkParser(RuntimeEnvironment.application.assets)

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
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
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun hotelInfoSiteUniversalLinkParsing() {
        var data = Uri.parse("https://www.expedia.com/mobile/deeplink/Las-Vegas-Hotels-TI-Treasure-Island-Hotel-And-Casino.h15930.Hotel-Information")
        var output = parser.parseDeepLink(data)
        Assert.assertTrue(output is HotelDeepLink)

        data = data.buildUpon().appendQueryParameter("chkin", "06/18/2017").build()
        output = parser.parseDeepLink(data) as HotelDeepLink
        Assert.assertEquals("15930", output.hotelId)
        Assert.assertEquals(LocalDate(2017, 6, 18), output.checkInDate)

        data = data.buildUpon().appendQueryParameter("chkout", "06/27/2017").build()
        output = parser.parseDeepLink(data) as HotelDeepLink
        Assert.assertEquals("15930", output.hotelId)
        Assert.assertEquals(LocalDate(2017, 6, 18), output.checkInDate)
        Assert.assertEquals(LocalDate(2017, 6, 27), output.checkOutDate)

        data = data.buildUpon().appendQueryParameter("rm1", "a2:c2:c7:c8").build()
        val children = arrayOf(ChildTraveler(2, false), ChildTraveler(7, false), ChildTraveler(8, false))
        output = parser.parseDeepLink(data) as HotelDeepLink
        Assert.assertEquals("15930", output.hotelId)
        Assert.assertEquals(LocalDate(2017, 6, 18), output.checkInDate)
        Assert.assertEquals(LocalDate(2017, 6, 27), output.checkOutDate)
        Assert.assertEquals(2, output.numAdults)
        assertChildTravelersEquals(children, (output.children as ArrayList).toTypedArray())
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
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

        val data_c1 = data.buildUpon().appendQueryParameter("passengers", "children:2[8;10],adults:2,seniors:0,infantinlap:Y").build()
        output = parser.parseDeepLink(data_c1) as FlightDeepLink
        Assert.assertTrue(output is FlightDeepLink)
        Assert.assertEquals("SEA", output.origin)
        Assert.assertEquals("BKK", output.destination)
        Assert.assertEquals(LocalDate(2017, 9, 27), output.departureDate)
        Assert.assertEquals(LocalDate(2017, 10, 11), output.returnDate)
        Assert.assertEquals(2, output.numAdults)

        val data_c2 = data.buildUpon().appendQueryParameter("passengers", "adults:5").build()
        output = parser.parseDeepLink(data_c2) as FlightDeepLink
        Assert.assertEquals(5, output.numAdults)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
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
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
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
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun homeUniversalLinkParsing() {
        var data = Uri.parse("https://www.expedia.com/mobile/deeplink")
        var output = parser.parseDeepLink(data)
        Assert.assertTrue(output is HomeDeepLink)

        data = Uri.parse("http://www.expedia.com/mobile/deeplink")
        output = parser.parseDeepLink(data)
        Assert.assertTrue(output is HomeDeepLink)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun memberPricingUniversalLinkParsing() {
        val data = Uri.parse("https://www.expedia.com/mobile/deeplink/member-pricing")
        val output = parser.parseDeepLink(data)
        Assert.assertTrue(output is MemberPricingDeepLink)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun signInUniversalLinkParsing() {
        val data = Uri.parse("https://www.expedia.com/mobile/deeplink/user/signin")
        val output = parser.parseDeepLink(data)
        Assert.assertTrue(output is SignInDeepLink)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun tripUniversalLinkParsing() {
        var data = Uri.parse("https://www.expedia.com/mobile/deeplink/trips")
        var output = parser.parseDeepLink(data)
        Assert.assertTrue(output is TripDeepLink)

        data = Uri.parse("https://www.expedia.com/mobile/deeplink/trips/7238447666975")
        output = parser.parseDeepLink(data) as TripDeepLink
        Assert.assertTrue(output is TripDeepLink)
        Assert.assertEquals("7238447666975", output.itinNum)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun unknownUniversalLinkParsing() {
        var data = Uri.parse("https://www.expedia.com/mobile/deeplink/user/wrongHost")
        var output = parser.parseDeepLink(data)
        Assert.assertTrue(output is HomeDeepLink)

        data = Uri.parse("https://www.expedia.com/wrongpath/user/wrongHost")
        output = parser.parseDeepLink(data)
        Assert.assertTrue(output is HomeDeepLink)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun sharedItineraryDeepLinkParsing() {
        val data = Uri.parse("https://www.expedia.com/m/trips/shared/0y5Ht7LVY1gqSwdrngvC0MCAdQKn-QHMK5hNDlKKtt6jwSkXTR2TnYs9xISPHASFzitz_Tty083fguArrsJbxx6j")
        var output = parser.parseDeepLink(data)
        Assert.assertTrue(output is SharedItineraryDeepLink)

        output = output as SharedItineraryDeepLink
        Assert.assertEquals("https://www.expedia.com/m/trips/shared/0y5Ht7LVY1gqSwdrngvC0MCAdQKn-QHMK5hNDlKKtt6jwSkXTR2TnYs9xISPHASFzitz_Tty083fguArrsJbxx6j", output.url)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun shortUrlUniversalLinkParsing() {
        val data = Uri.parse("http://e.xpda.co")
        var output = parser.parseDeepLink(data)
        Assert.assertTrue(output is ShortUrlDeepLink)
        output = output as ShortUrlDeepLink
        Assert.assertEquals("http://e.xpda.co", output.shortUrl)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun pointOfSaleDateFormatParsing() {
        val data = Uri.parse("https://www.expedia.co.kr/mobile/deeplink/Hotel-Search?hotelId=12539&startDate=2017.05.24&endDate=2017.05.31")
        val parsed = parser.parseUniversalDeepLink(data) as HotelDeepLink
        Assert.assertEquals(LocalDate(2017,5,24), parsed.checkInDate)
        Assert.assertEquals(LocalDate(2017,5,31), parsed.checkOutDate)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun mctcSearchParsing() {
        val data = Uri.parse("https://www.expedia.com/mobile/deeplink/Hotel-Search?paandi=true&trv_tax=18.66&trv_di=B&ICMDTL=htl.1808319.taid.678590.geoid.187147.testslice..clickid.WLhB8goQHIEAAaxKd8cAAADk.AUDID.10532&trv_curr=USD&ICMCID=Meta.tripa.Expedia_US-DM&SC=2&mctc=9&trv_mbl=L&trv_bp=151.82&startDate=4%2F26%2F2017&endDate=4%2F30%2F2017&adults=2&selected=1808319&regionId=179898")
        val parsed = parser.parseUniversalDeepLink(data) as HotelDeepLink
        Assert.assertEquals(9, parsed.mctc)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun mctcOfferParsing() {
        val data = Uri.parse("https://www.expedia.com/mobile/deeplink/Paris-Hotels-Hotel-Wilson-Opera.h1808319.Hotel-Information?langid=1033&mctc=5&chid=5bb6a8b1-86c6-4340-9ba1-fd3ef6906566&mrp=1&mdpcid=US.META.TRIVAGO.HOTELSCORESEARCH.HOTEL&mdpdtl=HTL.1808319.PARIS&trv_curr=USD&chkin=4/26/2017&chkout=4/30/2017&rateplanid=200803984_200803984_24&trv_dp=147&rm1=a2&paandi=true")
        val parsed = parser.parseUniversalDeepLink(data) as HotelDeepLink
        Assert.assertEquals(5, parsed.mctc)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun mctcSearchSelectedHotel() {
        val data = Uri.parse("https://www.expedia.com/mobile/deeplink/Hotel-Search?&selected=1808319")
        val parsed = parser.parseUniversalDeepLink(data) as HotelDeepLink
        Assert.assertEquals("1808319", parsed.selectedHotelId)
    }

    private fun assertChildTravelersEquals(childrenExpected: Array<ChildTraveler>, childrenActual: Array<ChildTraveler>) {
        Assert.assertEquals(childrenExpected.size, childrenActual.size)
        for (i in childrenExpected.indices) {
            Assert.assertEquals(childrenExpected[i].age, childrenActual[i].age)
            Assert.assertEquals(childrenExpected[i].usingSeat(), childrenActual[i].usingSeat())
        }
    }
}
