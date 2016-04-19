package com.expedia.bookings.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.interfaces.IPhoneLaunchActivityLaunchFragment

class NewPhoneLaunchFragment : Fragment(), IPhoneLaunchActivityLaunchFragment {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.widget_phone_new_launch, null)
        return view
    }

    override fun onBackPressed(): Boolean {
        return false
    }

}