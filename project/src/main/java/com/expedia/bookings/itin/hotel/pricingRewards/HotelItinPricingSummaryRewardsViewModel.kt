package com.expedia.bookings.itin.hotel.pricingRewards

import com.expedia.bookings.R
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.scopes.HasE3Endpoint
import com.expedia.bookings.itin.scopes.HasItinRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.scopes.HasWebViewLauncher
import com.expedia.bookings.itin.tripstore.data.Itin
import io.reactivex.subjects.PublishSubject

class HotelItinPricingSummaryRewardsViewModel<out S>(val scope: S) : IHotelPricingRewardsViewModel where S : HasLifecycleOwner, S : HasStringProvider, S : HasItinRepo, S : HasTripsTracking, S : HasWebViewLauncher, S : HasE3Endpoint {

    override val hideWidgetSubject: PublishSubject<Unit> = PublishSubject.create()
    override val rewardsButtonClickSubject: PublishSubject<Unit> = PublishSubject.create()

    override val logoSubject: PublishSubject<String> = PublishSubject.create()
    override val earnedPointsSubject: PublishSubject<String> = PublishSubject.create()
    override val basePointsSubject: PublishSubject<String> = PublishSubject.create()
    override val bonusPointsSubject: PublishSubject<List<String>> = PublishSubject.create()

    val itinObserver: LiveDataObserver<Itin> = LiveDataObserver { itin ->
        val rewardList = itin?.rewardList
        rewardList?.firstOrNull()?.let { reward ->
            val endPoint = scope.e3Endpoint.removeSuffix("/")

            val logoUrl = reward.logoUrl
            val logoEndpoint = endPoint.plus(logoUrl)
            if (!logoEndpoint.isBlank()) {
                logoSubject.onNext(logoEndpoint)
            }

            val totalPoints = reward.totalPoints
            val programName = reward.programName
            if (totalPoints != null && programName != null) {
                earnedPointsSubject.onNext(scope.strings.fetchWithPhrase(
                        R.string.itin_hotel_details_price_summary_rewards_earned_points_TEMPLATE,
                        mapOf("points" to totalPoints, "program" to programName)))
            }

            val basePoints = reward.basePoints
            if (basePoints != null) {
                basePointsSubject.onNext(scope.strings.fetchWithPhrase(
                        R.string.itin_hotel_details_price_summary_rewards_base_points_TEMPLATE,
                        mapOf("points" to basePoints)
                ))
            }

            val bonusPointsObject = reward.bonusPoints
            val bonusPointsList = mutableListOf<String>()
            bonusPointsObject?.forEach {
                val bonusPoints = it.m_pointValue
                val bonusPointsProgram = it.m_pointDescription
                if (bonusPoints != null && !bonusPoints.isBlank() && bonusPointsProgram != null && !bonusPointsProgram.isBlank()) {
                    bonusPointsList.add(scope.strings.fetchWithPhrase(
                            R.string.itin_hotel_details_price_summary_rewards_bonus_points_TEMPLATE,
                            mapOf("points" to bonusPoints, "program" to bonusPointsProgram)
                    ))
                }
            }
            if (bonusPointsList.isNotEmpty()) {
                bonusPointsSubject.onNext(bonusPointsList)
            }

            rewardsButtonClickSubject.subscribe {
                val rewardsUrl = reward.viewStatementURL
                val rewardsEndpoint = endPoint.plus(rewardsUrl)
                val tripId = itin.tripId
                val isGuest: Boolean = itin.isGuest
                if (!rewardsEndpoint.isBlank() && tripId != null && !tripId.isBlank()) {
                    scope.webViewLauncher.launchWebViewActivity(R.string.itin_hotel_details_price_summary_rewards_title, rewardsEndpoint, null, tripId, true, isGuest = isGuest)
                    scope.tripsTracking.trackItinHotelViewRewards()
                }
            }
        } ?: run {
            hideWidgetSubject.onNext(Unit)
        }
    }

    init {
        scope.itinRepo.liveDataItin.observe(scope.lifecycleOwner, itinObserver)
    }
}
