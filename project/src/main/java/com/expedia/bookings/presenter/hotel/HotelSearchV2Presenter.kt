package com.expedia.bookings.presenter.hotel

import android.animation.ArgbEvaluator
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.text.Html
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import com.expedia.account.graphics.ArrowXDrawable
import com.expedia.bookings.R
import com.expedia.bookings.location.CurrentLocationObservable
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.SuggestionV4Utils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.HotelSuggestionAdapter
import com.expedia.bookings.widget.RecyclerDividerDecoration
import com.expedia.bookings.widget.SearchInputCardView
import com.expedia.bookings.widget.TravelerSearchV2CardView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.HotelSearchViewModel
import com.expedia.vm.HotelSuggestionAdapterViewModel

//TODO: Find this a better home
fun calculateStep(start: Float, end: Float, percent: Float, forward: Boolean): Float {
    if (forward) {
        return calculateStep(start, end, percent)
    } else {
        return calculateStep(end, start, percent)
    }
}

fun calculateStep(start: Float, end: Float, percent: Float): Float {
    return (start + (percent * (end - start)))
}

public class HotelSearchV2Presenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

    val toolbar: Toolbar by bindView(R.id.searchv2_toolbar)
    val searchContainer: ViewGroup by bindView(R.id.search_v2_container)
    val suggestionRecyclerView: RecyclerView by bindView(R.id.suggestion_list)
    var navIcon: ArrowXDrawable
    val destinationCardView: SearchInputCardView by bindView(R.id.destination_card)
    val calendarCardView: SearchInputCardView by bindView(R.id.calendar_card)
    val travelerCardView: TravelerSearchV2CardView by bindView(R.id.traveler_card)
    var searchLocationEditText: SearchView? = null

    var searchViewModel: HotelSearchViewModel by notNullAndObservable { vm ->
        travelerCardView.hotelSearchViewModelSubject.onNext(vm)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addTransition(selectionToSuggestionTransition)
        addDefaultTransition(defaultTransition)
        showDefault()
    }


    internal var listener: SearchView.OnQueryTextListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String): Boolean {
            return false
        }

        override fun onQueryTextChange(query: String): Boolean {
            suggestionViewModel.queryObserver.onNext(query)
            return false
        }
    }


    init {
        View.inflate(context, R.layout.widget_hotel_search_v2_params, this)
        toolbar.inflateMenu(R.menu.searchv2_destination_menu)
        val statusBarHeight = Ui.getStatusBarHeight(getContext())
        if (statusBarHeight > 0) {
            val color = ContextCompat.getColor(context, R.color.hotels_primary_color)
            val statusBar = Ui.setUpStatusBar(getContext(), toolbar, searchContainer, color)
            addView(statusBar)
        }

        val totalTopPadding = suggestionRecyclerView.paddingTop + statusBarHeight
        suggestionRecyclerView.setPadding(0, totalTopPadding, 0, 0)

        navIcon = ArrowXDrawableUtil.getNavigationIconDrawable(getContext(), ArrowXDrawableUtil.ArrowDrawableType.CLOSE)
        navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        toolbar.navigationIcon = navIcon
        toolbar.setNavigationOnClickListener {
            com.mobiata.android.util.Ui.hideKeyboard(this@HotelSearchV2Presenter)
            val activity = getContext() as AppCompatActivity
            activity.onBackPressed()
        }
        searchLocationEditText = toolbar.menu.findItem(R.id.action_search_view).actionView as (SearchView?)
        searchLocationEditText?.setOnQueryTextListener(listener);
        searchLocationEditText?.setIconifiedByDefault(false)
        searchLocationEditText?.visibility = View.GONE

        suggestionRecyclerView.layoutManager = LinearLayoutManager(context)
        suggestionRecyclerView.addItemDecoration(RecyclerDividerDecoration(getContext(), 0, 0, 0, 0, 0, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25f, resources.displayMetrics).toInt(), false))


        destinationCardView.setOnClickListener {
            show(SuggestionSelectionState())
        }

        calendarCardView.setOnClickListener {

        }

    }



    class InputSelectionState
    class SuggestionSelectionState

    public fun showDefault() {
        show(InputSelectionState(), Presenter.FLAG_CLEAR_BACKSTACK)
    }

    private val defaultTransition = object : Presenter.DefaultTransition(InputSelectionState::class.java.name) {

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
        }

        override fun finalizeTransition(forward: Boolean) {
            searchContainer.visibility = VISIBLE
        }
    }

    /**
     * This is a watered down version of a higher ideal where instead of calculating each step, we define the start and
     * end, and allow the transition to do the work for us. Right now it's just a tuple that stores start and end
     * for us.
     */
    data class TransitionElement<T>(val start: T, val end: T)

    private val selectionToSuggestionTransition = object : Presenter.Transition(InputSelectionState::class.java, SuggestionSelectionState::class.java, LinearInterpolator(), 750) {

        //Parts of the animation:
        // suggestion bg fade from alpha 0 to goal alpha
        // If toolbar:  navbarIcon animation
        //              toolbar bg color from default to white
        //              Toolbar font color from white to grey
        //
        val bgFade = TransitionElement(0f, 1f)
        // This is probably not even
        val eval: ArgbEvaluator = ArgbEvaluator()
        //TODO: use actual colors
        val toolbarBgColor = TransitionElement(ContextCompat.getColor(context, R.color.hotels_primary_color), Color.RED)

        override fun startTransition(forward: Boolean) {
            //Toolbar color
            //TODO: use actual colors
            toolbar.setBackgroundColor(if (forward) toolbarBgColor.start else toolbarBgColor.end)
            //Suggestion Fade In
            suggestionRecyclerView.alpha = calculateStep(bgFade.start, bgFade.end, 0f, forward)
            suggestionRecyclerView.visibility = if (forward) GONE else VISIBLE
            searchContainer.visibility = if (forward) VISIBLE else GONE
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            //TODO: Worry about asymmetric interpolators
            val progress = if (forward) f else 1f - f
            super.updateTransition(f, forward)
            toolbar.setBackgroundColor(eval.evaluate(progress, toolbarBgColor.start, toolbarBgColor.end) as Int)
            suggestionRecyclerView.alpha = calculateStep(bgFade.start, bgFade.end, progress)
        }

        override fun finalizeTransition(forward: Boolean) {
            // Toolbar bg color
            toolbar.setBackgroundColor(if (forward) toolbarBgColor.end else toolbarBgColor.start)
            suggestionRecyclerView.alpha = if (forward) bgFade.end else bgFade.start

            if(forward) {
                suggestionRecyclerView.adapter = hotelSuggestionAdapter
                searchLocationEditText?.requestFocus()
            }
            suggestionRecyclerView.visibility = if (forward) VISIBLE else GONE
            searchLocationEditText?.visibility = if (forward) VISIBLE else GONE
            searchContainer.visibility = if (forward) GONE else VISIBLE
            toolbar.title = if (forward) null else resources.getString(R.string.hotel_toolbar_title)
            setUpToolBarIcon(forward)
        }
    }

    private fun setUpToolBarIcon(forward: Boolean) {
        if(forward) {
            navIcon = ArrowXDrawableUtil.getNavigationIconDrawable(context, ArrowXDrawableUtil.ArrowDrawableType.BACK)
            navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
            toolbar.navigationIcon = navIcon
        } else {
            navIcon = ArrowXDrawableUtil.getNavigationIconDrawable(context, ArrowXDrawableUtil.ArrowDrawableType.CLOSE)
            navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
            toolbar.navigationIcon = navIcon
        }
    }

    private val hotelSuggestionAdapter by lazy {
        val service = Ui.getApplication(getContext()).hotelComponent().suggestionsService()
        suggestionViewModel = HotelSuggestionAdapterViewModel(getContext(), service, CurrentLocationObservable.create(getContext()), true, true)
        HotelSuggestionAdapter(suggestionViewModel)
    }

    public var suggestionViewModel: HotelSuggestionAdapterViewModel by notNullAndObservable { vm ->
        vm.suggestionSelectedSubject.subscribe { suggestion ->
            com.mobiata.android.util.Ui.hideKeyboard(this)
            searchViewModel.suggestionObserver.onNext(suggestion)
            val suggestionName = Html.fromHtml(suggestion.regionNames.displayName).toString()
            destinationCardView.setText(suggestionName)
            searchLocationEditText?.setQuery(suggestionName, false)
            SuggestionV4Utils.saveSuggestionHistory(context, suggestion, SuggestionV4Utils.RECENT_HOTEL_SUGGESTIONS_FILE)
            showDefault()
        }
    }


}
