package com.expedia.ui;

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.presenter.rail.RailPresenter
import com.expedia.bookings.utils.Ui

class RailActivity : AppCompatActivity() {
    val railPresenter: RailPresenter by lazy {
        findViewById(R.id.rail_presenter)as RailPresenter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ui.getApplication(this).defaultRailComponents()
        Ui.getApplication(this).defaultTravelerComponent()
        setContentView(R.layout.rail_activity)
        Ui.showTransparentStatusBar(this)
    }

    override fun onBackPressed() {
        if (!railPresenter.back()) {
            super.onBackPressed()
        }
    }
}