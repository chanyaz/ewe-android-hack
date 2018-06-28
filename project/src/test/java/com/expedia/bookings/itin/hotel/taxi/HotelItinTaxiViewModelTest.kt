package com.expedia.bookings.itin.hotel.taxi

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.itin.common.ItinRepoInterface
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockItinRepo
import com.expedia.bookings.itin.helpers.MockLifecycleOwner
import com.expedia.bookings.itin.scopes.HasItinRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.services.TestObserver
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HotelItinTaxiViewModelTest {
    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private val localizedAddressTestObserver = TestObserver<String>()
    private val nonLocalizedAddressTestObserver = TestObserver<String>()
    private val localizedLocationNameTestObserver = TestObserver<String>()
    private val nonLocalizedLocationNameTestObserver = TestObserver<String>()
    private val happyHotel = ItinMocker.hotelDetailsHappy
    private val badHotel = ItinMocker.hotelDetailsNoPriceDetails

    private lateinit var sut: HotelItinTaxiViewModel<TestScope>

    @Before
    fun setup() {
        sut = HotelItinTaxiViewModel(TestScope())
        sut.nonLocalizedLocationNameSubject.subscribe(nonLocalizedLocationNameTestObserver)
        sut.nonLocalizedAddressSubject.subscribe(nonLocalizedAddressTestObserver)
        sut.localizedLocationNameSubject.subscribe(localizedLocationNameTestObserver)
        sut.localizedAddressSubject.subscribe(localizedAddressTestObserver)
    }

    @Test
    fun happyTest() {
        localizedAddressTestObserver.assertNoValues()
        nonLocalizedAddressTestObserver.assertNoValues()
        localizedLocationNameTestObserver.assertNoValues()
        nonLocalizedLocationNameTestObserver.assertNoValues()

        sut.observer.onChanged(happyHotel)

        localizedAddressTestObserver.assertValue("123 Some St, SomeCity, CA 94104")
        nonLocalizedAddressTestObserver.assertValue("12, Achaiah Shetty Layout, Aramane Nagar, Bengaluru, Karnataka, 560080 India")
        localizedLocationNameTestObserver.assertValue("Hotel California")
        nonLocalizedLocationNameTestObserver.assertValue("Crest Hotel")
    }

    @Test
    fun invalidHotel() {
        localizedAddressTestObserver.assertNoValues()
        nonLocalizedAddressTestObserver.assertNoValues()
        localizedLocationNameTestObserver.assertNoValues()
        nonLocalizedLocationNameTestObserver.assertNoValues()

        sut.observer.onChanged(badHotel)

        localizedAddressTestObserver.assertNoValues()
        nonLocalizedAddressTestObserver.assertNoValues()
        localizedLocationNameTestObserver.assertNoValues()
        nonLocalizedLocationNameTestObserver.assertNoValues()
    }

    private class TestScope : HasItinRepo, HasLifecycleOwner {
        val repo = MockItinRepo()
        val owner = MockLifecycleOwner()
        override val itinRepo: ItinRepoInterface = repo
        override val lifecycleOwner: LifecycleOwner = owner
    }
}
