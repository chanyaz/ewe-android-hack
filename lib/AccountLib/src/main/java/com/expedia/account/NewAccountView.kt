package com.expedia.account

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.TypedArray
import android.support.annotation.VisibleForTesting
import android.support.design.widget.TabLayout
import android.support.v4.view.PagerAdapter
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import com.expedia.account.handler.NewCreateAccountHandler
import com.expedia.account.handler.NewSignInHandler
import com.expedia.account.newsignin.NewSignInLayout
import com.expedia.account.newsignin.NewCreateAccountLayout
import com.expedia.account.recaptcha.Recaptcha
import com.expedia.account.util.Events
import com.expedia.account.util.NewFacebookHelper
import com.expedia.account.util.Utils
import com.expedia.account.view.FacebookLinkAccountsLayout
import com.mobiata.android.Log
import com.squareup.otto.Subscribe

open class NewAccountView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    enum class AccountTab {
        SIGN_IN,
        CREATE_ACCOUNT
    }

    private val toolBar: Toolbar by lazy { findViewById<Toolbar>(R.id.new_account_toolbar) }
    private val tabs: TabLayout by lazy { findViewById<TabLayout>(R.id.new_account_tabs) }
    private val tabsContainer: FrameLayout by lazy { findViewById<FrameLayout>(R.id.new_account_tabs_container) }
    @VisibleForTesting val viewPager: SwipeDisabledViewPager by lazy { findViewById<SwipeDisabledViewPager>(R.id.new_account_viewpager) }
    private val signInLayout: NewSignInLayout by lazy { findViewById<NewSignInLayout>(R.id.new_account_signin_view) }
    private val createAccountLayout: NewCreateAccountLayout by lazy { findViewById<NewCreateAccountLayout>(R.id.new_account_create_view) }
    private val facebookLinkAccountsLayout: FacebookLinkAccountsLayout by lazy { findViewById<FacebookLinkAccountsLayout>(R.id.new_account_facebook_link_accounts_view) }
    private val loadingView: FrameLayout by lazy { findViewById<FrameLayout>(R.id.new_account_loading_view) }

    private val pagerAdapter = SignInPagerAdapter()
    private val tabSelectedListener = SignInTabListener()
    private lateinit var brand: String
    @VisibleForTesting lateinit var config: Config
    private val facebookHelper: NewFacebookHelper by lazy { createFacebookHelper() }

    private lateinit var toolbarNavigationListener: OnClickListener

    init {
        View.inflate(context, R.layout.acct__widget_new_account_view, this)
        viewPager.adapter = pagerAdapter
        tabs.setupWithViewPager(viewPager)
        tabs.addOnTabSelectedListener(tabSelectedListener)
        stylize(context, attrs)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        facebookLinkAccountsLayout.setNavigationOnClickListener(OnClickListener {
            cancelFacebookLinkAccountsView()
        })
    }

    @SuppressLint("CustomViewStyleable")
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
        facebookLinkAccountsLayout.stylizeToolbar(attributes.getColor(R.styleable.acct__NewAccountView_acct__header_color, -1),
                attributes.getDrawable(R.styleable.acct__NewAccountView_acct__back_drawable))
        facebookLinkAccountsLayout.brand = brand
        attributes.recycle()
    }

    private fun verifyRequiredAttrs(attributes: TypedArray) {
        val requiredAttributeId = intArrayOf(
                R.styleable.acct__NewAccountView_acct__brand_string,
                R.styleable.acct__NewAccountView_acct__header_color,
                R.styleable.acct__NewAccountView_acct__cancel_drawable,
                R.styleable.acct__NewAccountView_acct__back_drawable,
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

    fun setupConfig(config: Config) {
        this.config = config
        signInLayout.setupConfig(config)
        createAccountLayout.setupConfig(config)
        signInLayout.configurePOS(config.enableFacebookButton)
        createAccountLayout.configurePOS(
                config.showSpamOptIn,
                config.enableSpamByDefault,
                config.marketingText,
                config.newTermsText
        )
        viewPager.currentItem = config.initialTab.ordinal
    }

    fun showFacebookLinkAccountsView(type: FacebookLinkAccountsLayout.SetupType) {
        when (type) {
            FacebookLinkAccountsLayout.SetupType.ACCOUNT_NOT_LINKED -> facebookLinkAccountsLayout.setupForAccountNotLinked()
            FacebookLinkAccountsLayout.SetupType.ACCOUNT_EXISTING -> facebookLinkAccountsLayout.setupForAccountExisting()
        }
        facebookLinkAccountsLayout.visibility = View.VISIBLE
        val animation = AnimationUtils.loadAnimation(context, R.anim.acct__slide_in_right)
        facebookLinkAccountsLayout.startAnimation(animation)
    }

    open fun cancelFacebookLinkAccountsView() {
        val animation = AnimationUtils.loadAnimation(context, R.anim.acct__slide_out_right)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                facebookLinkAccountsLayout.visibility = View.INVISIBLE
                facebookLinkAccountsLayout.clearAll()
            }
            override fun onAnimationStart(animation: Animation?) {
            }
        })
        facebookLinkAccountsLayout.startAnimation(animation)
    }

    open fun isOnSignInPage(): Boolean {
        return facebookLinkAccountsLayout.visibility != View.VISIBLE
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
                AccountTab.CREATE_ACCOUNT -> context.getString(R.string.acct__new_create_account_tab_text)
            }
        }

        override fun getCount(): Int {
            return AccountTab.values().size
        }

        private fun getAccountTabForPosition(position: Int): AccountTab {
            return AccountTab.values()[position]
        }
    }

    inner class SignInTabListener : TabLayout.OnTabSelectedListener {
        override fun onTabReselected(tab: TabLayout.Tab) {
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {
        }

        override fun onTabSelected(tab: TabLayout.Tab) {
            when (tab.position) {
                AccountTab.SIGN_IN.ordinal -> config.analyticsListener.newSignInTabClicked()
                AccountTab.CREATE_ACCOUNT.ordinal -> config.analyticsListener.newCreateAccountTabClicked()
            }
        }
    }

    @Subscribe
    open fun otto(e: Events.NewAccountSignInButtonClicked) {
        showLoading()
        val handler = NewSignInHandler(context, config, e.email, e.password, this@NewAccountView)
        if (config.enableRecaptcha) {
            Recaptcha.recaptchaCheck(context as Activity, config.recaptchaAPIKey, handler)
        } else {
            handler.doSignIn(null)
            Log.i("RECAPTCHA", "Not Enabled -> From Sign In Button Click")
        }
    }

    @Subscribe
    open fun otto(e: Events.NewCreateAccountButtonClicked) {
        showLoading()
        val handler = NewCreateAccountHandler(context, config, brand, createAccountLayout, viewPager, this@NewAccountView)
        if (config.enableRecaptcha) {
            Recaptcha.recaptchaCheck(context as Activity, config.recaptchaAPIKey, handler)
        } else {
            handler.doCreateAccount(null)
            Log.i("RECAPTCHA", "Not Enabled -> From TOS Button Click")
        }
    }

    @Suppress("UNUSED_PARAMETER")
    @Subscribe
    open fun otto(e: Events.NewSignInWithFacebookButtonClicked) {
        if (config.facebookAppId == null) {
            config.accountSignInListener.onFacebookRequested()
        } else {
            Utils.hideKeyboard(this)
            facebookHelper.doFacebookLogin(context)
        }
    }

    @Subscribe
    open fun otto(e: Events.NewLinkFromFacebookFired) {
        showLoading()
        facebookHelper.onLinkClicked(context)
        Utils.hideKeyboard(this)
    }

    open fun createFacebookHelper(): NewFacebookHelper {
        return NewFacebookHelper(context, config, brand, this)
    }

    @Subscribe
    open fun otto(e: Events.NewForgotPasswordButtonClicked) {
        config.accountSignInListener?.onForgotPassword()
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
        toolbarNavigationListener = listener
        toolBar.setNavigationOnClickListener(toolbarNavigationListener)
    }

    open fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        facebookHelper.onActivityResult(requestCode, resultCode, data)
    }

    fun showLoading() {
        setTabsEnable(false)
        loadingView.visibility = View.VISIBLE
        signInLayout.setEnable(false)
        createAccountLayout.setEnable(false)
        facebookLinkAccountsLayout.setEnable(false)
        toolBar.setNavigationOnClickListener(null)
    }

    fun cancelLoading() {
        setTabsEnable(true)
        loadingView.visibility = View.GONE
        signInLayout.setEnable(true)
        createAccountLayout.setEnable(true)
        facebookLinkAccountsLayout.setEnable(true)
        toolBar.setNavigationOnClickListener(toolbarNavigationListener)
    }

    private fun setTabsEnable(enable: Boolean) {
        for (i in 0 until AccountTab.values().size) {
            (tabs.getChildAt(0) as ViewGroup).getChildAt(i).isEnabled = enable
        }
    }
}
