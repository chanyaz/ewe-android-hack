package com.expedia.account.newsignin

import android.content.Context
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.account.R
import com.expedia.account.data.Db
import com.expedia.account.singlepage.SinglePageEmailNamePasswordLayout
import com.expedia.account.util.Events

class NewCreateAccountLayout(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    private val emailNamePasswordLayout: SinglePageEmailNamePasswordLayout by lazy { findViewById<SinglePageEmailNamePasswordLayout>(R.id.new_create_account_email_name_password_layout) }
    private val spamCheckboxLayout: LinearLayout by lazy { findViewById<LinearLayout>(R.id.new_agree_to_spam_layout) }
    private val spamCheckbox: CheckBox by lazy { findViewById<CheckBox>(R.id.new_agree_to_spam_checkbox) }
    private val spamOptInText: TextView by lazy { findViewById<TextView>(R.id.new_agree_to_spam_text) }
    private val newTermsTextView: TextView by lazy { findViewById<TextView>(R.id.new_create_account_terms_text) }
    private val createAccountButton: Button by lazy { findViewById<Button>(R.id.new_create_account_button) }

    init {
        View.inflate(context, R.layout.acct__widget_new_create_account_view, this)
        refreshCheckboxColor(spamCheckbox)
        createAccountButton.setOnClickListener {
            if (emailNamePasswordLayout.allTextValidSubject.value) {
                storeDataInNewUser()
                Events.post(Events.NewCreateAccountButtonClicked())
            } else {
                emailNamePasswordLayout.forceCheckAllFields()
            }
        }
        spamCheckbox.setOnCheckedChangeListener { _, isChecked ->
            refreshCheckboxColor(spamCheckbox)
            val user = Db.getNewUser()
            user.expediaEmailOptin = isChecked
            Events.post(Events.UserChangedSpamOptin(isChecked))
            refreshCheckboxContentDesc(spamCheckboxLayout)
        }
    }

    private fun refreshCheckboxContentDesc(checkboxContainer: LinearLayout) {
        val checkbox = checkboxContainer.getChildAt(0) as CheckBox
        val textview = checkboxContainer.getChildAt(1) as TextView
        var text = textview.text.toString()
        if (checkbox.isChecked) {
            text += " " + context.getString(R.string.acct__cont_desc_role_checkbox_checked)
        } else {
            text += " " + context.getString(R.string.acct__cont_desc_role_checkbox_unchecked)
        }
        checkboxContainer.contentDescription = text
    }

    fun configurePOS(showSpamOptIn: Boolean, enableSpamByDefault: Boolean, marketingText: CharSequence, newTermsText: CharSequence) {
        if (showSpamOptIn) {
            spamCheckboxLayout.visibility = View.VISIBLE
            spamOptInText.text = marketingText
            spamOptInText.movementMethod = if (containsLinks(marketingText)) LinkMovementMethod.getInstance() else null
            spamCheckbox.isChecked = enableSpamByDefault
            refreshCheckboxColor(spamCheckbox)
        }
        newTermsTextView.text = newTermsText
        newTermsTextView.movementMethod = if (containsLinks(newTermsText)) LinkMovementMethod.getInstance() else null
    }

    private fun refreshCheckboxColor(v: CheckBox) {
        val color =
                if (v.isChecked) {
                    ContextCompat.getColor(context, R.color.acct__single_page_checkbox_checked_color)
                } else {
                    ContextCompat.getColor(context, R.color.acct__single_page_text_color)
                }
        val drawable = ContextCompat.getDrawable(context, R.drawable.abc_btn_check_material)
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        v.buttonDrawable = drawable
    }

    fun brandIt(brand: String) {
        emailNamePasswordLayout.brandIt(brand)
    }

    private fun containsLinks(text: CharSequence): Boolean {
        if (text !is Spannable) {
            return false
        } else {
            val spans = text.getSpans(0, text.length, ClickableSpan::class.java)
            return spans.size > 0
        }
    }

    private fun storeDataInNewUser() {
        Db.getNewUser().enrollInLoyalty = true
        emailNamePasswordLayout.storeDataInNewUser()
    }

    fun focusEmailAddress() {
        emailNamePasswordLayout.focusEmailAddress()
    }

    fun focusFirstName() {
        emailNamePasswordLayout.focusFirstName()
    }

    fun focusLastName() {
        emailNamePasswordLayout.focusLastName()
    }

    fun focusPassword() {
        emailNamePasswordLayout.focusPassword()
    }

    fun setEnable(enable: Boolean) {
        emailNamePasswordLayout.setEnable(enable)
        spamCheckbox.isEnabled = enable
        createAccountButton.isEnabled = enable
    }
}
