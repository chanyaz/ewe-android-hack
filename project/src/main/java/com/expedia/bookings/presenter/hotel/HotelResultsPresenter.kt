package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.DecelerateInterpolator
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoScrollListener
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.HotelListAdapter
import com.expedia.bookings.widget.RecyclerDividerDecoration
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.mobiata.android.Log
import com.squareup.phrase.Phrase
import rx.Observer
import rx.Subscription
import rx.subjects.PublishSubject
import javax.inject.Inject
import kotlin.properties.Delegates

public class HotelResultsPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs), OnMapReadyCallback {

    private val PICASSO_TAG = "HOTEL_RESULTS_LIST"

    var hotelServices : HotelServices by Delegates.notNull()
    @Inject set

    var downloadSubscription: Subscription? = null
    var screenHeight : Int = 0
    var mapTransitionRunning : Boolean = false

    val recyclerView: RecyclerView by bindView(R.id.list_view)
    val mapView: MapView by bindView(R.id.map_view)
    val toolbar: Toolbar by bindView(R.id.toolbar)

    val hotelSubject = PublishSubject.create<Hotel>()
    val layoutManager = LinearLayoutManager(context)

    init {
        Ui.getApplication(getContext()).hotelComponent().inject(this)
        View.inflate(getContext(), R.layout.widget_hotel_results, this)
    }

    override fun onFinishInflate() {
        addDefaultTransition(defaultTransition)
        addTransition(mapTransition)

        recyclerView.setLayoutManager(layoutManager)
        recyclerView.addOnScrollListener(PicassoScrollListener(getContext(), PICASSO_TAG))
        recyclerView.addOnScrollListener(scrollListener)
        recyclerView.addOnItemTouchListener(touchListener)

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.setBackgroundColor(getResources().getColor(R.color.hotels_primary_color))
        toolbar.setNavigationOnClickListener { view -> back() }
        toolbar.setTitleTextAppearance(getContext(), R.style.CarsToolbarTitleTextAppearance)
        toolbar.setSubtitleTextAppearance(getContext(), R.style.CarsToolbarSubtitleTextAppearance)
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super<Presenter>.onVisibilityChanged(changedView, visibility)
        if (changedView == this && visibility == View.VISIBLE) {
            getViewTreeObserver().addOnGlobalLayoutListener(layoutListener)
        }
    }

    fun doSearch(params : HotelSearchParams) {
        downloadSubscription = hotelServices.suggestHotels(params, downloadListener)
        toolbar.setTitle(params.city.regionNames.shortName)
        var text = Phrase.from(getContext(), R.string.calendar_instructions_date_range_with_guests_TEMPLATE).put("startdate", DateUtils.localDateToMMMd(params.checkIn)).put("enddate", DateUtils.localDateToMMMd(params.checkOut)).put("guests", params.children.size() + 1).format()
        toolbar.setSubtitle(text)
    }

    val downloadListener : Observer<List<Hotel>> = object : Observer<List<Hotel>> {
        override fun onNext(hotels: List<Hotel>) {
            recyclerView.setAdapter(HotelListAdapter(hotels, hotelSubject))
            Log.d("Hotel Results Next")
        }

        override fun onCompleted() {
            layoutManager.scrollToPositionWithOffset(1, screenHeight/2)
            show(ResultsList())
            Log.d("Hotel Results Completed")
        }

        override fun onError(e: Throwable?) {
            Log.d("Hotel Results Error")
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        //do something with the map
    }

    val scrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {

        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (mapTransitionRunning || getCurrentState().equals(javaClass<ResultsMap>().getName())) {
                return
            }

            val y = mapView.getTranslationY() + (-dy * .5f)
            val halfway = screenHeight/2

            // scrolling down
            if (y <= 0) {
                mapView.setTranslationY(y)
            }

            if (recyclerView.getChildAt(0).getTop() >= halfway) {
                recyclerView.stopScroll()
                show(ResultsMap())
            }
        }
    }

    private val layoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            getViewTreeObserver().removeOnGlobalLayoutListener(this)
            screenHeight = getHeight()
            recyclerView.addItemDecoration(RecyclerDividerDecoration(getContext(), 0, 0, 0, 0, screenHeight, 0, false))
        }
    }

    private val defaultTransition = object : Presenter.DefaultTransition(javaClass<ResultsList>().getName()) {
        override fun finalizeTransition(forward: Boolean) {
            recyclerView.setTranslationY(0f)
            mapView.setTranslationY(0f)
        }
    }

    private val mapTransition = object : Presenter.Transition(javaClass<ResultsList>(), javaClass<ResultsMap>(), DecelerateInterpolator(), 500) {

        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            mapTransitionRunning = true
            if (!forward) {
                layoutManager.scrollToPositionWithOffset(1, screenHeight/2)
            }
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
            var distance : Float

            if (forward) {
                distance = (screenHeight * f)
            } else {
                distance = (screenHeight * (1 - f))
            }

            recyclerView.setTranslationY(distance)
        }

        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            mapView.setTranslationY(0f)
            if (!forward) {
                recyclerView.setTranslationY(0f)
                layoutManager.scrollToPositionWithOffset(1, screenHeight/2)
            } else {
                recyclerView.setTranslationY(screenHeight.toFloat())
            }
            mapTransitionRunning = false
        }
    }

    val touchListener = object : RecyclerView.OnItemTouchListener {
        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

        }

        override fun onInterceptTouchEvent(rv: RecyclerView?, e: MotionEvent?): Boolean {
            if (mapTransitionRunning) {
                return true
            }
            return false
        }

        override fun onTouchEvent(rv: RecyclerView?, e: MotionEvent?) {

        }
    }


    // Classes for state
    public class ResultsList
    public class ResultsMap

}
