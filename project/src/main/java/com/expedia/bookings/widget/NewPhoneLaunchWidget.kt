package com.expedia.bookings.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.location.Location
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.text.format.DateUtils
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.data.HotelSearchParams
import com.expedia.bookings.data.HotelSearchResponse
import com.expedia.bookings.data.Property
import com.expedia.bookings.data.collections.Collection
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.NearbyHotelParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.otto.Events
import com.expedia.bookings.services.CollectionServices
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.JodaUtils
import com.expedia.bookings.utils.NavUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.mobiata.android.Log
import com.squareup.otto.Subscribe
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.format.ISODateTimeFormat
import rx.Observer
import rx.Subscription
import rx.subjects.BehaviorSubject
import java.util.Locale
import javax.inject.Inject

class NewPhoneLaunchWidget(context: Context, attrs: AttributeSet) : CoordinatorLayout(context, attrs) {

    private val TAG = "NewPhoneLaunchWidget"
    private val HOTEL_SORT = "ExpertPicks"
    private val MINIMUM_TIME_AGO = 15 * DateUtils.MINUTE_IN_MILLIS // 15 minutes ago


    lateinit var collectionServices: CollectionServices
        @Inject set

    lateinit var hotelServices: HotelServices
        @Inject set

    var searchParams: HotelSearchParams ? = null
    private var downloadSubscription: Subscription? = null
    private var wasHotelsDownloadEmpty = false
    private var isAirAttachDismissed = false
    private var launchDataTimeStamp: DateTime? = null

    val fab: FloatingActionButton by lazy {
        findViewById(R.id.fab) as FloatingActionButton
    }

    val lobView: View by bindView(R.id.lob_view)
    val launchError: ViewGroup by bindView(R.id.launch_error)
    val airAttachBanner: ViewGroup by bindView(R.id.air_attach_banner)
    val airAttachBannerCloseButton: ImageView by bindView(R.id.air_attach_banner_close)
    val toolbarShadow: View by bindView(R.id.toolbar_dropshadow)
    val toolBarHeight: Float by lazy {
        Ui.getToolbarSize(context).toFloat()
    }

    val fabTranslationHeight by lazy {
        fab.height + context.resources.getDimensionPixelSize(R.dimen.new_launch_screen_fab_bottom_margin).toFloat()
    }

    val launchListWidget: LaunchListWidget by bindView(R.id.launch_list_widget)
    val appBarLayout: AppBarLayout by bindView(R.id.app_bar)
    var hasInternetConnection = BehaviorSubject.create<Boolean>()
    var showAirAttachBanner = BehaviorSubject.create<Boolean>()

    val darkView: View by bindView(R.id.darkness)
    var appBarLayoutHeight: Int = 0
    val lobHeightMatchingView: View by bindView(R.id.lob_height_matching_view)

    override fun onFinishInflate() {
        super.onFinishInflate()
        Ui.getApplication(context).defaultLaunchComponents()
        Ui.getApplication(context).launchComponent().inject(this)
        toolbarShadow.alpha = 0f
        launchListWidget.addOnScrollListener(scrollListener)

        fab.setOnClickListener {
            if (darkView.visibility == INVISIBLE) {
                showLobAndDarkView()
            } else {
                hideLobAndDarkView()
            }
        }

        darkView.setOnTouchListener { view, motionEvent ->
            hideLobAndDarkView()
            showFabButton()
            true
        }

        (launchListWidget.adapter as LaunchListAdapter).hotelSelectedSubject.subscribe { selectedHotel ->
            val params = HotelSearchParams()
            params.hotelId = selectedHotel.hotelId
            params.query = selectedHotel.localizedName
            params.searchType = HotelSearchParams.SearchType.HOTEL
            val now = LocalDate.now()
            params.checkInDate = now
            params.checkOutDate = now.plusDays(1)
            params.numAdults = 2
            params.children = null
            NavUtils.goToHotels(context, params)
        }

        (launchListWidget.adapter as LaunchListAdapter).seeAllClickSubject.subscribe { animOptions ->
            NavUtils.goToHotels(context, searchParams, animOptions, 0)
        }

        lobView.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                viewTreeObserver.removeOnPreDrawListener(this)
                lobHeightMatchingView.layoutParams.height = lobView.height
                appBarLayoutHeight = lobView.height
                return false
            }
        })
        hasInternetConnection.subscribe { isOnline ->
            if (!isOnline) {
                launchListWidget.scrollToPosition(0)
                launchListWidget.visibility = View.GONE
                launchError.visibility = View.VISIBLE
            } else {
                launchError.visibility = View.GONE
                launchListWidget.showListLoadingAnimation()
            }

        }

        showAirAttachBanner.subscribe { showAirAttach ->
            if (!showAirAttach) {
                airAttachBanner.visibility = GONE
            } else {
                val hotelSearchParams = ItineraryManager.getInstance().hotelSearchParamsForAirAttach
                if (isAirAttachDismissed) {
                    airAttachBanner.visibility = View.GONE
                    return@subscribe
                }
                if (airAttachBanner.visibility == View.GONE) {
                    airAttachBanner.visibility = View.VISIBLE
                    airAttachBanner.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                        override fun onPreDraw(): Boolean {
                            if (airAttachBanner.height == 0 || airAttachBanner.visibility == View.GONE) {
                                return true
                            }
                            airAttachBanner.viewTreeObserver.removeOnPreDrawListener(this)
                            animateAirAttachBanner(hotelSearchParams, true)
                            return false
                        }
                    })
                } else {
                    animateAirAttachBanner(hotelSearchParams, false)
                }

                airAttachBanner.visibility = View.VISIBLE
            }

        }

        airAttachBannerCloseButton.setOnClickListener {
            isAirAttachDismissed = true
            airAttachBanner.animate().translationY(airAttachBanner.height.toFloat()).setDuration(300).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    airAttachBanner.visibility = View.GONE
                }
            })
        }
    }

    private fun animateAirAttachBanner(hotelSearchParams: HotelSearchParams?, animate: Boolean) {
        airAttachBanner.translationY = airAttachBanner.height.toFloat()
        airAttachBanner.animate().translationY(0f).duration = (if (animate) 300 else 0).toLong()
        // In the absence of search params from user's itin info,
        // launch into hotels mode.
        if (hotelSearchParams == null) {
            airAttachBanner.setOnClickListener {
                val animOptions = AnimUtils.createActivityScaleBundle(airAttachBanner)
                NavUtils.goToHotels(context, animOptions)
                OmnitureTracking.trackPhoneAirAttachBannerClick()
            }
        } else {
            airAttachBanner.setOnClickListener {
                NavUtils.goToHotels(context, hotelSearchParams)
                OmnitureTracking.trackPhoneAirAttachBannerClick()
            }
            OmnitureTracking.trackPhoneAirAttachBanner()
        }
    }


    private fun showLobAndDarkView() {
        darkView.visibility = VISIBLE
        val showDarknessAnim = ObjectAnimator.ofFloat(darkView, "alpha", 0f, 0.7f)
        fabAnimation(showDarknessAnim)
        val fabAnimIn = ObjectAnimator.ofFloat(lobView, "translationY", 0f)
        fabAnimation(fabAnimIn)
        hideFabButton()
    }

    private fun hideLobAndDarkView() {
        val hideDarknessAnim = ObjectAnimator.ofFloat(darkView, "alpha", 0.7f, 0f)
        fabAnimation(hideDarknessAnim)
        darkView.visibility = INVISIBLE
        val fabAnimOut = ObjectAnimator.ofFloat(lobView, "translationY", -lobView.height.toFloat())
        fabAnimation(fabAnimOut)
    }

    // Added only to handle fling on recycler view have to find a better way
    val scrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                val manager = recyclerView?.layoutManager as StaggeredGridLayoutManager
                val positions = manager.findFirstCompletelyVisibleItemPositions(null)
                if (positions.contains(0)) {
                    appBarLayout.setExpanded(true, true)
                }
            }
        }
    }

    // showing fab button based on AppBarLayout offfset
    val onOffsetChangedListener = AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
        val value = Math.abs(verticalOffset) / toolBarHeight
        toolbarShadow.alpha = Math.min(1f, Math.max(0f, value))

        if (Math.abs(verticalOffset) == appBarLayoutHeight || darkView.visibility == VISIBLE) {
            // The lines of business are now visible, adjust accordingly.
            // When the actual fab animation happens, don't just move it by a randomly chosen number like this 500 here
            showFabButton()
        } else {
            // The lines of business are no longer visible, adjust accordingly
            hideFabButton()
        }
    }

    private fun showFabButton() {
        if (fab.visibility != VISIBLE) {
            fab.visibility = VISIBLE
        }
        val fabAnimIn = ObjectAnimator.ofFloat(fab, "translationY", if (airAttachBanner.visibility == VISIBLE) -airAttachBanner.height.toFloat() else 0f)
        fabAnimation(fabAnimIn)
    }

    private fun hideFabButton() {
        val fabAnimOut = ObjectAnimator.ofFloat(fab, "translationY", if (airAttachBanner.visibility == VISIBLE) airAttachBanner.height + fabTranslationHeight else fabTranslationHeight)
        fabAnimation(fabAnimOut)
    }

    private fun fabAnimation(fabAnim: ObjectAnimator) {
        fabAnim.duration = 250
        fabAnim.interpolator = AccelerateDecelerateInterpolator()
        fabAnim.start()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Events.register(this)
        appBarLayout.addOnOffsetChangedListener(onOffsetChangedListener)
    }

    override fun onDetachedFromWindow() {
        Events.unregister(this)
        cleanup()
        super.onDetachedFromWindow()
    }

    private fun cleanup() {
        if (downloadSubscription != null) {
            downloadSubscription?.unsubscribe()
            downloadSubscription = null
        }
    }

    fun getCollectionObserver(): Observer<Collection> {
        val defaultCollectionListener = object : Observer<Collection> {
            override fun onCompleted() {
                cleanup()
                Log.d(TAG, "Default collection download completed.")
            }

            override fun onError(e: Throwable) {
                Log.d(TAG, e.message)
            }

            override fun onNext(collection: Collection) {
                Events.post(Events.CollectionDownloadComplete(collection))
            }
        }
        return defaultCollectionListener
    }

    @Subscribe
    fun onLaunchResume(event: Events.PhoneLaunchOnResume) {
        // TODO  refresh the hotel list if it expired
        Log.i(TAG, "On Launch or Resume event" + event)
    }

    // Hotel Search
    @Subscribe
    fun onLocationFound(event: Events.LaunchLocationFetchComplete) {
        val loc = event.location
        Log.i(TAG, "Start hotel search")
        launchListWidget.visibility = View.VISIBLE
        launchError.visibility = View.GONE

        if (isNearByHotelDataExpired()) {
            val params = buildHotelSearchParams(loc)
            downloadSubscription = hotelServices.nearbyHotels(params, getNearByHotelObserver())
            launchDataTimeStamp = DateTime.now()
        }

    }

    private fun buildHotelSearchParams(loc: Location): NearbyHotelParams {
        val currentDate = LocalDate()
        val dtf = ISODateTimeFormat.date()

        val today = dtf.print(currentDate)
        val tomorrow = dtf.print(currentDate.plusDays(1))

        val params = NearbyHotelParams(loc.latitude.toString(),
                loc.longitude.toString(), "1",
                today, tomorrow, HOTEL_SORT, "true")
        searchParams = HotelSearchParams()
        searchParams?.checkInDate = currentDate
        searchParams?.checkOutDate = currentDate.plusDays(1)
        searchParams?.setSearchLatLon(loc.latitude, loc.longitude)
        searchParams?.setFromLaunchScreen(true)
        return params
    }

    fun getNearByHotelObserver(): Observer<MutableList<Hotel>> {
        val downloadListener = object : Observer<MutableList<Hotel>> {
            override fun onCompleted() {
                if (!wasHotelsDownloadEmpty) {
                    cleanup()
                }
                Log.d(TAG, "Hotel download completed.")
            }

            override fun onError(e: Throwable) {
                Log.d(TAG, e.message)
            }

            override fun onNext(nearbyHotelResponse: MutableList<Hotel>) {
                // Pump our results into a HotelSearchResponse to appease some
                // legacy code.
                val response = HotelSearchResponse()
                for (offer in nearbyHotelResponse) {
                    val p = Property()
                    p.updateFrom(offer)
                    response.addProperty(p)
                }
                if (nearbyHotelResponse.size > 0) {
                    wasHotelsDownloadEmpty = false
                    Events.post(Events.LaunchHotelSearchResponse(nearbyHotelResponse))
                } else {
                    wasHotelsDownloadEmpty = true
                    Events.post(Events.LaunchLocationFetchError())
                }
            }
        }
        return downloadListener
    }

    @Subscribe
    fun onLocationNotAvailable(event: Events.LaunchLocationFetchError) {
        Log.i(TAG, "Start collection download " + event)
        launchListWidget.visibility = View.VISIBLE
        val country = PointOfSale.getPointOfSale().twoLetterCountryCode.toLowerCase(Locale.US)
        val localeCode = PointOfSale.getPointOfSale().localeIdentifier
        launchDataTimeStamp = null
        downloadSubscription = collectionServices.getPhoneCollection(
                ProductFlavorFeatureConfiguration.getInstance().phoneCollectionId, country, localeCode,
                collectionDownloadListener)
    }

    private val collectionDownloadListener = object : Observer<Collection> {
        override fun onCompleted() {
            cleanup()
            Log.d(TAG, "Collection download completed.")
        }

        override fun onError(e: Throwable) {
            Log.d(TAG, "Error downloading locale/POS specific Collections. Kicking off default download.")
            val country = PointOfSale.getPointOfSale().twoLetterCountryCode.toLowerCase(Locale.US)
            downloadSubscription = collectionServices.getPhoneCollection(ProductFlavorFeatureConfiguration.getInstance().phoneCollectionId, country,
                    "default", getCollectionObserver())
        }

        override fun onNext(collection: Collection) {
            Events.post(Events.CollectionDownloadComplete(collection))
        }
    }

    private fun isNearByHotelDataExpired(): Boolean {
        return launchDataTimeStamp == null || JodaUtils.isExpired(launchDataTimeStamp, MINIMUM_TIME_AGO)

    }

}
