package com.expedia.bookings.mia.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.services.LoyaltyServices
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.LXNavUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.AutoResizeTextView
import com.expedia.bookings.widget.LXDetailSectionDataWidget
import com.expedia.bookings.widget.TextView

class MemberDealActivity : AppCompatActivity() {
    //    val clearSubject
    lateinit var loyaltyServices: LoyaltyServices


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.member_deal_activity)

        val toolBar = findViewById(R.id.mod_search_toolbar) as Toolbar
        toolBar.setNavigationOnClickListener { view ->
            onBackPressed()
        }

        val shopButton = findViewById(R.id.mod_shop_button) as Button
        shopButton.setOnClickListener { view ->
            LXNavUtils.goToActivities(this, null, 0)
            OmnitureTracking.trackMemberPricingShop()
        }

        val container = findViewById(R.id.registry_card) as LinearLayout
        container.removeAllViews()
        container.addView(createRow("The London Eye Experience", 0))
        Ui.getApplication(this).defaultHotelComponents();
        loyaltyServices = Ui.getApplication(this).hotelComponent().loyaltyServices();
        //container.addView(createRow(breakdown))
    }

    private fun createRow(headerText: String, image: Int): View {
        val row = LayoutInflater.from(this).inflate(R.layout.registry_card, null)
        val headerTextView = row.findViewById<TextView>(R.id.header_text_view)
        val backgroundImage = row.findViewById<ImageView>(R.id.header_background)
        var offerDescription = row.findViewById<LXDetailSectionDataWidget>(R.id.description)

        var editTextView = row.findViewById<EditText>(R.id.edit_amount_view)

        var donateButton = row.findViewById<AutoResizeTextView>(R.id.donate)

        val clearBtn = row.findViewById<View>(R.id.clear_btn)
        headerTextView.text = headerText
        backgroundImage.setImageResource(R.drawable.london_eye)
        offerDescription.bindData("Description", "Discover amazing beauty Discover amazing beauty Discover amazing beauty Discover amazing beauty Discover amazing beauty Discover amazing beauty Discover amazing beauty Discover amazing beauty Discover amazing beauty ", 2)
        clearBtn.setOnClickListener { view ->
            editTextView.text.clear()
        }
        donateButton.setOnClickListener { view ->

        }
        return row
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        // memberDealResponseProvider.fetchDeals()
        OmnitureTracking.trackMemberPricingPageLoad()
    }

    override fun onStop() {
        super.onStop()
    }
}