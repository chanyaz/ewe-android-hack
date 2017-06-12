package com.expedia.vm.itin

import android.app.Activity
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.support.annotation.VisibleForTesting
import android.support.v4.content.ContextCompat
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.activity.AccountLibActivity
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.data.trips.Trip
import com.expedia.bookings.data.user.User
import com.expedia.bookings.tracking.ItinPageUsableTrackingData
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.ItineraryLoaderLoginExtender
import com.expedia.model.UserLoginStateChangedModel
import com.expedia.util.endlessObserver
import com.squareup.phrase.Phrase
import rx.Subscription
import rx.subjects.PublishSubject
import javax.inject.Inject

@VisibleForTesting
open class ItinSignInViewModel(val context: Context) {

    lateinit var itinPageUsablePerformanceModel: ItinPageUsableTrackingData
        @Inject set

    var addGuestItinClickSubject = PublishSubject.create<Unit>()

    val statusImageVisibilitySubject = PublishSubject.create<Boolean>()
    val statusTextSubject = PublishSubject.create<String>()
    val statusImageSubject = PublishSubject.create<Drawable>()
    val statusTextColorSubject = PublishSubject.create<Int>()
    val statusImageColorSubject = PublishSubject.create<PorterDuffColorFilter>()

    val updateButtonTextSubject = PublishSubject.create<String>()
    val updateButtonContentDescriptionSubject = PublishSubject.create<String>()
    val updateButtonTextColorSubject = PublishSubject.create<Int>()
    val updateButtonImageVisibilitySubject = PublishSubject.create<Boolean>()
    val updateButtonColorSubject = PublishSubject.create<Int>()
    val syncItinManagerSubject = PublishSubject.create<Unit>()

    private val userStateManager = Ui.getApplication(context).appComponent().userStateManager()
    var userLoginStateChangedModel: UserLoginStateChangedModel = Ui.getApplication(context).appComponent().userLoginStateChangedModel()
    private var loginStateSubsciption: Subscription? = null

    var signInClickSubject = endlessObserver<Unit> {
        if (userStateManager.isUserAuthenticated()) {
            syncItinManagerSubject.onNext(Unit)
        } else {
            loginStateSubsciption = userLoginStateChangedModel.userLoginStateChanged.filter { true }.subscribe {
                startTimerOnSuccessfullSignIn()
                loginStateSubsciption?.unsubscribe()
            }

            OmnitureTracking.trackItinSignIn()
            doItinSignIn()
        }
    }

    private var mCurrentState = MessageState.NONE
    private var mCurrentSyncHasErrors = false

    enum class MessageState {
        NOT_LOGGED_IN,
        NO_UPCOMING_TRIPS,
        TRIPS_ERROR,
        FAILURE,
        NONE
    }

    init {
        Ui.getApplication(context).tripComponent().inject(this)
    }

    fun startTimerOnSuccessfullSignIn() {
        itinPageUsablePerformanceModel.markSuccessfulStartTime(System.currentTimeMillis())
    }

    fun getSignInText(): String {
        return Phrase.from(context, R.string.Sign_in_with_TEMPLATE)
                .putOptional("brand", BuildConfig.brand)
                .format().toString()
    }

    fun syncFailure(error: ItineraryManager.SyncError?) {
        mCurrentSyncHasErrors = true
        setState(MessageState.FAILURE)
    }

    fun newTripsUpdateState(trips: MutableCollection<Trip>?) {
        if (mCurrentSyncHasErrors) {
            if (trips == null) {
                setState(MessageState.TRIPS_ERROR)
            } else {
                setState(MessageState.FAILURE)
            }
        } else if (userStateManager.isUserAuthenticated() && Db.getUser() != null) {
            setState(MessageState.NO_UPCOMING_TRIPS)
        } else {
            setState(MessageState.NOT_LOGGED_IN)
        }

        mCurrentSyncHasErrors = false
    }

    fun setState(state: MessageState) {
        mCurrentState = state
        when (state) {
            MessageState.NO_UPCOMING_TRIPS -> updateMessageAndButton(context.getString(R.string.no_upcoming_trips), context.getString(R.string.refresh_trips_new), 0)
            MessageState.FAILURE -> updateMessageAndButton(context.getString(R.string.fetching_trips_error_connection), context.getString(R.string.refresh_trips_new), R.drawable.ic_itin_connection_error)
            MessageState.TRIPS_ERROR -> updateMessageAndButton(context.getString(R.string.fetching_trips_error_connection), context.getString(R.string.refresh_trips_new), R.drawable.ic_itin_connection_error)
            MessageState.NOT_LOGGED_IN -> updateMessageAndButton(context.getString(R.string.itin_sign_in_message), getSignInText(), 0)
            MessageState.NONE -> {
            }
        }
    }

    @VisibleForTesting
    protected open fun doItinSignIn() {
        val args = AccountLibActivity.createArgumentsBundle(LineOfBusiness.ITIN, ItineraryLoaderLoginExtender())
        User.signIn(context as Activity, args)
    }

    private fun updateMessageAndButton(messageText: String, buttonText: String, imageResId: Int) {
        statusImageVisibilitySubject.onNext(imageResId != 0)
        statusTextSubject.onNext(messageText)
        updateButtonTextSubject.onNext(buttonText)
        updateButtonContentDescriptionSubject.onNext(buttonText + " " + context.getString(R.string.accessibility_cont_desc_role_button))
        warningStatusAttributes(messageText)
        buttonAttributes(imageResId)
    }

    private fun buttonAttributes(imageResId: Int) {
        if (imageResId != 0) {
            statusImageSubject.onNext(ContextCompat.getDrawable(context, imageResId))
            updateButtonTextColorSubject.onNext(ContextCompat.getColor(context, R.color.white))
            updateButtonImageVisibilitySubject.onNext(false)
            updateButtonColorSubject.onNext(ContextCompat.getColor(context, R.color.exp_launch_blue))
        } else {
            updateButtonTextColorSubject.onNext(ContextCompat.getColor(context, R.color.gray9))
            updateButtonImageVisibilitySubject.onNext(true)
            updateButtonColorSubject.onNext(ContextCompat.getColor(context, R.color.brand_secondary))
        }
    }

    private fun warningStatusAttributes(messageText: String) {
        if (messageText == context.getString(R.string.fetching_trips_error_connection)) {
            statusTextColorSubject.onNext(ContextCompat.getColor(context, R.color.exp_action_required_red))
            val colorMatrix = PorterDuffColorFilter(ContextCompat.getColor(context, R.color.exp_action_required_red), PorterDuff.Mode.SRC_IN)
            statusImageColorSubject.onNext(colorMatrix)
        } else {
            statusTextColorSubject.onNext(ContextCompat.getColor(context, R.color.gray9))
            val colorMatrix = PorterDuffColorFilter(ContextCompat.getColor(context, R.color.gray9), PorterDuff.Mode.SRC_IN)
            statusImageColorSubject.onNext(colorMatrix)
        }
    }

}