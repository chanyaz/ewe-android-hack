package com.expedia.bookings.launch.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.utils.DeeplinkCreatorUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.vm.launch.ContinueBookingHolderViewModel

class ContinueBookingCard(itemView: View, val context: Context): RecyclerView.ViewHolder(itemView) {

    val cardView: CardView by bindView(R.id.card_view)
    val firstLineTextView: TextView by bindView(R.id.first_line)
    val secondLineTextView: TextView by bindView(R.id.second_line)

    fun bind(vm: ContinueBookingHolderViewModel) {
        firstLineTextView.text = vm.firstLine
        secondLineTextView.text = vm.secondLine
        cardView.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(DeeplinkCreatorUtils.generateDeeplinkForCurrentPath(LineOfBusiness.PACKAGES)))
            context.startActivity(intent)
        }
    }
}
