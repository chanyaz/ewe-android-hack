package com.expedia.cko.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.cko.lifecycleobserver.ConfirmationLifecycleObserver
import com.expedia.cko.widget.HeaderWidget
import com.expedia.cko.widget.ItineraryWidget

class ConfirmationActivity : AppCompatActivity() {

    private val headerWidget by bindView<HeaderWidget>(R.id.header_widget)
    private val itineraryWidget by bindView<ItineraryWidget>(R.id.itinerary_widget)
    private val emailTextView by bindView<TextView>(R.id.email_text_view)

    private val confirmationLifecycleObserver = object : ConfirmationLifecycleObserver {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmation)

        val lineOfBusiness = intent.getStringExtra("lineOfBusiness")

        Ui.getApplication(baseContext).defaultConfirmationComponent(lineOfBusiness).inject(this)
        this.lifecycle.addObserver(confirmationLifecycleObserver)
    }
}
