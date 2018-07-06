package com.expedia.account.newsignin

import android.content.Context
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import com.expedia.account.AnalyticsListener
import com.expedia.account.Config
import com.expedia.account.R
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class NewSignInLayoutTest {

    private val context: Context = RuntimeEnvironment.application
    private lateinit var contextThemeWrapper: ContextThemeWrapper
    private lateinit var signInLayout: NewSignInLayout

    @Before
    fun setup() {
        contextThemeWrapper = ContextThemeWrapper(context, R.style.Theme_AppCompat)
        signInLayout = LayoutInflater.from(contextThemeWrapper).inflate(R.layout.acct__widget_test_new_account_signin_view, null) as NewSignInLayout
        signInLayout.config = Config.build().setAnalyticsListener(Mockito.mock(AnalyticsListener::class.java))
    }

    @Test
    fun analyticsIsCalledWhenSignInButtonIsClicked() {
        signInLayout.signInButton.performClick()
        Mockito.verify(signInLayout.config.analyticsListener).signInButtonClicked()
    }

    @Test
    fun testOnlySingleFacebookButtonDisplayedWhenOnlyFacebookEnabled() {
        signInLayout.setupConfig(Config.build().setEnableFacebookSignIn(true).setEnableGoogleSignIn(false))

        assertEquals(View.VISIBLE, signInLayout.signInWithFacebookButton.visibility)
        assertEquals(View.VISIBLE, signInLayout.orTextView.visibility)
        assertEquals(View.GONE, signInLayout.signInWithGoogleButton.visibility)
        assertEquals(View.GONE, signInLayout.multipleSignInOptionsLayout.visibility)
    }

    @Test
    fun testOnlySingleGoogleButtonDisplayedWhenOnlyGoogleEnabled() {
        signInLayout.setupConfig(Config.build().setEnableFacebookSignIn(false).setEnableGoogleSignIn(true))

        assertEquals(View.VISIBLE, signInLayout.signInWithGoogleButton.visibility)
        assertEquals(View.VISIBLE, signInLayout.orTextView.visibility)
        assertEquals(View.GONE, signInLayout.signInWithFacebookButton.visibility)
        assertEquals(View.GONE, signInLayout.multipleSignInOptionsLayout.visibility)
    }

    @Test
    fun testMultipleSignInDisplayedWhenBothFacebookAndGoogleEnabled() {
        signInLayout.setupConfig(Config.build().setEnableFacebookSignIn(true).setEnableGoogleSignIn(true))

        assertEquals(View.GONE, signInLayout.signInWithGoogleButton.visibility)
        assertEquals(View.VISIBLE, signInLayout.orTextView.visibility)
        assertEquals(View.GONE, signInLayout.signInWithFacebookButton.visibility)
        assertEquals(View.VISIBLE, signInLayout.multipleSignInOptionsLayout.visibility)
    }

    @Test
    fun testNothingDisplayedWhenBothFacebookAndGoogleDisabled() {
        signInLayout.setupConfig(Config.build().setEnableFacebookSignIn(false).setEnableGoogleSignIn(false))

        assertEquals(View.GONE, signInLayout.signInWithGoogleButton.visibility)
        assertEquals(View.GONE, signInLayout.orTextView.visibility)
        assertEquals(View.GONE, signInLayout.signInWithFacebookButton.visibility)
        assertEquals(View.GONE, signInLayout.multipleSignInOptionsLayout.visibility)
    }


    @Test
    fun analyticsIsCalledWhenFacebookSignInButtonIsClicked() {
        signInLayout.signInWithFacebookButton.performClick()
        Mockito.verify(signInLayout.config.analyticsListener).facebookSignInButtonClicked()
    }
}
