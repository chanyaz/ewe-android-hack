package com.expedia.bookings.fragment

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.utils.FontCache
import com.mobiata.android.util.Ui


class LuggageScanFoundSmartTagFragment: Fragment() {
    private var mScanFoundSmartTagButton: Button? = null
    private var mOuterContainer: LinearLayout? = null
    private var mLuggageTagText: TextView? = null
    private var mAddTagToAccount: TextView? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_luggage_scan_found_smart_tag, container, false)

        mOuterContainer = Ui.findView<LinearLayout>(view, R.id.outer_container)
        mScanFoundSmartTagButton = Ui.findView<Button>(view, R.id.scan_a_found_smart_tag)
        mLuggageTagText = Ui.findView<TextView>(view, R.id.my_luggage_tag_text_view)
        mAddTagToAccount = Ui.findView<TextView>(view, R.id.add_a_tag_to_my_account_button)

        FontCache.setTypeface(mScanFoundSmartTagButton, FontCache.Font.ROBOTO_REGULAR)

        mScanFoundSmartTagButton?.setOnClickListener {
            //TODO: Launch the new activity to scan QR code
        }

        mAddTagToAccount?.setOnClickListener {
            val fragment = AddTagToAccountFragment()
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, fragment).addToBackStack(null).commit()

            val toolbar = activity.findViewById(R.id.toolbar) as Toolbar
            toolbar.title = resources.getString(R.string.add_a_tag_to_my_account_button)
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        }
        return view
    }

    fun onBackPressed() {
        if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStack()
        } else {
            activity.onBackPressed()
        }
    }
}