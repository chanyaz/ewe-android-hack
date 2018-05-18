package com.expedia.bookings.hotel.widget

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.extensions.setVisibility
import com.expedia.bookings.hotel.widget.adapter.HotelFavoritesRecyclerViewAdapter
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.vm.hotel.HotelFavoritesViewModel

class HotelFavoritesView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private val recyclerView by bindView<RecyclerView>(R.id.hotel_favorites_recycler_view)
    private val emptyContainer by bindView<LinearLayout>(R.id.hotel_favorites_empty_container)
    private val viewModel = HotelFavoritesViewModel(Ui.getApplication(context).appComponent().userStateManager(),
            Ui.getApplication(context).hotelComponent().hotelShortlistServices())
    private lateinit var adapter: HotelFavoritesRecyclerViewAdapter

    init {
        View.inflate(getContext(), R.layout.hotel_favorites_layout, this)
        initRecyclerView()
        viewModel.receivedResponseSubject.subscribe { updateViews() }
    }

    fun onClear() {
        viewModel.onClear()
    }

    private fun initRecyclerView() {
        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager
        adapter = HotelFavoritesRecyclerViewAdapter(viewModel.favoritesList)
        recyclerView.adapter = adapter
    }

    private fun updateViews() {
        recyclerView.setVisibility(viewModel.shouldShowList())
        emptyContainer.setVisibility(!viewModel.shouldShowList())
        adapter.notifyDataSetChanged()
    }
}
