package com.expedia.bookings.rail.widget

import android.view.View
import android.widget.TextView
import com.expedia.util.endlessObserver
import com.expedia.util.subscribeTextChange
import io.reactivex.disposables.CompositeDisposable

class EntryManager(private val formFields: List<TextView>, private val entryForm: FormListener) {
    interface FormListener {
        fun formFocusChanged(view: View, hasFocus: Boolean)
        fun formFieldChanged()
    }

    private var formEntrySubscriptions: CompositeDisposable? = null
    private val formFieldChangedSubscriber = endlessObserver<String> {
        entryForm.formFieldChanged()
    }
    private val formFocusListener = View.OnFocusChangeListener { view, boolean ->
        entryForm.formFocusChanged(view, boolean)
    }

    fun visibilityChanged(visible: Boolean) {
        formEntrySubscriptions?.dispose()
        var viewFocusListener: View.OnFocusChangeListener? = null

        if (visible) {
            formEntrySubscriptions = CompositeDisposable()
            viewFocusListener = formFocusListener
        }

        for (view in formFields) {
            if (visible) {
                formEntrySubscriptions?.add(view.subscribeTextChange(formFieldChangedSubscriber))
            }
            view.onFocusChangeListener = viewFocusListener
        }
    }
}