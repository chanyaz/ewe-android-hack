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
import android.widget.Toast
import com.expedia.bookings.R
import com.expedia.bookings.activity.LuggageAfterScanActivity
import com.expedia.bookings.luggagetags.BarCodeCaptureActivity
import com.expedia.bookings.utils.FontCache
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.vision.barcode.Barcode
import com.mobiata.android.util.Ui

class LuggageScanFoundSmartTagFragment: Fragment() {
    private var mScanFoundSmartTagButton: Button? = null
    private var mOuterContainer: LinearLayout? = null
    private var mLuggageTagText: TextView? = null
    private var mAddTagToAccount: TextView? = null
    private val BARCODE_CAPTURE_REQUEST: Int = 12345
    private var luggageAfterScanFragment: LuggageAfterScanFragment? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_luggage_scan_found_smart_tag, container, false)

        luggageAfterScanFragment = LuggageAfterScanFragment()

        mOuterContainer = Ui.findView<LinearLayout>(view, R.id.outer_container)
        mScanFoundSmartTagButton = Ui.findView<Button>(view, R.id.scan_a_found_smart_tag)
        mLuggageTagText = Ui.findView<TextView>(view, R.id.my_luggage_tag_text_view)
        mAddTagToAccount = Ui.findView<TextView>(view, R.id.add_a_tag_to_my_account_button)

        FontCache.setTypeface(mScanFoundSmartTagButton, FontCache.Font.ROBOTO_REGULAR)

        mScanFoundSmartTagButton?.setOnClickListener {
            val barcodeScanIntent: Intent = Intent(context, BarCodeCaptureActivity::class.java)
            barcodeScanIntent.putExtra("FROM", "SCAN_FOUND_TAG")
            startActivityForResult(barcodeScanIntent, BARCODE_CAPTURE_REQUEST)
        }

        mAddTagToAccount?.setOnClickListener {
            val fragment = AddTagToAccountFragment()
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, fragment).commit()
        }


        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == BARCODE_CAPTURE_REQUEST) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    val barcode: Barcode = data.getParcelableExtra(BarCodeCaptureActivity.BarcodeObject)
                    Toast.makeText(context, barcode.displayValue, Toast.LENGTH_SHORT).show()
                    val intent = Intent(context, LuggageAfterScanActivity::class.java)
                    intent.putExtra("TAG_ID", barcode.displayValue)
                    startActivity(intent)
                } else {
                    Toast.makeText(context, "No barcode captured", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}