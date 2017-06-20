package com.expedia.bookings.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import com.expedia.bookings.R
import com.mobiata.android.util.Ui

class LuggageAfterScanFragment : Fragment() {
    private var tagFoundPublicContainer: ScrollView? = null
    private var tagFoundPrivateContainer: ScrollView? = null
    private var tagNotFoundContainer: ScrollView? = null
    private var tagId: String? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_luggage_after_scan, container, false)
        tagId = arguments.getString("TAG_ID")

        tagFoundPublicContainer = Ui.findView(view, R.id.tag_found_public_container)
        tagFoundPrivateContainer = Ui.findView(view, R.id.tag_found_private_container)
        tagNotFoundContainer = Ui.findView(view, R.id.tag_not_found_container)



        return view
    }
}

