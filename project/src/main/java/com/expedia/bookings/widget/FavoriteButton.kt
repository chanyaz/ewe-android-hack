package com.expedia.bookings.widget

import android.content.Context
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.widget.ImageView
import android.widget.Toast
import com.expedia.bookings.R
import com.expedia.bookings.animation.HotelFavoriteBurstAnimation
import com.expedia.bookings.data.HotelFavoriteHelper
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeOnClick
import com.expedia.vm.hotel.FavoriteButtonViewModel

class FavoriteButton(context: Context, attrs : AttributeSet) : ImageView(context, attrs) {

    var isInDetailView = false
    val HEART_BURST_RADIUS = 36
    val HEART_BURST_DURATION: Long = 600
    val HEART_IMAGE_WAIT_TIME: Long = 200

    var viewModel: FavoriteButtonViewModel by notNullAndObservable { vm ->
        this.subscribeOnClick(vm.clickSubject)
        vm.favoriteChangeSubject.subscribe { hotelIdAndFavorite ->
            if (hotelIdAndFavorite.second) {
                displayMessageToUser(vm.firstTimeFavoritingSubject.value)
            }
            updateImageState(true)
        }
    }

    init {
        setImageResource(R.drawable.favoriting_unselected_with_shadow)
    }

    private fun displayMessageToUser(isFirstTimeFavoriting: Boolean) {
        if (isFirstTimeFavoriting) {
            showFirstTimeDialog()
        } else if (!isInDetailView) {
            showToast()
        }
    }

    private fun updateImageState(animated: Boolean) {
        if (HotelFavoriteHelper.isHotelFavorite(context, viewModel.hotelId)) {
            if (animated) {
                val displayMetrics = resources.displayMetrics
                val px = Math.round(HEART_BURST_RADIUS * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
                val burstDrawable = HotelFavoriteBurstAnimation(ContextCompat.getColor(context, R.color.hotel_favorite_button), px, HEART_BURST_DURATION)
                background = burstDrawable
                burstDrawable.startAnimation()
            }
            val handler = Handler()
            handler.postDelayed({
                val imageRes = if (isInDetailView) R.drawable.favoriting_selected else R.drawable.favoriting_selected
                setImageResource(imageRes)
            }, if (animated) HEART_IMAGE_WAIT_TIME else 0)
        } else {
            val imageRes = if (isInDetailView) R.drawable.favoriting_unselected else R.drawable.favoriting_unselected
            setImageResource(imageRes)
        }
    }

    fun updateImageState() { updateImageState(false) }

    private fun showToast() {
        val toast_message = resources.getString(R.string.favorite_toast)
        Toast.makeText(context, toast_message, Toast.LENGTH_LONG).show()
    }

    private fun showFirstTimeDialog() {
        AlertDialog.Builder(context)
            .setTitle(resources.getString(R.string.favorite_first_time_dialog_title))
            .setMessage(resources.getString(R.string.favorite_first_time_dialog_content))
            .setPositiveButton(resources.getString(R.string.favorite_first_time_dialog_ok_button),
                    { dialogInterface, i ->
                        if (!isInDetailView){
                            showToast()
                        }
                    })
            .show()
    }
}
