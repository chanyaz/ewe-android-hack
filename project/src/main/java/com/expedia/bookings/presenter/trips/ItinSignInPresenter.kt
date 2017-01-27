package com.expedia.bookings.presenter.trips

import android.app.Activity
import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.activity.AccountLibActivity
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.User
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.squareup.phrase.Phrase

class ItinSignInPresenter(context: Context, attr: AttributeSet?) : Presenter(context, attr) {
    val signInContainer: CardView by bindView(R.id.account_sign_in_container)
    val signInText: TextView by bindView(R.id.login_text_view)

    init {
        View.inflate(context, R.layout.itin_sign_in_presenter, this)

        signInText.text = Phrase.from(this, R.string.Sign_in_with_TEMPLATE)
                .putOptional("brand", BuildConfig.brand)
                .format()
        signInText.contentDescription = Phrase.from(this, R.string.Sign_in_with_cont_desc_TEMPLATE)
                .put("brand", BuildConfig.brand)
                .format()
        signInContainer.setOnClickListener {
            val args = AccountLibActivity.createArgumentsBundle(LineOfBusiness.ITIN, null)
            User.signIn(context as Activity, args)
        }

    }
}