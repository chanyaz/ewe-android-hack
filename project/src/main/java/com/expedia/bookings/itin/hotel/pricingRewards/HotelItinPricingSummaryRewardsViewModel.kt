package com.expedia.bookings.itin.hotel.pricingRewards

import com.expedia.bookings.R
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.scopes.HasHotelRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.tripstore.data.Itin
import io.reactivex.subjects.PublishSubject

class HotelItinPricingSummaryRewardsViewModel<out S>(val scope: S) : IHotelPricingRewardsViewModel where S : HasLifecycleOwner, S : HasStringProvider, S : HasHotelRepo {
    var itinObserver: LiveDataObserver<Itin>

    override val logoSubject: PublishSubject<String> = PublishSubject.create()
    override val earnedPointsSubject: PublishSubject<String> = PublishSubject.create()
    override val basePointsSubject: PublishSubject<String> = PublishSubject.create()
    override val bonusPointsSubject: PublishSubject<String> = PublishSubject.create()

    init {
        itinObserver = LiveDataObserver { itin ->
            val rewardList = itin?.rewardList
            rewardList?.firstOrNull()?.let { reward ->
                val logoUrl = reward.logoUrl
                if (logoUrl != null && !logoUrl.isBlank()) {
                    logoSubject.onNext(logoUrl)
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

                val bonusPointsObject = reward.bonusPoints?.firstOrNull()
                val bonusPoints = bonusPointsObject?.m_pointValue
                val bonusPointsProgram = bonusPointsObject?.m_pointDescription
                if (bonusPoints != null && !bonusPoints.isBlank() && bonusPointsProgram != null && !bonusPointsProgram.isBlank()) {
                    bonusPointsSubject.onNext(scope.strings.fetchWithPhrase(
                            R.string.itin_hotel_details_price_summary_rewards_bonus_points_TEMPLATE,
                            mapOf("points" to bonusPoints, "program" to bonusPointsProgram)
                    ))
                }
            }
        }

        scope.itinHotelRepo.liveDataItin.observe(scope.lifecycleOwner, itinObserver)
    }
}