package com.expedia.bookings.extensions

import android.os.Build
import android.widget.EditText
import io.reactivex.Observable

fun EditText.subscribeMaterialFormsError(observer: Observable<Boolean>, errorMessageId: Int, rightDrawableId: Int = 0) {
    observer.subscribe { hasError ->
        this.setRightDrawable(rightDrawableId)

        val errorMessage = this.context.resources.getString(errorMessageId)
        val parentTextInputLayout = this.getParentTextInputLayout()
        if (parentTextInputLayout != null) {
            this.setParentTextInputLayoutError(parentTextInputLayout, hasError, errorMessage)

            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP && this.paddingBottom != 8) {
                this.updatePaddingForOldApi()
            }
        }
    }
}
