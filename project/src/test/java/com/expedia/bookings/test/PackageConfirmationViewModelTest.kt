package com.expedia.bookings.test

import android.app.Activity
import java.util.ArrayList

import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowApplication
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.User
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.featureconfig.IProductFlavorFeatureConfiguration
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.Ui
import com.expedia.ui.CarActivity
import com.expedia.vm.packages.PackageConfirmationViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import rx.observers.TestSubscriber
import kotlin.properties.Delegates

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))

class PackageConfirmationViewModelTest {

    private var vm: PackageConfirmationViewModel by Delegates.notNull()
    private var shadowApplication: ShadowApplication? = null
    private var activity: Activity by Delegates.notNull()


    @Before
    fun before() {
        activity = Robolectric.buildActivity(AppCompatActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Hotels)
        Ui.getApplication(activity).defaultHotelComponents()
        shadowApplication = Shadows.shadowOf(activity).shadowApplication
    }

    @Test
    fun pkgLoyaltyPoints(){
        UserLoginTestUtil.Companion.setupUserAndMockLogin(UserLoginTestUtil.Companion.mockUser())
        assertTrue(User.isLoggedIn(activity))
        val expediaPointsSubscriber = TestSubscriber<String>()
        val userPoints = "100"
        vm = PackageConfirmationViewModel(activity)
        vm.rewardPointsObservable.subscribe(expediaPointsSubscriber)
        vm.setRewardsPoints.onNext(userPoints)

        expediaPointsSubscriber.assertValue("$userPoints Expedia+ Points")
    }

    @Test
    fun zeroPkgLoyaltyPoints(){
        UserLoginTestUtil.Companion.setupUserAndMockLogin(UserLoginTestUtil.Companion.mockUser())
        assertTrue(User.isLoggedIn(activity))
        val expediaPointsSubscriber = TestSubscriber<String>()
        val userPoints = "0"
        vm = PackageConfirmationViewModel(activity)
        vm.rewardPointsObservable.subscribe(expediaPointsSubscriber)
        vm.setRewardsPoints.onNext(userPoints)

        expediaPointsSubscriber.assertValueCount(0)
    }

    @Test
    fun nullPkgLoyaltyPoints(){
        UserLoginTestUtil.Companion.setupUserAndMockLogin(UserLoginTestUtil.Companion.mockUser())
        assertTrue(User.isLoggedIn(activity))
        val expediaPointsSubscriber = TestSubscriber<String>()
        val userPoints = null
        vm = PackageConfirmationViewModel(activity)
        vm.rewardPointsObservable.subscribe(expediaPointsSubscriber)
        vm.setRewardsPoints.onNext(userPoints)

        expediaPointsSubscriber.assertValueCount(0)
    }

    @Test
    fun  addCarToBookingHappyCase() {
        val origin = Mockito.mock(SuggestionV4::class.java)
        val destination = Mockito.mock(SuggestionV4::class.java)
        val checkInDate = LocalDate()
        val checkOutDate = LocalDate()
        var vm = PackageConfirmationViewModel(activity)
        val params = PackageSearchParams(origin, destination, checkInDate, checkOutDate, 1, ArrayList<Int>(), false)
        Db.setPackageParams(params)

        val leg = FlightLeg()
        leg.destinationAirportCode = "SEA"
        leg.destinationAirportLocalName = "Tacoma Intl."
        Db.setPackageFlightBundle(leg, FlightLeg())

        vm.searchForCarRentalsForTripObserver(activity).onNext(null)
        val intent = shadowApplication!!.nextStartedActivity

        assertEquals(CarActivity::class.java.name, intent.component.className)
        assertTrue(intent.getBooleanExtra(Codes.EXTRA_OPEN_SEARCH, false))
    }
}
