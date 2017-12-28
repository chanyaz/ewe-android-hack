package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelOffersResponse
import rx.subjects.PublishSubject

class ValueAddsContainer(context: Context, val attrs: AttributeSet?): LinearLayout(context, attrs) {

    val valueAddsSubject = PublishSubject.create<List<HotelOffersResponse.ValueAdds>>()

    init {
        valueAddsSubject.subscribe { valueAdds ->
            this.removeAllViews()
            for (valueAdd in valueAdds) {
                val valueAddView = LayoutInflater.from(getContext()).inflate(R.layout.checkmark_row, this, false) as TextView
                valueAddView.text = valueAdd.description
                this.addView(valueAddView)
            }
        }
    }
}
