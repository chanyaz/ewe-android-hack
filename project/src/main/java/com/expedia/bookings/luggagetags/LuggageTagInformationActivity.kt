package com.expedia.bookings.luggagetags

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import com.expedia.bookings.R

class LuggageTagInformationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_luggage_tag_information)

        val luggageTagInfoFragment = LuggageTagInfoFragment()

        supportFragmentManager.beginTransaction().add(R.id.fragment_container_luggage_tag_info, luggageTagInfoFragment).commit()

        val toolbar = findViewById(R.id.toolbar_luggage_tag_info) as Toolbar
        toolbar.setTitle("Enter Name Here")
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.getItemId()) {
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
