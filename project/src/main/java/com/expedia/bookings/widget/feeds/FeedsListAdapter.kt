package com.expedia.bookings.widget.feeds

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.OptimizedImageView
import com.expedia.bookings.widget.TextView

class FeedsListAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val feedList =
            listOf(StaffPickViewModel("", "Traverse City", "Michigan"),
                    FlightSearchViewModel("SFO", "AUS", true, "2", "Mar 22 - Mar 23", "$42", "2m")) // TODO - Remove mock data once we have a real response
    
    enum class FeedTypes {
        STAFF_PICK_HOLDER,
        FLIGHT_SEARCH_HOLDER
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when(holder.itemViewType) {

            FeedTypes.STAFF_PICK_HOLDER.ordinal -> {
                val staffPickHolder = holder as StaffPickCardHolder
                val viewModel = feedList[position] as StaffPickViewModel

                staffPickHolder.bind(viewModel)
            }

            FeedTypes.FLIGHT_SEARCH_HOLDER.ordinal -> {
                val flightSearchHolder = holder as FlightSearchCardHolder
                val viewModel = feedList[position] as FlightSearchViewModel

                flightSearchHolder.bind(viewModel)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val itemType = feedList[position].javaClass

        return when(itemType) {
            StaffPickViewModel::class.java -> FeedTypes.STAFF_PICK_HOLDER.ordinal

            FlightSearchViewModel::class.java -> FeedTypes.FLIGHT_SEARCH_HOLDER.ordinal

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

            FeedTypes.FLIGHT_SEARCH_HOLDER.ordinal -> {
                val view = LayoutInflater.from(context).inflate(R.layout.feeds_flight_search_card, parent, false)
                FlightSearchCardHolder(view)
            }

            else -> null
        }
    }

    override fun getItemCount(): Int {
        return feedList.count()
    }

    data class StaffPickViewModel(val backgroundImageUrl: String, val firstLine: String, val secondLine: String)
    class StaffPickCardHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val backgroundImage: OptimizedImageView by bindView(R.id.background_image)
        val firstLineTextView: TextView by bindView(R.id.first_line)
        val secondLineTextView: TextView by bindView(R.id.second_line)

        fun bind(viewModel: StaffPickViewModel) {
            val placeholder = ContextCompat.getDrawable(itemView.context, R.drawable.results_list_placeholder)
            backgroundImage.setImageDrawable(placeholder)
            firstLineTextView.text = viewModel.firstLine
            secondLineTextView.text = viewModel.secondLine
        }
    }

    data class FlightSearchViewModel(val origin: String, val destination: String, val isReturn: Boolean, val travelersLabel: String,
                                     val departureReturnDate: String, val currentPrice: String, val lastUpdated: String)
    class FlightSearchCardHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val origin: TextView by bindView(R.id.origin)
        val destination: TextView by bindView(R.id.destination)
        val numberTravelers: TextView by bindView(R.id.numberTravelers)
        val departureAndReturnDates: TextView by bindView(R.id.departureAndReturnDates)
        val price: TextView by bindView(R.id.price)
        val freshnessTimeTextView: TextView by bindView(R.id.freshness_time)
        val backgroundImage: OptimizedImageView by bindView(R.id.background_image)

        fun bind(vm: FlightSearchViewModel) {
            origin.text = vm.origin
            destination.text = vm.destination
            numberTravelers.text = vm.travelersLabel
            departureAndReturnDates.text = vm.departureReturnDate
            price.text = vm.currentPrice
            freshnessTimeTextView.text = vm.lastUpdated
        }
    }
}
