package com.expedia.bookings.itin.hotel.pricingRewards

import com.expedia.bookings.R
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.scopes.HasEndPointProvider
import com.expedia.bookings.itin.scopes.HasHotelRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.scopes.HasWebViewLauncher
import com.expedia.bookings.itin.tripstore.data.Itin
import io.reactivex.subjects.PublishSubject

class HotelItinPricingSummaryRewardsViewModel<out S>(val scope: S) : IHotelPricingRewardsViewModel where S : HasLifecycleOwner, S : HasStringProvider, S : HasHotelRepo, S : HasTripsTracking, S : HasWebViewLauncher, S : HasEndPointProvider {
    var itinObserver: LiveDataObserver<Itin>

    override val hideWidgetSubject: PublishSubject<Unit> = PublishSubject.create()
    override val rewardsButtonClickSubject: PublishSubject<Unit> = PublishSubject.create()

    override val logoSubject: PublishSubject<String> = PublishSubject.create()
    override val earnedPointsSubject: PublishSubject<String> = PublishSubject.create()
    override val basePointsSubject: PublishSubject<String> = PublishSubject.create()
    override val bonusPointsSubject: PublishSubject<List<String>> = PublishSubject.create()

    init {
        itinObserver = LiveDataObserver { itin ->
            val rewardList = itin?.rewardList
            rewardList?.firstOrNull()?.let { reward ->
                val endPoint = scope.endpointProvider.e3EndpointUrl.removeSuffix("/")

                val logoUrl = reward.logoUrl
                val logoEndpoint = endPoint.plus(logoUrl)
                if (!logoEndpoint.isBlank()) {
                    logoSubject.onNext(logoEndpoint)
                }

                val totalPoints = reward.totalPoints
                val programName = reward.programName
                if (totalPoints != null && !totalPoints.isBlank() && programName != null && !programName.isBlank()) {
                    earnedPointsSubject.onNext(scope.strings.fetchWithPhrase(
                            R.string.itin_hotel_details_price_summary_rewards_earned_points_TEMPLATE,
                            mapOf("points" to totalPoints, "program" to programName)))
                }

                val basePoints = reward.basePoints
                if (basePoints != null && !basePoints.isBlank()) {
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
                    if (!rewardsEndpoint.isBlank() && tripId != null && !tripId.isBlank()) {
                        scope.webViewLauncher.launchWebViewActivity(R.string.itin_hotel_details_price_summary_rewards_title, rewardsEndpoint, null, tripId)
                        scope.tripsTracking.trackHotelItinPricingRewardsClick()
                    }
                }
            } ?: run {
                hideWidgetSubject.onNext(Unit)
            }
        }

        scope.itinHotelRepo.liveDataItin.observe(scope.lifecycleOwner, itinObserver)
    }
}