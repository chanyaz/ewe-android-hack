package com.expedia.bookings.launch.widget

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Rect
import android.location.Location
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.RelativeLayout
import com.expedia.bookings.R
import com.expedia.bookings.animation.ActivityTransitionUtil
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.HotelSearchParams
import com.expedia.bookings.data.HotelSearchResponse
import com.expedia.bookings.data.Property
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.collections.Collection
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.NearbyHotelParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.launch.activity.NewPhoneLaunchActivity
import com.expedia.bookings.launch.vm.NewLaunchLobViewModel
import com.expedia.bookings.otto.Events
import com.expedia.bookings.services.CollectionServices
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.JodaUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.navigation.HotelNavUtils
import com.expedia.bookings.widget.FrameLayout
import com.expedia.bookings.widget.shared.SearchInputTextView
import com.expedia.util.updateVisibility
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

class NewPhoneLaunchWidget(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    private val TAG = "NewPhoneLaunchWidget"
    private val HOTEL_SORT = "ExpertPicks"
    private val MINIMUM_TIME_AGO = 15 * DateUtils.MINUTE_IN_MILLIS // 15 minutes ago
    private val DARK_VIEW_VISIBLE_ALPHA = 0.7f

    lateinit var collectionServices: CollectionServices
        @Inject set

    lateinit var hotelServices: HotelServices
        @Inject set

    var searchParams: HotelSearchParams? = null
    private var downloadSubscription: Subscription? = null
    private var wasHotelsDownloadEmpty = false
    private var launchDataTimeStamp: DateTime? = null
    private var isPOSChanged = false

    val fab: FloatingActionButton  by bindView(R.id.fab)

    private val fabAnimIn: ObjectAnimator by lazy {
        val fabAnimIn = ObjectAnimator.ofFloat(fab, "translationY", 0f)
        fabAnimIn.interpolator = AccelerateDecelerateInterpolator()
        fabAnimIn
    }

    private val fabAnimOut: ObjectAnimator by lazy {
        val fabAnimOut = ObjectAnimator.ofFloat(fab, "translationY", fabHeightAndBottomMargin)
        fabAnimOut.interpolator = AccelerateDecelerateInterpolator()
        fabAnimOut
    }

    val launchError: ViewGroup by bindView(R.id.launch_error)
    val toolbarShadow: View by bindView(R.id.toolbar_dropshadow)
    val toolBarHeight: Float by lazy {
        Ui.getToolbarSize(context).toFloat()
    }

    val fabHeightAndBottomMargin by lazy {
        context.resources.getDimensionPixelSize(R.dimen.new_launch_fab_height).toFloat() +
                context.resources.getDimensionPixelSize(R.dimen.new_launch_screen_fab_bottom_margin).toFloat()
    }

    val launchListWidget: LaunchListWidget by bindView(R.id.launch_list_widget)
    private val lobViewContainer: android.widget.FrameLayout by bindView(R.id.lob_view_container)
    private val lobView: NewLaunchLobWidget by lazy {
        val newLaunchLobWidget = LayoutInflater.from(context).inflate(R.layout.widget_new_launch_lob, null, false) as NewLaunchLobWidget
        newLaunchLobWidget.viewModel = NewLaunchLobViewModel(context, hasInternetConnection, posChangeSubject);
        newLaunchLobWidget
    }

    private val proWizardSearchBar: RelativeLayout by bindView(R.id.pro_wizard_search_bar)
    private val proWizardSearchCardView: CardView by bindView(R.id.pro_wizard_search_card_view)
    private val proWizardSearchBarView: SearchInputTextView by bindView(R.id.pro_wizard_search_bar_view)
    private val proWizardSearchBarShadow: View by bindView(R.id.pro_wizard_search_bar_shadow)

    private val proWizardItemDecorationPadding = PaddingItemDecoration(resources.getDimensionPixelOffset(R.dimen.launch_list_padding_to_pro_wizard))

    var hasInternetConnection = BehaviorSubject.create<Boolean>()
    var currentLocationSubject = BehaviorSubject.create<Location>()
    var locationNotAvailable = BehaviorSubject.create<Unit>()
    val posChangeSubject = BehaviorSubject.create<Unit>()

    val darkView: View by bindView(R.id.darkness)

    override fun onFinishInflate() {
        super.onFinishInflate()
        Ui.getApplication(context).defaultLaunchComponents()
        Ui.getApplication(context).launchComponent().inject(this)
        toolbarShadow.alpha = 0f
        launchListWidget.addOnScrollListener(scrollListener)
        lobViewContainer.addView(lobView)
        lobViewContainer.visibility = VISIBLE
        lobView.lobViewHeightChangeSubject.subscribe {
            adjustLobViewHeight()
        }

        fab.setOnClickListener {
            if (darkView.alpha == 0.0f) {
                showLobAndDarkView()
            } else {
                hideLobAndDarkView()
            }
        }
        val gestureDetector: GestureDetector = GestureDetector(context, gestureListener)

        darkView.setOnTouchListener { view, motionEvent ->
            gestureDetector.onTouchEvent(motionEvent)
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
            params.setSearchLatLon(selectedHotel.latitude, selectedHotel.longitude)
            HotelNavUtils.goToHotels(context, params)
        }

        (launchListWidget.adapter as LaunchListAdapter).seeAllClickSubject.subscribe { animOptions ->
            HotelNavUtils.goToHotels(context, searchParams, animOptions, 0)
        }

        adjustLobViewHeight()

        hasInternetConnection.subscribe { isOnline ->
            launchListWidget.onHasInternetConnectionChange(isOnline)
            if (!isOnline) {
                launchListWidget.scrollToPosition(0)
                launchError.visibility = View.VISIBLE
            } else {
                launchError.visibility = View.GONE
            }

        }

        currentLocationSubject.subscribe { currentLocation ->
            launchListWidget.visibility = View.VISIBLE
            launchError.visibility = View.GONE
            if (isNearByHotelDataExpired() || isPOSChanged) {
                isPOSChanged = false
                val params = buildHotelSearchParams(currentLocation)
                searchParams = buildDeeplinkToHotelSearchParams(currentLocation)
                downloadSubscription = hotelServices.nearbyHotels(params, getNearByHotelObserver())
                launchDataTimeStamp = DateTime.now()
                launchListWidget.showListLoadingAnimation()
            }
        }

        locationNotAvailable.subscribe {
            showCollections()
        }

        posChangeSubject.subscribe {
            isPOSChanged = true
            Ui.getApplication(context).defaultLaunchComponents()
            Ui.getApplication(context).launchComponent().inject(this)
            if (currentLocationSubject.value != null) {
                currentLocationSubject.onNext(currentLocationSubject.value)
            } else {
                locationNotAvailable.onNext(Unit)
            }
            launchListWidget.onPOSChange()

            initializeProWizard()
        }

        initializeProWizard()
    }

    fun initializeProWizard() {
        val proWizardBucketed = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.ProWizardTest)
        proWizardSearchBar.updateVisibility(proWizardBucketed)
        proWizardSearchBarShadow.updateVisibility(proWizardBucketed)

        proWizardSearchBarView.setText(PointOfSale.getPointOfSale().getProWizardLOBString(context))

        if (proWizardBucketed) {
            launchListWidget.removeItemDecoration(proWizardItemDecorationPadding)
            launchListWidget.addItemDecoration(proWizardItemDecorationPadding)
        } else {
            launchListWidget.removeItemDecoration(proWizardItemDecorationPadding)
        }
    }

    fun toggleProWizardClickListener(enable: Boolean) {
        if (enable) {
            proWizardSearchBarView.setOnClickListener(ProWizardClickListener())
        } else {
            proWizardSearchBarView.setOnClickListener(null)
        }
    }

    private fun showCollections() {
        launchListWidget.visibility = View.VISIBLE
        launchListWidget.showListLoadingAnimation()
        val country = PointOfSale.getPointOfSale().twoLetterCountryCode.toLowerCase(Locale.US)
        val localeCode = PointOfSale.getPointOfSale().localeIdentifier
        launchDataTimeStamp = null
        downloadSubscription = collectionServices.getPhoneCollection(
                ProductFlavorFeatureConfiguration.getInstance().phoneCollectionId, country, localeCode,
                collectionDownloadListener)
    }

    private fun adjustLobViewHeight() {
        lobViewContainer.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                viewTreeObserver.removeOnPreDrawListener(this)
                val lobContainerHeight = lobViewContainer.height.toFloat()
                lobViewContainer.translationY = -lobContainerHeight
                hideFabButton()
                return false
            }
        })
    }

    private fun handleBackOrDarkViewClick(): Boolean {
        val hideDarkView = darkView.alpha == DARK_VIEW_VISIBLE_ALPHA
        if (hideDarkView) {
            hideLobAndDarkView()
            showFabButton()
        }
        return hideDarkView
    }

    private fun showLobAndDarkView() {
        val showDarknessAnim = ObjectAnimator.ofFloat(darkView, "alpha", 0f, DARK_VIEW_VISIBLE_ALPHA)
        startAnimation(showDarknessAnim)
        val lobViewAnimIn = ObjectAnimator.ofFloat(lobViewContainer, "translationY", 0f)
        startAnimation(lobViewAnimIn)
        hideFabButton()
        OmnitureTracking.trackExpandedLobView()
    }

    private fun hideLobAndDarkView() {
        val hideDarknessAnim = ObjectAnimator.ofFloat(darkView, "alpha", DARK_VIEW_VISIBLE_ALPHA, 0f)
        startAnimation(hideDarknessAnim)
        val lobViewAnimOut = ObjectAnimator.ofFloat(lobViewContainer, "translationY", -lobViewContainer.height.toFloat())
        startAnimation(lobViewAnimOut)
    }

    private fun startAnimation(objectAnimator: ObjectAnimator) {
        objectAnimator.interpolator = AccelerateDecelerateInterpolator()
        objectAnimator.start()
    }

    val scrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val lobHeaderView = launchListWidget.layoutManager.findViewByPosition(0)
            val scrollY = lobHeaderView?.y ?: toolBarHeight
            val value = Math.abs(scrollY) / toolBarHeight
            toolbarShadow.alpha = Math.min(1f, Math.max(0f, value))
            if (lobHeaderView == null && lobViewContainer.translationY < 0) {
                showFabButton()
            } else {
                hideFabButton()
            }
        }
    }

    private fun showFabButton() {
        if (Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.ProWizardTest)) {
            return
        }

        if (fab.visibility != VISIBLE) {
            fab.visibility = VISIBLE
        }
        if ((fab.translationY >= fabHeightAndBottomMargin)
                && !fabAnimIn.isRunning) {
            fabAnimIn.start()
        }
    }

    private fun hideFabButton() {
        if (Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.ProWizardTest)) {
            return
        }

        if ((fab.translationY <= 0f)
                && !fabAnimOut.isRunning) {
            fabAnimOut.start()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Events.register(this)
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

    @Suppress("unused")
    @Subscribe
    fun onLaunchResume(event: Events.PhoneLaunchOnResume) {
        // TODO  refresh the hotel list if it expired
        Log.i(TAG, "On Launch or Resume event" + event)
    }

    private fun buildHotelSearchParams(loc: Location): NearbyHotelParams {
        val currentDate = LocalDate()
        val dtf = ISODateTimeFormat.date()

        val today = dtf.print(currentDate)
        val tomorrow = dtf.print(currentDate.plusDays(1))

        val params = NearbyHotelParams(loc.latitude.toString(),
                loc.longitude.toString(), "1",
                today, tomorrow, HOTEL_SORT, "true")
        return params
    }

    private fun buildDeeplinkToHotelSearchParams(loc: Location): HotelSearchParams {
        val currentDate = LocalDate()
        val searchParams = HotelSearchParams()
        searchParams.checkInDate = currentDate
        searchParams.checkOutDate = currentDate.plusDays(1)
        searchParams.setSearchLatLon(loc.latitude, loc.longitude)
        return searchParams
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
                locationNotAvailable.onNext(Unit)
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
                    locationNotAvailable.onNext(Unit)
                }
            }
        }
        return downloadListener
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
        return launchDataTimeStamp == null || JodaUtils.isExpired(launchDataTimeStamp, MINIMUM_TIME_AGO) || isPOSChanged

    }

    fun onBackPressed(): Boolean {
        return handleBackOrDarkViewClick()
    }

    private val gestureListener = object : GestureDetector.SimpleOnGestureListener() {

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            if (e2.y - e1.y < 0) {
                return handleBackOrDarkViewClick()
            } else {
                return super.onFling(e1, e2, velocityX, velocityY)
            }
        }

        override fun onDown(e: MotionEvent?): Boolean {
            return if (darkView.alpha == 0f) false else true
        }

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            return handleBackOrDarkViewClick()
        }
    }

    private inner class ProWizardClickListener() : View.OnClickListener {
        override fun onClick(v: View?) {
            OmnitureTracking.trackProWizardClick()
            val activity = context as NewPhoneLaunchActivity
            val pairs = ActivityTransitionUtil.createPairsWithAndroidComponents(activity,
                    proWizardSearchCardView, context.getString(R.string.pro_wizard_bar_hero_animation))
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(context as NewPhoneLaunchActivity, *pairs)

            HotelNavUtils.goToHotelsV2Params(context, null, options.toBundle(), 0)
        }
    }

    fun refreshState() {
        launchListWidget.notifyDataSetChanged()
    }

    private inner class PaddingItemDecoration(private val size: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            super.getItemOffsets(outRect, view, parent, state)

            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.top += size
            }
        }
    }
}
