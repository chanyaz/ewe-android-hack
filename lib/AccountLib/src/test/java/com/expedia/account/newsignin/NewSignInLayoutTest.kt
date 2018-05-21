package com.expedia.account.newsignin

import android.content.Context
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import com.expedia.account.AnalyticsListener
import com.expedia.account.Config
import com.expedia.account.R
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
    fun analyticsIsCalledWhenFacebookSignInButtonIsClicked() {
        signInLayout.signInWithFacebookButton.performClick()
        Mockito.verify(signInLayout.config.analyticsListener).facebookSignInButtonClicked()
    }
}
