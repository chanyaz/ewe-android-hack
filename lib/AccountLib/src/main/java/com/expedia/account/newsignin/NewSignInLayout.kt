package com.expedia.account.newsignin

import android.content.Context
import android.support.annotation.VisibleForTesting
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.FrameLayout
import com.expedia.account.Config
import com.expedia.account.R
import com.expedia.account.input.InputValidator
import com.expedia.account.input.rules.ExpediaEmailInputRule
import com.expedia.account.input.rules.ExpediaPasswordSignInInputRule
import com.expedia.account.singlepage.SinglePageInputTextPresenter
import com.expedia.account.util.CombiningFakeObservable
import com.expedia.account.util.Events
import io.reactivex.subjects.BehaviorSubject

class NewSignInLayout(context: Context, attributeSet: AttributeSet) : FrameLayout(context, attributeSet) {

    @VisibleForTesting
    val signInWithFacebookButton: Button by lazy { findViewById<Button>(R.id.new_signin_with_facebook_button) }
    @VisibleForTesting
    val multipleSignInOptionsLayout: MulitipleSignInOptionsLayout by lazy { findViewById<MulitipleSignInOptionsLayout>(R.id.multiple_signin_options_layout) }
    private val emailInput: SinglePageInputTextPresenter by lazy { findViewById<SinglePageInputTextPresenter>(R.id.new_signin_email_address) }
    private val passwordInput: SinglePageInputTextPresenter by lazy { findViewById<SinglePageInputTextPresenter>(R.id.new_signin_password) }
    private val forgotPassword: View by lazy { findViewById<View>(R.id.new_signin_forgot_password) }
    @VisibleForTesting val signInButton: Button by lazy { findViewById<Button>(R.id.new_signin_button) }
    private val validationObservable = CombiningFakeObservable()
    private val allFieldsValidSubject = BehaviorSubject.create<Boolean>()

    @VisibleForTesting lateinit var config: Config

    init {
        View.inflate(context, R.layout.acct__widget_new_account_signin_view, this)
        allFieldsValidSubject.onNext(false)
        signInButton.setOnClickListener {
            config.analyticsListener.signInButtonClicked()
            if (allFieldsValidSubject.value) {
                Events.post(Events.NewAccountSignInButtonClicked(emailInput.text, passwordInput.text))
            } else {
                emailInput.forceCheckWithFocus(false)
                passwordInput.forceCheckWithFocus(false)
            }
        }
        forgotPassword.setOnClickListener {
            Events.post(Events.NewForgotPasswordButtonClicked())
        }
        signInWithFacebookButton.setOnClickListener {
            config.analyticsListener.facebookSignInButtonClicked()
            Events.post(Events.NewSignInWithFacebookButtonClicked())
        }
        validationObservable.addSource(emailInput.statusObservable)
        validationObservable.addSource(passwordInput.statusObservable)
        passwordInput.isPasswordVisibilityToggleEnabled(true)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        emailInput.setValidator(object : InputValidator(ExpediaEmailInputRule()) {})
        passwordInput.setValidator(object : InputValidator(ExpediaPasswordSignInInputRule()) {})
        passwordInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                passwordInput.doneCheck()
                signInButton.callOnClick()
            }
            false
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        validationObservable.subscribe(allFieldsValidSubject)
    }

    override fun onDetachedFromWindow() {
        validationObservable.unsubscribe(allFieldsValidSubject)
        super.onDetachedFromWindow()
    }

    fun configurePOS(enableFacebookButton: Boolean, enableMultipleSignInLayout: Boolean) {
        signInWithFacebookButton.visibility = if (enableFacebookButton && !enableMultipleSignInLayout) View.VISIBLE else View.GONE
        multipleSignInOptionsLayout.visibility = if (enableMultipleSignInLayout) View.VISIBLE else View.GONE
    }

    fun setEnable(enable: Boolean) {
        signInWithFacebookButton.isEnabled = enable
        emailInput.isEnabled = enable
        passwordInput.isEnabled = enable
        passwordInput.isPasswordVisibilityToggleEnabled(enable)
        forgotPassword.isEnabled = enable
        signInButton.isEnabled = enable
    }

    fun setupConfig(config: Config) {
        this.config = config
    }
}
