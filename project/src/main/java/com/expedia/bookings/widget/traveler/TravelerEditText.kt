package com.expedia.bookings.widget.traveler

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.design.widget.TextInputLayout
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.EditText
import com.expedia.bookings.R
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.Ui
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeEditText
import com.expedia.util.subscribeTextChange
import com.expedia.vm.traveler.BaseTravelerValidatorViewModel
import rx.Observer
import rx.Subscription
import rx.subjects.BehaviorSubject
import java.util.ArrayList

class TravelerEditText(context: Context, attrs: AttributeSet?) : EditText(context, attrs), View.OnFocusChangeListener {
    var valid = true
    var preErrorDrawable: Drawable? = null

    val errorIcon: Drawable
    var errorContDesc = ""

    private var textChangedSubscription: Subscription? = null
    private var errorSubscription: Subscription? = null
    private var onFocusChangeListeners = ArrayList<OnFocusChangeListener>()

    var viewModel: BaseTravelerValidatorViewModel by notNullAndObservable {
        viewModel.textSubject.subscribeEditText(this)
        subscribeToError(viewModel.errorSubject)
        addTextChangedSubscriber(viewModel.textSubject)
    }

    init {
        onFocusChangeListener = this
        errorIcon = ContextCompat.getDrawable(context,
                Ui.obtainThemeResID(context, R.attr.skin_errorIndicationExclaimationDrawable))
        addTextChangedListener(TravelerTextWatcher())
        if(attrs != null){
            val attrSet = context.theme.obtainStyledAttributes(attrs, R.styleable.TravelerEditText, 0, 0);
            try {
                errorContDesc = attrSet.getString(R.styleable.TravelerEditText_error_cont_desc) ?: ""
            }
            finally {
                attrSet.recycle()
            }
        }
    }

    fun addOnFocusChangeListener(listener: OnFocusChangeListener) {
        onFocusChangeListeners.add(listener)
    }

    fun addTextChangedSubscriber(observer: Observer<String>?) {
        if (observer != null) {
            textChangedSubscription?.unsubscribe()
            textChangedSubscription = this.subscribeTextChange(observer)
        }
    }

    fun subscribeToError(errorSubject: BehaviorSubject<Boolean>?) {
        if (errorSubject != null) {
            errorSubscription?.unsubscribe()
            errorSubscription = errorSubject.subscribe { error ->
                if (error) setError() else resetError()
            }
        }
    }

    override fun onVisibilityChanged(changedView: View?, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility != View.VISIBLE) {
            textChangedSubscription?.unsubscribe()
            errorSubscription?.unsubscribe()
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
        val text = this.text?.toString()
        var hint = this.hint?.toString()
        if (Strings.isEmpty(hint)) {
            hint = (this.parent as? TextInputLayout)?.hint.toString()
        }
        val conDescription = if (Strings.isNotEmpty(this.contentDescription)) {
            this.contentDescription.toString()
        } else ""
        nodeInfo.text = if (!valid) " $hint, $text, $errorContDesc, $conDescription" else " $hint, $text, $conDescription"
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
        if (!hasFocus) {
            viewModel.validate()
        }
        onFocusChangeListeners.forEach { listener ->
            listener.onFocusChange(view, hasFocus)
        }
    }

    private inner class TravelerTextWatcher(): TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
            resetError()
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

    }
}