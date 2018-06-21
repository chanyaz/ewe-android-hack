package com.expedia.bookings.hotel.widget

import android.app.Activity
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.shortlist.HotelShortlistItem
import com.expedia.bookings.data.hotels.shortlist.ShortlistItem
import com.expedia.bookings.hotel.widget.viewholder.HotelFavoritesItemViewHolder
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import android.support.design.internal.SnackbarContentLayout
import android.support.design.widget.Snackbar

@RunWith(RobolectricRunner::class)
@Config(shadows = [ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class])
class HotelFavoritesViewTest {
    private lateinit var view: HotelFavoritesView
    private val userStateManager = UserLoginTestUtil.getUserStateManager()

    @Before
    fun setup() {
        AbacusTestUtils.bucketTests(AbacusUtils.HotelUGCSearch)
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        userStateManager.signIn(activity)
        activity.setTheme(R.style.Theme_Hotels_Default)
        Ui.getApplication(activity).defaultHotelComponents()
        view = android.view.LayoutInflater.from(activity).inflate(R.layout.hotel_favorites_view_test, null) as HotelFavoritesView
    }

    @Test
    fun testRemove() {
        view.viewModel.favoritesList.add(createHotelShortlistItem())
        view.viewModel.favoritesList.add(createHotelShortlistItem())
        view.viewModel.receivedResponseSubject.onNext(Unit)
        assertEquals(view.recyclerView.visibility, View.VISIBLE)
        forceRecyclerUiUpdate()
        var favoriteViewHolder = (view.recyclerView.findViewHolderForAdapterPosition(0) as HotelFavoritesItemViewHolder)

        assertEquals(favoriteViewHolder.favoriteTouchTarget.visibility, View.VISIBLE)
        favoriteViewHolder.favoriteTouchTarget.performClick()
        forceRecyclerUiUpdate()
        assertEquals(view.recyclerView.visibility, View.VISIBLE)

        favoriteViewHolder = (view.recyclerView.findViewHolderForAdapterPosition(0) as HotelFavoritesItemViewHolder)
        assertNull(view.recyclerView.findViewHolderForAdapterPosition(1))
        favoriteViewHolder.favoriteTouchTarget.performClick()
        assertEquals(view.recyclerView.visibility, View.GONE)
        assertEquals(view.emptyContainer.visibility, View.VISIBLE)
        assertTrue(view.undoSnackbar.isShown)

        val snackbarLayout = view.undoSnackbar.view as Snackbar.SnackbarLayout
        val snackbarContent = snackbarLayout.getChildAt(0) as SnackbarContentLayout
        snackbarContent.actionView.performClick()
        forceRecyclerUiUpdate()
        assertEquals(view.recyclerView.visibility, View.VISIBLE)
        assertEquals(view.emptyContainer.visibility, View.GONE)
        favoriteViewHolder = view.recyclerView.findViewHolderForAdapterPosition(0) as HotelFavoritesItemViewHolder
        assertEquals(favoriteViewHolder.favoriteTouchTarget.visibility, View.VISIBLE)
    }

    private fun createHotelShortlistItem(): HotelShortlistItem {
        return HotelShortlistItem().apply {
            shortlistItem = ShortlistItem().apply {
                itemId = "itemId"
            }
        }
    }

    private fun forceRecyclerUiUpdate() {
        view.recyclerView.measure(0, 0)
        view.recyclerView.layout(0, 0, 100, 1000)
    }
}
