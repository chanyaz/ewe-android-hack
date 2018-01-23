package com.expedia.bookings.rail.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.rail.presenter.RailPresenter
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView

class RailActivity : AppCompatActivity() {
    val railPresenter by bindView<RailPresenter>(R.id.rail_presenter)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ui.getApplication(this).defaultRailComponents()
        Ui.getApplication(this).defaultTravelerComponent()
        setContentView(R.layout.rail_activity)
    }

    override fun onBackPressed() {
        if (!railPresenter.back()) {
            super.onBackPressed()
        }
    }
}
