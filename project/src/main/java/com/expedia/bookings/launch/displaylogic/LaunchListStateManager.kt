package com.expedia.bookings.launch.displaylogic

import android.arch.lifecycle.MutableLiveData
import com.expedia.account.util.NetworkConnectivity
import com.expedia.bookings.data.user.UserStateManager
import com.expedia.bookings.launch.widget.LaunchDataItem
import com.expedia.bookings.launch.widget.LaunchListLogic
import com.expedia.model.UserLoginStateChangedModel

class LaunchListStateManager (private val networkConnectivity: NetworkConnectivity,
                              private val userLoginStateChangedModel: UserLoginStateChangedModel,
                              private val userStateManager: UserStateManager,
                              private val launchListLogic: LaunchListLogic) {

    val launchListStateLiveData = MutableLiveData<LaunchListState>()

    init {
        subscribeToUserLoginStateChanged()
    }

    fun updateLaunchListState() {
        launchListStateLiveData.value = getCurrentLaunchListState()
    }

    @Suppress("UNUSED_PARAMETER")
    fun onHasInternetConnectionChange(hasInternetConnection: Boolean) {
        updateLaunchListState()
    }

    private fun subscribeToUserLoginStateChanged() {
        userLoginStateChangedModel.userLoginStateChanged.subscribe {
            updateLaunchListState()
        }
    }

    private fun getCurrentLaunchListState(): LaunchListState {
        return if (!networkConnectivity.isOnline()) {
                    NoInternet(getNoInternetItems())
                } else if (userStateManager.isUserAuthenticated()) {
                    SignInNoHistory(getSignInNoHistoryItems())
                } else {
                    GuestNoHistory(getGuestNoHistoryItems())
                }
    }

    private fun getGuestNoHistoryItems(): List<LaunchDataItem> {
        val items = ArrayList<LaunchDataItem>()
        items.addBrandLobItems()
        items.addGuestNoHistoryItems()
        return items
    }

    private fun getSignInNoHistoryItems(): List<LaunchDataItem> {
        val items = ArrayList<LaunchDataItem>()
        items.addBrandLobItems()
        items.addSignInNoHistoryItems()
        return items
    }

    private fun getNoInternetItems(): List<LaunchDataItem> {
        val items = ArrayList<LaunchDataItem>()
        items.addBrandLobItems()
        return items
    }

    private fun ArrayList<LaunchDataItem>.addBrandLobItems() {
        if (launchListLogic.showLaunchHeaderItem()) {
            this.add(LaunchDataItem(LaunchDataItem.BRAND_HEADER))
        }
        this.add(LaunchDataItem(LaunchDataItem.LOB_VIEW))
    }

    private fun ArrayList<LaunchDataItem>.addGuestNoHistoryItems() {
        this.add(LaunchDataItem(LaunchDataItem.SIGN_IN_VIEW))
        this.addMultiMerchItems()
    }

    private fun ArrayList<LaunchDataItem>.addSignInNoHistoryItems() {
        if (launchListLogic.show2XBanner()) {
            this.add(LaunchDataItem(LaunchDataItem.EARN_2X_MESSAGING_BANNER))
        }
        this.add(LaunchDataItem(LaunchDataItem.MEMBER_ONLY_DEALS))
        this.addMultiMerchItems()
    }

    private fun ArrayList<LaunchDataItem>.addMultiMerchItems() {
        this.add(LaunchDataItem(LaunchDataItem.MESO_LMD_SECTION_HEADER_VIEW))
        if (launchListLogic.showMesoHotelAd()) {
            this.add(LaunchDataItem(LaunchDataItem.MESO_HOTEL_AD_VIEW))
        }
        if (launchListLogic.showMesoDestinationAd()) {
            this.add(LaunchDataItem(LaunchDataItem.MESO_DESTINATION_AD_VIEW))
        }
        this.add(LaunchDataItem(LaunchDataItem.LAST_MINUTE_DEALS))
    }
}
