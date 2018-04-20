package com.expedia.bookings.launch.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.launch.activity.PhoneLaunchActivity
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class SignInPlaceholderCardTest {
    lateinit var sut: TestSignInPlaceholderCard
    val context: Context = RuntimeEnvironment.application
    lateinit var phoneLaunchActivity: PhoneLaunchActivity

    @Before
    fun before() {
        phoneLaunchActivity = gethoneLaunchActivity()
        createSystemUnderTest()
    }

    private fun givenNewSignInEnabled() {
        AbacusTestUtils.bucketTestsAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppAccountNewSignIn)
    }

    private fun givenNewSignInDisabled() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppAccountNewSignIn, 0)
    }

    @Test
    fun testClickCreateAccountGoToNewCreateAccountPage() {
        givenNewSignInEnabled()
        sut.button_two.performClick()
        assertEquals(1, sut.goToNewCreateAccountPageTimes)
    }

    @Test
    fun testClickCreateAccountGoToOldCreateAccountPage() {
        givenNewSignInDisabled()
        sut.button_two.performClick()
        assertEquals(1, sut.goToOldCreateAccountPageTimes)
    }

    private fun createSystemUnderTest() {
        val view = LayoutInflater.from(phoneLaunchActivity).inflate(R.layout.feeds_prompt_card, null)
        sut = TestSignInPlaceholderCard(view, phoneLaunchActivity)
    }

    private fun gethoneLaunchActivity(): PhoneLaunchActivity {
        val phoneLaunchActivityController = Robolectric.buildActivity(PhoneLaunchActivity::class.java)
        val phoneLaunchActivity = phoneLaunchActivityController.get()
        return phoneLaunchActivity
    }

    class TestSignInPlaceholderCard(view: View, context: Context) : SignInPlaceholderCard(view, context) {
        var goToNewCreateAccountPageTimes = 0
        var goToOldCreateAccountPageTimes = 0

        override fun goToNewCreateAccountPage() {
            goToNewCreateAccountPageTimes += 1
        }

        override fun goToOldCreateAccountPage() {
            goToOldCreateAccountPageTimes += 1
        }
    }
}
