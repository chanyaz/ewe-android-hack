package com.expedia.account

import android.content.Context
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class NewAccountViewTest {

    private val context: Context = RuntimeEnvironment.application
    private lateinit var contextThemeWrapper: ContextThemeWrapper
    private lateinit var newAccountView: NewAccountView

    @Before
    fun setup() {
        contextThemeWrapper = ContextThemeWrapper(context, R.style.Theme_AppCompat)
        newAccountView = LayoutInflater.from(contextThemeWrapper).inflate(R.layout.acct__widget_test_new_account_view, null) as NewAccountView
        newAccountView.config = Config.build().setAnalyticsListener(Mockito.mock(AnalyticsListener::class.java))
    }

    @Test
    fun AnalyticsIsCalledWhenSignInTabClicked() {
        newAccountView.viewPager.currentItem = 1
        newAccountView.viewPager.currentItem = 0
        Mockito.verify(newAccountView.config.analyticsListener).newSignInTabClicked()
    }

    @Test
    fun AnalyticsIsCalledWhenCreateAccountTabClicked() {
        newAccountView.viewPager.currentItem = 1
        Mockito.verify(newAccountView.config.analyticsListener).newCreateAccountTabClicked()
    }
}
