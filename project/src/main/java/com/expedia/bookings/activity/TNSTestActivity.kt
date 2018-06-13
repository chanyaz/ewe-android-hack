package com.expedia.bookings.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import io.reactivex.subjects.PublishSubject

class TNSTestActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView

    val loadingIndicator: ProgressBar by bindView(R.id.tns_test_indicator)
    val loadingIndicatorText by bindView<TextView>(R.id.tns_loading_indicator_text)
    val loadingIndicatorSubject = PublishSubject.create<Pair<Boolean, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tns_test)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        loadingIndicatorSubject.subscribe { it ->
            loadingIndicator.visibility = if (it.first) View.VISIBLE else View.GONE
            loadingIndicatorText.visibility = if (it.first) View.VISIBLE else View.GONE
            loadingIndicatorText.text = it.second
        }
        recyclerView = findViewById<RecyclerView>(R.id.tns_test_list).apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = TNSTestAdapter(this.context, loadingIndicatorSubject)
        }
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
}
