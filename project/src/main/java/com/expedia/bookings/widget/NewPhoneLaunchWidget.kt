package com.expedia.bookings.widget

import android.animation.ObjectAnimator
import android.content.Context
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.expedia.bookings.R
import com.expedia.bookings.data.HotelSearchParams
import com.expedia.bookings.data.HotelSearchResponse
import com.expedia.bookings.data.Property
import com.expedia.bookings.data.collections.Collection
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.NearbyHotelParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.otto.Events
import com.expedia.bookings.services.CollectionServices
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.mobiata.android.Log
import com.squareup.otto.Subscribe
import org.joda.time.LocalDate
import org.joda.time.format.ISODateTimeFormat
import rx.Observer
import rx.Subscription
import java.util.Locale
import javax.inject.Inject

class NewPhoneLaunchWidget(context: Context, attrs: AttributeSet?) : CoordinatorLayout(context, attrs) {

    private val TAG = "NewPhoneLaunchWidget"
    private val HOTEL_SORT = "ExpertPicks"

    lateinit var collectionServices: CollectionServices
        @Inject set

    lateinit var hotelServices: HotelServices
        @Inject set

    private var downloadSubscription: Subscription? = null
    private var wasHotelsDownloadEmpty: Boolean = false

    val fab: FloatingActionButton by lazy {
        findViewById(R.id.fab) as FloatingActionButton
    }

    val fabTranslationHeight by lazy {
        (context.resources.getDimensionPixelSize(R.dimen.new_launch_screen_fab_height) +
                context.resources.getDimensionPixelSize(R.dimen.new_launch_screen_fab_bottom_margin)).toFloat()
    }

    val launchListWidget: LaunchListWidget by bindView(R.id.launch_list_widget)
    val appBarLayout: AppBarLayout by bindView(R.id.app_bar)
    val darkView: View by bindView(R.id.darkness)
    val appBarLayoutHeight: Int by lazy {
        appBarLayout.height
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        Ui.getApplication(context).defaultLaunchComponents()
        Ui.getApplication(context).launchComponent().inject(this)
        launchListWidget.addOnScrollListener(scrollListener)

        fab.setOnClickListener {
            if (darkView.visibility == INVISIBLE) {
                showLobAndDarkView()
            } else {
                hideLobAndDarkView()
            }
        }

        darkView.setOnClickListener {
            hideLobAndDarkView()
        }
    }

    private fun showLobAndDarkView() {
        darkView.visibility = VISIBLE
        appBarLayout.setExpanded(true, true)
    }

    private fun hideLobAndDarkView() {
        darkView.visibility = INVISIBLE
        appBarLayout.setExpanded(false, true)
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
        val fabAnimIn = ObjectAnimator.ofFloat(fab, "translationY", 0f)
        fabAnimation(fabAnimIn)
    }

    private fun hideFabButton() {
        val fabAnimOut = ObjectAnimator.ofFloat(fab, "translationY", fabTranslationHeight)
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

    fun showLoadingAnimationOnList() {
        launchListWidget.showListLoadingAnimation()
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


        val currentDate = LocalDate()
        val dtf = ISODateTimeFormat.date()

        val today = dtf.print(currentDate)
        val tomorrow = dtf.print(currentDate.plusDays(1))

        val params = NearbyHotelParams(loc.latitude.toString(),
                loc.longitude.toString(), "1",
                today, tomorrow, HOTEL_SORT, "true")
        val searchParams = HotelSearchParams()
        searchParams.checkInDate = currentDate
        searchParams.checkOutDate = currentDate.plusDays(1)
        searchParams.setSearchLatLon(loc.latitude, loc.longitude)
        searchParams.setFromLaunchScreen(true)
        downloadSubscription = hotelServices.nearbyHotels(params, getNearByHotelObserver())

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
        val localeCode = context.resources.configuration.locale.toString()
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

}
