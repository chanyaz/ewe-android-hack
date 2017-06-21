package com.expedia.bookings.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
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
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TravelerTagsRecyclerView
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.vision.barcode.Barcode
import com.mobiata.android.util.Ui

class LuggageScanFoundSmartTagFragment : Fragment() {
    private var mScanFoundSmartTagButton: Button? = null
    private var mOuterContainer: LinearLayout? = null
    private var mLuggageTagText: TextView? = null
    private var mAddTagToAccount: TextView? = null
    private val BARCODE_CAPTURE_REQUEST: Int = 12345
    private var luggageAfterScanFragment: LuggageAfterScanFragment? = null
    val recyclerView: TravelerTagsRecyclerView by bindView(R.id.list_view)

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
            val toolbar = activity.findViewById(R.id.toolbar) as Toolbar
            toolbar.title = resources.getString(R.string.add_a_tag_to_my_account_button)
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)

            val fragment = AddTagToAccountFragment()
            val transaction = fragmentManager.beginTransaction()
            transaction.add(R.id.fragment_container, fragment).addToBackStack(null).commit()

            toolbar.setNavigationOnClickListener { view ->
                val activity = context as AppCompatActivity
                activity.onBackPressed()
                toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp)
                toolbar.setTitle(R.string.luggage_tag)
            }
        }

        return view
    }

    override fun onResume() {
        super.onResume()
    }

    fun onBackPressed() {
        if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStack()
        } else {
            activity.onBackPressed()
        }
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