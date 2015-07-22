package com.expedia.bookings.widget;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.LinearLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.lx.LXCategoryMetadata;
import com.expedia.bookings.data.lx.LXSortFilterMetadata;
import com.expedia.bookings.data.lx.LXSortType;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.CollectionUtils;
import com.expedia.bookings.utils.Ui;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class LXSortFilterWidget extends FrameLayout {

	public static final String DEEPLINK_FILTER_DILIMITER = "\\|";
	private Map<String, LXCategoryMetadata> selectedFilterCategories = new HashMap<>();
	private boolean isFilteredToZeroResults = false;

	public LXSortFilterWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@InjectView(R.id.sort_filter_done_button)
	Button doneButton;

	@InjectView(R.id.price_sort_button)
	Button priceSortButton;

	@InjectView(R.id.popularity_sort_button)
	Button popularitySortButton;

	@InjectView(R.id.filter_categories)
	LinearLayout filterCategoriesContainer;

	@InjectView(R.id.dynamic_feedback_container)
	DynamicFeedbackWidget dynamicFeedbackWidget;

	@OnClick(R.id.sort_filter_done_button)
	public void onDoneButtonClicked() {
		if (isFilteredToZeroResults) {
			dynamicFeedbackWidget.showDynamicFeedback();
			dynamicFeedbackWidget.animateDynamicFeedbackWidget();
		}
		else {
			Events.post(new Events.LXFilterDoneClicked());
		}
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);

		Drawable navIcon = getResources().getDrawable(R.drawable.ic_check_white_24dp).mutate();
		navIcon.setColorFilter(getResources().getColor(R.color.lx_actionbar_text_color), PorterDuff.Mode.SRC_IN);
		doneButton.setCompoundDrawablesWithIntrinsicBounds(navIcon, null, null, null);
		// Reset Popularity sort as default.
		popularitySortButton.setSelected(true);
		priceSortButton.setSelected(false);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		Events.register(this);
	}

	@Override
	protected void onDetachedFromWindow() {
		Events.unregister(this);
		super.onDetachedFromWindow();
	}

	@OnClick(R.id.price_sort_button)
	public void onPriceSortClicked() {
		popularitySortButton.setSelected(false);
		priceSortButton.setSelected(true);
		postLXFilterChangedEvent();
	}

	@OnClick(R.id.popularity_sort_button)
	public void onPopularitySortClicked() {
		popularitySortButton.setSelected(true);
		priceSortButton.setSelected(false);
		postLXFilterChangedEvent();
	}

	public void bind(Map<String, LXCategoryMetadata> filterCategories) {
		filterCategoriesContainer.removeAllViews();
		if (filterCategories != null) {
			for (Map.Entry<String, LXCategoryMetadata> filterCategory : filterCategories.entrySet()) {

				LXCategoryMetadata lxCategoryMetadata = filterCategory.getValue();
				String categoryKey = filterCategory.getKey();
				lxCategoryMetadata.checked = selectedFilterCategories.containsKey(categoryKey) ? true : false;
				LXFilterCategoryWidget categoryView = Ui
					.inflate(R.layout.section_lx_filter_row, filterCategoriesContainer, false);
				categoryView.bind(lxCategoryMetadata, categoryKey);
				filterCategoriesContainer.addView(categoryView);
			}
		}
		else {
			// Set to default state, as we have new search params available.
			selectedFilterCategories.clear();
			popularitySortButton.setSelected(true);
			priceSortButton.setSelected(false);
		}

		// Hide the dynamic feedback & update done button in case we have zero filters applied.
		if (selectedFilterCategories.size() == 0) {
			updateDoneButton();
			dynamicFeedbackWidget.hideDynamicFeedback();
		}
	}

	@Subscribe
	public void onCategoryCheckChanged(Events.LXFilterCategoryCheckedChanged event) {
		// Updating the category map.
		if (event.lxCategoryMetadata.checked) {
			selectedFilterCategories.put(event.categoryKey, event.lxCategoryMetadata);
		}
		else {
			selectedFilterCategories.remove(event.categoryKey);
		}
		postLXFilterChangedEvent();
	}

	public void setDeepLinkFilters(String filters) {
		selectedFilterCategories.clear();
		String[] filter = filters.split(DEEPLINK_FILTER_DILIMITER);
		for (String filterDisplayValue : filter) {
			LXCategoryMetadata lxCategoryMetadata = new LXCategoryMetadata();
			lxCategoryMetadata.checked = true;
			lxCategoryMetadata.displayValue = filterDisplayValue;
			selectedFilterCategories.put(filterDisplayValue, lxCategoryMetadata);
		}
	}

	private void postLXFilterChangedEvent() {
		LXSortFilterMetadata lxSortFilterMetadata = new LXSortFilterMetadata();
		lxSortFilterMetadata.lxCategoryMetadataMap = selectedFilterCategories;
		lxSortFilterMetadata.sort = priceSortButton.isSelected() ? LXSortType.PRICE : LXSortType.POPULARITY;
		Events.post(new Events.LXFilterChanged(lxSortFilterMetadata));
	}

	@Subscribe
	public void onLXSearchFilterResultsReady(Events.LXSearchFilterResultsReady event) {
		if (selectedFilterCategories.size() == 0) {
			isFilteredToZeroResults = false;
			updateDoneButton();
			dynamicFeedbackWidget.hideDynamicFeedback();
			bind(event.filterCategories);
			return;
		}
		else {
			dynamicFeedbackWidget.showDynamicFeedback();
		}

		int filteredActivitiesCount = 0;
		if (CollectionUtils.isNotEmpty(event.filteredActivities)) {
			filteredActivitiesCount += event.filteredActivities.size();
		}

		isFilteredToZeroResults = filteredActivitiesCount == 0;
		updateDoneButton();
		dynamicFeedbackWidget.setDynamicCounterText(filteredActivitiesCount);
	}

	private void updateDoneButton() {
		doneButton.setAlpha(isFilteredToZeroResults ? 0.15f : 1.0f);
	}

	@Subscribe
	public void onDynamicFeedbackClearButtonClicked(Events.DynamicFeedbackClearButtonClicked event) {
		LXSortFilterMetadata lxSortFilterMetadata = defaultFilterMetadata();
		Events.post(new Events.LXFilterChanged(lxSortFilterMetadata));
	}

	@NotNull
	private LXSortFilterMetadata defaultFilterMetadata() {
		LXSortFilterMetadata lxSortFilterMetadata = new LXSortFilterMetadata();
		lxSortFilterMetadata.sort = LXSortType.POPULARITY;
		selectedFilterCategories.clear();
		return lxSortFilterMetadata;
	}

	public int getNumberOfSelectedFilters() {
		return selectedFilterCategories.size();
	}
}
