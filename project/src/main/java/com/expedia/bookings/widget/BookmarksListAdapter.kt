package com.expedia.bookings.widget

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.expedia.bookings.R
import com.expedia.bookings.data.Bookmark
import com.expedia.bookings.utils.bindView

class BookmarksListAdapter(val bookmarksList: ArrayList<Bookmark>): RecyclerView.Adapter<BookmarksListAdapter.BookmarkViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
        val item = LayoutInflater.from(parent.context).inflate(R.layout.bookmark_item, parent, false)
        return BookmarkViewHolder(item)
    }

    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
        holder.bindItems(bookmarksList[position])
    }

    override fun getItemCount(): Int {
        return bookmarksList.size
    }

    class BookmarkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val titleTextView: TextView by bindView(R.id.trip_title)
        val numTravelersText: TextView by bindView(R.id.number_of_travelers)
        val tripDateText: TextView by bindView(R.id.trip_date)

        fun bindItems(bookmark: Bookmark) {
            titleTextView.text = bookmark.title
            tripDateText.text = bookmark.dateOfTrip.toString()
            numTravelersText.text = bookmark.numberOfGuests.toString()
            itemView.setOnClickListener {
                Toast.makeText(itemView.context, bookmark.deeplinkURL, Toast.LENGTH_SHORT).show()
            }
        }
    }
}