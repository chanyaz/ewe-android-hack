package com.expedia.bookings.widget
import android.app.PendingIntent

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build

import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import com.expedia.bookings.R
import com.expedia.bookings.data.Bookmark
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.TravelerParams
import com.expedia.bookings.utils.BookmarkUtils
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.bindView
import rx.subjects.PublishSubject
import java.util.ArrayList

class BookmarksListAdapter(var bookmarksList: ArrayList<Bookmark>) : RecyclerView.Adapter<BookmarksListAdapter.BookmarkViewHolder>() {

    var notifyDataChange = PublishSubject.create<Unit>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
        val item = LayoutInflater.from(parent.context).inflate(R.layout.bookmark_item, parent, false)
        notifyDataChange.subscribe {
            bookmarksList.clear()
            bookmarksList = BookmarkUtils.getAllBookmarks(parent.context)
            notifyDataSetChanged()
        }
        return BookmarkViewHolder(parent.context, item, notifyDataChange)
    }


    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
        holder.bindItems(bookmarksList[position])
    }

    override fun getItemCount(): Int {
        return bookmarksList.size
    }

    class BookmarkViewHolder(val context: Context, itemView: View, var notifyDataChange: PublishSubject<Unit>) : RecyclerView.ViewHolder(itemView), PopupMenu.OnMenuItemClickListener {

        val titleTextView: TextView by bindView(R.id.trip_title)
        val subtitleTextView: TextView by bindView(R.id.bookmark_subtitle)
        val bookmarkTypeIcon: ImageView by bindView(R.id.bookmark_icon)
        val lineOfBusinessTextView: TextView by bindView(R.id.bookmark_type_textView)
        val shareButton: ImageView by bindView(R.id.bookmark_share_button)
        val editTraveler: ImageView by bindView(R.id.edit_traveler_button)

        val travelerWidget: TravelerWidgetV2 by lazy {
            var travelerWidget = TravelerWidgetV2(context, null)
            travelerWidget
        }

        fun bindItems(bookmark: Bookmark) {
            titleTextView.text = bookmark.title
            setBookmarkSubtitleText(bookmark)
            setBookmarkIcon(bookmark)
            setLobString(bookmark)
            itemView.setOnClickListener {
                Toast.makeText(itemView.context, bookmark.title, Toast.LENGTH_SHORT).show()
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(bookmark.deeplinkURL))
                context.startActivity(intent)
            }
            editTraveler.setOnClickListener { openTravelerWidget(bookmark, editTraveler) }

            shareButton.setOnClickListener {
                shareBookmark(bookmark)
            }
        }

        private fun setBookmarkSubtitleText(bookmark: Bookmark) {
            val numOfTravelersString = if (bookmark.numberOfGuests > 1) "travelers" else "traveler"
            val formattedStartDate = LocaleBasedDateFormatUtils.localDateToMMMd(bookmark.startDate)
            val formattedEndDate = LocaleBasedDateFormatUtils.localDateToMMMd(bookmark.endDate)
            subtitleTextView.text = "$formattedStartDate - $formattedEndDate, ${bookmark.numberOfGuests} $numOfTravelersString"
        }

        private fun setBookmarkIcon(bookmark: Bookmark) {
            val icon = when (bookmark.lineOfBusiness) {
                LineOfBusiness.FLIGHTS_V2 -> R.drawable.flights_details_icon_flight
                LineOfBusiness.HOTELS -> R.drawable.ic_lob_hotels
                LineOfBusiness.PACKAGES -> R.drawable.ic_lob_packages
                else -> R.drawable.ic_stat_expedia
            }
            bookmarkTypeIcon.setImageDrawable(context.getDrawable(icon))
        }

        private fun setLobString(bookmark: Bookmark) {
            lineOfBusinessTextView.text = when (bookmark.lineOfBusiness) {
                LineOfBusiness.FLIGHTS_V2 -> "Flights"
                LineOfBusiness.HOTELS -> "Hotels"
                LineOfBusiness.PACKAGES -> "Hotel + Flights"
                else -> "Trip"
            }
        }

        var currentBookmark: Bookmark? = null

        private fun openTravelerWidget(bookmark: Bookmark, view: View) {
            currentBookmark = bookmark
            val popup = PopupMenu(context, view)
            val inflater = popup.menuInflater
            inflater.inflate(R.menu.bookmark_menu, popup.menu);
            popup.show()
        }

        override fun onMenuItemClick(item: MenuItem): Boolean {
            when (item.getItemId()) {
                R.id.edit_traveler -> {
                    val bookmark = currentBookmark!!
                    travelerWidget.traveler.getViewModel().travelerParamsObservable.onNext(TravelerParams(bookmark.numberOfGuests, emptyList(), emptyList(), emptyList()))
                    travelerWidget.traveler.getViewModel().travelerParamsObservable.subscribe { it ->
                        var index = getBookMarkIndex(bookmark)
                        val oldGuests = bookmark.numberOfGuests
                        val guest = it.numberOfAdults
                        var deepLinkURL = bookmark.deeplinkURL.replace("&noOfTravelers=" + oldGuests, "&noOfTravelers=" + it.numberOfAdults)
                        var bookMarkItem = BookmarkUtils.getAllBookmarks(context)[index]
                        bookMarkItem.numberOfGuests = guest
                        bookMarkItem.deeplinkURL = deepLinkURL
                        var aa = BookmarkUtils.getAllBookmarks(context)
                        aa.set(index, bookMarkItem)
                        BookmarkUtils.saveAllBookmarks(context, aa)
                        notifyDataChange.onNext(Unit)
                    }
                    travelerWidget.travelerDialog.show()
                    return true
                }
                else -> return false
            }
        }

        private fun shareBookmark(bookmark: Bookmark) {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Checkout out this trip I created")

            val title = bookmark.title.replace("Trip", "trip")
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Hey, I'd like you to checkout this "+ title +" that I created. "+bookmark.deeplinkURL.replace(" ", ""))
            shareIntent.type = "text/plain"

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                context.startActivity(shareIntent)
            } else {
                val receiver = Intent(context, Bookmark::class.java)
                val pendingIntent = PendingIntent.getBroadcast(context, 0, receiver, PendingIntent.FLAG_UPDATE_CURRENT)
                val chooserIntent = Intent.createChooser(shareIntent, "", pendingIntent.intentSender)
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, shareIntent)
                context.startActivity(chooserIntent)
            }
        }


        private fun getBookMarkIndex(bookmark: Bookmark): Int {
            var bookmarkIndex = 0
            BookmarkUtils.getAllBookmarks(context).forEachIndexed { index, it ->
                    if (it.title == bookmark.title && it.deeplinkURL == bookmark.deeplinkURL && it.endDate == bookmark.endDate
                            && it.lineOfBusiness == bookmark.lineOfBusiness && it.startDate == bookmark.startDate && it.numberOfGuests == bookmark.numberOfGuests) {
                        bookmarkIndex = index
                    }
            }
            return bookmarkIndex
        }
    }
}