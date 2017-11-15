package com.expedia.bookings.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem

import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.DebugColorPaletteAdapter
import com.expedia.bookings.widget.DebugColorPaletteItem

class DebugColorPaletteActivity : AppCompatActivity() {
    private val colorRecyclerView by bindView<RecyclerView>(R.id.super_color_view)
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
                DebugColorPaletteItem(colorId = R.color.app_status_bar_color),
                DebugColorPaletteItem(colorId = R.color.brand_secondary),

                DebugColorPaletteItem(DebugColorPaletteItem.TITLE, "Gray Colors"),
                DebugColorPaletteItem(colorId = R.color.gray100),
                DebugColorPaletteItem(colorId = R.color.gray200),
                DebugColorPaletteItem(colorId = R.color.gray300),
                DebugColorPaletteItem(colorId = R.color.gray400),
                DebugColorPaletteItem(colorId = R.color.gray500),
                DebugColorPaletteItem(colorId = R.color.gray600),
                DebugColorPaletteItem(colorId = R.color.gray700),
                DebugColorPaletteItem(colorId = R.color.gray800),
                DebugColorPaletteItem(colorId = R.color.gray900),

                DebugColorPaletteItem(DebugColorPaletteItem.TITLE, "Random Hotel Colors"),
                DebugColorPaletteItem(colorId = R.color.hotelsv2_discount_green),
                DebugColorPaletteItem(colorId = R.color.etp_text_color),
                DebugColorPaletteItem(colorId = R.color.pay_now_earn_text_color),
                DebugColorPaletteItem(colorId = R.color.hotel_urgency_message_color),
                DebugColorPaletteItem(colorId = R.color.hotel_tonight_only_color),
                DebugColorPaletteItem(colorId = R.color.hotel_mobile_exclusive_color),
                DebugColorPaletteItem(colorId = R.color.member_pricing_text_color),
                DebugColorPaletteItem(colorId = R.color.member_pricing_flag_bg_color),
                DebugColorPaletteItem(colorId = R.color.hotel_coupon_code_error_message_color))
    }
}
