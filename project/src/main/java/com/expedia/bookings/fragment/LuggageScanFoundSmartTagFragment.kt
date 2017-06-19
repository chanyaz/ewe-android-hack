package com.expedia.bookings.fragment

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R

class LuggageScanFoundSmartTagFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_luggage_scan_found_smart_tag, container, false)

        return super.onCreateView(inflater, container, savedInstanceState)
    }
}