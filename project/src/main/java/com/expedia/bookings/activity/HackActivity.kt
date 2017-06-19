package com.expedia.bookings.activity

import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.TranslateResponse
import com.expedia.bookings.services.TranslateServices
import com.jakewharton.rxbinding.widget.RxTextView
import rx.Observer
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class HackActivity : AppCompatActivity() {

    var inputTranslate: EditText? = null
    var outputTranslate: TextView? = null
    var currencyTextView: TextView? = null
    val translateServices: TranslateServices = TranslateServices(AndroidSchedulers.mainThread(), Schedulers.io())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hack_activity)
        val myToolbar = findViewById(R.id.main_toolbar) as Toolbar

        val appBarLayout = findViewById(R.id.main_appbar) as AppBarLayout
        inputTranslate = findViewById(R.id.input_translate) as EditText
        outputTranslate = findViewById(R.id.output_translate) as TextView
        currencyTextView = findViewById(R.id.edit_currency_view) as EditText
        appBarLayout.addOnOffsetChangedListener({ appBarLayout: AppBarLayout, offset: Int ->
            if (Math.abs(offset) >= appBarLayout.getTotalScrollRange()) {
                myToolbar.setNavigationIcon(R.drawable.ic_action_bar_brand_logo)
            } else {
                myToolbar.navigationIcon = null
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


        var rate = 65

        RxTextView.textChanges(currencyTextView as EditText).debounce(200, TimeUnit.MILLISECONDS).subscribe {
            Log.d("Testingggg Mahak", it.toString())

            (currencyTextView as TextView).text = (((it.toString().toInt()) * rate).toString())
        }
    }
}