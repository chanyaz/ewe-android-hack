package com.expedia.account.newsignin

import android.content.Context
import android.support.annotation.VisibleForTesting
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
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

    val signInWithGoogleButton: Button by lazy { findViewById<Button>(R.id.new_signin_with_google_button) }

    val orTextView: TextView by lazy { findViewById<TextView>(R.id.or) }

    @VisibleForTesting
    val multipleSignInOptionsLayout: MultipleSignInOptionsLayout by lazy { findViewById<MultipleSignInOptionsLayout>(R.id.multiple_signin_options_layout) }

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
        setUpViews()
    }

    private fun setUpViews() {
        val enableGoogleSignIn = config.isGoogleSignInEnabled
        val enableFacebookSignIn = config.isFacebookSignInEnabled

        val isMultipleSignInOptionsViewEnabled = enableFacebookSignIn && enableGoogleSignIn

        multipleSignInOptionsLayout.visibility = if (isMultipleSignInOptionsViewEnabled) View.VISIBLE else View.GONE

        signInWithFacebookButton.visibility = if (!isMultipleSignInOptionsViewEnabled && enableFacebookSignIn) View.VISIBLE else View.GONE

        signInWithGoogleButton.visibility = if (!isMultipleSignInOptionsViewEnabled && enableGoogleSignIn) View.VISIBLE else View.GONE

        orTextView.visibility = if (!enableFacebookSignIn && !enableGoogleSignIn) View.GONE else View.VISIBLE
    }
}
