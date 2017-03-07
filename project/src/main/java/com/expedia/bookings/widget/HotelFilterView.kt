package com.expedia.bookings.widget

import android.content.Context
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.HotelFavoriteHelper
import com.expedia.bookings.data.hotel.Sort
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.extension.shouldShowCircleForRatings
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.accessibility.AccessibleEditText
import com.expedia.bookings.widget.animation.ResizeHeightAnimator
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeOnClick
import com.expedia.util.subscribeVisibility
import com.expedia.vm.ShopWithPointsViewModel
import com.expedia.vm.hotel.BaseHotelFilterViewModel
import com.squareup.phrase.Phrase
import rx.Observer

class HotelFilterView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    val toolbar: Toolbar by bindView(R.id.filter_toolbar)
    val filterHotelVip: CheckBox by bindView(R.id.filter_hotel_vip)
    val filterHotelFavorite: CheckBox by bindView(R.id.filter_hotel_favorite)
    val filterVipContainer: View by bindView(R.id.filter_vip_container)
    val optionLabel: TextView by bindView(R.id.option_label)
    val filterFavoriteContainer: View by bindView(R.id.filter_favorite_container)
    val filterStarOne: ImageButton by bindView(R.id.filter_hotel_star_rating_one)
    val filterStarTwo: ImageButton by bindView(R.id.filter_hotel_star_rating_two)
    val filterStarThree: ImageButton by bindView(R.id.filter_hotel_star_rating_three)
    val filterStarFour: ImageButton by bindView(R.id.filter_hotel_star_rating_four)
    val filterStarFive: ImageButton by bindView(R.id.filter_hotel_star_rating_five)
    val ratingOneBackground: View by bindView(R.id.rating_one_background)
    val ratingTwoBackground: View by bindView(R.id.rating_two_background)
    val ratingThreeBackground: View by bindView(R.id.rating_three_background)
    val ratingFourBackground: View by bindView(R.id.rating_four_background)
    val ratingFiveBackground: View by bindView(R.id.rating_five_background)
    val sortContainer: LinearLayout by bindView(R.id.sort_hotel)
    val sortByButtonGroup: Spinner by bindView(R.id.sort_by_selection_spinner)
    val filterHotelName: AccessibleEditText by bindView(R.id.filter_hotel_name_edit_text)
    val clearNameButton: ImageView by bindView(R.id.clear_search_button)
    val dynamicFeedbackWidget: DynamicFeedbackWidget by bindView(R.id.dynamic_feedback_container)
    val dynamicFeedbackClearButton: TextView by bindView(R.id.dynamic_feedback_clear_button)
    val filterContainer: ViewGroup by bindView(R.id.filter_container)

    val doneButton: Button by lazy {
        val button = LayoutInflater.from(context).inflate(R.layout.toolbar_checkmark_item, null) as Button
        button.setTextColor(ContextCompat.getColor(context, R.color.cars_actionbar_text_color))
        button.setText(R.string.done)

        val icon = ContextCompat.getDrawable(context, R.drawable.ic_check_white_24dp).mutate()
        icon.setColorFilter(ContextCompat.getColor(context, R.color.cars_actionbar_text_color), PorterDuff.Mode.SRC_IN)
        button.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null)

        button
    }
    val toolbarDropshadow: View by bindView(R.id.toolbar_dropshadow)
    val priceRangeBar: FilterRangeSeekBar by bindView(R.id.price_range_bar)
    val priceRangeContainer: View by bindView(R.id.price_range_container)
    val priceHeader: View by bindView(R.id.price)
    val priceRangeMinText: TextView by bindView(R.id.price_range_min_text)
    val priceRangeMaxText: TextView by bindView(R.id.price_range_max_text)
    val neighborhoodLabel: TextView by bindView(R.id.neighborhood_label)
    val neighborhoodContainer: LinearLayout by bindView(R.id.neighborhoods)
    val neighborhoodMoreLessLabel: TextView by bindView(R.id.show_more_less_text)
    val neighborhoodMoreLessIcon: ImageButton by bindView(R.id.show_more_less_icon)
    val neighborhoodMoreLessView: RelativeLayout by bindView(R.id.collapsed_container)

    val priceRangeStub: ViewStub by bindView(R.id.price_range_stub)
    val a11yPriceRangeStub: ViewStub by bindView(R.id.a11y_price_range_stub)
    val a11yPriceRangeStartSeekBar: FilterSeekBar by bindView(R.id.price_a11y_start_bar)
    val a11yPriceRangeStartText: TextView by bindView(R.id.price_a11y_start_text)
    val a11yPriceRangeEndSeekBar: FilterSeekBar by bindView(R.id.price_a11y_end_bar)
    val a11yPriceRangeEndText: TextView by bindView(R.id.price_a11y_end_text)

    val rowHeight = resources.getDimensionPixelSize(R.dimen.hotel_neighborhood_height)
    val ANIMATION_DURATION = 500L

    val sortByAdapter = object : ArrayAdapter<Sort>(getContext(), R.layout.spinner_sort_dropdown_item) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val textView: TextView = super.getView(position, convertView, parent) as TextView
            textView.text = resources.getString(getItem(position).resId)
            return textView
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
            return getView(position, convertView, parent)
        }
    }

    var shopWithPointsViewModel: ShopWithPointsViewModel? = null

    val sortByObserver: Observer<Boolean> = endlessObserver { isCurrentLocationSearch ->
        sortByAdapter.clear()
        val sortList = Sort.values().toMutableList()
        sortList.remove(viewModel.sortItemToRemove())
        sortByAdapter.addAll(sortList)

        if (!isCurrentLocationSearch) {
            sortByAdapter.remove(sortList.first { it == Sort.DISTANCE })
        }

        // Remove Sort by Deals in case of SWP.
        if (shopWithPointsViewModel?.swpEffectiveAvailability?.value ?: false) {
            sortByAdapter.remove(sortList.first { it == Sort.DEALS })
        }

        sortByAdapter.notifyDataSetChanged()
        sortByButtonGroup.setSelection(0, false)
    }

    var viewModel: BaseHotelFilterViewModel by notNullAndObservable { vm ->

        doneButton.subscribeOnClick(vm.doneObservable)
        vm.priceRangeContainerObservable.subscribeVisibility(priceRangeContainer)
        vm.priceRangeContainerObservable.subscribeVisibility(priceHeader)
        filterVipContainer.setOnClickListener {
            clearHotelNameFocus()
            filterHotelVip.isChecked = !filterHotelVip.isChecked
            vm.vipFilteredObserver.onNext(filterHotelVip.isChecked)
        }

        if (HotelFavoriteHelper.showHotelFavoriteTest(viewModel.showHotelFavorite())) {
            filterFavoriteContainer.setOnClickListener {
                clearHotelNameFocus()
                updateFavoriteFilter()
                HotelTracking.trackHotelFilterFavoriteClicked(filterHotelFavorite.isChecked)
            }
        }

        filterStarOne.subscribeOnClick(vm.oneStarFilterObserver)
        filterStarTwo.subscribeOnClick(vm.twoStarFilterObserver)
        filterStarThree.subscribeOnClick(vm.threeStarFilterObserver)
        filterStarFour.subscribeOnClick(vm.fourStarFilterObserver)
        filterStarFive.subscribeOnClick(vm.fiveStarFilterObserver)

        dynamicFeedbackClearButton.setOnClickListener {
            dynamicFeedbackClearButton.announceForAccessibility(context.getString(R.string.filters_cleared))
            vm.clearObservable.onNext(Unit)
            vm.trackClearFilter()
        }

        vm.finishClear.subscribe {
            //check if filterHotelName is empty to avoid calling handleFiltering
            if (filterHotelName.text.length > 0) filterHotelName.text.clear()
            resetStars()

            filterHotelVip.isChecked = false
            if (HotelFavoriteHelper.showHotelFavoriteTest(viewModel.showHotelFavorite())) {
                filterHotelFavorite.isChecked = false
            }

            for (i in 0..neighborhoodContainer.childCount - 1) {
                val v = neighborhoodContainer.getChildAt(i)
                if (v is HotelsNeighborhoodFilter) {
                    v.neighborhoodCheckBox.isChecked = false
                }
            }

            dynamicFeedbackWidget.hideDynamicFeedback()
            neighborhoodMoreLessView.visibility = View.GONE
        }

        var priceStartCurrentProgress: Int
        var priceEndCurrentProgress: Int

        vm.newPriceRangeObservable.subscribe { priceRange ->
            if (AccessibilityUtil.isTalkBackEnabled(context)) {
                priceStartCurrentProgress = priceRange.notches
                priceEndCurrentProgress = priceRange.notches
                a11yPriceRangeStartSeekBar.upperLimit = priceRange.notches
                a11yPriceRangeEndSeekBar.upperLimit = priceRange.notches
                a11yPriceRangeStartText.text = priceRange.defaultMinPriceText
                a11yPriceRangeEndText.text = priceRange.defaultMaxPriceText
                a11yPriceRangeStartSeekBar.a11yName = context.getString(R.string.hotel_price_range_start)
                a11yPriceRangeEndSeekBar.a11yName = context.getString(R.string.hotel_price_range_end)
                a11yPriceRangeStartSeekBar.currentA11yValue = a11yPriceRangeStartText.text.toString()
                a11yPriceRangeEndSeekBar.currentA11yValue = a11yPriceRangeEndText.text.toString()

                a11yPriceRangeStartSeekBar.setOnSeekBarChangeListener { seekBar, progress, fromUser ->
                    priceStartCurrentProgress = priceRange.notches - progress
                    if (priceStartCurrentProgress < priceEndCurrentProgress) {
                        a11yPriceRangeStartText.text = priceRange.formatValue(priceRange.notches - progress)
                        a11yPriceRangeStartSeekBar.currentA11yValue = a11yPriceRangeStartText.text.toString()
                        announceForAccessibility(a11yPriceRangeStartSeekBar.currentA11yValue)
                        vm.priceRangeChangedObserver.onNext(priceRange.getUpdatedPriceRange(priceRange.notches - progress, priceEndCurrentProgress))
                    }
                }

                a11yPriceRangeEndSeekBar.setOnSeekBarChangeListener { seekBar, progress, fromUser ->
                    priceEndCurrentProgress = progress
                    if (priceEndCurrentProgress > priceStartCurrentProgress) {
                        a11yPriceRangeEndText.text = priceRange.formatValue(progress)
                        a11yPriceRangeEndSeekBar.currentA11yValue = a11yPriceRangeEndText.text.toString()
                        announceForAccessibility(a11yPriceRangeEndSeekBar.currentA11yValue)
                        vm.priceRangeChangedObserver.onNext(priceRange.getUpdatedPriceRange(priceStartCurrentProgress, progress))
                    }
                }
            } else {
                priceRangeBar.upperLimit = priceRange.notches
                priceRangeMinText.text = priceRange.defaultMinPriceText
                priceRangeMaxText.text = priceRange.defaultMaxPriceText

                priceRangeBar.setOnRangeSeekBarChangeListener(object : FilterRangeSeekBar.OnRangeSeekBarChangeListener {
                    override fun onRangeSeekBarDragChanged(bar: FilterRangeSeekBar?, minValue: Int, maxValue: Int) {
                        priceRangeMinText.text = priceRange.formatValue(minValue)
                        priceRangeMaxText.text = priceRange.formatValue(maxValue)
                    }

                    override fun onRangeSeekBarValuesChanged(bar: FilterRangeSeekBar?, minValue: Int, maxValue: Int) {
                        priceRangeMinText.text = priceRange.formatValue(minValue)
                        priceRangeMaxText.text = priceRange.formatValue(maxValue)
                        vm.priceRangeChangedObserver.onNext(priceRange.getUpdatedPriceRange(minValue, maxValue))
                    }
                })
            }
        }

        vm.hotelStarRatingBar.subscribe { rating ->
            if (rating == 1) {
                starSelection(filterStarOne, ratingOneBackground, -1, true)
            } else if (rating == 6) {
                starSelection(filterStarOne, ratingOneBackground, 1)
            }
            if (rating == 2) {
                starSelection(filterStarTwo, ratingTwoBackground, -1, true)
            } else if (rating == 7) {
                starSelection(filterStarTwo, ratingTwoBackground, 2)
            }
            if (rating == 3) {
                starSelection(filterStarThree, ratingThreeBackground, -1, true)
            } else if (rating == 8) {
                starSelection(filterStarThree, ratingThreeBackground, 3)
            }
            if (rating == 4) {
                starSelection(filterStarFour, ratingFourBackground, -1, true)
            } else if (rating == 9) {
                starSelection(filterStarFour, ratingFourBackground, 4)
            }
            if (rating == 5) {
                starSelection(filterStarFive, ratingFiveBackground, -1, true)
            } else if (rating == 10) {
                starSelection(filterStarFive, ratingFiveBackground, 5)
            }

            if (rating <= 5) {
                vm.trackHotelRefineRating(rating.toString())
            }
        }

        vm.doneButtonEnableObservable.subscribe { enable ->
            doneButton.alpha = (if (enable) 1.0f else (0.15f))
            doneButton.isEnabled = enable
        }

        vm.updateDynamicFeedbackWidget.subscribe {
            if (it < 0) {
                dynamicFeedbackWidget.hideDynamicFeedback()
            } else {
                dynamicFeedbackWidget.showDynamicFeedback()
                dynamicFeedbackWidget.setDynamicCounterText(it)
            }
        }

        vm.filteredZeroResultObservable.subscribe {
            dynamicFeedbackWidget.animateDynamicFeedbackWidget()
        }

        filterHotelName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                clearNameButton.visibility = if (Strings.isEmpty(s)) View.GONE else View.VISIBLE
                vm.filterHotelNameObserver.onNext(s)
            }
        })

        filterHotelName.setOnFocusChangeListener { view, isFocus ->
            if (!isFocus) {
                com.mobiata.android.util.Ui.hideKeyboard(this)
            }
        }

        clearNameButton.setOnClickListener { view ->
            filterHotelName.text = null
        }

        vm.sortSpinnerObservable.subscribe { sortType ->
            val position = sortByAdapter.getPosition(sortType)
            sortByButtonGroup.setSelection(position, false)
        }

        sortByButtonGroup.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                vm.userFilterChoices.userSort = sortByAdapter.getItem(position)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        sortByButtonGroup.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                clearHotelNameFocus()
            }
            false
        }

        vm.neighborhoodListObservable.subscribe { list ->
            neighborhoodContainer.removeAllViews()
            if (list != null && list.size > 1 && vm.isClientSideFiltering()) {
                neighborhoodLabel.visibility = View.VISIBLE
                if (list.size > 4) {
                    neighborhoodMoreLessView.visibility = View.VISIBLE
                }

                for (i in 1..list.size - 1) {
                    val neighborhoodView = LayoutInflater.from(getContext()).inflate(R.layout.section_hotel_neighborhood_row, neighborhoodContainer, false) as HotelsNeighborhoodFilter
                    neighborhoodView.bind(list[i], vm)
                    neighborhoodView.subscribeOnClick(neighborhoodView.checkObserver)
                    neighborhoodContainer.addView(neighborhoodView)
                }

                setupNeighborhoodView()
            } else {
                neighborhoodLabel.visibility = View.GONE
                neighborhoodMoreLessView.visibility = View.GONE
            }
        }

        neighborhoodMoreLessView.subscribeOnClick(vm.neighborhoodMoreLessObservable)

        vm.sortContainerVisibilityObservable.subscribeVisibility(sortContainer)

        vm.neighborhoodExpandObservable.subscribe { isSectionExpanded ->
            if (isSectionExpanded) {
                AnimUtils.rotate(neighborhoodMoreLessIcon)
                neighborhoodMoreLessLabel.text = resources.getString(R.string.show_less)
                neighborhoodMoreLessView.contentDescription = resources.getString(R.string.hotels_filter_show_less_cont_desc)

                for (i in 3..neighborhoodContainer.childCount - 1) {
                    val v = neighborhoodContainer.getChildAt(i)
                    if (v is HotelsNeighborhoodFilter) {
                        v.visibility = View.VISIBLE
                    }
                }

                val resizeAnimator = ResizeHeightAnimator(ANIMATION_DURATION)
                resizeAnimator.addViewSpec(neighborhoodContainer, rowHeight * neighborhoodContainer.childCount)
                resizeAnimator.start()

            } else {
                setupNeighborhoodView()
            }
        }

        //TODO server side filters WIP - this is temporary, will be added back soon
        vm.clientSideFilterObservable.subscribe { clientFilter ->
            if (!clientFilter) {
                optionLabel.visibility = View.GONE
                filterVipContainer.visibility = View.GONE
            }
        }
    }

    private fun updateFilterStarContentDesc(starButton: ImageButton, contDescStringID: Int, isSelected: Boolean = false) {
        starButton.contentDescription = Phrase.from(context,
                if (isSelected) R.string.star_rating_selected_cont_desc_TEMPLATE else R.string.star_rating_not_selected_cont_desc_TEMPLATE)
                .put("star_rating", context.getString(contDescStringID))
                .format().toString()
    }

    private fun setupNeighborhoodView() {
        AnimUtils.reverseRotate(neighborhoodMoreLessIcon)
        neighborhoodMoreLessLabel.text = resources.getString(R.string.show_more)
        neighborhoodMoreLessView.contentDescription = resources.getString(R.string.hotels_filter_show_more_cont_desc)

        val resizeAnimator = ResizeHeightAnimator(ANIMATION_DURATION)
        resizeAnimator.addViewSpec(neighborhoodContainer, rowHeight * 3)
        resizeAnimator.start()

        for (i in 3..neighborhoodContainer.childCount - 1) {
            val v = neighborhoodContainer.getChildAt(i)
            if (v is HotelsNeighborhoodFilter) {
                v.visibility = View.GONE
            }
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (AccessibilityUtil.isTalkBackEnabled(context)) {
            a11yPriceRangeStub.inflate()
        } else {
            priceRangeStub.inflate()
        }
    }

    init {
        View.inflate(getContext(), R.layout.widget_hotel_filter, this)

        if (PointOfSale.getPointOfSale().supportsVipAccess()) {
            filterVipContainer.visibility = View.VISIBLE
            optionLabel.visibility = View.VISIBLE
        }

        if (shouldShowCircleForRatings()) {
            setCircleDrawableForRatingBtnBackground()
        }

        dynamicFeedbackWidget.hideDynamicFeedback()

        val statusBarHeight = Ui.getStatusBarHeight(getContext())
        if (statusBarHeight > 0) {
            val color = ContextCompat.getColor(context, Ui.obtainThemeResID(context, R.attr.primary_color))
            val statusBar = Ui.setUpStatusBar(context, toolbar, filterContainer, color)
            addView(statusBar)
        }

        toolbar.inflateMenu(R.menu.cars_lx_filter_menu)
        toolbar.title = resources.getString(R.string.sort_and_filter)
        toolbar.setTitleTextAppearance(context, R.style.ToolbarTitleTextAppearance)
        toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.cars_actionbar_text_color))

        toolbar.menu.findItem(R.id.apply_check).actionView = doneButton

        filterContainer.viewTreeObserver.addOnScrollChangedListener {
            val scrollY = filterContainer.scrollY
            val ratio = (scrollY).toFloat() / 100
            toolbarDropshadow.alpha = ratio
        }

        sortByAdapter.setNotifyOnChange(false)
        sortByButtonGroup.adapter = sortByAdapter
        resetStars()
    }

    fun resetStars() {
        starSelection(filterStarOne, ratingOneBackground, 1)
        starSelection(filterStarTwo, ratingTwoBackground, 2)
        starSelection(filterStarThree, ratingThreeBackground, 3)
        starSelection(filterStarFour, ratingFourBackground, 4)
        starSelection(filterStarFive, ratingFiveBackground, 5)
    }

    fun starSelection(star: ImageButton, background: View, value: Int, isSelected: Boolean = false) {
        var contDesc: Int = 0
        var isStar: Boolean = true

        if (PointOfSale.getPointOfSale().shouldShowCircleForRatings()) {
            isStar = false
        }
        when (star.id) {
            R.id.filter_hotel_star_rating_one -> contDesc = if (isStar) R.string.star_rating_one_cont_desc else R.string.star_circle_rating_one_cont_desc
            R.id.filter_hotel_star_rating_two -> contDesc = if (isStar) R.string.star_rating_two_cont_desc else R.string.star_circle_rating_two_cont_desc
            R.id.filter_hotel_star_rating_three -> contDesc = if (isStar) R.string.star_rating_three_cont_desc else R.string.star_circle_rating_three_cont_desc
            R.id.filter_hotel_star_rating_four -> contDesc = if (isStar) R.string.star_rating_four_cont_desc else R.string.star_circle_rating_four_cont_desc
            R.id.filter_hotel_star_rating_five -> contDesc = if (isStar) R.string.star_rating_five_cont_desc else R.string.star_circle_rating_five_cont_desc
        }
        updateFilterStarContentDesc(star, contDesc, isSelected)

        clearHotelNameFocus()
        if (value < 0) {
            star.setColorFilter(ContextCompat.getColor(context, android.R.color.white))
            background.setBackgroundColor(ContextCompat.getColor(context, Ui.obtainThemeResID(context, R.attr.primary_color)))
        } else {
            star.setColorFilter(ContextCompat.getColor(context, Ui.obtainThemeResID(context, R.attr.primary_color)))
            background.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
        }
    }

    fun refreshFavoriteCheckbox() {
        if (HotelFavoriteHelper.showHotelFavoriteTest(viewModel.showHotelFavorite())) {
            if (HotelFavoriteHelper.getLocalFavorites().isNotEmpty()) {
                filterFavoriteContainer.visibility = View.VISIBLE
                optionLabel.text = context.resources.getString(R.string.filter_options)
                viewModel.favoriteFilteredObserver.onNext(filterHotelFavorite.isChecked)
            } else {
                if (viewModel.userFilterChoices.favorites) updateFavoriteFilter()
                filterFavoriteContainer.visibility = View.GONE
                optionLabel.text = context.resources.getString(R.string.vip)
            }
        }
    }

    private fun clearHotelNameFocus() {
        filterHotelName.clearFocus()
        com.mobiata.android.util.Ui.hideKeyboard(this)
    }

    private fun setCircleDrawableForRatingBtnBackground() {
        filterStarOne.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.btn_filter_rating_one_circle));
        filterStarTwo.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.btn_filter_rating_two_circle));
        filterStarThree.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.btn_filter_rating_three_circle));
        filterStarFour.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.btn_filter_rating_four_circle));
        filterStarFive.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.btn_filter_rating_five_circle));
    }

    private fun updateFavoriteFilter() {
        filterHotelFavorite.isChecked = !filterHotelFavorite.isChecked
        viewModel.favoriteFilteredObserver.onNext(filterHotelFavorite.isChecked)
    }
}
