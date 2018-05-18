package com.expedia.bookings.test.robolectric

import android.app.Activity
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.lx.LXActivity
import com.expedia.bookings.data.lx.LXSearchResponse
import com.expedia.bookings.features.Features
import com.expedia.bookings.utils.FeatureTestUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.LXResultsListAdapter
import com.expedia.bookings.widget.LoadingRecyclerViewAdapter
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import java.util.ArrayList
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class LXResultsListAdapterTest {

    private var activity: Activity by Delegates.notNull()
    lateinit var activities: ArrayList<LXActivity>
    var discountType: String? = null
    lateinit var lxResultsListAdapter: LXResultsListAdapter
    lateinit var destination: String
    var totalActivityCount: Int by Delegates.notNull<Int>()

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_LX)
        Ui.getApplication(activity).defaultLXComponents()

        val lxSearchResponse = LXSearchResponse()
        activities = ArrayList<LXActivity>()

        val firstActivity = LXActivity()
        firstActivity.price = Money("19", "USD")
        activities.add(firstActivity)

        val secondActivity = LXActivity()
        secondActivity.price = Money("32", "USD")
        activities.add(secondActivity)

        val thirdActivity = LXActivity()
        thirdActivity.price = Money("31", "USD")
        activities.add(thirdActivity)

        lxSearchResponse.activities = activities
        destination = "Silicon Valley"
        totalActivityCount = 3
    }

    @Test
    fun activityCountHeaderShownForAsFirstCell() {
        FeatureTestUtils.enableFeature(activity, Features.all.lxActivityResultsHeader)
        lxResultsListAdapter = LXResultsListAdapter()
        lxResultsListAdapter.setItems(activities, discountType, destination)

        val itemViewType = lxResultsListAdapter.getItemViewType(0)
        assertEquals(LoadingRecyclerViewAdapter.ACTIVITY_COUNT_HEADER_VIEW, itemViewType)
    }

    @Test
    fun activityDataViewShownForAsSecondCell() {
        FeatureTestUtils.enableFeature(activity, Features.all.lxActivityResultsHeader)
        lxResultsListAdapter = LXResultsListAdapter()
        lxResultsListAdapter.setItems(activities, discountType, destination)

        val itemViewType = lxResultsListAdapter.getItemViewType(1)
        assertEquals(LoadingRecyclerViewAdapter.DATA_VIEW, itemViewType)
    }

    @Test
    fun activityDataViewShownForAsThirdCell() {
        FeatureTestUtils.enableFeature(activity, Features.all.lxActivityResultsHeader)
        lxResultsListAdapter = LXResultsListAdapter()
        lxResultsListAdapter.setItems(activities, discountType, destination)

        val itemViewType = lxResultsListAdapter.getItemViewType(2)
        assertEquals(LoadingRecyclerViewAdapter.DATA_VIEW, itemViewType)
    }
}
