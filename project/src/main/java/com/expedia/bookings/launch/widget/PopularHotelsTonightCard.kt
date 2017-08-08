package com.expedia.bookings.launch.widget

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.navigation.HotelNavUtils
import com.expedia.bookings.widget.OptimizedImageView
import com.expedia.bookings.widget.TextView
import com.expedia.vm.launch.PopularHotelsTonightViewModel

class PopularHotelsTonightCard(itemView: View, context: Context): RecyclerView.ViewHolder(itemView) {
    val backgroundImage: OptimizedImageView by bindView(R.id.background_image)
    val firstLineTextView: TextView by bindView(R.id.first_line)
    val secondLineTextView: TextView by bindView(R.id.second_line)

    init {
        val animOptions = AnimUtils.createActivityScaleBundle(itemView)
        itemView.setOnClickListener { HotelNavUtils.goToHotels(context, animOptions) }
    }

    fun bind(vm: PopularHotelsTonightViewModel) {
        backgroundImage.setBackgroundResource(vm.background)
        firstLineTextView.text = vm.firstLine
        secondLineTextView.text = vm.secondLine
    }
}