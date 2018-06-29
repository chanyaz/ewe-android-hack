package com.expedia.bookings.launch.displaylogic

import com.expedia.bookings.launch.widget.LaunchDataItem

sealed class LaunchListState(val trackName: String, open val launchItemList: List<LaunchDataItem>)
data class NoInternet(override val launchItemList: List<LaunchDataItem>) : LaunchListState("NoInternet", launchItemList)
data class GuestNoHistory(override val launchItemList: List<LaunchDataItem>) : LaunchListState("GuestNoHist", launchItemList)
data class SignInNoHistory(override val launchItemList: List<LaunchDataItem>) : LaunchListState("SignInNoHist", launchItemList)
