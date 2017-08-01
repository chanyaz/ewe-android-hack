package com.expedia.bookings.test

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.Ui
import com.expedia.ui.LOBWebViewActivity
import com.expedia.vm.packages.PackageConfirmationViewModel
import org.joda.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowApplication
import com.expedia.bookings.services.TestObserver
import java.util.ArrayList
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
        activity.setTheme(R.style.Theme_Hotels_Default)
        Ui.getApplication(activity).defaultHotelComponents()
        shadowApplication = ShadowApplication.getInstance()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun pkgLoyaltyPoints(){
        UserLoginTestUtil.setupUserAndMockLogin(UserLoginTestUtil.mockUser())
        val expediaPointsSubscriber = TestObserver<String>()
        val userPoints = "100"
        vm = PackageConfirmationViewModel(activity)
        vm.rewardPointsObservable.subscribe(expediaPointsSubscriber)
        vm.setRewardsPoints.onNext(userPoints)

        expediaPointsSubscriber.assertValue("$userPoints Expedia+ Points")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun zeroPkgLoyaltyPoints(){
        UserLoginTestUtil.setupUserAndMockLogin(UserLoginTestUtil.mockUser())
        val expediaPointsSubscriber = TestObserver<String>()
        val userPoints = "0"
        vm = PackageConfirmationViewModel(activity)
        vm.rewardPointsObservable.subscribe(expediaPointsSubscriber)
        vm.setRewardsPoints.onNext(userPoints)

        expediaPointsSubscriber.assertValueCount(0)
    }

    @Test
    fun nullPkgLoyaltyPoints(){
        UserLoginTestUtil.setupUserAndMockLogin(UserLoginTestUtil.mockUser())
        val expediaPointsSubscriber = TestObserver<String>()
        val userPoints = null
        vm = PackageConfirmationViewModel(activity)
        vm.rewardPointsObservable.subscribe(expediaPointsSubscriber)
        vm.setRewardsPoints.onNext(userPoints)

        expediaPointsSubscriber.assertValueCount(0)
    }

    @Test
    fun noShowPkgLoyaltyPoints(){
        UserLoginTestUtil.setupUserAndMockLogin(UserLoginTestUtil.mockUser())
        val expediaPointsSubscriber = TestObserver<String>()
        val userPoints = "100"
        vm = PackageConfirmationViewModel(activity)
        //adding test POS configuration without rewards enabled
        PointOfSaleTestConfiguration.configurePointOfSale(activity, "MockSharedData/pos_with_show_rewards_false.json", false)
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
        val vm = PackageConfirmationViewModel(activity)
        val params = PackageSearchParams(origin, destination, checkInDate, checkOutDate, 1, ArrayList<Int>(), false)
        Db.setPackageParams(params)

        val leg = FlightLeg()
        leg.destinationAirportCode = "SEA"
        leg.destinationAirportLocalName = "Tacoma Intl."
        Db.setPackageFlightBundle(leg, FlightLeg())

        vm.searchForCarRentalsForTripObserver(activity).onNext(null)
        val intent = shadowApplication!!.nextStartedActivity
        val intentUrl = intent.getStringExtra("ARG_URL")

        assertEquals(LOBWebViewActivity::class.java.name, intent.component.className)
        assertTrue(intentUrl.startsWith(PointOfSale.getPointOfSale().carsTabWebViewURL))
    }
}
