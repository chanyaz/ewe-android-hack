package com.expedia.bookings.presenter

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import com.expedia.bookings.utils.Constants

/**
 * A presenter that can also have intent as its child
 */
open class IntentPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

    override fun handleNewState(newState: Any, flags: Int) {
        val newStateToShow = newState
        if (newStateToShow is Intent) {
            val newStateId = newStateToShow.component.className
            if (flags and Presenter.FLAG_CLEAR_TOP == Presenter.FLAG_CLEAR_TOP) {
                var index = -1
                for (i in backstack.indices) {
                    if ((backstack[i] as? Intent)?.component?.className == newStateId) {
                        index = i
                        break
                    }
                }
                if (index == -1) {
                    return
                }
                while (backstack.size > index + 1) {
                    backstack.pop()
                }
                return
            }

            val requestCode = newStateToShow.getIntExtra(Constants.REQUEST, -1)
            (context as AppCompatActivity).startActivityForResult(newStateToShow, requestCode)
            return
        }
        super.handleNewState(newStateToShow, flags)
    }

    override fun handleBack(flags: Int, currentChild: Any): Boolean {
        if (currentChild is Intent) {
            val requestCode = currentChild.getIntExtra(Constants.REQUEST, -1)
            (context as AppCompatActivity).startActivityForResult(currentChild, requestCode)
            return true
        }
        return super.handleBack(flags, currentChild)
    }
}
