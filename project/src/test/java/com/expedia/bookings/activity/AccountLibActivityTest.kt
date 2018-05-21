package com.expedia.bookings.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import com.expedia.account.AccountView
import com.expedia.account.NewAccountView
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import org.hamcrest.Matchers
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.android.controller.ActivityController

@RunWith(RobolectricRunner::class)
class AccountLibActivityTest {

    private lateinit var mockAnalyticsProvider: AnalyticsProvider
    private lateinit var activityController: ActivityController<TestAccountLibActivity>
    private lateinit var activity: TestAccountLibActivity

    private val signInPageName = "App.Account.SignIn"
    private val createAccountPageName = "App.Account.Create"

    @Before
    fun setUp() {
        activityController = createSystemUnderTestWithIntent(createIntentDefaultToCreateAccountTab())
        activity = activityController.get()
        activity.setTheme(R.style.Theme_AccountLib)
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
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

    // Sign In Page Tracking
    @Test
    fun testSignInButtonClickedIsTrackedOnOmniture() {
        activity.analyticsListener.signInButtonClicked()
        OmnitureTestUtils.assertLinkTracked(
                "Accounts", "App.Account.EmailSignIn", Matchers.allOf(
                OmnitureMatchers.withEvars(mapOf(18 to "D=pageName", 61 to PointOfSale.getPointOfSale().tpid.toString()))), mockAnalyticsProvider)
    }

    @Test
    fun testfacebookSignInClickedIsTrackedOnOmniture() {
        activity.analyticsListener.facebookSignInButtonClicked()
        OmnitureTestUtils.assertLinkTracked(
                "Accounts", "App.Account.FacebookSignIn", Matchers.allOf(
                OmnitureMatchers.withEvars(mapOf(18 to "D=pageName", 61 to PointOfSale.getPointOfSale().tpid.toString()))), mockAnalyticsProvider)
    }

    @Test
    fun testSignInSuccessIsTrackedOnOmniture() {
        activity.analyticsListener.signInSucceeded()
        OmnitureTestUtils.assertLinkTracked(
                "Accounts", "App.Account.Login.Success", Matchers.allOf(
                OmnitureMatchers.withEvars(mapOf(61 to PointOfSale.getPointOfSale().tpid.toString())),
                OmnitureMatchers.withEventsString("event26")), mockAnalyticsProvider)
    }

    @Test
    fun testSignInErrorIsTrackedOnOmniture() {
        activity.analyticsListener.userReceivedErrorOnSignInAttempt("Sign in Error")
        OmnitureTestUtils.assertStateTracked(
                signInPageName, Matchers.allOf(
                OmnitureMatchers.withEvars(mapOf(18 to "D=pageName")), OmnitureMatchers.withProps(mapOf(36 to "Sign in Error"))), mockAnalyticsProvider)
    }

    @Test
    fun testSignInTabClickedIsTrackedOnOmniture() {
        activity.analyticsListener.newSignInTabClicked()
        OmnitureTestUtils.assertLinkTracked(
                "Accounts", "App.Account.SignIn", Matchers.allOf(
                OmnitureMatchers.withEvars(mapOf(18 to "D=pageName", 61 to PointOfSale.getPointOfSale().tpid.toString()))), mockAnalyticsProvider)
    }

    @Test
    fun testCreateAccountTabClickedIsTrackedOnOmniture() {
        activity.analyticsListener.newCreateAccountTabClicked()
        OmnitureTestUtils.assertLinkTracked(
                "Accounts", "App.Account.Create", Matchers.allOf(
                OmnitureMatchers.withEvars(mapOf(18 to "D=pageName"))), mockAnalyticsProvider)
    }

    // Create Account Page Tracking
    @Test
    fun testCreateAccountButtonClickedIsTrackedOnOmniture() {
        activity.analyticsListener.createButtonClicked()
        OmnitureTestUtils.assertLinkTracked(
                "Accounts", "App.Account.CreateAccount",
                Matchers.allOf(OmnitureMatchers.withEvars(mapOf(18 to "D=pageName"))), mockAnalyticsProvider)
    }

    @Test
    fun testCreateAccountSuccessIsTrackedOnOmniture() {
        activity.analyticsListener.userSucceededInCreatingAccount()
        OmnitureTestUtils.assertStateTracked(
                createAccountPageName, Matchers.allOf(
                OmnitureMatchers.withEvars(mapOf(18 to "D=pageName")), OmnitureMatchers.withEventsString("event25,event26,event61")), mockAnalyticsProvider)
    }

    @Test
    fun testCreateAccountErrorIsTrackedOnOmniture() {
        activity.analyticsListener.userReceivedErrorOnAccountCreationAttempt("Server Failed")
        OmnitureTestUtils.assertStateTracked(
                createAccountPageName, Matchers.allOf(
                OmnitureMatchers.withEvars(mapOf(18 to "D=pageName")), OmnitureMatchers.withProps(mapOf(36 to "Server Failed"))), mockAnalyticsProvider)
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
