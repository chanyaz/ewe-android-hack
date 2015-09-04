package com.expedia.bookings.widget

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import android.widget.TableLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView

public class HotelReviewsPageView(context: Context) : ScrollView(context) {

    val reviewsTable: TableLayout by bindView(R.id.reviews_table)
    val summaryContainer: LinearLayout by bindView(R.id.summary_container)

    init {
        View.inflate(getContext(), R.layout.hotel_reviews_page_widget, this)
    }


}




