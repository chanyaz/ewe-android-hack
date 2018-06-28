package com.expedia.bookings.launch.vm

import Utils.StrUtils
import android.content.Context
import android.support.v4.content.ContextCompat
import com.expedia.account.AccountService
import com.expedia.account.data.JoinRewardsResponse
import com.expedia.account.handler.JoinRewardsResponseHandler
import com.expedia.bookings.R
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.tracking.OmnitureTracking
import com.mobiata.android.Log
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import nl.dionsegijn.konfetti.ParticleSystem
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size

open class JoinRewardsViewModel(private val context: Context, private val accountService: AccountService) {
    val CONFETTI_SIZE_IN_DP = 6
    val CONFETTI_BURST_AMOUNT = 70

    val shouldAllowUsersToCancelOnTouch: Boolean = false
    var joinRewardsIsComplete: Boolean = false
    var joinRewardsWasSuccessful: Boolean = false

    val loadingMessage: String by lazy { context.getString(R.string.loading_message) }

    val successText: ModalStrings by lazy {
        ModalStrings(context.getString(R.string.your_in_title),
                context.getString(R.string.book_travel),
                context.getString(R.string.done))
    }

    val failureText: ModalStrings by lazy {
        ModalStrings(context.getString(R.string.error_with_request),
                context.getString(R.string.try_again),
                context.getString(R.string.cancel))
    }

    val termsText: CharSequence by lazy {
        StrUtils.generateAgreeToTermsLink(context, PointOfSale.getPointOfSale().loyaltyTermsAndConditionsUrl)
    }

    private val joinRewardsResponseHandler: JoinRewardsResponseHandler = JoinRewardsResponseHandler(accountService)
    private lateinit var joinRewardsSubject: PublishSubject<JoinRewardsResponse>

    fun joinRewards() {
        setupJoinRewardsSubject()
        joinRewardsResponseHandler.joinRewards(joinRewardsSubject)
    }

    private fun setupJoinRewardsSubject() {
        joinRewardsSubject = PublishSubject.create<JoinRewardsResponse>()
        joinRewardsSubject.subscribe(object : Observer<JoinRewardsResponse> {
            override fun onComplete() {
                joinRewardsIsComplete = true
            }

            override fun onSubscribe(d: Disposable) {
                // Not used
            }

            override fun onNext(joinRewardsResponse: JoinRewardsResponse) {
                joinRewardsIsComplete = true

                if (joinRewardsResponse.loyaltyMembershipActive) {
                    joinRewardsWasSuccessful = true
                    OmnitureTracking.trackJoinRewardsSuccess()
                }

                if (joinRewardsResponse.errors != null) {
                    OmnitureTracking.trackJoinRewardsError(joinRewardsResponse.errors?.first()?.errorInfo?.summary)
                }
            }

            override fun onError(e: Throwable) {
                Log.d(e.message)
                joinRewardsIsComplete = true
                joinRewardsWasSuccessful = false
                OmnitureTracking.trackJoinRewardsError(e.message)
            }
        })
    }

    fun configureConfettiView(particleSystem: ParticleSystem): ParticleSystem {
        return particleSystem.addColors(getConfettiColors())
                .setDirection(0.0, 359.0)
                .setSpeed(1f, 10f)
                .setFadeOutEnabled(true)
                .setTimeToLive(2000L)
                .addShapes(Shape.RECT)
                .addSizes(Size(CONFETTI_SIZE_IN_DP))
    }

    fun getConfettiColors(): MutableList<Int> {
        return mutableListOf(
                ContextCompat.getColor(context, R.color.confetti_color_one),
                ContextCompat.getColor(context, R.color.confetti_color_two),
                ContextCompat.getColor(context, R.color.confetti_color_three),
                ContextCompat.getColor(context, R.color.confetti_color_four),
                ContextCompat.getColor(context, R.color.confetti_color_five),
                ContextCompat.getColor(context, R.color.confetti_color_six))
    }

    data class ModalStrings(val modalTitle: String, val primaryActionText: String, val secondaryActionText: String)
}
