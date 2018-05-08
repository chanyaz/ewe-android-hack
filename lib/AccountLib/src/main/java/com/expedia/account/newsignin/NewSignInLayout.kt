package com.expedia.account.newsignin

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.FrameLayout
import com.expedia.account.R
import com.expedia.account.input.InputValidator
import com.expedia.account.input.rules.ExpediaEmailInputRule
import com.expedia.account.input.rules.ExpediaPasswordSignInInputRule
import com.expedia.account.singlepage.SinglePageInputTextPresenter
import com.expedia.account.util.CombiningFakeObservable
import com.expedia.account.util.Events
import io.reactivex.subjects.BehaviorSubject

class NewSignInLayout(context: Context, attributeSet: AttributeSet) : FrameLayout(context, attributeSet) {

    private val signInWithFacebookButton: Button by lazy { findViewById<Button>(R.id.new_signin_with_facebook_button) }
    private val emailInput: SinglePageInputTextPresenter by lazy { findViewById<SinglePageInputTextPresenter>(R.id.new_signin_email_address) }
    private val passwordInput: SinglePageInputTextPresenter by lazy { findViewById<SinglePageInputTextPresenter>(R.id.new_signin_password) }
    private val forgotPassword: View by lazy { findViewById<View>(R.id.new_signin_forgot_password) }
    private val signInButton: Button by lazy { findViewById<Button>(R.id.new_signin_button) }
    private val validationObservable = CombiningFakeObservable()
    private val allFieldsValidSubject = BehaviorSubject.create<Boolean>()

    init {
        View.inflate(context, R.layout.acct__widget_new_account_signin_view, this)
        signInButton.setOnClickListener {
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

    fun configurePOS(enableFacebookButton: Boolean) {
        signInWithFacebookButton.visibility = if (enableFacebookButton) View.VISIBLE else View.INVISIBLE
    }

    fun setEnable(enable: Boolean) {
        signInWithFacebookButton.isEnabled = enable
        emailInput.isEnabled = enable
        passwordInput.isEnabled = enable
        passwordInput.isPasswordVisibilityToggleEnabled(enable)
        forgotPassword.isEnabled = enable
        signInButton.isEnabled = enable
    }
}
