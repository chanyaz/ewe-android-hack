package com.expedia.account.sample

import android.content.Context
import android.util.AttributeSet
import com.expedia.account.NewAccountView
import com.expedia.account.util.Events
import com.expedia.account.util.MockNewFacebookHelper
import com.expedia.account.util.NewFacebookHelper
import com.squareup.otto.Subscribe

class MockNewAccountView(context: Context, attributeSet: AttributeSet) : NewAccountView(context, attributeSet){

    private var mCreateCalled = false
    private var mIsMockModeEnabled = false

    override fun createFacebookHelper(): NewFacebookHelper {
        mCreateCalled = true
        return if (mIsMockModeEnabled)
            MockNewFacebookHelper(context, config, "expedia",this)
        else
            NewFacebookHelper(context, config, "expedia", this)
    }

    fun setMockMode(enabled: Boolean) {
        if (mCreateCalled) {
            throw RuntimeException("setFacebookMockMode() should be called before setupConfig().")
        }
        mIsMockModeEnabled = enabled
    }

    @Subscribe
    override fun otto(e: Events.NewAccountSignInButtonClicked) {
        super.otto(e)
    }

    @Subscribe
    override fun otto(e: Events.NewCreateAccountButtonClicked) {
        super.otto(e)
    }

    @Subscribe
    override fun otto(e: Events.NewSignInWithFacebookButtonClicked) {
        super.otto(e)
    }

    @Subscribe
    override fun otto(e: Events.NewForgotPasswordButtonClicked) {
        super.otto(e)
    }

    @Subscribe
    override fun otto(e: Events.NewLinkFromFacebookFired) {
        super.otto(e)
    }
}
