package packages

import com.expedia.bookings.data.Money
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.packages.MultiItemCreateTripParams
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.data.packages.PackageSearchParams
import org.joda.time.LocalDate
import org.junit.Test
import kotlin.test.assertEquals

class MultiItemCreateTripParamsTest {

    @Test(expected = IllegalArgumentException::class)
    fun testExceptionThrownForMissingFlightPIID() {
        val params = getMIDPackageSearchParams()
        params.latestSelectedOfferInfo.flightPIID = null
        MultiItemCreateTripParams.fromPackageSearchParams(params)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testExceptionThrownForMissingHotelId() {
        val params = getMIDPackageSearchParams()
        params.latestSelectedOfferInfo.hotelId = null
        MultiItemCreateTripParams.fromPackageSearchParams(params)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testExceptionThrownForMissingInventoryType() {
        val params = getMIDPackageSearchParams()
        params.latestSelectedOfferInfo.inventoryType = null
        MultiItemCreateTripParams.fromPackageSearchParams(params)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testExceptionThrownForMissingRatePlanCode() {
        val params = getMIDPackageSearchParams()
        params.latestSelectedOfferInfo.ratePlanCode = null
        MultiItemCreateTripParams.fromPackageSearchParams(params)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testExceptionThrownForMissingRoomTypeCode() {
        val params = getMIDPackageSearchParams()
        params.latestSelectedOfferInfo.roomTypeCode = null
        MultiItemCreateTripParams.fromPackageSearchParams(params)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testExceptionThrownForMissingProductOfferPrice() {
        val params = getMIDPackageSearchParams()
        params.latestSelectedOfferInfo.productOfferPrice = null
        MultiItemCreateTripParams.fromPackageSearchParams(params)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testExceptionThrownForMissingHotelCheckInDate() {
        val params = getMIDPackageSearchParams()
        params.latestSelectedOfferInfo.hotelCheckInDate = null
        MultiItemCreateTripParams.fromPackageSearchParams(params)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testExceptionThrownForMissingHotelCheckOutDate() {
        val params = getMIDPackageSearchParams()
        params.latestSelectedOfferInfo.hotelCheckOutDate = null
        MultiItemCreateTripParams.fromPackageSearchParams(params)
    }

    @Test
    fun testValidCreateTripParams() {
        val params = getMIDPackageSearchParams()
        val multiItemCreateTripParams = MultiItemCreateTripParams.fromPackageSearchParams(params)
        assertEquals("mid_create_trip", multiItemCreateTripParams.flightPIID)
        assertEquals("AA", multiItemCreateTripParams.inventoryType)
        assertEquals("AAA", multiItemCreateTripParams.ratePlanCode)
        assertEquals("AA", multiItemCreateTripParams.roomTypeCode)
        assertEquals("2017-12-07", multiItemCreateTripParams.startDate)
        assertEquals("2017-12-08", multiItemCreateTripParams.endDate)
        assertEquals("1", multiItemCreateTripParams.adultsQueryParam)
        assertEquals("0", multiItemCreateTripParams.childAges)
    }

    private fun getMIDPackageSearchParams(): PackageSearchParams {
        val packageParams = PackageSearchParams.Builder(maxRange = 1, maxStay = 1)
                .startDate(LocalDate.parse("2017-12-07"))
                .endDate(LocalDate.parse("2017-12-08"))
                .destination(SuggestionV4())
                .origin(SuggestionV4())
                .children(listOf(0))
                .adults(1)
                .build() as PackageSearchParams
        packageParams.latestSelectedOfferInfo.hotelId = "1111"
        packageParams.latestSelectedOfferInfo.flightPIID = "mid_create_trip"
        packageParams.latestSelectedOfferInfo.inventoryType = "AA"
        packageParams.latestSelectedOfferInfo.ratePlanCode = "AAA"
        packageParams.latestSelectedOfferInfo.roomTypeCode = "AA"
        packageParams.latestSelectedOfferInfo.hotelCheckInDate = "2017-12-07"
        packageParams.latestSelectedOfferInfo.hotelCheckOutDate = "2017-12-08"
        packageParams.latestSelectedOfferInfo.productOfferPrice = PackageOfferModel.PackagePrice()
        packageParams.latestSelectedOfferInfo.productOfferPrice?.packageTotalPrice = Money(100, "USD")
        return packageParams
    }
}
