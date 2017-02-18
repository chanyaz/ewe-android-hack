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
import com.squareup.phrase.Phrase


class ItinPOSHeaderAdapter(val context: Context) : BaseAdapter() {

    var pointOfSales = createPOSList()
    val flagsArray = context.resources.obtainTypedArray(R.array.flags)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = inflateView(position, R.color.white)
        val icon = view.findViewById(R.id.imageView) as ImageView

        var iconParams = icon.layoutParams
        iconParams.height = context.resources.getDimension(R.dimen.itin_pos_header_flag_icon).toInt()
        iconParams.width = context.resources.getDimension(R.dimen.itin_pos_header_flag_icon).toInt()
        icon.layoutParams = iconParams

        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = inflateView(position, R.color.black)
        return view
    }

    private fun inflateView(position: Int, color: Int): View {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.itin_pos_header_spinner, null)
        val icon = view.findViewById(R.id.imageView) as ImageView
        val names = view.findViewById(R.id.textView) as TextView

        names.setTextColor(ContextCompat.getColor(context, color))
        names.text = pointOfSales.get(position).threeLetterCountryCode
        icon.setImageDrawable(flagsArray.getDrawable(position))


        view.contentDescription = Phrase.from(context, R.string.accessibility_cont_desc_pos_TEMPLATE).put("country_name", getCountryName(position)).format().toString()
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

    fun getCountryName(position: Int): String {
        val countryName = context.getString(pointOfSales.get(position).countryNameResId)
        return countryName
    }
}
