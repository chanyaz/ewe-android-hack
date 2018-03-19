package com.expedia.bookings.itin.triplist

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isBottomNavigationBarEnabled
import com.expedia.bookings.utils.isBrandColorEnabled

class TripListFragment : Fragment() {
    private val tripToolbar: Toolbar by bindView(R.id.trip_list_toolbar)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_trip_folders_list, null)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handleToolbarBackgroundColor()
        handleToolbarVisibility()
    }

    private fun handleToolbarBackgroundColor() {
        if (isBrandColorEnabled(context)) {
            tripToolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.brand_primary))
        }
    }

    private fun handleToolbarVisibility() {
        tripToolbar.visibility = if (isBottomNavigationBarEnabled(context)) View.VISIBLE else View.GONE
    }
}
