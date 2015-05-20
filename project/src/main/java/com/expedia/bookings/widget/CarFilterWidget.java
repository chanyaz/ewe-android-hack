package com.expedia.bookings.widget;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.Toolbar;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CarCategory;
import com.expedia.bookings.data.cars.CarFilter;
import com.expedia.bookings.data.cars.CarSearch;
import com.expedia.bookings.data.cars.CategorizedCarOffers;
import com.expedia.bookings.data.cars.SearchCarOffer;
import com.expedia.bookings.data.cars.Transmission;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.SpannableBuilder;
import com.expedia.bookings.utils.Ui;
import com.squareup.otto.Subscribe;
import com.squareup.phrase.Phrase;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class CarFilterWidget extends LinearLayout {

	private CarFilter filter = new CarFilter();
	private boolean isZero = false;

	public CarFilterWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOrientation(VERTICAL);
		inflate(context, R.layout.widget_car_filter, this);
	}

	@InjectView(R.id.car_filter_scrollview)
	ScrollView scrollView;

	@InjectView(R.id.ac_filter_checkbox)
	CheckBox airConditioningCheckbox;

	@InjectView(R.id.unlimited_mileage_filter_checkbox)
	CheckBox unlimitedMileageCheckbox;

	@InjectView(R.id.ac_filter)
	View airConditioningFilter;

	@InjectView(R.id.unlimited_mileage_filter)
	View unlimitedMileageFilter;

	@InjectView(R.id.car_type_text)
	View carTypeText;

	@InjectView(R.id.filter_categories)
	LinearLayout filterCategoriesContainer;

	@InjectView(R.id.filter_suppliers)
	LinearLayout filterSuppliersContainer;

	@InjectView(R.id.transmission_filter_automatic)
	Button auto;

	@InjectView(R.id.transmission_filter_manual)
	Button manual;

	@InjectView(R.id.transmission_filter_all)
	Button all;

	@InjectView(R.id.filter_categories_divider)
	View divider;

	@InjectView(R.id.toolbar)
	Toolbar toolbar;

	@InjectView(R.id.dynamic_feedback_container)
	ViewGroup dynamicFeedbackContainer;

	@InjectView(R.id.dynamic_feedback_counter)
	TextView dynamicFeedbackCounter;

	private Button doneButton;

	@OnClick(R.id.transmission_filter_all)
	public void allClicked() {
		auto.setSelected(false);
		manual.setSelected(false);
		all.setSelected(true);
		filter.carTransmissionType = null;
		postCarFilterEvent();

		OmnitureTracking.trackAppCarFilterUsage(getContext(), "All");
	}

	@OnClick(R.id.transmission_filter_manual)
	public void manualClicked() {
		auto.setSelected(false);
		manual.setSelected(true);
		all.setSelected(false);
		filter.carTransmissionType = Transmission.MANUAL_TRANSMISSION;
		postCarFilterEvent();

		OmnitureTracking.trackAppCarFilterUsage(getContext(), "Manual");

	}

	@OnClick(R.id.transmission_filter_automatic)
	public void autoClicked() {
		auto.setSelected(true);
		manual.setSelected(false);
		all.setSelected(false);
		filter.carTransmissionType = Transmission.AUTOMATIC_TRANSMISSION;
		postCarFilterEvent();

		OmnitureTracking.trackAppCarFilterUsage(getContext(), "Auto");

	}

	@OnClick(R.id.ac_filter)
	public void onAirConditioningFilterClick() {
		airConditioningCheckbox.setChecked(!airConditioningCheckbox.isChecked());
		OmnitureTracking.trackAppCarFilterUsage(getContext(), "Air");

	}

	@OnClick(R.id.unlimited_mileage_filter)
	public void onMileageFilterClick() {
		unlimitedMileageCheckbox.setChecked(!unlimitedMileageCheckbox.isChecked());
		OmnitureTracking.trackAppCarFilterUsage(getContext(), "Unlimited");
	}

	@OnCheckedChanged(R.id.ac_filter_checkbox)
	public void onACFilterCheckedChanged(boolean checked) {
		filter.hasAirConditioning = checked;
		postCarFilterEvent();
		OmnitureTracking.trackAppCarFilterUsage(getContext(), "Air");
	}

	@OnCheckedChanged(R.id.unlimited_mileage_filter_checkbox)
	public void onMileageFilterCheckedChanged(boolean checked) {
		filter.hasUnlimitedMileage = checked;
		postCarFilterEvent();
		OmnitureTracking.trackAppCarFilterUsage(getContext(), "Unlimited");
	}

	@OnClick(R.id.dynamic_feedback_clear_button)
	public void onClearFiltersClick() {
		reset();
		Events.post(new Events.CarsFilterDone(filter));
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);

		toolbar.setTitle(getResources().getString(R.string.filter));
		toolbar.setTitleTextAppearance(getContext(), R.style.CarsToolbarTitleTextAppearance);
		toolbar.setTitleTextColor(getResources().getColor(R.color.cars_actionbar_text_color));
		toolbar.inflateMenu(R.menu.cars_filter_menu);

		MenuItem item = toolbar.getMenu().findItem(R.id.apply_check);
		setupToolBarCheckmark(item);

		int statusBarHeight = Ui.getStatusBarHeight(getContext());
		if (statusBarHeight > 0) {
			int color = getContext().getResources().getColor(R.color.cars_status_bar_color);
			addView(Ui.setUpStatusBar(getContext(), null, null, color), 0);
		}

		setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});

		scrollView.addOnScrollListener(scrollListener);
	}

	private final ScrollView.OnScrollListener scrollListener = new ScrollView.OnScrollListener() {
		@Override
		public void onScrollChanged(ScrollView scrollView, int x, int y, int oldx, int oldy) {
			int drift = oldy - y;
			float total = dynamicFeedbackContainer.getTranslationY() - drift;
			total = Math.min(dynamicFeedbackContainer.getHeight() * 2, total);
			total = Math.max(0, total);
			dynamicFeedbackContainer.setTranslationY(total);
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

	public Button setupToolBarCheckmark(final MenuItem menuItem) {
		doneButton = Ui.inflate(getContext(), R.layout.toolbar_checkmark_item, null);
		doneButton.setText(R.string.done);
		doneButton.setTextColor(getResources().getColor(R.color.cars_actionbar_text_color));
		doneButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isNonZeroResults()) {
					((Activity) getContext()).onBackPressed();
				}
				else {
					showDynamicFeedback();
				}
			}
		});

		Drawable navIcon = getResources().getDrawable(R.drawable.ic_check_white_24dp).mutate();
		navIcon.setColorFilter(getResources().getColor(R.color.cars_actionbar_text_color), PorterDuff.Mode.SRC_IN);
		doneButton.setCompoundDrawablesWithIntrinsicBounds(navIcon, null, null, null);
		menuItem.setActionView(doneButton);
		return doneButton;
	}

	private void postCarFilterEvent() {
		Events.post(new Events.CarsFilterDone(filter));
	}

	public int getNumCheckedFilters(boolean isDetails) {
		return (isDetails ? 0 : filter.categoriesIncluded.size())
				+ filter.suppliersIncluded.size()
				+ (filter.hasUnlimitedMileage ? 1 : 0)
				+ (filter.hasAirConditioning ? 1 : 0);
	}

	private void reset() {
		hideDynamicFeedback();

		allClicked();
		airConditioningCheckbox.setChecked(false);
		unlimitedMileageCheckbox.setChecked(false);

		for (int i = 0, count = filterSuppliersContainer.getChildCount(); i < count; i++) {
			View v = filterSuppliersContainer.getChildAt(i);
			if (v instanceof CarsSupplierFilterWidget) {
				((CarsSupplierFilterWidget) v).setChecked(false);
			}
		}

		for (int i = 0, count = filterCategoriesContainer.getChildCount(); i < count; i++) {
			View v = filterCategoriesContainer.getChildAt(i);
			if (v instanceof CarsCategoryFilterWidget) {
				((CarsCategoryFilterWidget) v).setChecked(false);
			}
		}
	}

	public void bind(CarSearch search) {
		List<CategorizedCarOffers> categories = search.categories;

		filter = new CarFilter();
		filterCategoriesContainer.removeAllViews();
		filterSuppliersContainer.removeAllViews();

		reset();

		// Find the supported categories, suppliers and filters for this search
		for (int i = 0, size = categories.size(); i < size; i++) {
			filter.categoriesSupported.add(categories.get(i).category);
			appendSupplierSetFromOffers(categories.get(i).offers, filter.suppliersSupported);
		}

		// Build the ui
		for (CarCategory category : filter.categoriesSupported) {
			CarsCategoryFilterWidget categoryView = Ui.inflate(R.layout.section_cars_category_filter_row, filterCategoriesContainer, false);
			categoryView.bind(category);
			filterCategoriesContainer.addView(categoryView);
		}

		for (String supplier : filter.suppliersSupported) {
			CarsSupplierFilterWidget supplierView = Ui.inflate(R.layout.section_cars_supplier_filter_row, filterSuppliersContainer, false);
			supplierView.bind(supplier);
			filterSuppliersContainer.addView(supplierView);
		}
		scrollView.setScrollY(0);
	}

	public void onTransitionToResults() {
		carTypeText.setVisibility(VISIBLE);
		filterCategoriesContainer.setVisibility(VISIBLE);
		divider.setVisibility(VISIBLE);

		// Enable all suppliers
		for (int i = 0, count = filterSuppliersContainer.getChildCount(); i < count; i++) {
			filterSuppliersContainer.getChildAt(i).setEnabled(true);
		}
	}

	public void onTransitionToDetails(CategorizedCarOffers unfilteredCategorizedCarOffers) {
		carTypeText.setVisibility(GONE);
		filterCategoriesContainer.setVisibility(GONE);
		divider.setVisibility(GONE);

		Set<String> suppliersAvailableOnDetails = new HashSet<>();
		appendSupplierSetFromOffers(unfilteredCategorizedCarOffers.offers, suppliersAvailableOnDetails);

		for (int i = 0, count = filterSuppliersContainer.getChildCount(); i < count; i++) {
			CarsSupplierFilterWidget checkWidget = (CarsSupplierFilterWidget) filterSuppliersContainer.getChildAt(i);
			String title = checkWidget.getText().toString();
			checkWidget.setEnabled(suppliersAvailableOnDetails.contains(title));
		}
	}

	private void appendSupplierSetFromOffers(List<SearchCarOffer> offers, Set<String> availableSuppliers) {
		for (SearchCarOffer offer : offers) {
			availableSuppliers.add(offer.vendor.name);
		}
	}

	@Subscribe
	public void onCategoryCheckChanged(Events.CarsCategoryFilterCheckChanged event) {
		if (event.checked) {
			filter.categoriesIncluded.add(event.category);
		}
		else {
			filter.categoriesIncluded.remove(event.category);
		}

		postCarFilterEvent();
		OmnitureTracking.trackAppCarFilterUsage(getContext(), "Category");
	}

	@Subscribe
	public void onSupplierCheckChanged(Events.CarsSupplierFilterCheckChanged event) {
		if (event.checked) {
			filter.suppliersIncluded.add(event.supplier);
		}
		else {
			filter.suppliersIncluded.remove(event.supplier);
		}

		postCarFilterEvent();
		OmnitureTracking.trackAppCarFilterUsage(getContext(), "Vendor");
	}

	@Subscribe
	public void onCarsIsFiltered(Events.CarsIsFiltered event) {
		if (!filter.altered()) {
			isZero = false;
			updateDoneButton();
			hideDynamicFeedback();
			return;
		}
		else {
			showDynamicFeedback();
		}

		int count = 0;
		if (event.filteredCarOffers == null) {
			for (CategorizedCarOffers category : event.filteredCarSearch.categories) {
				count += category.offers.size();
			}
		}
		else {
			count += event.filteredCarOffers.offers.size();
		}

		CharSequence text = Phrase.from(getContext().getResources().getQuantityString(R.plurals.number_results_template, count))
			.put("number", count)
			.format();

		if (count == 0) {
			SpannableBuilder sb = new SpannableBuilder();
			sb.append(text, new ForegroundColorSpan(0xFFE6492C));
			text = sb.build();
			isZero = true;
		}
		else {
			isZero = false;
		}
		updateDoneButton();
		dynamicFeedbackCounter.setText(text);
	}

	private boolean isNonZeroResults() {
		if (isZero) {
			AnimUtils.doTheHarlemShake(dynamicFeedbackContainer);
		}

		return !isZero;
	}

	private void hideDynamicFeedback() {
		dynamicFeedbackContainer.setVisibility(View.GONE);
		dynamicFeedbackContainer.setTranslationY(0.0f);
		dynamicFeedbackContainer.animate().alpha(0.0f);
	}

	private void showDynamicFeedback() {
		dynamicFeedbackContainer.setVisibility(View.VISIBLE);
		dynamicFeedbackContainer.setTranslationY(0.0f);
		dynamicFeedbackContainer.animate().alpha(1.0f);
	}

	private void updateDoneButton() {
		if (isZero) {
			doneButton.setAlpha(0.15f);
		}
		else {
			doneButton.setAlpha(1.0f);
		}
	}
}
