package com.expedia.bookings.itin.hotel.pricingRewards

import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.R
import com.expedia.bookings.itin.common.ItinAdditionalInfoItem
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockHotelRepo
import com.expedia.bookings.itin.helpers.MockLifecycleOwner
import com.expedia.bookings.itin.helpers.MockStringProvider
import com.expedia.bookings.itin.hotel.repositories.ItinHotelRepoInterface
import com.expedia.bookings.itin.scopes.HasHotelRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.tripstore.extensions.firstHotel
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.services.TestObserver
import org.junit.Test
import kotlin.test.assertEquals

class HotelItinPricingAdditionalInfoViewModelTest {
    private val hotel = ItinMocker.hotelDetailsHappy.firstHotel()!!

    @Test
    fun testToolbarTitleSubject() {
        val testObserver = TestObserver<String>()
        val viewModel = HotelItinPricingAdditionalInfoViewModel(MockAdditionalInfoScope())
        viewModel.toolbarTitleSubject.subscribe(testObserver)

        testObserver.assertEmpty()
        viewModel.hotelObserver.onChanged(hotel)
        testObserver.assertValueCount(1)
        assertEquals((R.string.itin_hotel_details_price_summary_additional_info_button_text).toString(), testObserver.values()[0])

        testObserver.dispose()
    }

    @Test
    fun testAdditionInfoItemSubject() {
        val testObserver = TestObserver<List<ItinAdditionalInfoItem>>()
        val viewModel = HotelItinPricingAdditionalInfoViewModel(MockAdditionalInfoScope())
        viewModel.additionalInfoItemSubject.subscribe(testObserver)

        testObserver.assertEmpty()
        viewModel.hotelObserver.onChanged(hotel)
        testObserver.assertValueCount(1)
        val items = testObserver.values()[0]
        val item1 = items[0]
        val item2 = items[1]
        val item3 = items[2]

        assertEquals((R.string.itin_hotel_details_price_summary_additional_info_additional_hotel_fees).toString(), item1.heading)
        assertEquals((R.string.itin_hotel_details_price_summary_additional_info_taxes_and_fees).toString(), item2.heading)
        assertEquals((R.string.itin_hotel_details_price_summary_additional_info_guest_charges_room_capacity).toString(), item3.heading)

        val expectedHotelFees = StringBuilder()
        expectedHotelFees.append("The below fees and deposits only apply if they are not included in your selected room rate.")
        expectedHotelFees.append("The price above DOES NOT include any applicable hotel service fees, charges for optional incidentals (such as minibar snacks or telephone calls), or regulatory surcharges. The hotel will assess these fees, charges, and surcharges upon check-out.")
        expectedHotelFees.append("The following fees and deposits are charged by the property at time of service, check-in, or check-out.<ul>                <li>An in-room refrigerator is available for an additional fee</li><li>Rollaway beds are available for an additional fee</li>  </ul>")
        expectedHotelFees.append("The above list may not be comprehensive. Fees and deposits may not include tax and are subject to change.")
        expectedHotelFees.append("<p>You'll be asked to pay the following charges at the property:</p>")
        expectedHotelFees.append("<ul><li>Breakage deposit: USD 250.00 per stay</li></ul>")
        expectedHotelFees.append("<p>We have included all charges provided to us by the property. However, charges can vary, for example, based on length of stay or the room you book. </p>")
        assertEquals(expectedHotelFees.toString(), item1.content)

        val expectedTaxesAndFees = "What are the taxes and service fees? The taxes are tax recovery charges Expedia pays to its vendor (e.g. hotels); for details, please see our Terms of Use. We retain our service fees as compensation in servicing your travel reservation."
        assertEquals(expectedTaxesAndFees, item2.content)

        val expectedGuestCharges = StringBuilder()
        expectedGuestCharges.append("Base rate is for 2 guests.")
        expectedGuestCharges.append("If rates are listed below, they may be quoted in the currency of the country where the property is located.")
        expectedGuestCharges.append("This property considers guests of any age to be an adult.")
        expectedGuestCharges.append("Availability of accommodation in the same property for extra guests is not guaranteed.")
        assertEquals(expectedGuestCharges.toString(), item3.content)

        testObserver.dispose()
    }

    class MockAdditionalInfoScope : HasStringProvider, HasLifecycleOwner, HasHotelRepo {
        override val strings: StringSource = MockStringProvider()
        override val itinHotelRepo: ItinHotelRepoInterface = MockHotelRepo()
        override val lifecycleOwner: LifecycleOwner = MockLifecycleOwner()
    }
}
