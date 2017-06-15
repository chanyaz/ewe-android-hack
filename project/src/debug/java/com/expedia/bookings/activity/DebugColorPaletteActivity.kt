package com.expedia.bookings.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem

import com.expedia.bookings.R
import com.expedia.bookings.widget.DebugColorPaletteAdapter
import com.expedia.bookings.widget.DebugColorPaletteItem

class DebugColorPaletteActivity : AppCompatActivity() {
    private val colorRecyclerView: RecyclerView by lazy { findViewById(R.id.super_color_view) as RecyclerView }
    private val colorRecyclerAdapter = DebugColorPaletteAdapter()

    private val MAX_SPAN_COUNT = 2

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.debug_color_palette_activity)
        initializeGrid()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun initializeGrid() {
        colorRecyclerAdapter.updateColors(colors)
        val layoutManager = GridLayoutManager(this, MAX_SPAN_COUNT)
        layoutManager.spanSizeLookup = CustomSpanSizeLookUp(MAX_SPAN_COUNT, colorRecyclerAdapter)
        colorRecyclerView.layoutManager = layoutManager
        colorRecyclerView.adapter = colorRecyclerAdapter
    }

    private class CustomSpanSizeLookUp(val maxSpanCount: Int, val adapter: DebugColorPaletteAdapter) : GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int {
            val type = adapter.getItemViewType(position)
            if (type == DebugColorPaletteItem.TITLE) {
                return maxSpanCount
            } else {
                return 1
            }
        }
    }

    private val colors by lazy {
        arrayOf(
                DebugColorPaletteItem(DebugColorPaletteItem.TITLE, "App Colors"),
                DebugColorPaletteItem(colorId = R.color.app_primary),
                DebugColorPaletteItem(colorId = R.color.app_primary_dark),
                DebugColorPaletteItem(colorId = R.color.app_primary_light),
                DebugColorPaletteItem(colorId = R.color.brand_secondary),

                DebugColorPaletteItem(DebugColorPaletteItem.TITLE, "Gray Colors"),
                DebugColorPaletteItem(colorId = R.color.gray1),
                DebugColorPaletteItem(colorId = R.color.gray2),
                DebugColorPaletteItem(colorId = R.color.gray3),
                DebugColorPaletteItem(colorId = R.color.gray4),
                DebugColorPaletteItem(colorId = R.color.gray5),
                DebugColorPaletteItem(colorId = R.color.gray6),
                DebugColorPaletteItem(colorId = R.color.gray7),
                DebugColorPaletteItem(colorId = R.color.gray8),
                DebugColorPaletteItem(colorId = R.color.gray9),
                DebugColorPaletteItem(colorId = R.color.gray10),


                DebugColorPaletteItem(DebugColorPaletteItem.TITLE, "Random Hotel Colors"),
                DebugColorPaletteItem(colorId = R.color.hotelsv2_discount_green),
                DebugColorPaletteItem(colorId = R.color.hotel_price_breakdown_discount_green),
                DebugColorPaletteItem(colorId = R.color.amenity_text_color_large),
                DebugColorPaletteItem(colorId = R.color.amenity_icon_color),
                DebugColorPaletteItem(colorId = R.color.hotel_filter_spinner_text_color),
                DebugColorPaletteItem(colorId = R.color.etp_text_color),
                DebugColorPaletteItem(colorId = R.color.pay_now_earn_text_color),
                DebugColorPaletteItem(colorId = R.color.hotel_cell_distance_text),
                DebugColorPaletteItem(colorId = R.color.hotel_detail_divider),
                DebugColorPaletteItem(colorId = R.color.pwp_total_available_points),
                DebugColorPaletteItem(colorId = R.color.hotel_urgency_message_color),
                DebugColorPaletteItem(colorId = R.color.hotel_tonight_only_color),
                DebugColorPaletteItem(colorId = R.color.hotel_mobile_exclusive_color),
                DebugColorPaletteItem(colorId = R.color.hotel_member_pricing_text_color),
                DebugColorPaletteItem(colorId = R.color.hotel_member_pricing_bg_color),
                DebugColorPaletteItem(colorId = R.color.hotel_coupon_code_error_message_color),
                DebugColorPaletteItem(colorId = R.color.hotel_review_background_color),
                DebugColorPaletteItem(colorId = R.color.hotel_review_title_color),
                DebugColorPaletteItem(colorId = R.color.hotel_review_date_color),
                DebugColorPaletteItem(colorId = R.color.hotel_review_summary_rating_color),
                DebugColorPaletteItem(colorId = R.color.hotel_overall_rating_background_color),
                DebugColorPaletteItem(colorId = R.color.hotel_review_body_color),
                DebugColorPaletteItem(colorId = R.color.hotel_reviews_row_divider_color))
    }
}
