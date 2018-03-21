package com.expedia.bookings.itin.hotel.pricingRewards

import android.arch.lifecycle.LifecycleOwner
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
import org.junit.Before
import org.junit.Test

class HotelItinPricingRewardsToolbarViewModelTest {

    private val toolbarTitleTestObserver = TestObserver<String>()
    private val toolbarSubTitleTestObserver = TestObserver<String>()
    val hotelName: String = "Orchard Hotel"
    lateinit var sut: HotelItinPricingRewardsToolbarViewModel<MockPricingRewardsToolbarScope>

    @Before
    fun setup() {
        sut = HotelItinPricingRewardsToolbarViewModel(MockPricingRewardsToolbarScope())
    }

    @Test
    fun updateItinTest() {
        val hotel = ItinMocker.hotelDetailsNoPriceDetails.firstHotel()!!
        sut.toolbarSubTitleSubject.subscribe(toolbarSubTitleTestObserver)
        sut.toolbarTitleSubject.subscribe(toolbarTitleTestObserver)

        toolbarSubTitleTestObserver.assertNoValues()
        toolbarTitleTestObserver.assertNoValues()
        sut.observer.onChanged(hotel)
        toolbarSubTitleTestObserver.assertValue(hotelName)
        toolbarTitleTestObserver.assertValueCount(1)
    }

    class MockPricingRewardsToolbarScope : HasLifecycleOwner, HasStringProvider, HasHotelRepo {
        override val strings: StringSource = MockStringProvider()
        override val itinHotelRepo: ItinHotelRepoInterface = MockHotelRepo()
        override val lifecycleOwner: LifecycleOwner = MockLifecycleOwner()
    }
}
