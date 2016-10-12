package com.expedia.bookings.widget.traveler

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.accessibility.AccessibleSpinner
import com.expedia.util.notNullAndObservable
import com.expedia.vm.traveler.BaseTravelerValidatorViewModel
import java.util.ArrayList

class TravelerSpinner(context: Context, attrs: AttributeSet?) : AccessibleSpinner(context, attrs), View.OnFocusChangeListener {
    private var onFocusChangeListeners = ArrayList<OnFocusChangeListener>()

    var viewModel: BaseTravelerValidatorViewModel by notNullAndObservable {

    }

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        onFocusChangeListener = this
    }

    fun addOnFocusChangeListener(listener: OnFocusChangeListener) {
        onFocusChangeListeners.add(listener)
    }

    override fun onFocusChange(view: View?, hasFocus: Boolean) {
        if (hasFocus) {
            Ui.hideKeyboard(this)
            performClick()
        } else {
            viewModel.validate()
        }
        onFocusChangeListeners.forEach { listener ->
            listener.onFocusChange(view, hasFocus)
        }
    }
}