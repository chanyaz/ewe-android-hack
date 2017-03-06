package com.expedia.bookings.widget.itin

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.FrameLayout
import android.widget.Spinner
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.dialog.ClearPrivateDataDialog
import com.expedia.bookings.otto.Events
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.bindView
import com.mobiata.android.util.SettingUtils

class ItinPOSHeader(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs), ClearPrivateDataDialog.ClearPrivateDataDialogListener {

    val dialog = ClearPrivateDataDialog()
    val spinner: Spinner by bindView(R.id.country_trips_signin)
    val posText: TextView by bindView(R.id.pos_trips_signin)
    var adapter = ItinPOSHeaderAdapter(context)
    var position = adapter.findPOSIndex(PointOfSale.getPointOfSale().pointOfSaleId.id)
    var lastPosition = position

    init {
        View.inflate(context, R.layout.itin_pos_header, this)
        spinner.adapter = adapter

    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        posText.text = adapter.pointOfSales[position].url
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, selectedPosition: Int, id: Long) {
                position = selectedPosition
                dialog.setListener(this@ItinPOSHeader)
                val activity = context as AppCompatActivity
                dialog.show(activity.supportFragmentManager, "clearPrivateDataDialog")
                OmnitureTracking.trackItinChangePOS()
            }
        }

        setPOSAdapterPosition(position)
    }

    fun setCurrentPOS() {
        position = adapter.findPOSIndex(PointOfSale.getPointOfSale().pointOfSaleId.id)
        lastPosition = position
        posText.text = adapter.pointOfSales[position].url.capitalize()

        setPOSAdapterPosition(lastPosition)
    }

    override fun onPrivateDataCleared() {
        lastPosition = position
        posText.text = adapter.pointOfSales[position].url.capitalize()
        SettingUtils.save(context, R.string.PointOfSaleKey, Integer.toString(adapter.pointOfSales[position].pointOfSaleId.id))
        PointOfSale.onPointOfSaleChanged(context)
        Events.post(Events.PhoneLaunchOnPOSChange())
    }

    override fun onDialogCancel() {
        setPOSAdapterPosition(lastPosition)
    }

    private fun setPOSAdapterPosition(index: Int) {
        val clickListener = spinner.onItemSelectedListener
        spinner.onItemSelectedListener = null
        spinner.post {
            spinner.setSelection(index, false)
            spinner.postDelayed({ spinner.onItemSelectedListener = clickListener} , 100L)
        }
    }
}
