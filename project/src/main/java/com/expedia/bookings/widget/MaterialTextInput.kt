package com.expedia.bookings.widget

import android.content.Context
import android.support.design.widget.TextInputLayout
import android.text.InputType
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R


public open class MaterialTextInput(context: Context, attrs: AttributeSet?) : TextInputLayout(context, attrs) {

    init {
        View.inflate(context, R.layout.material_text_input, this)

        val attrSet = context.theme.obtainStyledAttributes(attrs, R.styleable.MaterialTextInput, 0, 0);
        try {
            editText.inputType = attrSet.getInteger(R.styleable.MaterialTextInput_android_inputType,
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE)
            editText.textSize = attrSet.getDimension(R.styleable.MaterialTextInput_android_textSize, 0f)
            editText.setSingleLine(attrSet.getBoolean(R.styleable.MaterialTextInput_android_singleLine, false))
            editText.setCompoundDrawablesWithIntrinsicBounds(null, null, attrSet.getDrawable(R.styleable.MaterialTextInput_android_drawableRight), null)
            editText.isFocusable = attrSet.getBoolean(R.styleable.MaterialTextInput_android_focusable, true)
            editText.isFocusableInTouchMode = attrSet.getBoolean(R.styleable.MaterialTextInput_android_focusableInTouchMode, true)
            editText.isCursorVisible = attrSet.getBoolean(R.styleable.MaterialTextInput_android_cursorVisible, true)
            editText.isLongClickable = attrSet.getBoolean(R.styleable.MaterialTextInput_android_longClickable, false)
        } finally {
            attrSet.recycle();
        }
    }
}