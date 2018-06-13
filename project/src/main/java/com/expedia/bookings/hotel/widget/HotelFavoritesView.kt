package com.expedia.bookings.hotel.widget

import android.content.Context
import android.support.annotation.VisibleForTesting
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.shortlist.HotelShortlistItem
import com.expedia.bookings.extensions.setInverseVisibility
import com.expedia.bookings.extensions.setVisibility
import com.expedia.bookings.hotel.vm.HotelFavoritesViewModel
import com.expedia.bookings.hotel.widget.adapter.HotelFavoritesRecyclerViewAdapter
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.navigation.HotelNavUtils

class HotelFavoritesView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    @VisibleForTesting
    val recyclerView by bindView<RecyclerView>(R.id.hotel_favorites_recycler_view)
    @VisibleForTesting
    val emptyContainer by bindView<LinearLayout>(R.id.hotel_favorites_empty_container)
    @VisibleForTesting
    val viewModel = HotelFavoritesViewModel(context,
            Ui.getApplication(context).appComponent().userStateManager(),
            Ui.getApplication(context).hotelComponent().hotelFavoritesManager())
    private val isUserLoggedIn = viewModel.isUserAuthenticated()
    private lateinit var adapter: HotelFavoritesRecyclerViewAdapter
    private val hotelFavoritesPageEmptyTitle by bindView<TextView>(R.id.hotel_favorites_page_empty_text_view)
    private val hotelFavoritesSignInTitle by bindView<TextView>(R.id.hotel_favorites_sign_in_text_view)

    init {
        View.inflate(getContext(), R.layout.hotel_favorites_layout, this)
        initRecyclerView()
        hotelFavoritesPageEmptyTitle.setVisibility(isUserLoggedIn)
        hotelFavoritesSignInTitle.setInverseVisibility(isUserLoggedIn)
        viewModel.receivedResponseSubject.subscribe { updateViews() }
        viewModel.favoriteHotelRemovedSubject.subscribe { index -> adapter.notifyItemRemoved(index) }
        viewModel.favoritesEmptySubject.subscribe { updateViews() }
    }

    fun onClear() {
        viewModel.onClear()
    }

    fun setUseShopWithPoint(useSWP: Boolean) {
        viewModel.useShopWithPoints = useSWP
    }

    private fun initRecyclerView() {
        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager
        adapter = HotelFavoritesRecyclerViewAdapter(viewModel.favoritesList)
        adapter.hotelSelectedSubject.subscribe { hotelShortlistItem ->
            navigateToInfosite(hotelShortlistItem)
        }
        adapter.hotelFavoriteButtonClickedSubject.subscribe { unfavoritedHotel ->
            viewModel.removeFavoritedHotel(unfavoritedHotel)
        }
        recyclerView.adapter = adapter
    }

    private fun updateViews() {
        recyclerView.setVisibility(viewModel.shouldShowList())
        emptyContainer.setVisibility(!viewModel.shouldShowList())
        adapter.notifyDataSetChanged()
    }

    private fun navigateToInfosite(hotelShortlistItem: HotelShortlistItem) {
        viewModel.createHotelIntent(hotelShortlistItem)?.let { intent ->
            HotelNavUtils.goToHotels(context, intent)
        }
    }
}
