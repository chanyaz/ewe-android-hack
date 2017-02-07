package com.expedia.bookings.widget.feeds

import android.animation.Animator
import android.content.Context
import android.graphics.Color
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnticipateInterpolator
import android.widget.LinearLayout
import butterknife.ButterKnife
import com.expedia.bookings.R
import com.expedia.bookings.data.collections.Collection
import com.expedia.bookings.data.collections.CollectionLocation
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.utils.Images
import com.expedia.bookings.utils.NavUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.CollectionViewHolder
import com.expedia.bookings.widget.FrameLayout
import com.expedia.bookings.widget.OptimizedImageView
import com.expedia.bookings.widget.TextView
import com.expedia.vm.GenericViewModel
import com.expedia.vm.SignInPlaceHolderViewModel

class FeedsListAdapter(val context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    lateinit private var parentView: ViewGroup

    var feedList = emptyList<Any>()
    
    enum class FeedTypes {
        FLIGHT_SEARCH_HOLDER,
        COLLECTION_VIEW_HOLDER,
        HEADING_VIEW_HOLDER,
        SIGNIN_PLACEHOLDER_VIEW_HOLDER,
        GENERIC_PLACEHOLDER_VIEW_HOLDER,
        POPULAR_HOTELSTONIGHT_VIEW_HOLDER,
        INTRODUCING_SCRATCHPAD_VIEW_HOLDER
    }

    fun showStaffPicks(collection: Collection) {
        val headingView = HeadingViewModel(collection.title)
        val staffPicks = mutableListOf(makeSignInPlaceholderViewModel(), headingView)
        staffPicks.addAll(collection.locations)
        feedList = staffPicks
        notifyDataSetChanged()
    }

    fun showFeeds() {
        val mockFeedsList =
                listOf(IntroducingScratchpadViewModel("Introducing Scratchpad", "Save your searches for faster booking string TBD", "", "OK, I GOT IT"),
                        PopularHotelsTonightViewModel("Popular Hotels Tonight", "Hotels nearby from $199", "SHOP NOW", ""),
                        FeedsListAdapter.HeadingViewModel("Flights"),
                        FeedsListAdapter.FlightSearchViewModel("SFO", "AUS", true, "2", "Mar 22 - Mar 23", "$42", "2m"),
                        FeedsListAdapter.FlightSearchViewModel("SFO", "AUS", true, "2", "Mar 22 - Mar 23", "$42", "2m"),
                        FeedsListAdapter.FlightSearchViewModel("SFO", "AUS", true, "2", "Mar 22 - Mar 23", "$42", "2m"),
                        FeedsListAdapter.FlightSearchViewModel("SFO", "AUS", true, "2", "Mar 22 - Mar 23", "$42", "2m"))

        feedList = mockFeedsList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when(holder.itemViewType) {

            FeedTypes.HEADING_VIEW_HOLDER.ordinal -> {
                val viewModel = feedList[position] as HeadingViewModel
                val titleTextView = getHeadingTitleTextView(holder.itemView)
                titleTextView.text = viewModel.title
            }

            FeedTypes.FLIGHT_SEARCH_HOLDER.ordinal -> {
                val flightSearchHolder = holder as FlightSearchCardHolder
                val viewModel = feedList[position] as FlightSearchViewModel

                flightSearchHolder.bind(viewModel)
            }

            FeedTypes.COLLECTION_VIEW_HOLDER.ordinal -> {
                val collectionViewHolder = holder as CollectionViewHolder
                val viewModel = feedList[position] as CollectionLocation
                val fullWidthTile = true
                val showBrowseHotelsLabel = true

                // set background image
                val url = Images.getCollectionImageUrl(viewModel, parentView.width/2)
                val drawable = Images.makeCollectionBitmapDrawable(context, holder, url, FeedsListWidget.PICASSO_TAG)
                collectionViewHolder.backgroundImage.setImageDrawable(drawable)
                collectionViewHolder.collectionUrl = url
                collectionViewHolder.bindListData(viewModel, fullWidthTile, showBrowseHotelsLabel)
            }

            FeedTypes.SIGNIN_PLACEHOLDER_VIEW_HOLDER.ordinal -> {
                val viewModel = feedList[position] as GenericViewModel
                (holder as AbstractGenericPlaceholderCard).bind(viewModel)
            }

            FeedTypes.GENERIC_PLACEHOLDER_VIEW_HOLDER.ordinal -> {
                val viewModel = feedList[position] as GenericViewModel
                (holder as AbstractGenericPlaceholderCard).bind(viewModel)
            }

            FeedTypes.POPULAR_HOTELSTONIGHT_VIEW_HOLDER.ordinal -> {
                val viewModel = feedList[position] as GenericViewModel
                (holder as PopularHotelsTonightCard).bind(viewModel)
            }

            FeedTypes.INTRODUCING_SCRATCHPAD_VIEW_HOLDER.ordinal -> {
                val viewModel = feedList[position] as IntroducingScratchpadViewModel
                val introducingScratchpadCard = holder as IntroducingScratchpadCard
                introducingScratchpadCard.bind(viewModel)
                introducingScratchpadCard.addDismissClickListener(object: IntroducingScratchpadCard.DismissClickListener {
                    override fun gotTouch(card: IntroducingScratchpadCard) {
                        // move list up
                        removeItemFromFeedList(viewModel) // TODO why can't we just provide lambda here? Interface is just 1 function
                    }
                })
            }
        }
    }

    private fun removeItemFromFeedList(item: Any) {
        val list = feedList.toMutableList()
        list.remove(item)
        feedList = list.toList()
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        val itemType = feedList[position].javaClass

        return when(itemType) {
            FlightSearchViewModel::class.java -> FeedTypes.FLIGHT_SEARCH_HOLDER.ordinal

            CollectionLocation::class.java -> FeedTypes.COLLECTION_VIEW_HOLDER.ordinal

            HeadingViewModel::class.java -> FeedTypes.HEADING_VIEW_HOLDER.ordinal

            SignInPlaceHolderViewModel::class.java -> FeedTypes.SIGNIN_PLACEHOLDER_VIEW_HOLDER.ordinal

            GenericPlaceholderViewModel::class.java -> FeedTypes.GENERIC_PLACEHOLDER_VIEW_HOLDER.ordinal

            PopularHotelsTonightViewModel::class.java -> FeedTypes.POPULAR_HOTELSTONIGHT_VIEW_HOLDER.ordinal

            IntroducingScratchpadViewModel::class.java -> FeedTypes.INTRODUCING_SCRATCHPAD_VIEW_HOLDER.ordinal

            else -> -1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        parentView = parent

        return when (viewType) {
            FeedTypes.FLIGHT_SEARCH_HOLDER.ordinal -> {
                val view = LayoutInflater.from(context).inflate(R.layout.feeds_flight_search_card, parent, false)
                FlightSearchCardHolder(view)
            }

            FeedTypes.COLLECTION_VIEW_HOLDER.ordinal -> {
                val view = LayoutInflater.from(context).inflate(R.layout.section_collection_list_card, parent, false)
                val collectionViewHolder = CollectionViewHolder(view)
                collectionViewHolder
            }

            FeedTypes.HEADING_VIEW_HOLDER.ordinal -> {
                val view = LayoutInflater.from(context).inflate(R.layout.launch_header_root, parent, false)
                val viewParentLayout = view.findViewById(R.id.parent_layout) as FrameLayout

                // TODO - to clean this up we should just move snippet_launch_list_header into the parent_layout container (why add it at runtime??)
                val headingView = LayoutInflater.from(context).inflate(R.layout.snippet_launch_list_header, null)
                val launchListTitle = getHeadingTitleTextView(headingView)
                FontCache.setTypeface(launchListTitle, FontCache.Font.ROBOTO_MEDIUM)

                viewParentLayout.addView(headingView)
                object : RecyclerView.ViewHolder(view) {}
            }

            FeedTypes.SIGNIN_PLACEHOLDER_VIEW_HOLDER.ordinal -> {
                val view = LayoutInflater.from(context).inflate(R.layout.feeds_prompt_card, parent, false)
                SignInPlaceholderCard(view, context)
            }

            FeedTypes.GENERIC_PLACEHOLDER_VIEW_HOLDER.ordinal -> {
                val view = LayoutInflater.from(context).inflate(R.layout.feeds_prompt_card, parent, false)
                object : AbstractGenericPlaceholderCard(view, context, R.drawable.plus_pattern) {}
            }

            FeedTypes.POPULAR_HOTELSTONIGHT_VIEW_HOLDER.ordinal -> {
                val view = LayoutInflater.from(context).inflate(R.layout.feeds_popular_hotels_tonight_card, parent, false)
                PopularHotelsTonightCard(view, context)
            }

            FeedTypes.INTRODUCING_SCRATCHPAD_VIEW_HOLDER.ordinal -> {
                val view = LayoutInflater.from(context).inflate(R.layout.feeds_prompt_card, parent, false)
                IntroducingScratchpadCard(view, context)
            }

            else -> null
        }
    }

    override fun getItemCount(): Int {
        return feedList.count()
    }

    private fun getHeadingTitleTextView(headingView: View): TextView {
        return ButterKnife.findById<TextView>(headingView, R.id.launch_list_header_title)
    }

    private fun makeSignInPlaceholderViewModel(): SignInPlaceHolderViewModel {
        return SignInPlaceHolderViewModel("Shop and earn 3x points!", "Book on the app and earn triple points.", "SIGN IN", "CREATE ACCOUNT")
    }

    data class FlightSearchViewModel(val origin: String, val destination: String, val isReturn: Boolean, val travelersLabel: String,
                                     val departureReturnDate: String, val currentPrice: String, val lastUpdated: String)
    class FlightSearchCardHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val origin: TextView by bindView(R.id.origin)
        val destination: TextView by bindView(R.id.destination)
        val numberTravelers: TextView by bindView(R.id.number_travelers)
        val departureAndReturnDates: TextView by bindView(R.id.departure_and_return_dates)
        val price: TextView by bindView(R.id.price)
        val freshnessTimeTextView: TextView by bindView(R.id.freshness_time)
        val backgroundImage: OptimizedImageView by bindView(R.id.background_image)

        fun bind(vm: FlightSearchViewModel) {
            origin.text = vm.origin
            destination.text = vm.destination
            numberTravelers.text = vm.travelersLabel
            departureAndReturnDates.text = vm.departureReturnDate
            price.text = vm.currentPrice
            freshnessTimeTextView.text = vm.lastUpdated
        }
    }


    class SignInPlaceholderCard(itemView: View, context: Context): AbstractGenericPlaceholderCard(itemView, context, R.drawable.plus_pattern) {
        init {
            itemView.setOnClickListener { NavUtils.goToSignIn(context) }
        }
    }

    class PopularHotelsTonightCard(itemView: View, context: Context): RecyclerView.ViewHolder(itemView) {
        val backgroundImage: OptimizedImageView by bindView(R.id.background_image)
        val firstLineTextView: TextView by bindView(R.id.first_line)
        val secondLineTextView: TextView by bindView(R.id.second_line)

        init {
            val animOptions = AnimUtils.createActivityScaleBundle(itemView)
            itemView.setOnClickListener { NavUtils.goToHotels(context, animOptions) }
        }

        fun bind(vm: GenericViewModel) {
            firstLineTextView.text = vm.firstLine
            secondLineTextView.text = vm.secondLine
        }
    }

    class IntroducingScratchpadCard(itemView: View, context: Context): AbstractGenericPlaceholderCard(itemView, context, R.drawable.scratchpad_intro_placeholder) {
        lateinit private var dismissClickListener: DismissClickListener

        init {
            itemView.setOnClickListener {
                val listener = object: Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator) {}
                    override fun onAnimationStart(animation: Animator) {}
                    override fun onAnimationEnd(animation: Animator) { dismissClickListener.gotTouch(this@IntroducingScratchpadCard) }
                    override fun onAnimationCancel(animation: Animator) { dismissClickListener.gotTouch(this@IntroducingScratchpadCard) }
                }
                itemView.animate()
                        .setDuration(1000)
                        .alpha(0f)
                        .setInterpolator(AnticipateInterpolator())
                        .setListener(listener)
                        .start()
            }
        }

        fun addDismissClickListener(listener: DismissClickListener) {
            dismissClickListener = listener
        }

        interface DismissClickListener {
            fun gotTouch(card: IntroducingScratchpadCard)
        }
    }

    abstract class AbstractGenericPlaceholderCard(itemView: View, val context: Context, backgroundDrawable: Int): RecyclerView.ViewHolder(itemView) {
        val topHalf: LinearLayout by bindView(R.id.top_half)
        val firstLineTextView: TextView by bindView(R.id.first_line)
        val secondLineTextView: TextView by bindView(R.id.second_line)
        val button_one: TextView by bindView(R.id.button_one)
        val button_two: TextView by bindView(R.id.button_two)

        init {
            setupBackground(backgroundDrawable)
            setupFonts()
        }

        fun bind(vm: GenericViewModel) {
            firstLineTextView.text = vm.firstLine
            secondLineTextView.text = vm.secondLine
            button_one.text = vm.buttonOneLabel
            button_one.visibility = if (vm.buttonOneLabel.isNotBlank()) View.VISIBLE else View.GONE
            button_two.text = vm.buttonTwoLabel
            button_two.visibility = if (vm.buttonTwoLabel.isNotBlank()) View.VISIBLE else View.GONE
        }

        private fun setupFonts() {
            FontCache.setTypeface(firstLineTextView, FontCache.Font.ROBOTO_MEDIUM)
            FontCache.setTypeface(secondLineTextView, FontCache.Font.ROBOTO_MEDIUM)
            FontCache.setTypeface(button_one, FontCache.Font.ROBOTO_MEDIUM)
            FontCache.setTypeface(button_two, FontCache.Font.ROBOTO_MEDIUM)
        }

        private fun setupBackground(backgroundDrawable: Int) {
            val colorBackground = ShapeDrawable()
            colorBackground.paint.color = Color.parseColor("#4CA0FF")
            val patternTopLayer = ContextCompat.getDrawable(context, backgroundDrawable) as BitmapDrawable
            patternTopLayer.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT) // TODO - may not need this
            patternTopLayer.alpha = 30
            topHalf.background = LayerDrawable(arrayOf(colorBackground, patternTopLayer))
        }
    }

    data class HeadingViewModel(val title: String)
    data class GenericPlaceholderViewModel(override val firstLine: String, override val secondLine: String, override val buttonOneLabel: String, override val buttonTwoLabel: String): GenericViewModel
    data class PopularHotelsTonightViewModel(override val firstLine: String, override val secondLine: String, override val buttonOneLabel: String, override val buttonTwoLabel: String): GenericViewModel
    data class IntroducingScratchpadViewModel(override val firstLine: String, override val secondLine: String, override val buttonOneLabel: String, override val buttonTwoLabel: String): GenericViewModel
}
