package com.expedia.bookings.itin.triplist

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.widget.TextView

class TripListView(context: Context) : LinearLayout(context) {
    //val textView by bindView<TextView>(R.id.text_view_test_fragment)
    //val recyclerView by bindView<RecyclerView>(R.id.recycler_view_test_fragment)

    init {
        this.orientation = LinearLayout.VERTICAL
        View.inflate(getContext(), R.layout.trip_list_view, this)

        val textView = findViewById<TextView>(R.id.text_view_test_fragment)
        textView.text = "abc"

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view_test_fragment)
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = TripListAdapter(TripListFragment.inputArray)
        }
    }

    inner class TripListAdapter(private val list: List<String>) : RecyclerView.Adapter<TripListAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent?.context).inflate(R.layout.fragment_trip_folder_list_item, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.textView.text = list[position]
            PicassoHelper.Builder(holder.imageView).build().load("https://picsum.photos/500/300/?random")
        }

        inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
            val textView: TextView = view.findViewById(R.id.trip_list_item_text_view)
            val imageView: ImageView = view.findViewById(R.id.trip_list_item_image_view)
        }
    }
}
