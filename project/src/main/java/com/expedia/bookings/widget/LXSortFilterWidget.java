package com.expedia.bookings.widget;

import java.util.Map;

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
import com.expedia.bookings.utils.Ui;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.Observable;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.OnClickEvent;
import rx.android.view.ViewObservable;
import rx.functions.Func1;

public class LXSortFilterWidget extends LinearLayout {

	private Map<String, LXCategoryMetadata> filterCategories;

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

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		Events.register(this);

		Drawable navIcon = getResources().getDrawable(R.drawable.ic_check_white_24dp).mutate();
		navIcon.setColorFilter(getResources().getColor(R.color.lx_actionbar_text_color), PorterDuff.Mode.SRC_IN);
		doneButton.setCompoundDrawablesWithIntrinsicBounds(navIcon, null, null, null);
	}

	@OnClick(R.id.price_sort_button)
	public void onPriceSortClicked() {
		popularitySortButton.setSelected(false);
		priceSortButton.setSelected(true);
	}

	@OnClick(R.id.popularity_sort_button)
	public void onPopularitySortClicked() {
		popularitySortButton.setSelected(true);
		priceSortButton.setSelected(false);
	}

	public Observable<LXSortFilterMetadata> filterSortEventStream() {
		Scheduler scheduler = AndroidSchedulers.mainThread();

		return ViewObservable.clicks(doneButton, true)
			.map(filterSortMetadata)
			.subscribeOn(scheduler)
			.observeOn(scheduler);
	}

	private Func1<OnClickEvent, LXSortFilterMetadata> filterSortMetadata = new Func1<OnClickEvent, LXSortFilterMetadata>() {
		@Override
		public LXSortFilterMetadata call(OnClickEvent nothing) {
			LXSortFilterMetadata lxSortFilterMetadata = new LXSortFilterMetadata();
			lxSortFilterMetadata.lxCategoryMetadataMap = filterCategories;
			lxSortFilterMetadata.sort = priceSortButton.isSelected() ? LXSortType.PRICE : LXSortType.POPULARITY;
			return lxSortFilterMetadata;
		}
	};

	public void bind(Map<String, LXCategoryMetadata> filterCategories) {
		// Reset Popularity sort as default.
		popularitySortButton.setSelected(true);
		priceSortButton.setSelected(false);
		this.filterCategories = filterCategories;
		filterCategoriesContainer.removeAllViews();
		if (filterCategories != null) {
			for (Map.Entry<String, LXCategoryMetadata> filterCategory : filterCategories.entrySet()) {

				LXCategoryMetadata lxCategoryMetadata = filterCategory.getValue();
				String categoryKey = filterCategory.getKey();

				LXFilterCategoryWidget categoryView = Ui
					.inflate(R.layout.section_lx_filter_row, filterCategoriesContainer, false);
				categoryView.bind(lxCategoryMetadata, categoryKey);
				filterCategoriesContainer.addView(categoryView);
			}
		}
	}

	@Subscribe
	public void onCategoryCheckChanged(Events.LXFilterCategoryCheckedChanged event) {
		if (filterCategories != null) {
			// Updating the category map.
			filterCategories.put(event.categoryKey, event.lxCategoryMetadata);
		}
	}
}
