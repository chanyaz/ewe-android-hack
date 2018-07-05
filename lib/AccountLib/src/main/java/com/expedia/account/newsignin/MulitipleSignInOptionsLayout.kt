package com.expedia.account.newsignin

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.expedia.account.R

class MulitipleSignInOptionsLayout(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    init{
        View.inflate(context, R.layout.acct__widget_multiple_signin_options, this)
    }
}
