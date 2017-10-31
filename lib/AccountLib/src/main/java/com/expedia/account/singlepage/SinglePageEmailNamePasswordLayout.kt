package com.expedia.account.singlepage

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import com.expedia.account.R
import com.expedia.account.data.Db
import com.expedia.account.input.InputValidator
import com.expedia.account.input.LookupNameValidator
import com.expedia.account.input.rules.ExpediaEmailInputRule
import com.expedia.account.input.rules.ExpediaNameInputRule
import com.expedia.account.input.rules.ExpediaPasswordInputRule
import com.expedia.account.util.CombiningFakeObservable
import com.expedia.account.util.InvalidCharacterTextWatcher
import rx.subjects.BehaviorSubject

class SinglePageEmailNamePasswordLayout(context: Context, attrs: AttributeSet): LinearLayout (context, attrs) {

    private val vFirstNameInput: SinglePageInputTextPresenter by lazy {
        findViewById<SinglePageInputTextPresenter>(R.id.single_page_first_name)
    }
    private val vLastNameInput: SinglePageInputTextPresenter by lazy {
        findViewById<SinglePageInputTextPresenter>(R.id.single_page_last_name)
    }
    private val vEmailAddress: SinglePageInputTextPresenter by lazy {
        findViewById<SinglePageInputTextPresenter>(R.id.single_page_email_address)
    }
    private val vPassword: SinglePageInputTextPresenter by lazy {
        findViewById<SinglePageInputTextPresenter>(R.id.single_page_password)
    }

    private val mFirstNameValidator = LookupNameValidator(ExpediaNameInputRule())
    private val mLastNameValidator = LookupNameValidator(ExpediaNameInputRule())

    val mValidationObservable = CombiningFakeObservable()
    val allTextValidSubject = BehaviorSubject.create<Boolean>()

    init{
        orientation = LinearLayout.VERTICAL
        View.inflate(context, R.layout.acct__widget_single_page_email_name_password, this)
        mValidationObservable.addSource(vEmailAddress.statusObservable)
        mValidationObservable.addSource(vFirstNameInput.statusObservable)
        mValidationObservable.addSource(vLastNameInput.statusObservable)
        mValidationObservable.addSource(vPassword.statusObservable)
        vPassword.isPasswordVisibilityToggleEnabled(true)
        mValidationObservable.subscribe(allTextValidSubject)
    }

    fun storeDataInNewUser() {
        Db.getNewUser().email = vEmailAddress.text.toString()
        Db.getNewUser().firstName = vFirstNameInput.text
        Db.getNewUser().lastName = vLastNameInput.text
        Db.getNewUser().password = vPassword.text
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        vFirstNameInput.setValidator(mFirstNameValidator)
        vLastNameInput.setValidator(mLastNameValidator)
        vFirstNameInput.addTextChangedListener(InvalidCharacterTextWatcher(null))
        vLastNameInput.addTextChangedListener(InvalidCharacterTextWatcher(null))
        vEmailAddress.setValidator(object : InputValidator(ExpediaEmailInputRule()) {})
        vPassword.setValidator(object : InputValidator(ExpediaPasswordInputRule()) {})
        vPassword.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                vPassword.doneCheck()
                true
            }
            false
        }
    }

    fun forceCheckAllFields() {
        vFirstNameInput.forceCheckWithFocus(false)
        vLastNameInput.forceCheckWithFocus(false)
        vEmailAddress.forceCheckWithFocus(false)
        vPassword.forceCheckWithFocus(false)
    }

    fun brandIt(brand: String) {
        vEmailAddress.brandIt(brand)
        vFirstNameInput.brandIt(brand)
        vLastNameInput.brandIt(brand)
        vPassword.brandIt(brand)
    }
}