package com.expedia.bookings.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
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
import org.mockito.Mockito
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
        assertEquals(View.VISIBLE, activity.newAccountView.visibility)
    }

    @Test
    fun testOldAccountViewIsVisibleWhenInControl() {
        givenNewSignInDisabled()
        activityController.create().start().visible()
        assertEquals(View.VISIBLE, activity.accountView.visibility)
    }

    @Test
    fun testNewAccountViewIsGoneWhenInControl() {
        givenNewSignInDisabled()
        activityController.create().start().visible()
        assertEquals(View.GONE, activity.newAccountView.visibility)
    }

    @Test
    fun testOldAccountViewIsGoneWhenInBucketed() {
        givenNewSignInEnabled()
        activityController.create().start().visible()
        assertEquals(View.GONE, activity.accountView.visibility)
    }

    @Test
    fun testOnBackPressedWhenInBucketed() {
        givenNewSignInEnabled()
        activityController.create().start().visible()
        activity.newAccountView = Mockito.mock(NewAccountView::class.java)
        Mockito.`when`(activity.newAccountView.isOnSignInPage()).thenAnswer { false }
        activity.onBackPressed()
        Mockito.verify(activity.newAccountView).isOnSignInPage()
        Mockito.verify(activity.newAccountView).cancelFacebookLinkAccountsView()
    }

    @Test
    fun testOnBackPressedWhenInControl() {
        givenNewSignInDisabled()
        activityController.create().start().visible()
        activity.accountView = Mockito.mock(AccountView::class.java)
        activity.onBackPressed()
        Mockito.verify(activity.accountView).back()
    }

    @Test
    fun testOnActivityResultWhenInBucketed() {
        givenNewSignInEnabled()
        activityController.create().start().visible()
        activity.newAccountView = Mockito.mock(TestNewAccountView::class.java)
        val intent = Intent()
        activity.onActivityResult(1, 2, intent)
        Mockito.verify(activity.newAccountView).onActivityResult(1, 2, intent)
    }

    @Test
    fun testOnActivityResultWhenInControl() {
        givenNewSignInDisabled()
        activityController.create().start().visible()
        activity.accountView = Mockito.mock(AccountView::class.java)
        activity.onActivityResult(1, 2, null)
        Mockito.verify(activity.accountView).onActivityResult(1, 2, null)
    }

    class TestAccountLibActivity : AccountLibActivity() {
        lateinit var initialTabForTest: NewAccountView.AccountTab
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            initialTabForTest = initialTab
        }
    }

    open class TestNewAccountView(context: Context, attrs: AttributeSet) : NewAccountView(context, attrs) {
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {}
        override fun isOnSignInPage(): Boolean { return false }
        override fun cancelFacebookLinkAccountsView() {}
    }
}
