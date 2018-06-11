package com.expedia.bookings.launch.fragment

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.expedia.bookings.R
import com.expedia.bookings.launch.vm.JoinRewardsViewModel
import com.mobiata.android.util.AndroidUtils
import kotlinx.android.synthetic.main.fragment_join_rewards_dialog.*

open class JoinRewardsDialogFragment : DialogFragment() {
    val LOTTIE_PROGRESS_START_FRAME = 0
    val LOTTIE_PROGRESS_END_FRAME = 238
    val LOTTIE_SUCCESS_START_FRAME = 238
    val LOTTIE_SUCCESS_END_FRAME = 400
    val LOTTIE_FAILURE_START_FRAME = 657
    val LOTTIE_FAILURE_END_FRAME = 820

    private lateinit var joinRewardsViewModel: JoinRewardsViewModel
    private lateinit var activityCallBacks: UserHasSuccessfullyJoinedRewards

    companion object {
        @JvmStatic
        fun newInstance() = JoinRewardsDialogFragment()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            activityCallBacks = context as UserHasSuccessfullyJoinedRewards
        } catch (exception: ClassCastException) {
            throw ClassCastException(context.toString() + " must implement UserHasSuccessfullyJoinedRewards")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        initViewModel()
        setupDialog()
        return inflater.inflate(R.layout.fragment_join_rewards_dialog, container, false)
    }

    private fun initViewModel() {
        joinRewardsViewModel = JoinRewardsViewModel(context)
    }

    private fun setupDialog() {
        dialog.window.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(joinRewardsViewModel.shouldAllowUsersToCancelOnTouch)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTermsText()

        primaryActionCardView.setOnClickListener {
            joinRewards()

            titleTextView.text = joinRewardsViewModel.loadingMessage

            hideViewsForProgressLoading()

            lottieProgressView.setMinAndMaxFrame(LOTTIE_PROGRESS_START_FRAME, LOTTIE_PROGRESS_END_FRAME)
            setLottieAnimationListener()

            lottieProgressView.visibility = View.VISIBLE
            lottieProgressView.playAnimation()
        }

        secondaryActionButton.setOnClickListener {
            dismiss()
        }
    }

    open fun joinRewards() {
        joinRewardsViewModel.joinRewards()
    }

    fun setupTermsText() {
        termsTextView.text = joinRewardsViewModel.termsText
        termsTextView.movementMethod = LinkMovementMethod.getInstance()
    }

    fun setLottieAnimationListener() {
        lottieProgressView.addAnimatorListener(object : AnimatorListenerAdapter() {
            override fun onAnimationRepeat(animation: Animator?) {
                super.onAnimationRepeat(animation)
                onLottieAnimationRepeat()
            }

            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                onLottieAnimationEnd()
            }
        })
    }

    fun onLottieAnimationRepeat() {
        if (joinRewardsViewModel.joinRewardsIsComplete) {
            lottieProgressView.pauseAnimation()
            lottieProgressView.repeatCount = 1
            if (joinRewardsViewModel.joinRewardsWasSuccessful) {
                lottieProgressView.setMinAndMaxFrame(LOTTIE_SUCCESS_START_FRAME, LOTTIE_SUCCESS_END_FRAME)
            } else {
                lottieProgressView.setMinAndMaxFrame(LOTTIE_FAILURE_START_FRAME, LOTTIE_FAILURE_END_FRAME)
            }

            lottieProgressView.resumeAnimation()
        }
    }

    fun onLottieAnimationEnd() {
        if (joinRewardsViewModel.joinRewardsWasSuccessful) {
            primaryActionCardView.setOnClickListener { onSuccessActions() }
            secondaryActionButton.setOnClickListener { onSuccessActions() }

            titleTextView.text = joinRewardsViewModel.successText.modalTitle
            primaryActionTextView.text = joinRewardsViewModel.successText.primaryActionText
            secondaryActionButton.text = joinRewardsViewModel.successText.secondaryActionText

            setupAndShowSuccessConfetti()
        } else {
            titleTextView.text = joinRewardsViewModel.failureText.modalTitle
            primaryActionTextView.text = joinRewardsViewModel.failureText.primaryActionText
            secondaryActionButton.text = joinRewardsViewModel.failureText.secondaryActionText
        }

        primaryActionCardView.visibility = View.VISIBLE
        secondaryActionButton.visibility = View.VISIBLE
    }

    open fun onSuccessActions() {
        dismiss()
        activityCallBacks.onJoinRewardsSuccess()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(activity, theme) {
            override fun onBackPressed() {
                // Don't you dare close this dialog with the back button
            }
        }
    }

    fun hideViewsForProgressLoading() {
        termsTextView.visibility = View.GONE

        firstCheckImageView.visibility = View.GONE
        firstReasonTextView.visibility = View.GONE

        secondCheckImageView.visibility = View.GONE
        secondReasonTextView.visibility = View.GONE

        thirdCheckImageView.visibility = View.GONE
        thirdReasonTextView.visibility = View.GONE

        primaryActionCardView.visibility = View.GONE
        secondaryActionButton.visibility = View.GONE

        confettiView.visibility = View.GONE
    }

    fun setupAndShowSuccessConfetti() {
        confettiView.visibility = View.VISIBLE
        val xPosToEmitFrom = (lottieProgressView.x + lottieProgressView.width / 2) - AndroidUtils.dpToPx(context, joinRewardsViewModel.CONFETTI_SIZE_IN_DP / 2)
        val yPosToEmttFrom = (lottieProgressView.y + lottieProgressView.height / 2) - AndroidUtils.dpToPx(context, joinRewardsViewModel.CONFETTI_SIZE_IN_DP / 2)

        joinRewardsViewModel.configureConfettiView(confettiView.build())
                .setPosition(xPosToEmitFrom, yPosToEmttFrom)
                .burst(joinRewardsViewModel.CONFETTI_BURST_AMOUNT)
    }

    interface UserHasSuccessfullyJoinedRewards {
        fun onJoinRewardsSuccess()
    }

}
