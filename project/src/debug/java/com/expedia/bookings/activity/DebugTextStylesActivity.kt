package com.expedia.bookings.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import com.expedia.bookings.R
import com.expedia.bookings.widget.DebugTextStyleAdapter

class DebugTextStylesActivity : AppCompatActivity() {
    private val textStylesRecyclerView: RecyclerView by lazy { findViewById(R.id.debug_text_styles_list) as RecyclerView }
    private val textStyleAdapter = DebugTextStyleAdapter(this)

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.debug_text_styles_activity)
        val ab = supportActionBar
        ab?.setDisplayHomeAsUpEnabled(true)

        textStylesRecyclerView.layoutManager = LinearLayoutManager(this)
        textStylesRecyclerView.adapter = textStyleAdapter
        textStyleAdapter.updateStyles(styles)
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

    private val styles by lazy {
        arrayOf(
                R.style.Core_Text_100,
                R.style.Core_Text_100_Medium,
                R.style.Core_Text_100_LightColor,
                R.style.Core_Text_200,
                R.style.Core_Text_200_Medium,
                R.style.Core_Text_200_LightColor,
                R.style.Core_Text_200_Medium_LightColor,
                R.style.Core_Text_300,
                R.style.Core_Text_300_Medium,
                R.style.Core_Text_300_Medium_LightColor,
                R.style.Core_Text_400,
                R.style.Core_Text_400_LightColor,
                R.style.Core_Text_400_Medium,
                R.style.Core_Text_500,
                R.style.Core_Text_500_Medium,
                R.style.Core_Text_500_Medium_LightColor,
                R.style.Core_Text_600,
                R.style.Core_Text_700,
                R.style.Core_Text_800,
                R.style.Core_Text_900)
    }
}