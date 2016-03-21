package com.expedia.bookings.widget.feeds

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.OptimizedImageView
import java.util.*

class FeedsListAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val feedList = listOf<Any>(StaffPickViewModel("", "Traverse City", "Michigan")) // TODO - remove once we have real data

    enum class FeedTypes {
        STAFF_PICK_HOLDER
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when(holder.itemViewType) {

            FeedTypes.STAFF_PICK_HOLDER.ordinal -> {
                val staffPickHolder = holder as StaffPickCardHolder
                val viewModel = feedList[position] as StaffPickViewModel

                val placeholder = ContextCompat.getDrawable(staffPickHolder.itemView.context, R.drawable.results_list_placeholder)
                staffPickHolder.backgroundImage.setImageDrawable(placeholder)
                staffPickHolder.firstLineTextView.text = viewModel.firstLine
                staffPickHolder.secondLineTextView.text = viewModel.secondLine
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val itemType = feedList[position].javaClass

        return when(itemType) {
            StaffPickViewModel::class.java -> FeedTypes.STAFF_PICK_HOLDER.ordinal

            else -> -1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        val context = parent.context

        return when(viewType) {
            FeedTypes.STAFF_PICK_HOLDER.ordinal -> {
                val view = LayoutInflater.from(context).inflate(R.layout.feeds_staff_pick_card, parent, false)
                StaffPickCardHolder(view)
            }

            else -> null
        }
    }

    override fun getItemCount(): Int {
        return feedList.count()
    }

    class StaffPickCardHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val backgroundImage: OptimizedImageView by bindView(R.id.background_image)
        val firstLineTextView: TextView by bindView(R.id.first_line)
        val secondLineTextView: TextView by bindView(R.id.second_line)
    }

    data class StaffPickViewModel(val backgroundImageUrl: String, val firstLine: String, val secondLine: String)

}
