package com.expedia.bookings.itin.triplist

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.widget.TextView
import com.mobiata.android.Log

class TestFragment : Fragment() {
    //private val textView by bindView<TextView>(R.id.text_view_test_fragment)
    var number: Int? = null
    var recyclerView: RecyclerView? = null
    var listAdapter = TripListAdapter(TripListFragment.inputArray)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_trip_list_test, null)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("SRINI: Fragment created $number")
    }

    override fun onResume() {
        super.onResume()

        val textView = view?.findViewById<TextView>(R.id.text_view_test_fragment)
        textView?.text = "abc"

        recyclerView = view?.findViewById<RecyclerView>(R.id.recycler_view_test_fragment)
        recyclerView?.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = listAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        Log.d("SRINI: Fragment destroyed $number")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        listAdapter.notifyDataSetChanged()
    }

    fun resetAdapter(list: List<String>) {
        listAdapter = TripListAdapter(list)
        recyclerView?.swapAdapter(listAdapter, false)
        recyclerView?.adapter?.notifyDataSetChanged()
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
