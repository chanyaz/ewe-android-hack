package com.expedia.account

import android.app.Activity
import android.content.Context
import android.content.res.TypedArray
import android.support.design.widget.TabLayout
import android.support.v4.view.PagerAdapter
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.expedia.account.handler.NewCreateAccountHandler
import com.expedia.account.handler.NewSignInHandler
import com.expedia.account.newsignin.NewSignInLayout
import com.expedia.account.newsignin.NewCreateAccountLayout
import com.expedia.account.recaptcha.Recaptcha
import com.expedia.account.util.Events
import com.mobiata.android.Log
import com.squareup.otto.Subscribe

open class NewAccountView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    enum class AccountTab {
        SIGN_IN,
        CREATE_ACCOUNT
    }

    private val toolBar: Toolbar by lazy { findViewById<Toolbar>(R.id.new_account_toolbar) }
    private val tabs: TabLayout by lazy { findViewById<TabLayout>(R.id.new_account_tabs) }
    private val tabsContainer: FrameLayout by lazy { findViewById<FrameLayout>(R.id.new_account_tabs_container) }
    private val viewPager: SwipeDisabledViewPager by lazy { findViewById<SwipeDisabledViewPager>(R.id.new_account_viewpager) }
    private val signInLayout: NewSignInLayout by lazy { findViewById<NewSignInLayout>(R.id.new_account_signin_view) }
    private val createAccountLayout: NewCreateAccountLayout by lazy { findViewById<NewCreateAccountLayout>(R.id.new_account_create_view) }

    private val pagerAdapter = SignInPagerAdapter()
    private var brand: String? = null
    private var config: Config? = null

    init {
        View.inflate(context, R.layout.acct__widget_new_account_view, this)
        viewPager.adapter = pagerAdapter
        tabs.setupWithViewPager(viewPager)
        stylize(context, attrs)
    }

    private fun stylize(context: Context, attrs: AttributeSet) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.acct__NewAccountView)
        verifyRequiredAttrs(attributes)
        brand = attributes.getString(R.styleable.acct__NewAccountView_acct__brand_string)
        toolBar.setBackgroundColor(attributes.getColor(R.styleable.acct__NewAccountView_acct__header_color, -1))
        toolBar.navigationIcon = attributes.getDrawable(R.styleable.acct__NewAccountView_acct__cancel_drawable)
        tabsContainer.setBackgroundColor(attributes.getColor(R.styleable.acct__NewAccountView_acct__header_color, -1))
        tabs.setSelectedTabIndicatorColor(attributes.getColor(R.styleable.acct__NewAccountView_acct__tab_indicator_color, -1))
        tabs.setTabTextColors(attributes.getColor(R.styleable.acct__NewAccountView_acct__tab_text_color, -1),
                attributes.getColor(R.styleable.acct__NewAccountView_acct__tab_text_selected_color, -1))
        attributes.recycle()
    }

    private fun verifyRequiredAttrs(attributes: TypedArray) {
        val requiredAttributeId = intArrayOf(
                R.styleable.acct__NewAccountView_acct__brand_string,
                R.styleable.acct__NewAccountView_acct__header_color,
                R.styleable.acct__NewAccountView_acct__cancel_drawable,
                R.styleable.acct__NewAccountView_acct__tab_indicator_color,
                R.styleable.acct__NewAccountView_acct__tab_text_color,
                R.styleable.acct__NewAccountView_acct__tab_text_selected_color)

        for (attributeId: Int in requiredAttributeId) {
            if (!attributes.hasValue(attributeId)) {
                val name = resources.getResourceName(attributeId)
                throw RuntimeException("$name is not defined")
            }
        }
    }

    fun setConfig(config: Config) {
        this.config = config
        signInLayout.configurePOS(config.enableFacebookButton)
        createAccountLayout.configurePOS(
                config.showSpamOptIn,
                config.enableSpamByDefault,
                config.marketingText,
                config.newTermsText
        )
        viewPager.currentItem = config.initialTab.ordinal
    }

    inner class SignInPagerAdapter : PagerAdapter() {
        override fun isViewFromObject(view: View?, targetObject: Any?): Boolean {
            return targetObject == view
        }

        override fun instantiateItem(container: ViewGroup?, position: Int): FrameLayout {
            when (getAccountTabForPosition(position)) {
                AccountTab.SIGN_IN -> {
                    container?.addView(signInLayout)
                    return signInLayout
                }
                AccountTab.CREATE_ACCOUNT -> {
                    container?.addView(createAccountLayout)
                    return createAccountLayout
                }
            }
        }

        override fun getPageTitle(position: Int): CharSequence {
            return when (getAccountTabForPosition(position)) {
                AccountTab.SIGN_IN -> context.getString(R.string.acct__Sign_In)
                AccountTab.CREATE_ACCOUNT -> context.getString(R.string.acct__new_create_account_text)
            }
        }

        override fun getCount(): Int {
            return AccountTab.values().size
        }

        private fun getAccountTabForPosition(position: Int): AccountTab {
            return AccountTab.values()[position]
        }
    }

    @Subscribe
    open fun otto(event: Events.NewAccountSignInButtonClicked) {
        config?.let {
            val handler = NewSignInHandler(context, it, event.email, event.password)
            if (it.enableRecaptcha) {
                Recaptcha.recaptchaCheck(context as Activity, it.recaptchaAPIKey, handler)
            } else {
                handler.doSignIn(null)
                Log.i("RECAPTCHA", "Not Enabled -> From Sign In Button Click")
            }
        }
    }

    @Subscribe
    open fun otto(event: Events.NewCreateAccountButtonClicked) {
        config?.let {
            createAccountLayout.storeDataInNewUser()
            val handler = NewCreateAccountHandler(context, it, brand, createAccountLayout, viewPager)
            if (it.enableRecaptcha) {
                Recaptcha.recaptchaCheck(context as Activity, it.recaptchaAPIKey, handler)
            } else {
                handler.doCreateAccount(null)
                Log.i("RECAPTCHA", "Not Enabled -> From TOS Button Click")
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    @Subscribe
    fun otto(event: Events.NewForgotPasswordButtonClicked) {
        config?.accountSignInListener?.onForgotPassword()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Events.register(this)
    }

    override fun onDetachedFromWindow() {
        Events.unregister(this)
        super.onDetachedFromWindow()
    }

    fun setNavigationOnClickListener(listener: OnClickListener) {
        toolBar.setNavigationOnClickListener(listener)
    }
}
