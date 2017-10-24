package com.expedia.bookings.mia.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.payment.CampaignDetails
import com.expedia.bookings.data.payment.ContributeResponse
import com.expedia.bookings.services.LoyaltyServices
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.LXNavUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.AutoResizeTextView
import com.expedia.bookings.widget.LXDetailSectionDataWidget
import com.expedia.bookings.widget.TextView
import com.mobiata.android.Log
import rx.Observer

class MemberDealActivity : AppCompatActivity() {
    //    val clearSubject
    lateinit var loyaltyServices: LoyaltyServices
    lateinit var campaignDetails: CampaignDetails
    lateinit var container: LinearLayout
    var isDeepLink = false
    var tripId = "";
    var context: Context = this


    private fun share() {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Please use this link below to check my travel plan\nYou can contribute funds to make it come true! :) \nhttps://www.expedia.com/mobile/deeplink/gift-a-getaway/${Db.getTripId()?.tripId}")
        startActivity(Intent.createChooser(sharingIntent, "Give a get away"))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.member_deal_activity)
        val intent = intent
        isDeepLink = if (!intent.getStringExtra("tripid").isNullOrEmpty()) true else false
        tripId = intent.getStringExtra("tripid") ?: Db.getTripId().tripId
        if (tripId == null && Db.getTripId()?.tripId != null) {
            share()
        }
        val toolBar = findViewById(R.id.mod_search_toolbar) as Toolbar
        toolBar.setNavigationOnClickListener { view ->
            onBackPressed()
        }

        val shopButton = findViewById(R.id.mod_shop_button) as Button
        shopButton.setOnClickListener { view ->
            LXNavUtils.goToActivities(this, null, 0)
            OmnitureTracking.trackMemberPricingShop()
        }

        container = findViewById(R.id.registry_card) as LinearLayout
        container.removeAllViews()
        Ui.getApplication(this).defaultHotelComponents();
        loyaltyServices = Ui.getApplication(this).hotelComponent().getLoyaltyServices();
        if (tripId.isNotEmpty())
            loyaltyServices.getCampainDetails(tripId, makeCampaignDetailsObserver());
        else
            loyaltyServices.getCampainDetails(Db.getTripId().tripId, makeCampaignDetailsObserver());

        //container.addView(createRow(breakdown))

//        container.addView(createRow("The London Eye Experience", 0))
    }

    fun makeCampaignDetailsObserver(): Observer<CampaignDetails> {
        return object : Observer<CampaignDetails> {
            override fun onNext(response: CampaignDetails) {
                campaignDetails = response
                container.addView(createRow())
            }

            override fun onError(e: Throwable) {
            }

            override fun onCompleted() {
                // ignore
            }
        }
    }

    fun makeDonateObserver(): Observer<ContributeResponse> {
        return object : Observer<ContributeResponse> {
            override fun onNext(response: ContributeResponse) {
                Toast.makeText(context, "Thanks for your gift !!", Toast.LENGTH_SHORT).show()
                Log.d("donate done")
            }

            override fun onError(e: Throwable) {
            }

            override fun onCompleted() {
                // ignore
            }
        }
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
            loyaltyServices.contribute(getTuid(), "", editTextView.text.toString(), campaignDetails.tuid, campaignDetails.tripId, makeDonateObserver())
        }
        return row
    }

    private fun createRow(): View {
        val row = LayoutInflater.from(this).inflate(R.layout.registry_card, null)
        val headerTextView = row.findViewById<TextView>(R.id.header_text_view)
        val raisedFunds = row.findViewById<TextView>(R.id.raised_funds)
        val backgroundImage = row.findViewById<ImageView>(R.id.header_background)
        var offerDescription = row.findViewById<LXDetailSectionDataWidget>(R.id.description)
        var editBoxContainer = row.findViewById<RelativeLayout>(R.id.edit_box_container)
        var editTextView = row.findViewById<EditText>(R.id.edit_amount_view)
        val ratingBar = row.findViewById<RatingBar>(R.id.user_rating_bar)
        var availablePoints = 0
        if (campaignDetails.donationList != null)
            campaignDetails.donationList!!.forEach { it -> availablePoints += it.amount.toInt() }

        var donateButton = row.findViewById<AutoResizeTextView>(R.id.donate)

        val clearBtn = row.findViewById<View>(R.id.clear_btn)
        headerTextView.text = campaignDetails.title
        raisedFunds.text = "Raised Funds - $" + availablePoints + "/" + campaignDetails.fundsRequested

        raisedFunds.setOnClickListener {
            donationDialog.show()
        }

        backgroundImage.setImageResource(R.drawable.london_eye)
        ratingBar.rating = (availablePoints.toFloat() / campaignDetails.fundsRequested.toFloat()) * 10
        if (campaignDetails.message != null)
            offerDescription.bindData("Description", campaignDetails.message, 2)

        clearBtn.setOnClickListener { view ->
            editTextView.text.clear()
        }
        donateButton.setOnClickListener { view ->
            loyaltyServices.contribute(getTuid(), getUserName(), campaignDetails.tuid, editTextView.text.toString(), campaignDetails.tripId, makeDonateObserver())

        }
        if (isDeepLink) {
            editBoxContainer.visibility = View.VISIBLE
            donateButton.text = "DONATE"
        } else {
            editBoxContainer.visibility = View.GONE
            donateButton.text = "REDEEM"
        }
        return row
    }

    val donationDialogView: View by lazy {
        val view = LayoutInflater.from(this).inflate(R.layout.donation_dialog_view, null) as LinearLayout
        /*val test = android.widget.TextView(this)
        test.text = "Testingggg"
        view.addView(test)*/

        for (donation in campaignDetails.donationList!!) {
            val donationView = LayoutInflater.from(this).inflate(R.layout.donation_row_view, null) as LinearLayout
            val by = donationView.findViewById<TextView>(R.id.donated_by)
            val amount  = donationView.findViewById<TextView>(R.id.donated_amount)
            by.text = donation.donorName
            amount.text = "$" + donation.amount
            view.addView(donationView)
        }

        view
    }

    val donationDialog: AlertDialog by lazy {
        val builder = AlertDialog.Builder(this, R.style.Theme_AlertDialog)

        builder.setView(donationDialogView)
        builder.setPositiveButton(this.getString(R.string.DONE), null)
        val dialog: AlertDialog = builder.create()
        /*dialog.setOnShowListener {
            cardsPickerDialog.setCancelable(false)
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                railCardPickerViewModel.doneClickedSubject.onNext(Unit)
                this.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_HOVER_ENTER)
            }
            dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        }*/
        /*dialog.setOnDismissListener {
            this.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_HOVER_ENTER)
        }*/
        dialog
    }

    fun getTuid(): String {
        val userStateManager = Ui.getApplication(this).appComponent().userStateManager()
        val user = userStateManager.userSource.user

        return user?.tuidString ?: ""
    }

    fun getUserName(): String {
        val userStateManager = Ui.getApplication(this).appComponent().userStateManager()
        val user = userStateManager.userSource.user

        return user?.primaryTraveler?.firstName ?: ""
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