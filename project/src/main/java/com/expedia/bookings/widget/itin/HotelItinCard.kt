package com.expedia.bookings.widget.itin

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.data.User
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.trips.ItinCardDataHotel
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.RecyclerGallery
import com.expedia.ui.GalleryActivity
import com.google.gson.GsonBuilder


class HotelItinCard(context: Context, attributeSet: AttributeSet?) : ItinCard<ItinCardDataHotel>(context, attributeSet), RecyclerGallery.GalleryItemListener {

    val mVIPTextView: TextView by bindView(R.id.vip_label_text_view)
    val mRoomUpgradeAvailableBanner: TextView by bindView(R.id.room_upgrade_available_banner)

    init {
        mHeaderGallery.setOnItemClickListener(this)
    }

    override fun bind(itinCardData: ItinCardDataHotel) {
        super.bind(itinCardData)
        setupIsVipHotelTextView(itinCardData)
        setupRoomUpgradeBanner(itinCardData)
    }

    override fun finishExpand() {
        mHeaderGallery.showPhotoCount = isBucketedForGallery()
        mHeaderGallery.canScroll = isBucketedForGallery()
        OmnitureTracking.trackHotelItinGallery()
        super.finishExpand()
    }

    override fun onGalleryItemClicked(item: Any) {
        if (!isBucketedForGallery()) {
            return
        }
        val i = Intent(context, GalleryActivity::class.java)
        val contentGenerator = mItinContentGenerator as HotelItinContentGenerator
        val gson = GsonBuilder().create()
        val json = gson.toJson(contentGenerator.itinCardData.property.mediaList)
        i.putExtra("Urls", json)
        i.putExtra("Position", mHeaderGallery.selectedItem)
        i.putExtra("Name", contentGenerator.itinCardData.propertyName)
        i.putExtra("Rating", contentGenerator.itinCardData.propertyRating)
        context.startActivity(i)
    }

    private fun setupRoomUpgradeBanner(itinCardData: ItinCardDataHotel) {
        val isFeatureOn = FeatureToggleUtil.isFeatureEnabled(getContext(), R.string.preference_itin_hotel_upgrade)
        val isTripUpgradeable = itinCardData.tripComponent.parentTrip.isTripUpgradable
        val isRoomUpgradable = isFeatureOn && isTripUpgradeable && !itinCardData.isSharedItin
        mRoomUpgradeAvailableBanner.visibility = if (isRoomUpgradable) View.VISIBLE else View.GONE
    }

    private fun setupIsVipHotelTextView(itinCardData: ItinCardDataHotel) {
        val isVipAccess = itinCardData.isVip
        val customerLoyaltyMembershipTier = User.getLoggedInLoyaltyMembershipTier(context)
        val isSilverOrGoldMember = customerLoyaltyMembershipTier == LoyaltyMembershipTier.MIDDLE || customerLoyaltyMembershipTier == LoyaltyMembershipTier.TOP
        val posSupportVipAccess = PointOfSale.getPointOfSale().supportsVipAccess()
        mVIPTextView.visibility = if (isVipAccess && isSilverOrGoldMember && posSupportVipAccess) View.VISIBLE else View.GONE
    }

    private fun isBucketedForGallery(): Boolean {
        return Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidItinHotelGallery)
    }
}