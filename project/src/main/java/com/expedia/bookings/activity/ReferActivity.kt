package com.expedia.bookings.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.utils.FireBaseRewardsUtil

class ReferActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_refer)
        (findViewById(R.id.number_of_available_coupons) as TextView).setText(FireBaseRewardsUtil.getNumberOfRefers().toString())
        (findViewById(R.id.button_final) as Button).setOnClickListener({
            FireBaseRewardsUtil.shareRewards(this, Db.getUser().username)

        })
        (findViewById(R.id.close) as ImageButton).setOnClickListener({
            finish()
        })
    }
}
