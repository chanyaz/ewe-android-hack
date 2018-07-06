package com.expedia.account.newsignin

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import com.expedia.account.R

class MultipleSignInOptionsLayout(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    val googleSignInButton: ImageButton by lazy { findViewById<ImageButton>(R.id.google_sign_in_button) }

    val facebookSignInButton: ImageButton by lazy { findViewById<ImageButton>(R.id.facebook_sign_in_button) }

    val viewModel by lazy {
        MultipleSignInOptionsLayoutViewModel()
    }

    init {
        View.inflate(context, R.layout.acct__widget_multiple_signin_options, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
    }
}

