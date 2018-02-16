package com.expedia.account.singlepage

import android.content.Context
import android.support.design.widget.TextInputLayout
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import com.expedia.account.R
import com.expedia.account.input.BaseInputTextPresenter
import com.expedia.account.util.StatusObservableWrapper
import com.expedia.account.util.Utils
import com.expedia.account.view.AccessibleEditText

open class SinglePageInputTextPresenter(context: Context, attrs: AttributeSet) : BaseInputTextPresenter(context, attrs) {

    private val editTextLayout: TextInputLayout by lazy {
        findViewById<TextInputLayout>(R.id.single_page_text_field_layout)
    }

    init {
        View.inflate(context, R.layout.acct__widget_singlepage_text_input, this)
        editText = findViewById<AccessibleEditText>(R.id.single_page_text_field)

        editTextLayout.hint = hintText
        editText.hint = hintText
        editText.inputType = inputType
        editText.imeOptions = imeOptions
        editText.setErrorContDesc(errorContDesc)

        fixPasswordFont()
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (mValidator != null) {
                    showInternal(mValidator.onNewText(charSequence.toString()))
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })

        editText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (mValidator != null) {
                showInternal(mValidator.onFocusChanged(editText.text.toString(), hasFocus))
            }
        }

        createAndAddTransitions()
        mStatusObservable = StatusObservableWrapper(StatusObservableWrapper.StatusEmitter { this.isGood })
        show(BaseInputTextPresenter.STATE_WAITING)
    }

    fun brandIt(brand: String) {
        Utils.brandHint(editText, brand)
        Utils.brandHint(editTextLayout, brand)
    }

    override fun showError() {
        editText.setStatus(AccessibleEditText.Status.INVALID)
        mStatusObservable.emit(false)
        editTextLayout.isErrorEnabled = true
        editTextLayout.error = errorText
    }

    override fun showGood() {
        editText.setStatus(AccessibleEditText.Status.VALID)
        mStatusObservable.emit(true)
        editTextLayout.isErrorEnabled = false
        editTextLayout.error = null
    }

    override fun showWaiting() {
        editText.setStatus(AccessibleEditText.Status.DEFAULT)
        mStatusObservable.emit(false)
        editTextLayout.isErrorEnabled = false
        editTextLayout.error = null
    }

    override fun showProgress() {
        editText.setStatus(AccessibleEditText.Status.DEFAULT)
        mStatusObservable.emit(false)
        editTextLayout.isErrorEnabled = false
        editTextLayout.error = null
    }

    fun isPasswordVisibilityToggleEnabled(enabled: Boolean) {
        editTextLayout.isPasswordVisibilityToggleEnabled = enabled
    }

    fun doneCheck() {
        if (mValidator != null) {
                showInternal(mValidator.onFocusChanged(editText.text.toString(), false))
        }
        postDelayed({
            editText.clearFocus()
        }, 50)
    }
}
