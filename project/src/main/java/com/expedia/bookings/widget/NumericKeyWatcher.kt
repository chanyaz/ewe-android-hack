package com.expedia.bookings.widget

import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.BaseInputConnection
import io.reactivex.subjects.PublishSubject

class NumericKeyWatcher(view: View, mutable: Boolean, val outPutTextSubject: PublishSubject<String>) : BaseInputConnection(view, mutable) {
    val DELETION_TEXT = ""

    override fun sendKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN ) {
            if (event.keyCode == KeyEvent.KEYCODE_DEL) {
                outPutTextSubject.onNext(DELETION_TEXT)
            } else if (isNumericKeycode(event)) {
                outPutTextSubject.onNext(event.displayLabel.toString())
            } else {
                outPutTextSubject.onNext(DELETION_TEXT)
            }
        }
        return false
    }

    override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
        // magic: in latest Android, deleteSurroundingText(1, 0) will be called for backspace
        if (beforeLength == 1 && afterLength == 0) {
            return sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL)) && sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL))
        }

        return super.deleteSurroundingText(beforeLength, afterLength)
    }

    private fun isNumericKeycode(event: KeyEvent): Boolean {
        return (event.keyCode == KeyEvent.KEYCODE_0 ||
                event.keyCode == KeyEvent.KEYCODE_1 ||
                event.keyCode == KeyEvent.KEYCODE_2 ||
                event.keyCode == KeyEvent.KEYCODE_3 ||
                event.keyCode == KeyEvent.KEYCODE_4 ||
                event.keyCode == KeyEvent.KEYCODE_5 ||
                event.keyCode == KeyEvent.KEYCODE_6 ||
                event.keyCode == KeyEvent.KEYCODE_7 ||
                event.keyCode == KeyEvent.KEYCODE_8 ||
                event.keyCode == KeyEvent.KEYCODE_9)
    }
}
