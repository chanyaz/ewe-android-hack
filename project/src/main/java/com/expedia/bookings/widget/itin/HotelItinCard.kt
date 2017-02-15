package com.expedia.bookings.widget.itin

import android.animation.AnimatorSet
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.data.Property
import com.expedia.bookings.data.RoomUpgradeOffersResponse
import com.expedia.bookings.data.User
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.trips.ItinCardDataHotel
import com.expedia.bookings.services.RoomUpgradeOffersService
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.RecyclerGallery
import com.expedia.ui.GalleryActivity
import com.google.gson.GsonBuilder
import rx.Observer
import rx.subjects.PublishSubject
import javax.inject.Inject


class HotelItinCard(context: Context, attributeSet: AttributeSet?) : ItinCard<ItinCardDataHotel>(context, attributeSet), RecyclerGallery.GalleryItemListener {

    lateinit var roomUpgradeService: RoomUpgradeOffersService
        @Inject set

    val mVIPTextView: TextView by bindView(R.id.vip_label_text_view)
    val mRoomUpgradeAvailableBanner: TextView by bindView(R.id.room_upgrade_available_banner)
    val mRoomUpgradeOffersSubject: PublishSubject<Property.RoomUpgradeType> = PublishSubject.create<Property.RoomUpgradeType>()

    init {
        Ui.getApplication(context).defaultTripComponents()
        Ui.getApplication(context).tripComponent().inject(this)
        mHeaderGallery.setOnItemClickListener(this)
        mRoomUpgradeOffersSubject.subscribe { type ->
            val itinCardData = mItinContentGenerator.mItinCardData as ItinCardDataHotel
            itinCardData.property.roomUpgradeOfferType = type
            mRoomUpgradeAvailableBanner.visibility = if (itinCardData.hasRoomUpgradeOffers()) View.VISIBLE else View.GONE
        }
    }

    override fun bind(itinCardData: ItinCardDataHotel) {
        super.bind(itinCardData)
        setupIsVipHotelTextView(itinCardData)
        if (!itinCardData.hasFetchedUpgradeOffers() && isRoomUpgradable()) {
            mRoomUpgradeAvailableBanner.visibility = View.GONE
            roomUpgradeService.fetchOffers(itinCardData.property.roomUpgradeOffersApiUrl, makeOffersObservable())
        } else {
            setupRoomUpgradeBanner()
        }
    }

    override fun finishExpand() {
        mHeaderGallery.showPhotoCount = isBucketedForGallery()
        mHeaderGallery.canScroll = isBucketedForGallery()
        mRoomUpgradeAvailableBanner.visibility = View.GONE
        OmnitureTracking.trackHotelItinGallery()
        super.finishExpand()
    }

    override fun collapse(animate: Boolean): AnimatorSet {
        setupRoomUpgradeBanner()
        return super.collapse(animate)
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

    private fun setupRoomUpgradeBanner() {
        val itinCardData = mItinContentGenerator.itinCardData as ItinCardDataHotel
        mRoomUpgradeAvailableBanner.visibility = if (itinCardData.hasRoomUpgradeOffers()) View.VISIBLE else View.GONE
    }

    private fun isRoomUpgradable(): Boolean {
        val itinCardData = mItinContentGenerator.itinCardData as ItinCardDataHotel
        val isFeatureOn = FeatureToggleUtil.isFeatureEnabled(context, R.string.preference_itin_hotel_upgrade)
        val hasRoomOffersApi = itinCardData.property.roomUpgradeOffersApiUrl != null
        val isRoomUpgradable = isFeatureOn && !itinCardData.isSharedItin && hasRoomOffersApi
        return isRoomUpgradable
    }

    private fun makeOffersObservable() : Observer<RoomUpgradeOffersResponse> {
        return object: Observer<RoomUpgradeOffersResponse> {
            override fun onError(e: Throwable?) {
                e?.printStackTrace()
                mRoomUpgradeOffersSubject.onNext(Property.RoomUpgradeType.NO_UPGRADE_OFFERS)
            }

            override fun onNext(response: RoomUpgradeOffersResponse) {
                mRoomUpgradeOffersSubject.onNext(if (response.upgradeOffers.roomOffers.isNotEmpty()) Property.RoomUpgradeType.HAS_UPGRADE_OFFERS else Property.RoomUpgradeType.NO_UPGRADE_OFFERS)
            }

            override fun onCompleted() { }
        }
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