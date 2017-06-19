package com.expedia.bookings.activity

import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import com.expedia.bookings.R
import com.expedia.bookings.data.lx.LXSearchResponse
import com.expedia.bookings.data.lx.LxSearchParams
import com.expedia.bookings.services.LxServices
import com.expedia.bookings.utils.Ui
import com.expedia.hackathon.LXCrossSellAdapter
import com.tomerrosenfeld.customanalogclockview.CustomAnalogClock
import org.joda.time.LocalDate
import rx.Observer
import java.util.*

class HackActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hack_activity)
        val myToolbar = findViewById(R.id.main_toolbar) as Toolbar

        val appBarLayout = findViewById(R.id.main_appbar) as AppBarLayout
        appBarLayout.addOnOffsetChangedListener({ appBarLayout: AppBarLayout, offset: Int ->
            val drawable = getDrawable(R.drawable.ic_action_bar_brand_logo)
            drawable.alpha = Math.abs(offset * 255) / appBarLayout.totalScrollRange
            myToolbar.navigationIcon = drawable
        })

        setupMyViews()
    }

    private fun setupMyViews() {
        val departureClock = findViewById(R.id.departure_clock) as CustomAnalogClock
        departureClock.setTimezone(TimeZone.getTimeZone("Asia/Calcutta"))

        val arrivalClock = findViewById(R.id.arrival_clock) as CustomAnalogClock
        arrivalClock.setTimezone(TimeZone.getTimeZone("PST"))

        val lxCrossSell = findViewById(R.id.lx_cross_sell) as RecyclerView
        val params = LxSearchParams.Builder()
                .location("Paris")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(14))
                .build() as LxSearchParams
        Ui.getApplication(this).lxComponent().lxService.lxCategorySearch(params, object: Observer<LXSearchResponse> {
            override fun onError(e: Throwable?) {

            }

            override fun onCompleted() {

            }

            override fun onNext(t: LXSearchResponse?) {
                lxCrossSell.adapter = LXCrossSellAdapter(t!!.activities)
            }

        })
    }

}