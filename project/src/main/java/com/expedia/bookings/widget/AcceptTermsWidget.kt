package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.util.publishOnClick
import com.expedia.vm.AcceptTermsViewModel

public class AcceptTermsWidget(context: Context, attrs: AttributeSet): LinearLayout(context, attrs) {

    val acceptButton: Button by bindView(R.id.i_accept_terms_button)
    val vm = AcceptTermsViewModel()

    override fun onFinishInflate() {
        acceptButton.setOnClickListener {
            this.visibility = View.INVISIBLE
            vm.acceptedTermsObservable.onNext(true)
        }
    }
}
