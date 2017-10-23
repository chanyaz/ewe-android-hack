package com.expedia.bookings.launch.widget

import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.launch.vm.BigImageLaunchViewModel
import com.expedia.bookings.launch.vm.TrendingDestinationViewModel
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.squareup.picasso.Picasso

class TrendingDestinationViewHolder(private val view: View): RecyclerView.ViewHolder(view) {

    val titleView: TextView by bindView(R.id.big_image_title)
    val subTitleView: TextView by bindView(R.id.big_image_subtitle)
    val iconImageView: ImageView by bindView(R.id.big_image_icons)
    val bgImageView: ImageView by bindView(R.id.big_image_background)

    fun bind(vm: TrendingDestinationViewModel) {
        titleView.setText(vm.titleId)
        subTitleView.setText(vm.subtitleId)
        iconImageView.setImageDrawable(ContextCompat.getDrawable(view.context, vm.icon))
        vm.backgroundUrlChangeSubject.subscribe { url ->
            if (url != null) {
                vm.backgroundUrl = url
                updateBackgroundImage(vm)
            }
        }
        updateBackgroundImage(vm)
        bgImageView.setColorFilter(ContextCompat.getColor(view.context, vm.bgGradient), PorterDuff.Mode.SRC_ATOP)
    }

    fun updateBackgroundImage(vm: TrendingDestinationViewModel) {
        if (vm.backgroundUrl != null) {
            Picasso.with(view.context).load(vm.backgroundUrl).into(bgImageView)
        } else if (vm.backgroundResId != null) {
            bgImageView.setImageResource(vm.backgroundResId !!)
        } else {
            bgImageView.setImageResource(vm.backgroundFallback)
        }
    }
}

