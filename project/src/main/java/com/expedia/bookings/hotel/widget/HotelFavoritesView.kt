package com.expedia.bookings.hotel.widget

import android.app.Activity
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
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat

class HotelFavoritesView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    @VisibleForTesting
    val recyclerView by bindView<RecyclerView>(R.id.hotel_favorites_recycler_view)
    @VisibleForTesting
    val emptyContainer by bindView<LinearLayout>(R.id.hotel_favorites_empty_container)
    @VisibleForTesting
    val viewModel = HotelFavoritesViewModel(context,
            Ui.getApplication(context).appComponent().userStateManager(),
            Ui.getApplication(context).hotelComponent().hotelFavoritesManager())
    @VisibleForTesting
    val undoSnackbar by lazy {
        val parentView = (context as Activity).findViewById<View>(android.R.id.content)
        Snackbar.make(parentView, R.string.hotel_favorites_page_undo_message, Snackbar.LENGTH_LONG)
                .setAction(R.string.hotel_favorites_page_undo_button_text) { _ -> viewModel.undoLastRemove() }
                .setActionTextColor(ContextCompat.getColor(context, R.color.brand_secondary))
    }

    private val isUserLoggedIn = viewModel.isUserAuthenticated()
    private lateinit var adapter: HotelFavoritesRecyclerViewAdapter
    private val hotelFavoritesPageEmptyTitle by bindView<TextView>(R.id.hotel_favorites_page_empty_text_view)
    private val hotelFavoritesSignInTitle by bindView<TextView>(R.id.hotel_favorites_sign_in_text_view)

    init {
        View.inflate(getContext(), R.layout.hotel_favorites_layout, this)
        initRecyclerView()
        emptyContainer.setInverseVisibility(isUserLoggedIn)
        hotelFavoritesPageEmptyTitle.setVisibility(isUserLoggedIn)
        hotelFavoritesSignInTitle.setInverseVisibility(isUserLoggedIn)
        initViewModelSubscriptions()
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
        adapter.favoriteButtonClickedAtIndexSubject.subscribe { favoriteHotelIndex ->
            viewModel.removeFavoriteHotelAtIndex(favoriteHotelIndex)
        }
        recyclerView.adapter = adapter
    }

    private fun updateViews() {
        updateListVisibility()
        adapter.notifyDataSetChanged()
    }

    private fun updateListVisibility() {
        recyclerView.setVisibility(viewModel.shouldShowList())
        emptyContainer.setVisibility(!viewModel.shouldShowList())
    }

    private fun navigateToInfosite(hotelShortlistItem: HotelShortlistItem) {
        viewModel.createHotelIntent(hotelShortlistItem)?.let { intent ->
            HotelNavUtils.goToHotels(context, intent)
        }
    }

    private fun updateRecyclerForUndo(index: Int) {
        updateListVisibility()
        adapter.notifyItemInserted(index)
        recyclerView.scrollToPosition(index)
    }

    private fun initViewModelSubscriptions() {
        viewModel.receivedResponseSubject.subscribe {
            updateViews()
        }
        viewModel.favoriteRemovedAtIndexSubject.subscribe { index ->
            adapter.notifyItemRemoved(index)
            undoSnackbar.show()
        }
        viewModel.favoriteAddedAtIndexSubject.subscribe { index ->
            updateRecyclerForUndo(index)
        }
        viewModel.favoritesEmptySubject.subscribe { updateViews() }

        viewModel.favoriteRemovedFromCacheSubject.subscribe { updateViews() }
    }
}
