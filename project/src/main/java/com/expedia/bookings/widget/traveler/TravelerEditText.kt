package com.expedia.bookings.widget.traveler

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.EditText
import com.expedia.bookings.R
import com.expedia.bookings.utils.Ui
import com.expedia.util.subscribeTextChange
import rx.Observer
import rx.Subscription
import rx.subjects.PublishSubject

class TravelerEditText(context: Context, attrs: AttributeSet?) : EditText(context, attrs) {
    var valid = true
    var preErrorDrawable: Drawable? = null
    val errorIcon: Drawable
    var errorContDesc = ""

    private var textChangedSubscription: Subscription? = null
    private var errorSubscription: Subscription? = null

    init {
        errorIcon = ContextCompat.getDrawable(context,
                Ui.obtainThemeResID(context, R.attr.skin_errorIndicationExclaimationDrawable))
        addTextChangedListener(TravelerTextWatcher())
        if(attrs != null){
            val attrSet = context.theme.obtainStyledAttributes(attrs, R.styleable.TravelerEditText, 0, 0);
            try {
                errorContDesc = attrSet.getString(R.styleable.TravelerEditText_error_cont_desc) ?: ""
            }
            finally {
                attrSet.recycle();
            }
        }
    }

    fun addTextChangedSubscriber(observer: Observer<String>) {
        textChangedSubscription = this.subscribeTextChange(observer)
    }

    fun subscribeToError(errorSubject: PublishSubject<Boolean>) {
        errorSubscription = errorSubject.subscribe { error ->
            if (error) setError() else resetError()
        }
    }

    override fun onVisibilityChanged(changedView: View?, visibility: Int) {
        if (visibility == GONE) {
            textChangedSubscription?.unsubscribe()
            errorSubscription?.unsubscribe()
        }
        super.onVisibilityChanged(changedView, visibility)
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
        val text = this.text.toString()
        val hint = this.hint.toString()
        nodeInfo.text = if (!valid) " $hint, $text, $errorContDesc" else " $hint, $text"
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