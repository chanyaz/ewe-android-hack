package com.expedia.account.view

import android.content.Context
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.account.R
import com.expedia.account.data.Db
import com.expedia.account.input.rules.ExpediaEmailInputRule
import com.expedia.account.input.rules.ExpediaPasswordInputRule
import com.expedia.account.singlepage.SinglePageInputTextPresenter
import com.expedia.account.util.CombiningFakeObservable
import com.expedia.account.util.Events
import com.expedia.account.util.Utils
import com.expedia.bookings.text.HtmlCompat
import io.reactivex.subjects.BehaviorSubject

class FacebookLinkAccountsLayout(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    enum class SetupType {
        ACCOUNT_NOT_LINKED,
        ACCOUNT_EXISTING
    }

    val toolbar: Toolbar by lazy { findViewById<Toolbar>(R.id.new_facebook_toolbar) }
    private val message: TextView by lazy { findViewById<TextView>(R.id.new_facebook_header_message) }
    private val email: SinglePageInputTextPresenter by lazy { findViewById<SinglePageInputTextPresenter>(R.id.new_facebook_email_address) }
    private val password: SinglePageInputTextPresenter by lazy { findViewById<SinglePageInputTextPresenter>(R.id.new_facebook_password) }
    private val forgotPassword: View by lazy { findViewById<View>(R.id.new_facebook_forgot_password) }
    private val linkAccountsButton: Button by lazy { findViewById<Button>(R.id.new_facebook_link_accounts_button) }

    var brand: String? = null

    private var validationObservable = CombiningFakeObservable()
    private val allTextValidSubject = BehaviorSubject.create<Boolean>()

    init {
        View.inflate(context, R.layout.acct__widget_facebook_link_accounts, this)
        validationObservable = CombiningFakeObservable()
        validationObservable.addSource(email.statusObservable)
        validationObservable.addSource(password.statusObservable)
        password.isPasswordVisibilityToggleEnabled(true)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        email.setValidator(ExpediaEmailInputRule())
        password.setValidator(ExpediaPasswordInputRule())
        password.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                password.doneCheck()
            }
            false
        }
        forgotPassword.setOnClickListener {
            Events.post(Events.NewForgotPasswordButtonClicked())
        }
        linkAccountsButton.setOnClickListener {
            if (allTextValidSubject.value) {
                storeDataInNewUser()
                Events.post(Events.NewLinkFromFacebookFired())
            } else {
                email.forceCheckWithFocus(false)
                password.forceCheckWithFocus(false)
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        validationObservable.subscribe(allTextValidSubject)
    }

    override fun onDetachedFromWindow() {
        validationObservable.unsubscribe(allTextValidSubject)
        super.onDetachedFromWindow()
    }

    fun setupForAccountNotLinked() {
        val formatted = Utils.obtainBrandedPhrase(context,
                R.string.acct__fb_enter_your_brand_credentials, brand)
                .format()
        message.text = formatted
        val user = Db.getNewUser()
        user.email = ""
        email.setText(user.email)
        email.isEnabled = true
        email.visibility = View.VISIBLE
        email.requestFocus(true)
        password.visibility = View.VISIBLE
    }

    fun setupForAccountExisting() {
        val user = Db.getNewUser()
        val formatted = Utils.obtainBrandedPhrase(context,
                R.string.acct__fb_weve_found_your_account, brand)
                .put("email_address", user.email)
                .format()
                .toString()
        message.text = HtmlCompat.fromHtml(formatted)
        email.setText(user.email)
        email.isEnabled = false
        email.visibility = View.VISIBLE
        password.visibility = View.VISIBLE
        password.requestFocus(true)
    }

    fun clearAll() {
        email.setText("")
        password.setText("")
    }

    private fun storeDataInNewUser() {
        Db.getNewUser().email = email.text
        Db.getNewUser().password = password.text
    }
}
