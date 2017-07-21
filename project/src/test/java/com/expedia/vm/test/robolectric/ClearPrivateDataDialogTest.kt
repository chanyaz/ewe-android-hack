package com.expedia.vm.test.robolectric

import com.expedia.bookings.R
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.data.user.User
import com.expedia.bookings.fragment.AccountSettingsFragment
import com.expedia.bookings.fragment.AccountSettingsFragmentTest
import com.expedia.bookings.model.PointOfSaleStateModel
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowUserManager::class, ShadowAccountManagerEB::class))
@RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
class ClearPrivateDataDialogTest {

    lateinit var fragment: AccountSettingsFragment
    lateinit private var activity: AccountSettingsFragmentTest.FragmentUtilActivity
    lateinit private var pointOfSaleStateModel: PointOfSaleStateModel

    @Before
    fun before() {
        activity = Robolectric.setupActivity(AccountSettingsFragmentTest.FragmentUtilActivity::class.java)
        activity.setTheme(R.style.NewLaunchTheme)
        pointOfSaleStateModel = PointOfSaleStateModel()
        setPointOfSale(PointOfSaleId.UNITED_STATES)
    }

    @Test
    fun testClearPrivateDataWhenUserLoggedIn() {
        givenFragmentSetup()
        givenSignedInAsUser(givenUser())
        fragment.onAboutRowClicked(9)
        val alertDialog = Shadows.shadowOf(ShadowAlertDialog.getLatestAlertDialog())

        assertNotNull(alertDialog)
        assertEquals("Clear Private Data?", alertDialog.title)
        assertEquals("This will sign you out and remove search history.", alertDialog.message)
        assertTrue(alertDialog.isCancelable)
    }

    @Test
    fun testClearPrivateDataGuestUser() {
        givenFragmentSetup()
        fragment.onAboutRowClicked(9)
        val alertDialog = Shadows.shadowOf(ShadowAlertDialog.getLatestAlertDialog())

        assertNotNull(alertDialog)
        assertEquals("Clear Private Data?", alertDialog.title)
        assertEquals("This will remove all search history.", alertDialog.message)
        assertTrue(alertDialog.isCancelable)
    }

    private fun givenUser(): User {
        val user = User()
        val traveler = Traveler()
        traveler.email = "qa-ehcc@mobiata.com"
        user.primaryTraveler = traveler
        return user
    }

    private fun givenSignedInAsUser(user: User) {
        UserLoginTestUtil.setupUserAndMockLogin(user)
    }

    private fun givenFragmentSetup() {
        fragment = AccountSettingsFragment()
        activity.supportFragmentManager.beginTransaction().add(1, fragment, null).commit()
    }

    private fun setPointOfSale(posId: PointOfSaleId) {
        pointOfSaleStateModel = PointOfSaleStateModel()
        PointOfSaleTestConfiguration.configurePOS(activity, "ExpediaSharedData/ExpediaPointOfSaleConfig.json", Integer.toString(posId.id), false)
        pointOfSaleStateModel.pointOfSaleChangedSubject.onNext(PointOfSale.getPointOfSale())
    }
}
