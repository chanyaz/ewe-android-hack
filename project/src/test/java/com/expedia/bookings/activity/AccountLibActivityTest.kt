package com.expedia.bookings.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.expedia.account.AccountView
import com.expedia.account.NewAccountView
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.android.controller.ActivityController

@RunWith(RobolectricRunner::class)
class AccountLibActivityTest {

    private lateinit var activityController: ActivityController<TestAccountLibActivity>
    private lateinit var activity: TestAccountLibActivity

    @Before
    fun setUp() {
        activityController = createSystemUnderTestWithIntent(createIntentDefaultToCreateAccountTab())
        activity = activityController.get()
        activity.setTheme(R.style.Theme_AccountLib)
    }

    private fun createSystemUnderTestWithIntent(intent: Intent): ActivityController<TestAccountLibActivity> =
            Robolectric.buildActivity(TestAccountLibActivity::class.java, intent)

    private fun createIntentDefaultToCreateAccountTab(): Intent {
        val intent = Intent()
        val bundle = AccountLibActivity.createArgumentsBundle(LineOfBusiness.PROFILE, NewAccountView.AccountTab.CREATE_ACCOUNT, null)
        return intent.putExtra("ARG_BUNDLE", bundle)
    }

    private fun givenNewSignInEnabled() {
        AbacusTestUtils.bucketTestsAndEnableRemoteFeature(activity, AbacusUtils.EBAndroidAppAccountNewSignIn)
    }

    private fun givenNewSignInDisabled() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.EBAndroidAppAccountNewSignIn, 0)
    }

    @Test
    fun testAccountTabsDefaultToCreateAccount() {
        givenNewSignInEnabled()
        activityController.create().start().visible()
        assertEquals(NewAccountView.AccountTab.CREATE_ACCOUNT, activity.initialTabForTest)
    }

    @Test
    fun testNewAccountViewIsVisibleWhenInBucketed() {
        givenNewSignInEnabled()
        activityController.create().start().visible()
        assertEquals(View.VISIBLE, activity.newAccountViewForTest.visibility)
    }

    @Test
    fun testOldAccountViewIsVisibleWhenInControl() {
        givenNewSignInDisabled()
        activityController.create().start().visible()
        assertEquals(View.VISIBLE, activity.accountViewForTest.visibility)
    }

    @Test
    fun testNewAccountViewIsGoneWhenInControl() {
        givenNewSignInDisabled()
        activityController.create().start().visible()
        assertEquals(View.GONE, activity.newAccountViewForTest.visibility)
    }

    @Test
    fun testOldAccountViewIsGoneWhenInBucketed() {
        givenNewSignInEnabled()
        activityController.create().start().visible()
        assertEquals(View.GONE, activity.accountViewForTest.visibility)
    }

    class TestAccountLibActivity : AccountLibActivity() {
        lateinit var initialTabForTest: NewAccountView.AccountTab
        lateinit var newAccountViewForTest: NewAccountView
        lateinit var accountViewForTest: AccountView

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            initialTabForTest = initialTab
            newAccountViewForTest = newAccountView
            accountViewForTest = accountView
        }
    }
}
