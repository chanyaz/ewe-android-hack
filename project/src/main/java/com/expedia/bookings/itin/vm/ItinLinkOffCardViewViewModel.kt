package com.expedia.bookings.itin.vm

import android.content.Intent
import io.reactivex.subjects.PublishSubject

abstract class ItinLinkOffCardViewViewModel {

    data class CardViewParams(
            val heading: CharSequence,
            val subHeading: CharSequence?,
            val wrapSubHeading: Boolean,
            val iconId: Int,
            val intent: Intent?
    )

    val cardViewParamsSubject: PublishSubject<CardViewParams> = PublishSubject.create()

    abstract fun updateCardView(params: CardViewParams)
}
