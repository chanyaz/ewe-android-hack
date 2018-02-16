package com.expedia.bookings.hotel.widget

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.accessibility.AccessibleEditText
import io.reactivex.subjects.PublishSubject

class HotelNameFilterView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    val filterNameChangedSubject = PublishSubject.create<CharSequence>()

    private val filterHotelName: AccessibleEditText by bindView(R.id.filter_hotel_name_edit_text)
    private val clearNameButton: ImageView by bindView(R.id.clear_search_button)

    init {
        View.inflate(context, R.layout.hotel_filter_name_view, this)
        filterHotelName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                clearNameButton.visibility = if (Strings.isEmpty(s)) View.GONE else View.VISIBLE
                filterNameChangedSubject.onNext(s)
            }
        })

        filterHotelName.setOnFocusChangeListener { _, isFocus ->
            if (!isFocus) {
                Ui.hideKeyboard(this)
            }
        }

        clearNameButton.setOnClickListener {
            filterHotelName.text = null
        }
    }

    fun reset() {
        //check if filterHotelName is empty to avoid calling handleFiltering
        if (filterHotelName.text.length > 0) filterHotelName.text.clear()
    }

    fun resetFocus() {
        filterHotelName.clearFocus()
    }

    fun updateName(name: String) {
        filterHotelName.setText(name)
    }
}
