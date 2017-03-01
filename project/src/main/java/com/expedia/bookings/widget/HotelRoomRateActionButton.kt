package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.util.subscribeOnClick
import rx.subjects.PublishSubject

class HotelRoomRateActionButton(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    val viewRoomButton: Button by bindView(R.id.view_room_button)
    val bookButton: Button by bindView(R.id.book_button)
    val soldOutButton: Button by bindView(R.id.sold_out_button)
    val viewRoomClickedSubject = PublishSubject.create<Unit>()
    val bookButtonClickedSubject = PublishSubject.create<Unit>()

    init {
        View.inflate(context, R.layout.hotel_room_rate_action_button, this)
        viewRoomButton.subscribeOnClick(viewRoomClickedSubject)
        bookButton.subscribeOnClick(bookButtonClickedSubject)
        hideAllButtons()
    }

    fun showSoldOutButton() {
        soldOutButton.visibility = View.VISIBLE
        viewRoomButton.visibility = View.GONE
        bookButton.visibility = View.GONE
    }

    fun hideSoldOutButton() {
        soldOutButton.visibility = View.GONE
    }

    fun hideAllButtons() {
        viewRoomButton.visibility = View.GONE
        bookButton.visibility = View.GONE
        soldOutButton.visibility = View.GONE
        soldOutButton.setStateListAnimator(null)
    }



    fun showBookButton() {
        if(soldOutButton.visibility == View.VISIBLE) {
            showSoldOutButton()
        }
        else {
            soldOutButton.visibility = View.GONE
            viewRoomButton.visibility = View.GONE
            bookButton.visibility = View.VISIBLE
        }
    }
    fun showViewRoomButton() {
        if(soldOutButton.visibility == View.VISIBLE) {
            showSoldOutButton()
        }
        else {
            soldOutButton.visibility = View.GONE
            viewRoomButton.visibility = View.VISIBLE
            bookButton.visibility = View.GONE
        }
    }

    fun setViewRoomButtonPadding(padding: Int) {
        viewRoomButton.setPadding(padding, 0, padding, 0)
    }

    fun setBookButtonPadding(padding: Int) {
        bookButton.setPadding(padding, 0, padding, 0)
    }

    fun changeViewRoomButtonTextToSelectForPackages() {
        viewRoomButton.text = resources.getString(R.string.select)
    }
}