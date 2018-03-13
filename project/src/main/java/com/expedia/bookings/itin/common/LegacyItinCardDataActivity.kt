package com.expedia.bookings.itin.common

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItinCardData
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.widget.itin.ItinCard

class LegacyItinCardDataActivity : AppCompatActivity(), ItinCard.OnItinCardClickListener {

    companion object {
        private const val ITIN_ID_EXTRA = "ITIN_ID"

        @JvmStatic
        fun createIntent(context: Context, id: String): Intent {
            val i = Intent(context, LegacyItinCardDataActivity::class.java)
            i.putExtra(ITIN_ID_EXTRA, id)
            return i
        }
    }

    private var itinCard: ItinCard<ItinCardData>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val card: ItinCard<ItinCardData> = ItinCard(this)
        setContentView(card)
        itinCard = card
    }

    override fun onResume() {
        super.onResume()
        val data = ItineraryManager.getInstance().getItinCardDataFromItinId(intent.getStringExtra(ITIN_ID_EXTRA))
        if (data == null) {
            finish()
            return
        }
        itinCard?.bind(data)
        itinCard?.setCardSelected(true)
        itinCard?.expand(false)
        itinCard?.setOnItinCardClickListener(this)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        super.finish()
        overridePendingTransition(R.anim.slide_in_left_complete, R.anim.slide_out_right_no_fill_after)
    }

    override fun onCloseButtonClicked() {
        onBackPressed()
    }
}
