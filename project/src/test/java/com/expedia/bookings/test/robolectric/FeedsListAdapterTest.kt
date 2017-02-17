package com.expedia.bookings.test.robolectric

import android.support.v7.widget.RecyclerView
import android.widget.LinearLayout
import butterknife.ButterKnife
import com.expedia.bookings.R
import com.expedia.bookings.data.collections.CollectionLocation
import com.expedia.bookings.widget.CollectionViewHolder
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.feeds.FeedsListAdapter
import com.expedia.vm.SignInPlaceHolderViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FeedsListAdapterTest {

    val context = RuntimeEnvironment.application

    lateinit var sut: FeedsListAdapter

    @Before
    fun setup() {
        sut = FeedsListAdapter(context)
    }

    @Test
    fun getItemViewType() {
        val expectedFeedTypeForViewModelMap =
                mapOf<Any, FeedsListAdapter.FeedTypes>(
                        Pair(FeedsListAdapter.FlightSearchViewModel::class.java, FeedsListAdapter.FeedTypes.FLIGHT_SEARCH_HOLDER),
                        Pair(CollectionLocation::class.java, FeedsListAdapter.FeedTypes.COLLECTION_VIEW_HOLDER),
                        Pair(FeedsListAdapter.HeadingViewModel::class.java, FeedsListAdapter.FeedTypes.HEADING_VIEW_HOLDER),
                        Pair(SignInPlaceHolderViewModel::class.java, FeedsListAdapter.FeedTypes.SIGNIN_PLACEHOLDER_VIEW_HOLDER),
                        Pair(FeedsListAdapter.GenericPlaceholderViewModel::class.java, FeedsListAdapter.FeedTypes.GENERIC_PLACEHOLDER_VIEW_HOLDER),
                        Pair(FeedsListAdapter.PopularHotelsTonightViewModel::class.java, FeedsListAdapter.FeedTypes.POPULAR_HOTELSTONIGHT_VIEW_HOLDER))

        givenFeedListWithMockData()

        for ((index, viewModel) in sut.feedList.withIndex()) {
            val feedType = sut.getItemViewType(index)
            val expectedFeedType = expectedFeedTypeForViewModelMap[viewModel.javaClass]
            assertEquals(expectedFeedType!!.ordinal, feedType, "Expected feed type not returned for given view model")
        }
    }

    @Test
    fun createViewHolder() {
        val parentViewGroup = LinearLayout(context)
        val expectedViewHolderForFeedTypeMap =
                mapOf<FeedsListAdapter.FeedTypes, Any>(
                        Pair(FeedsListAdapter.FeedTypes.COLLECTION_VIEW_HOLDER, CollectionViewHolder::class.java),
                        Pair(FeedsListAdapter.FeedTypes.FLIGHT_SEARCH_HOLDER, FeedsListAdapter.FlightSearchCardHolder::class.java),
                        Pair(FeedsListAdapter.FeedTypes.POPULAR_HOTELSTONIGHT_VIEW_HOLDER, FeedsListAdapter.PopularHotelsTonightCard::class.java),
                        Pair(FeedsListAdapter.FeedTypes.SIGNIN_PLACEHOLDER_VIEW_HOLDER, FeedsListAdapter.SignInPlaceholderCard::class.java))

        for (feedType in expectedViewHolderForFeedTypeMap.keys) {
            val viewHolder = sut.onCreateViewHolder(parentViewGroup, feedType.ordinal)
            val expectedViewHolder = expectedViewHolderForFeedTypeMap[feedType]
            assertEquals(expectedViewHolder, viewHolder!!.javaClass)
        }
    }

    @Test
    fun onBindViewHolderGenericPlaceholderViewModel() {
        val lineOne = "line 1"
        val lineTwo = "line 2"
        val buttonOneLabel = "buttonOne"
        val buttonTwoLabel = "buttonTwo"

        sut.feedList = listOf(FeedsListAdapter.GenericPlaceholderViewModel(lineOne, lineTwo, buttonOneLabel, buttonTwoLabel))

        val viewHolder = sut.createViewHolder(LinearLayout(context), FeedsListAdapter.FeedTypes.GENERIC_PLACEHOLDER_VIEW_HOLDER.ordinal)
        val viewModelPosition = 0
        sut.onBindViewHolder(viewHolder, viewModelPosition)

        assertEquals(lineOne, findTextViewById(viewHolder, R.id.first_line).text)
        assertEquals(lineTwo, findTextViewById(viewHolder, R.id.second_line).text)
        assertEquals(buttonOneLabel, findTextViewById(viewHolder, R.id.button_one).text)
        assertEquals(buttonTwoLabel, findTextViewById(viewHolder, R.id.button_two).text)
    }

    @Test
    fun onBindViewHolderFlightSearchViewModel() {
        val origin = "SFO"
        val destination = "LHR"
        val isReturn = false
        val travelers = "2"
        val departureReturnDate = "10 Apr - 20 Apr"
        val price = "$101.00"
        val lastUpdatedTimeStamp = "2m"
        sut.feedList = listOf(FeedsListAdapter.FlightSearchViewModel(origin, destination, isReturn, travelers, departureReturnDate, price, lastUpdatedTimeStamp))

        val flightSearchViewHolder = sut.createViewHolder(LinearLayout(context), FeedsListAdapter.FeedTypes.FLIGHT_SEARCH_HOLDER.ordinal) as FeedsListAdapter.FlightSearchCardHolder
        val viewModelPosition = 0
        sut.onBindViewHolder(flightSearchViewHolder, viewModelPosition)

        assertEquals(origin, flightSearchViewHolder.origin.text)
        assertEquals(destination, flightSearchViewHolder.destination.text)
        assertEquals(travelers, flightSearchViewHolder.numberTravelers.text)
        assertEquals(departureReturnDate, flightSearchViewHolder.departureAndReturnDates.text)
        assertEquals(price, flightSearchViewHolder.price.text)
        assertEquals(lastUpdatedTimeStamp, flightSearchViewHolder.freshnessTimeTextView.text)
    }

    @Test
    fun onBindViewHolderSignInViewModel() {
        val firstLine = "firstLineSubject"
        val secondLine = "secondLineSubject"
        val buttonOneLabel = "buttonOne"
        val buttonTwoLabel = "buttonTwo"

        sut.feedList = listOf(SignInPlaceHolderViewModel(firstLine, secondLine, buttonOneLabel, buttonTwoLabel))
        val signInViewHolder = sut.createViewHolder(LinearLayout(context), FeedsListAdapter.FeedTypes.SIGNIN_PLACEHOLDER_VIEW_HOLDER.ordinal)
        val viewModelPosition = 0

        sut.onBindViewHolder(signInViewHolder, viewModelPosition)

        assertEquals(firstLine, findTextViewById(signInViewHolder, R.id.first_line).text)
        assertEquals(secondLine, findTextViewById(signInViewHolder, R.id.second_line).text)
        assertEquals(buttonOneLabel, findTextViewById(signInViewHolder, R.id.button_one).text)
        assertEquals(buttonTwoLabel, findTextViewById(signInViewHolder, R.id.button_two).text)
    }

    @Test
    fun onBindViewHolderHeadingViewModel() {
        val title = "Heading"

        sut.feedList = listOf(FeedsListAdapter.HeadingViewModel(title))
        val headingViewHolder = sut.createViewHolder(LinearLayout(context), FeedsListAdapter.FeedTypes.HEADING_VIEW_HOLDER.ordinal)
        val viewModelPosition = 0

        sut.onBindViewHolder(headingViewHolder, viewModelPosition)
        assertEquals(title, findTextViewById(headingViewHolder, R.id.launch_list_header_title).text)
    }

    @Test
    fun onBindViewHolderPopularHotelsTonight() {
        val firstLine = "first line"
        val secondLine = "second line"

        sut.feedList = listOf(FeedsListAdapter.PopularHotelsTonightViewModel(firstLine, secondLine, "", ""))
        val popularHotelsTonightHolder = sut.createViewHolder(LinearLayout(context), FeedsListAdapter.FeedTypes.POPULAR_HOTELSTONIGHT_VIEW_HOLDER.ordinal)
        val viewModelPosition = 0

        sut.onBindViewHolder(popularHotelsTonightHolder, viewModelPosition)
        assertEquals(firstLine, findTextViewById(popularHotelsTonightHolder, R.id.first_line).text)
        assertEquals(secondLine, findTextViewById(popularHotelsTonightHolder, R.id.second_line).text)
    }

    private fun givenFeedListWithMockData() {
        val testViewModels = listOf(FeedsListAdapter.PopularHotelsTonightViewModel("", "", "", ""),
                FeedsListAdapter.GenericPlaceholderViewModel("line 1", "line 2", "", ""),
                SignInPlaceHolderViewModel("", "", "", ""),
                FeedsListAdapter.HeadingViewModel(""),
                FeedsListAdapter.FlightSearchViewModel("", "", false, "", "", "", ""),
                CollectionLocation())

        sut.feedList = testViewModels
    }

    private fun findTextViewById(viewHolder: RecyclerView.ViewHolder, textViewId: Int) = ButterKnife.findById<TextView>(viewHolder.itemView, textViewId)
}
