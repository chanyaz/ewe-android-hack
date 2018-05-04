package com.expedia.bookings.itin.hotel.taxi

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockHotelRepo
import com.expedia.bookings.itin.helpers.MockLifecycleOwner
import com.expedia.bookings.itin.hotel.repositories.ItinHotelRepoInterface
import com.expedia.bookings.itin.scopes.HasHotelRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.tripstore.extensions.firstHotel
import com.expedia.bookings.services.TestObserver
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HotelItinTaxiViewModelTest {
    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    val localizedAddressTestObserver = TestObserver<String>()
    val nonLocalizedAddressTestObserver = TestObserver<String>()
    val localizedLocationNameTestObserver = TestObserver<String>()
    val nonLocalizedLocationNameTestObserver = TestObserver<String>()
    val happyHotel = ItinMocker.hotelDetailsHappy.firstHotel()
    val badHotel = ItinMocker.hotelDetailsNoPriceDetails.firstHotel()

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

    private class TestScope : HasHotelRepo, HasLifecycleOwner {
        val repo = MockHotelRepo()
        val owner = MockLifecycleOwner()
        override val itinHotelRepo: ItinHotelRepoInterface = repo
        override val lifecycleOwner: LifecycleOwner = owner
    }
}
