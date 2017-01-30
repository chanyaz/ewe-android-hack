package com.expedia.bookings.widget.itin

import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.pos.PointOfSale


class ItinPOSHeaderAdapter(val context: Context) : BaseAdapter() {

    var pointOfSales = createPOSList()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = inflateView(position, R.color.white)
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = inflateView(position, R.color.black)
        return view
    }

    fun inflateView(position: Int, color: Int) : View {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.itin_pos_header_spinner, null)

        val icon = view.findViewById(R.id.imageView) as ImageView
        val names = view.findViewById(R.id.textView) as TextView
        names.setTextColor(ContextCompat.getColor(context, color))

        icon.setImageResource(pointOfSales.get(position).countryFlagResId)
        names.text = pointOfSales.get(position).threeLetterCountryCode
        return view
    }

    override fun getItem(position: Int): CharSequence {
        return pointOfSales.get(position).threeLetterCountryCode
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return pointOfSales.count()
    }

    private fun createPOSList(): List<PointOfSale> {
        val poses = PointOfSale.getAllPointsOfSale(context)
        return poses
    }

    fun findPOSIndex(posId: Int): Int {
        pointOfSales.forEachIndexed { i, pointOfSale ->
            if (posId == pointOfSale.pointOfSaleId.id) {
                return i
            }
        }
        return -1
    }
}
