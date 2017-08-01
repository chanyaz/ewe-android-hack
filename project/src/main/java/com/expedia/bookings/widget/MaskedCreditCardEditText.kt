package com.expedia.bookings.widget

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import com.expedia.bookings.utils.NumberMaskFormatter
import com.expedia.bookings.widget.accessibility.AccessibleEditText
import io.reactivex.subjects.PublishSubject

class MaskedCreditCardEditText(context: Context, attributeSet: AttributeSet?) : AccessibleEditText(context, attributeSet) {
    val cardNumberTextSubject = PublishSubject.create<String>()

    init {
        cardNumberTextSubject.subscribe { text ->
            clearAndSetText(text)
        }
    }

    fun showMaskedNumber(number: String) {
        setText(NumberMaskFormatter.obscureCreditCardNumber(number))
        setSelection(text.length)
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection {
        outAttrs.inputType = InputType.TYPE_CLASS_NUMBER
        outAttrs.imeOptions = EditorInfo.IME_ACTION_NEXT
        return NumericKeyWatcher(this, false, cardNumberTextSubject)
    }

    private fun clearAndSetText(text: CharSequence?) {
        val letter = text?.lastOrNull()?.toString() ?: ""
        setText(letter)
    }

    override fun onCheckIsTextEditor(): Boolean {
        return true
    }
}