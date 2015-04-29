package com.expedia.bookings.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.textView

public class HotelActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		frameLayout {
			textView("Get yer hotels")
		}
	}

}
