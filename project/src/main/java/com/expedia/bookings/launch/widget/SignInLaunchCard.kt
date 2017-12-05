package com.expedia.bookings.launch.widget

import android.content.Context
import android.os.Build
import android.support.v7.widget.RecyclerView
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.navigation.NavUtils
import com.expedia.bookings.widget.TextView
import com.expedia.vm.launch.BrandSignInLaunchHolderViewModel

class BrandSignInLaunchCard(itemView: View, val context: Context) : RecyclerView.ViewHolder(itemView) {
    val firstLineTextView: TextView by bindView(R.id.first_line)
    val secondLineTextView: TextView by bindView(R.id.second_line)
    val button_one: TextView by bindView(R.id.button_one)

    init {
        setupFonts()

        button_one.setOnClickListener {
            NavUtils.goToSignIn(context, false, true, 0)
            OmnitureTracking.trackLaunchSignIn()
        }
    }

    fun bind(vm: BrandSignInLaunchHolderViewModel) {
        firstLineTextView.text = vm.firstLine
        secondLineTextView.text = vm.secondLine
        button_one.text = vm.buttonOneLabel
        button_one.contentDescription = (vm.buttonOneLabel + " " + context.getString(R.string.accessibility_cont_desc_role_button))

        if (Build.VERSION.SDK_INT >= 21){
            button_one.letterSpacing = .05f
        }
    }

    private fun setupFonts() {
        FontCache.setTypeface(firstLineTextView, FontCache.Font.ROBOTO_MEDIUM)
        FontCache.setTypeface(secondLineTextView, FontCache.Font.ROBOTO_REGULAR)
        FontCache.setTypeface(button_one, FontCache.Font.ROBOTO_MEDIUM)
    }
}