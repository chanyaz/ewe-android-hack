package com.expedia.bookings.widget.traveler

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.EditText
import com.expedia.bookings.R
import com.expedia.bookings.section.InvalidCharacterHelper
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.extensions.getParentTextInputLayout
import com.expedia.bookings.extensions.subscribeEditText
import com.expedia.bookings.extensions.subscribeTextChange
import com.expedia.util.notNullAndObservable
import com.expedia.vm.traveler.BaseTravelerValidatorViewModel
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import java.util.ArrayList
import android.view.autofill.AutofillManager
import android.view.autofill.AutofillValue

open class TravelerEditText(context: Context, attrs: AttributeSet?) : EditText(context, attrs), View.OnFocusChangeListener {
    var valid = true
    var preErrorDrawable: Drawable? = null

    val errorIcon: Drawable
    var errorContDesc = ""
    val defaultErrorString = context.resources.getString(R.string.accessibility_cont_desc_role_error)

    private var textChangedSubscription: Disposable? = null
    private var errorSubscription: Disposable? = null
    private var onFocusChangeListeners = ArrayList<OnFocusChangeListener>()
    private var autoFillManager: AutofillManager? = null

    var viewModel: BaseTravelerValidatorViewModel by notNullAndObservable {
        viewModel.textSubject.subscribeEditText(this)
        subscribeToError(viewModel.errorSubject)
        addTextChangedSubscriber(viewModel.textSubject)
        viewModel.addInvalidCharacterListener(InvalidCharacterHelper.InvalidCharacterListener { _, mode ->
            val activity = context as AppCompatActivity
            InvalidCharacterHelper.showInvalidCharacterPopup(activity.supportFragmentManager, mode)
        })
    }

    init {
        onFocusChangeListener = this
        errorIcon = ContextCompat.getDrawable(context,
                Ui.obtainThemeResID(context, R.attr.skin_errorIndicationExclaimationDrawable))
        addTextChangedListener(TravelerTextWatcher())
        if (attrs != null) {
            val attrSet = context.theme.obtainStyledAttributes(attrs, R.styleable.TravelerEditText, 0, 0)
            try {
                errorContDesc = attrSet.getString(R.styleable.TravelerEditText_error_cont_desc) ?: ""
            } finally {
                attrSet.recycle()
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            autoFillManager = context.getSystemService(AutofillManager::class.java)
        }
    }

    fun addOnFocusChangeListener(listener: OnFocusChangeListener) {
        onFocusChangeListeners.add(listener)
    }

    fun addTextChangedSubscriber(observer: Observer<String>?) {
        if (observer != null) {
            textChangedSubscription?.dispose()
            textChangedSubscription = this.subscribeTextChange(observer)
        }
        InvalidCharacterHelper.generateInvalidCharacterTextWatcher(this, viewModel, viewModel.invalidCharacterMode)
    }

    fun subscribeToError(errorSubject: BehaviorSubject<Boolean>?) {
        if (errorSubject != null) {
            errorSubscription?.dispose()
            errorSubscription = errorSubject.subscribe { error ->
                if (error) setError() else resetError()
            }
        }
    }

    override fun onVisibilityChanged(changedView: View?, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility != View.VISIBLE) {
            textChangedSubscription?.dispose()
            errorSubscription?.dispose()
        }
    }

    private fun setError() {
        if (valid) {
            errorIcon.bounds = Rect(0, 0, errorIcon.intrinsicWidth, errorIcon.intrinsicHeight)
            val compounds = compoundDrawables
            if (compounds != null) {
                preErrorDrawable = compounds[2]
                setCompoundDrawablesWithIntrinsicBounds(compounds[0], compounds[1], errorIcon, compounds[3])
                valid = false
            }
        }
    }

    override fun onInitializeAccessibilityNodeInfo(nodeInfo: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(nodeInfo)
        nodeInfo.text = getAccessibilityNodeInfo()
    }

    fun getAccessibilityNodeInfo(): String {
        val text = this.text?.toString()
        var hint = this.hint?.toString()
        if (Strings.isEmpty(hint)) {
            hint = this.getParentTextInputLayout()?.hint.toString()
        }
        val error = this.getParentTextInputLayout()?.error ?: errorContDesc
        val conDescription = if (Strings.isNotEmpty(this.contentDescription)) {
            this.contentDescription.toString()
        } else ""
        return if (!valid) " $hint, $text, $defaultErrorString, $error, $conDescription" else " $hint, $text, $conDescription"
    }

    private fun resetError() {
        if (!valid) {
            val compounds = compoundDrawables
            if (compounds != null) {
                setCompoundDrawablesWithIntrinsicBounds(compounds[0], compounds[1], preErrorDrawable, compounds[3])
                valid = true
            }
        }
    }

    override fun onFocusChange(view: View?, hasFocus: Boolean) {
        if (hasFocus) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (text.isEmpty()) {
                    // Prevents incorrect auto-fill content from popping up
                    autoFillManager?.cancel()
                }
            }
        } else {
            viewModel.validate()
        }
        onFocusChangeListeners.forEach { listener ->
            listener.onFocusChange(view, hasFocus)
        }
    }

    override fun autofill(value: AutofillValue?) {
        var autofillValue = getAutofillValue(value)
        super.autofill(autofillValue)
        viewModel.validate()
    }

    // PhoneNumberEditText which extends this class retrieves autofill value in a different manner
    open fun getAutofillValue(value: AutofillValue?): AutofillValue? {
        return value
    }

    private inner class TravelerTextWatcher : TextWatcher {
        override fun afterTextChanged(p0: Editable?) = viewModel.errorSubject.onNext(false)
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit
    }
}
