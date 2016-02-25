package com.expedia.bookings.widget.traveler

import android.text.Editable
import android.text.TextWatcher
import com.expedia.bookings.widget.traveler.TravelerEditText
import rx.Observer

class TravelerEditTextWatcher(val observer: Observer<String>, val view: TravelerEditText): TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        //do nothing
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        //do nothing
    }

    override fun afterTextChanged(s: Editable?) {
        view.resetError()
        observer.onNext(s.toString())
    }
}