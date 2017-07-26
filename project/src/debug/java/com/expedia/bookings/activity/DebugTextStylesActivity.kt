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
        arrayOf(R.style.Core_Text_Default,
                R.style.Core_Text_Default_Medium,
                R.style.Core_Text_Default_LightColor,
                R.style.Core_Text_Default_Medium_LightColor,
                R.style.Core_Text_Small,
                R.style.Core_Text_Small_Medium,
                R.style.Core_Text_Small_LightColor,
                R.style.Core_Text_Large,
                R.style.Core_Text_Large_Gray3,
                R.style.Core_Text_Large_Medium,
                R.style.Core_Text_Large_Medium_LightColor,
                R.style.Core_Text_XLarge,
                R.style.Core_Text_XLarge_LightColor,
                R.style.Core_Text_XLarge_Medium,
                R.style.Core_Text_XXLarge,
                R.style.Core_Text_XXLarge_Medium,
                R.style.Core_Text_XXLarge_Medium_LightColor,
                R.style.Core_Text_XXLarge_Medium_Gray3,
                R.style.Core_Text_XXXLarge,
                R.style.Core_Text_XXXLarge_Medium)
    }
}