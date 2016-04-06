package com.expedia.bookings.widget.feeds

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoScrollListener
import com.expedia.bookings.data.User
import com.expedia.bookings.data.collections.Collection
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.otto.Events
import com.expedia.bookings.services.CollectionServices
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.RecyclerDividerDecoration
import com.squareup.otto.Subscribe
import rx.Observer
import rx.exceptions.OnErrorNotImplementedException
import java.util.*
import javax.inject.Inject

class FeedsListWidget(context: Context, attrs: AttributeSet) : RecyclerView(context, attrs) {

    companion object {
        val PICASSO_TAG = "LAUNCH_LIST"
    }

    lateinit var collectionServices: CollectionServices
        @Inject set

    private val picassoScrollListener = PicassoScrollListener(context, PICASSO_TAG)
    private val feedsListAdapter = FeedsListAdapter(getActivity()!!)
    private var isUserLoggedIn = false // TODO - Only gonna need this if we wanna be efficient with when we fetch collections

    override fun onFinishInflate() {
        super.onFinishInflate()

        Ui.getApplication(context).launchComponent().inject(this)
        Events.register(this)

        setupItemDecoration()
        layoutManager = LinearLayoutManager(context, VERTICAL, false)
        adapter = feedsListAdapter

        if (User.isLoggedIn(context)) {
            feedsListAdapter.showFeeds()
        }
        else {
            showStaffPicks()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        addOnScrollListener(picassoScrollListener)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeOnScrollListener(picassoScrollListener)
    }

    private fun getActivity(): Activity? {
        var context = this.context;
        while (context is ContextWrapper) {
            if (context is Activity) {
                return context;
            }
            context = context.baseContext;
        }
        return null;
    }

    private fun setupItemDecoration() {
        val top = 0
        val right = context.resources.getDimensionPixelSize(R.dimen.launch_tile_margin_middle)
        val left = right
        val bottom = context.resources.getDimensionPixelSize(R.dimen.launch_tile_margin_bottom)
        val recyclerDividerDecoration = RecyclerDividerDecoration(context, left, top, right, bottom, 0, 0, false)
        addItemDecoration(recyclerDividerDecoration)
    }

    private fun showStaffPicks() {
        val staffPicksListener = object : Observer<Collection> {
            override fun onNext(collection: Collection) {
                feedsListAdapter.showStaffPicks(collection)
            }

            override fun onCompleted() {
                // do nothing
            }

            override fun onError(e: Throwable?) {
                OnErrorNotImplementedException(e)
            }
        }
        val country = PointOfSale.getPointOfSale().twoLetterCountryCode.toLowerCase(Locale.US)
        collectionServices.getPhoneCollection(ProductFlavorFeatureConfiguration.getInstance().phoneCollectionId, country, "default", staffPicksListener)
        // TODO locale param always default? we should fix that in the service layer if that's the case
    }

    @Subscribe fun onUserLoggedIn(event: Events.LoggedInSuccessful) {
        // load Feeds
        isUserLoggedIn = true
        feedsListAdapter.showFeeds()
    }

    @Subscribe fun onSignOut(event: Events.SignOut) {
        isUserLoggedIn = false
        // is staff picks already showing? >Do nothing

        // showing scratchpad?
        // hide scratchpad
        // show staff picks
        showStaffPicks()
    }
}
