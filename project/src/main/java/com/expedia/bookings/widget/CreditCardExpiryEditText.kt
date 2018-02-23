package com.expedia.bookings.widget

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import com.expedia.bookings.widget.accessibility.AccessibleEditText

class CreditCardExpiryEditText(context: Context, attributeSet: AttributeSet?) : AccessibleEditText(context, attributeSet) {

    private var textViewLengthBeforeEditing: Int = 0
    private val space = "/"
    private val initialMonthCharToAdd = "0"
    init {
        this.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                var deletion = false
                if (textViewLengthBeforeEditing > s.length) {
                    deletion = true
                }
                if (s.length == 1 && Character.getNumericValue(s.elementAt(0)) >= 2) {
                    s.insert(0, initialMonthCharToAdd)
                } else if (s.length == 2 && !deletion) {
                    s.insert(2, space)
                } else if (s.length == 2 && deletion) {
                    s.delete(1, 2)
                }
            }

            override fun beforeTextChanged(s: CharSequence, p1: Int, p2: Int, p3: Int) {
                textViewLengthBeforeEditing = s.length
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }
        })
    }
}
