package com.expedia.bookings.itin.hotel.pricingRewards

import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.itin.common.ItinRepoInterface
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockItinRepo
import com.expedia.bookings.itin.helpers.MockLifecycleOwner
import com.expedia.bookings.itin.helpers.MockStringProvider
import com.expedia.bookings.itin.scopes.HasItinRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.services.TestObserver
import org.junit.Before
import org.junit.Test

class HotelItinPricingRewardsToolbarViewModelTest {

    private val toolbarTitleTestObserver = TestObserver<String>()
    private val toolbarSubTitleTestObserver = TestObserver<String>()
    val hotelName: String = "Crest Hotel"
    lateinit var sut: HotelItinPricingRewardsToolbarViewModel<MockPricingRewardsToolbarScope>

    @Before
    fun setup() {
        sut = HotelItinPricingRewardsToolbarViewModel(MockPricingRewardsToolbarScope())
    }

    @Test
    fun updateItinTest() {
        val hotel = ItinMocker.hotelDetailsHappy
        sut.toolbarSubTitleSubject.subscribe(toolbarSubTitleTestObserver)
        sut.toolbarTitleSubject.subscribe(toolbarTitleTestObserver)

        toolbarSubTitleTestObserver.assertNoValues()
        toolbarTitleTestObserver.assertNoValues()
        sut.observer.onChanged(hotel)
        toolbarSubTitleTestObserver.assertValue(hotelName)
        toolbarTitleTestObserver.assertValueCount(1)
    }

    class MockPricingRewardsToolbarScope : HasLifecycleOwner, HasStringProvider, HasItinRepo {
        override val strings: StringSource = MockStringProvider()
        override val itinRepo: ItinRepoInterface = MockItinRepo()
        override val lifecycleOwner: LifecycleOwner = MockLifecycleOwner()
    }
}
