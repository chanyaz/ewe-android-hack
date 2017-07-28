package com.expedia.bookings.test.robolectric

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.expedia.bookings.activity.DeepLinkRouterActivity
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.util.ActivityController
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowUserManager::class, ShadowAccountManagerEB::class))
@RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
class ProfileScreenDeepLinkTest {

    internal var context: Context = RuntimeEnvironment.application

    @Test
    fun testSignedInDeepLinkAsLoggedIn() {
        val user = UserLoginTestUtil.mockUser()
        UserLoginTestUtil.setupUserAndMockLogin(user)

        val intent = Intent()
        val deepLinkText = Uri.parse("expda://signIn")
        intent.data = deepLinkText

        val deepLinkRouterActivityController = createSystemUnderTest()
        val mockItineraryManager = createMockItineraryManager()
        val deepLinkRouterActivity = deepLinkRouterActivityController.get()

        deepLinkRouterActivity.mockItineraryManager = mockItineraryManager
        deepLinkRouterActivityController.withIntent(intent)
        deepLinkRouterActivityController.setup()

        assertEquals(1, deepLinkRouterActivity.signInCallsCount)
    }

    @Test
    fun testSignedInDeepLinkAsLoggedOut() {
        val intent = Intent()
        val deepLinkText = Uri.parse("expda://signIn")
        intent.data = deepLinkText

        val deepLinkRouterActivityController = createSystemUnderTest()
        val mockItineraryManager = createMockItineraryManager()
        val deepLinkRouterActivity = deepLinkRouterActivityController.get()

        deepLinkRouterActivity.mockItineraryManager = mockItineraryManager
        deepLinkRouterActivityController.withIntent(intent)
        deepLinkRouterActivityController.setup()

        assertEquals(1, deepLinkRouterActivity.signInCallsCount)
    }

    private fun createMockItineraryManager(): ItineraryManager {
        val mockItineraryManager = Mockito.mock(ItineraryManager::class.java)
        return mockItineraryManager
    }

    private fun createSystemUnderTest(): ActivityController<TestDeepLinkRouterActivity> {
        val deepLinkRouterActivityController = Robolectric.buildActivity(TestDeepLinkRouterActivity::class.java)
        return deepLinkRouterActivityController
    }

    class TestDeepLinkRouterActivity() : DeepLinkRouterActivity() {
        lateinit var mockItineraryManager: ItineraryManager
        var signInCallsCount = 0

        override fun handleSignIn() {
            signInCallsCount++
        }

        override fun getItineraryManagerInstance(): ItineraryManager {
            return mockItineraryManager
        }

        override fun getFirebaseDynamicLinksInstance(): FirebaseDynamicLinks? {
            return null
        }
    }
}
