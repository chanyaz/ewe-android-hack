package com.expedia.bookings.activity

import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
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
import com.expedia.bookings.data.rail.responses.TranslateResponse
import com.expedia.bookings.services.TranslateServices
import com.jakewharton.rxbinding.widget.RxTextView
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class HackActivity : AppCompatActivity() {

    var inputTranslate: EditText? = null
    var outputTranslate: TextView? = null
    val translateServices: TranslateServices = TranslateServices(AndroidSchedulers.mainThread(), Schedulers.io())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hack_activity)
        val myToolbar = findViewById(R.id.main_toolbar) as Toolbar

        val appBarLayout = findViewById(R.id.main_appbar) as AppBarLayout
        inputTranslate = findViewById(R.id.input_translate) as EditText
        outputTranslate = findViewById(R.id.output_translate) as TextView
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

        RxTextView.textChanges(inputTranslate as EditText).debounce(500, TimeUnit.MILLISECONDS).subscribe {
            Log.d("Testingggg", it.toString())
            translateServices.translate(it.toString(), object : Observer<TranslateResponse> {
                override fun onError(e: Throwable?) {
                    //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onCompleted() {
                    //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onNext(t: TranslateResponse?) {
                    Log.d("Testingggg", t!!.text[0])
                    (outputTranslate as TextView).text = t!!.text[0]

                }

            })
        }
    }

}