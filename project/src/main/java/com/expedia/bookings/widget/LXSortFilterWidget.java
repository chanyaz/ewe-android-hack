package com.expedia.bookings.widget;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.lx.LXCategoryMetadata;
import com.expedia.bookings.data.lx.LXSortFilterMetadata;
import com.expedia.bookings.data.lx.LXSortType;
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.CollectionUtils;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.expedia.util.RxKt;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

public class LXSortFilterWidget extends LinearLayout {

	private Map<String, LXCategoryMetadata> selectedFilterCategories = new HashMap<>();
	private boolean isFilteredToZeroResults = false;
	private View toolbarBackgroundView;
	private boolean userBucketedForCategoriesTest;
	private boolean themeAllThingsToDo;
	private String filterActivity;
	private boolean clearTextFromReset = false;
	private boolean skippedFirstFilter = false;
	private boolean filterSelected = false;

	public LXSortFilterWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.widget_lx_sort_filter, this);
	}

	private Button doneButton;

	@InjectView(R.id.price_sort_button)
	Button priceSortButton;

	@InjectView(R.id.popularity_sort_button)
	Button popularitySortButton;

	@InjectView(R.id.filter_categories)
	LinearLayout filterCategoriesContainer;

	@InjectView(R.id.dynamic_feedback_container)
	DynamicFeedbackWidget dynamicFeedbackWidget;

	@InjectView(R.id.toolbar_sort_filter)
	Toolbar toolbar;

	@InjectView(R.id.toolbar_dropshadow)
	View toolbarDropshadow;

	@InjectView(R.id.scroll_filter)
	android.widget.ScrollView scrollFilter;

	@InjectView(R.id.space_below_filter_categories)
	Space spaceBelowFilterCategories;

	@InjectView(R.id.category_title)
	TextView categoryTitle;

	public @InjectView(R.id.filter_activity_name_edit_text)
	EditText activityNameFilterEditText;

	@InjectView(R.id.filter_by_text_container)
	LinearLayout activityNameFilterContainer;

	@InjectView(R.id.filter_divider_line)
	View filterDivider;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);

		toolbar.setTitle(getResources().getString(R.string.sort_and_filter));
		toolbar.setTitleTextAppearance(getContext(), R.style.LXToolbarTitleTextAppearance);
		toolbar.setTitleTextColor(getResources().getColor(R.color.lx_actionbar_text_color));
		toolbar.inflateMenu(R.menu.cars_lx_filter_menu);

		MenuItem item = toolbar.getMenu().findItem(R.id.apply_check);
		setupToolBarCheckmark(item);

		int statusBarHeight = Ui.getStatusBarHeight(getContext());
		if (statusBarHeight > 0) {
			int color = getContext().getResources()
				.getColor(Ui.obtainThemeResID(getContext(), R.attr.primary_color));
			toolbarBackgroundView = Ui.setUpStatusBar(getContext(), null, null, color);
			addView(toolbarBackgroundView, 0);
		}
		// Reset Popularity sort as default.
		popularitySortButton.setSelected(true);
		priceSortButton.setSelected(false);

		scrollFilter.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
			@Override
			public void onScrollChanged() {
				int scrollY = scrollFilter.getScrollY();
				float ratio = (float) (scrollY) / 100;
				toolbarDropshadow.setAlpha(ratio);
			}
		});

		if (AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppLXFilterSearch)) {
			activityNameFilterContainer.setVisibility(VISIBLE);
			filterDivider.setVisibility(VISIBLE);
		}

		RxKt.subscribeTextChange(activityNameFilterEditText, activityFilterSubscriber);
	}

	private Observer activityFilterSubscriber = new Observer() {
		@Override
		public void onComplete() {
		}

		@Override
		public void onError(Throwable e) {
		}

		@Override
		public void onSubscribe(@NonNull Disposable d) {

		}

		@Override
		public void onNext(Object o) {
			if (clearTextFromReset) {
				clearTextFromReset = false;
			}
			else if (!skippedFirstFilter) {
				skippedFirstFilter = true;
			}
			else {
				postLXFilterChangedEvent();
			}
		}
	};

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
		OmnitureTracking.trackLinkLXSort(LXSortType.PRICE);
	}

	@OnClick(R.id.popularity_sort_button)
	public void onPopularitySortClicked() {
		popularitySortButton.setSelected(true);
		priceSortButton.setSelected(false);
		postLXFilterChangedEvent();
		OmnitureTracking.trackLinkLXSort(LXSortType.POPULARITY);
	}

	public void bind(Map<String, LXCategoryMetadata> filterCategories) {
		if (filterCategories != null) {
			//No need to add the remove and add views back in the filterCategroiesContainer upon selection of filter.
			if (!filterSelected) {
				filterCategoriesContainer.removeAllViews();
				for (Map.Entry<String, LXCategoryMetadata> filterCategory : filterCategories.entrySet()) {
					LXCategoryMetadata lxCategoryMetadata = filterCategory.getValue();
					String categoryKey = filterCategory.getKey();
					lxCategoryMetadata.checked = selectedFilterCategories.containsKey(categoryKey);
					LXFilterCategoryWidget categoryView = Ui
						.inflate(R.layout.section_lx_filter_row, filterCategoriesContainer, false);
					categoryView.bind(lxCategoryMetadata, categoryKey);
					filterCategoriesContainer.addView(categoryView);
				}
			}
		}
		else {
			filterSelected = false;
			filterCategoriesContainer.removeAllViews();
			resetSortAndFilter();
		}

		// Hide the dynamic feedback & update done button in case we have zero filters applied.
		if (selectedFilterCategories.size() == 0 && !isActivityFilterApplied()) {
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
		filterSelected = true;
		postLXFilterChangedEvent();
		OmnitureTracking.trackLinkLXFilter(event.categoryKey);
	}

	private void postLXFilterChangedEvent() {
		LXSortFilterMetadata lxSortFilterMetadata = new LXSortFilterMetadata(selectedFilterCategories,
			priceSortButton.isSelected() ? LXSortType.PRICE : LXSortType.POPULARITY,
			activityNameFilterEditText.getText().toString());
		Events.post(new Events.LXFilterChanged(lxSortFilterMetadata));
	}

	@Subscribe
	public void onLXSearchFilterResultsReady(Events.LXSearchFilterResultsReady event) {
		if (selectedFilterCategories.size() == 0 && !isActivityFilterApplied()) {
			isFilteredToZeroResults = false;
			updateDoneButton();
			dynamicFeedbackWidget.hideDynamicFeedback();
			animateDynamicFeedbackHeight(false);
			bind(event.filterCategories);
			return;
		}
		else {
			dynamicFeedbackWidget.showDynamicFeedback();
			animateDynamicFeedbackHeight(true);
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
		filterSelected = false;
		OmnitureTracking.trackLinkLXSortAndFilterCleared();
		LXSortFilterMetadata lxSortFilterMetadata = defaultFilterMetadata();
		Events.post(new Events.LXFilterChanged(lxSortFilterMetadata));
	}

	@NotNull
	public LXSortFilterMetadata defaultFilterMetadata() {
		popularitySortButton.setSelected(true);
		priceSortButton.setSelected(false);
		selectedFilterCategories.clear();
		activityNameFilterEditText.getEditableText().clear();
		return new LXSortFilterMetadata(selectedFilterCategories, LXSortType.POPULARITY);
	}

	public int getNumberOfSelectedFilters() {
		return isActivityFilterApplied() ? selectedFilterCategories.size() + 1
			: selectedFilterCategories.size();
	}

	private boolean isActivityFilterApplied() {
		return Strings.isNotEmpty(activityNameFilterEditText.getText());
	}

	public Button setupToolBarCheckmark(final MenuItem menuItem) {
		doneButton = Ui.inflate(getContext(), R.layout.toolbar_checkmark_item, null);
		doneButton.setText(R.string.done);
		doneButton.setTextColor(getResources().getColor(R.color.lx_actionbar_text_color));
		doneButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				filterSelected = false;
				if (isFilteredToZeroResults) {
					dynamicFeedbackWidget.showDynamicFeedback();
					animateDynamicFeedbackHeight(true);
					dynamicFeedbackWidget.animateDynamicFeedbackWidget();
				}
				else {
					Events.post(new Events.LXFilterDoneClicked());
					if (isActivityFilterApplied()) {
						OmnitureTracking.trackLinkLXTextSearch();
					}
				}
				Ui.hideKeyboard(doneButton);
			}
		});

		Drawable navIcon = getResources().getDrawable(R.drawable.ic_check_white_24dp).mutate();
		navIcon.setColorFilter(getResources().getColor(R.color.lx_actionbar_text_color), PorterDuff.Mode.SRC_IN);
		doneButton.setCompoundDrawablesWithIntrinsicBounds(navIcon, null, null, null);
		menuItem.setActionView(doneButton);
		return doneButton;
	}

	public LXSortFilterWidget setSelectedFilterCategories(String filters) {
		LXSortFilterMetadata lxSortFilterMetadata = new LXSortFilterMetadata(filters);
		this.selectedFilterCategories = lxSortFilterMetadata.lxCategoryMetadataMap;
		return this;
	}

	public void categoryFilterVisibility(boolean visibility) {
		categoryTitle.setVisibility(visibility ? VISIBLE : GONE);
		filterCategoriesContainer.setVisibility(visibility ? VISIBLE : GONE);
		if (toolbarBackgroundView != null) {
			toolbarBackgroundView.setVisibility(visibility ? VISIBLE : GONE);
		}
	}

	public void setToolbarTitle(CharSequence title) {
		toolbar.setTitle(title);
	}

	public void resetSortAndFilter() {
		// Set to default state, as we have new search params available.
		filterSelected = false;
		selectedFilterCategories.clear();
		clearTextFromReset = true;
		activityNameFilterEditText.getText().clear();
		popularitySortButton.setSelected(true);
		priceSortButton.setSelected(false);
		filterCategoriesContainer.requestLayout();
	}

	private void animateDynamicFeedbackHeight(final boolean showDynamicFeedback) {
		if (toolbarBackgroundView != null) {
			toolbarBackgroundView.setVisibility(VISIBLE);
		}
		if (!userBucketedForCategoriesTest || themeAllThingsToDo) {
			spaceBelowFilterCategories.setVisibility(showDynamicFeedback ? VISIBLE : GONE);
			return;
		}
		if ((spaceBelowFilterCategories.getVisibility() == VISIBLE && showDynamicFeedback) || (
			spaceBelowFilterCategories.getVisibility() == GONE && !showDynamicFeedback)) {
			return;
		}

		ObjectAnimator anim = ObjectAnimator
			.ofFloat(this, "translationY",
				showDynamicFeedback ? getResources().getDimensionPixelSize(R.dimen.space_below_lx_filter_view_height)
					: 0f,
				showDynamicFeedback ? 0f
					: getResources().getDimensionPixelSize(R.dimen.space_below_lx_filter_view_height));
		anim.setDuration(300);
		anim.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				if (!showDynamicFeedback) {
					spaceBelowFilterCategories.setVisibility(GONE);
					setTranslationY(0);
				}
			}

			@Override
			public void onAnimationStart(Animator animation) {
				if (showDynamicFeedback) {
					spaceBelowFilterCategories.setVisibility(VISIBLE);
				}
			}
		});
		anim.start();
	}

	public void setUserBucketedForCategoriesTest(boolean userBucketedForCategoriesTest) {
		this.userBucketedForCategoriesTest = userBucketedForCategoriesTest;
	}

	public void setThemeAllThingsToDo(boolean themeAllThingsToDo) {
		this.themeAllThingsToDo = themeAllThingsToDo;
	}

	public void setFocusToToolbarForAccessibility() {
		toolbar.clearFocus();
		toolbar.requestFocus();
	}

	public String getFilterActivity() {
		return filterActivity;
	}

	public LXSortFilterWidget setFilterActivity(String filterActivity) {
		this.filterActivity = filterActivity;
		return this;
	}

	public boolean isFilteredToZeroResults() {
		return isFilteredToZeroResults;
	}

	public DynamicFeedbackWidget getDynamicFeedbackWidget() {
		return dynamicFeedbackWidget;
	}
}
