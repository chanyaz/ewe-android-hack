package com.expedia.bookings.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.luggagetags.AddLuggageTag
import com.expedia.bookings.utils.FontCache
import com.mobiata.android.util.Ui

class AddTagToAccountFragment: Fragment() {
    private var mScanQRCodeButton: Button? = null
    private var mOuterContainer: LinearLayout? = null
    private var mORText: TextView? = null
    private var mManuallyEnterTagId: TextView? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_add_luggage_tag_to_account, container, false)

        mOuterContainer = Ui.findView<LinearLayout>(view, R.id.outer_container)
        mORText = Ui.findView<TextView>(view, R.id.my_luggage_tag_text_view)
        mScanQRCodeButton = Ui.findView<Button>(view, R.id.scan_my_QR_code)
        mManuallyEnterTagId = Ui.findView<TextView>(view, R.id.manually_enter_tag_ID)

        FontCache.setTypeface(mScanQRCodeButton, FontCache.Font.ROBOTO_REGULAR)

        mManuallyEnterTagId?.setOnClickListener {
            startActivity(Intent(context, AddLuggageTag::class.java))
        }

        return view
    }
}