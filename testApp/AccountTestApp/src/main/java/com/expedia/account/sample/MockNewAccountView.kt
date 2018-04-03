package com.expedia.account.sample

import android.content.Context
import android.util.AttributeSet
import com.expedia.account.NewAccountView
import com.expedia.account.util.Events
import com.squareup.otto.Subscribe

class MockNewAccountView(context: Context, attributeSet: AttributeSet) : NewAccountView(context, attributeSet){

    private var mCreateCalled = false
    private var mIsMockModeEnabled = false

    fun setMockMode(enabled: Boolean) {
        if (mCreateCalled) {
            throw RuntimeException("setFacebookMockMode() should be called before setConfig().")
        }
        mIsMockModeEnabled = enabled
    }

    @Subscribe
    override fun otto(event: Events.NewAccountSignInButtonClicked) {
        super.otto(event)
    }

    @Subscribe
    override fun otto(event: Events.NewCreateAccountButtonClicked) {
        super.otto(event)
    }
}
