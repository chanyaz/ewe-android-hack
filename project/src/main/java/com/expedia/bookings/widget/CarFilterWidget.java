package com.expedia.bookings.widget;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CarCategory;
import com.expedia.bookings.data.cars.CarFilter;
import com.expedia.bookings.data.cars.CarSearch;
import com.expedia.bookings.data.cars.CategorizedCarOffers;
import com.expedia.bookings.data.cars.SearchCarOffer;
import com.expedia.bookings.data.cars.Transmission;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.CarDataUtils;
import com.expedia.bookings.utils.Ui;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class CarFilterWidget extends LinearLayout {

	public CarFilter carFilterResults = new CarFilter();
	public CarFilter carFilterDetails = new CarFilter();
	public CarFilter workingCarFilterResults = new CarFilter();
	public CarFilter workingCarFilterDetails = new CarFilter();
	boolean isDetails;

	public CarFilterWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOrientation(VERTICAL);
		inflate(context, R.layout.widget_car_filter, this);
	}

	@InjectView(R.id.ac_filter_checkbox)
	CheckBox airConditioningCheckbox;

	@InjectView(R.id.umlimited_mileage_filter_checkbox)
	CheckBox unlimitedMileageCheckbox;

	@InjectView(R.id.ac_filter)
	View airConditioningFilter;

	@InjectView(R.id.umlimited_mileage_filter)
	View unlimitedMileageFilter;

	@InjectView(R.id.car_type_text)
	View carTypeText;

	@InjectView(R.id.filter_categories)
	LinearLayout filterCategoriesContainer;

	@InjectView(R.id.filter_suppliers_results)
	LinearLayout filterSuppliersContainerResults;

	@InjectView(R.id.filter_suppliers_details)
	LinearLayout filterSuppliersContainerDetails;

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

	public void setIsDetails(boolean isDetails) {
		this.isDetails = isDetails;
	}

	public CarFilter getWorkingFilter() {
		return isDetails ? workingCarFilterDetails : workingCarFilterResults;
	}

	public CarFilter getFilter() {
		return isDetails ? carFilterDetails : carFilterResults;
	}

	@OnClick(R.id.transmission_filter_all)
	public void allClicked() {
		auto.setSelected(false);
		manual.setSelected(false);
		all.setSelected(true);
		getWorkingFilter().carTransmissionType = CarDataUtils.transmissionFromString(getContext(), all.getText().toString());
	}

	@OnClick(R.id.transmission_filter_manual)
	public void manualClicked() {
		auto.setSelected(false);
		manual.setSelected(true);
		all.setSelected(false);
		getWorkingFilter().carTransmissionType = CarDataUtils.transmissionFromString(getContext(), manual.getText().toString());
	}

	@OnClick(R.id.transmission_filter_automatic)
	public void autoClicked() {
		auto.setSelected(true);
		manual.setSelected(false);
		all.setSelected(false);
		getWorkingFilter().carTransmissionType = CarDataUtils.transmissionFromString(getContext(), auto.getText().toString());
	}

	@OnClick(R.id.ac_filter)
	public void onAirconditioningFilterClick() {
		airConditioningCheckbox.setChecked(!airConditioningCheckbox.isChecked());
	}

	@OnClick(R.id.umlimited_mileage_filter)
	public void onMileageFilterClick() {
		unlimitedMileageCheckbox.setChecked(!unlimitedMileageCheckbox.isChecked());
	}

	@OnCheckedChanged(R.id.ac_filter_checkbox)
	public void onACFilterCheckedChanged(boolean checked) {
		getWorkingFilter().hasAirConditioning = checked;
	}

	@OnCheckedChanged(R.id.umlimited_mileage_filter_checkbox)
	public void onMileageFilterCheckedChanged(boolean checked) {
		getWorkingFilter().hasUnlimitedMileage = checked;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		Events.register(this);

		Drawable navIcon = getResources().getDrawable(R.drawable.ic_close_white_24dp).mutate();
		navIcon.setColorFilter(getResources().getColor(R.color.cars_actionbar_text_color), PorterDuff.Mode.SRC_IN);
		toolbar.setNavigationIcon(navIcon);
		toolbar.setTitle(getResources().getString(R.string.filter));
		toolbar.setTitleTextAppearance(getContext(), R.style.CarsToolbarTitleTextAppearance);
		toolbar.setTitleTextColor(getResources().getColor(R.color.cars_actionbar_text_color));
		toolbar.inflateMenu(R.menu.cars_filter_menu);

		toolbar.setNavigationOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((Activity) getContext()).onBackPressed();
			}
		});

		MenuItem item = toolbar.getMenu().findItem(R.id.apply_check);
		setupToolBarCheckmark(item);

		allClicked();


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
	}

	public Button setupToolBarCheckmark(final MenuItem menuItem) {
		Button tv = Ui.inflate(getContext(), R.layout.toolbar_checkmark_item, null);
		tv.setText(R.string.apply);
		tv.setTextColor(getResources().getColor(R.color.cars_actionbar_text_color));
		tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//copy the working filter to filter

				carFilterDetails = new CarFilter(workingCarFilterDetails);
				carFilterResults = new CarFilter(workingCarFilterResults);
				if (isDetails) {
					Events.post(new Events.CarsFilterDone(carFilterDetails));
				}
				else {
					Events.post(new Events.CarsFilterDone(carFilterResults));
				}
			}
		});
		Drawable navIcon = getResources().getDrawable(R.drawable.ic_check_white_24dp).mutate();
		navIcon.setColorFilter(getResources().getColor(R.color.cars_actionbar_text_color), PorterDuff.Mode.SRC_IN);
		tv.setCompoundDrawablesWithIntrinsicBounds(navIcon, null, null, null);
		menuItem.setActionView(tv);
		return tv;
	}

	public void bind(CarSearch search) {
		AtomicBoolean hasManual = new AtomicBoolean();
		AtomicBoolean hasAuto = new AtomicBoolean();
		AtomicBoolean hasUnlimitedMileage = new AtomicBoolean();
		AtomicBoolean hasAirConditioning = new AtomicBoolean();

		List<CategorizedCarOffers> categories = search.categories;
		carFilterResults = new CarFilter();
		carFilterDetails = new CarFilter();
		workingCarFilterResults = new CarFilter();
		workingCarFilterDetails = new CarFilter();
		Set<CarCategory> filterCategories = new HashSet<CarCategory>();
		Set<String> filterSuppliers = new HashSet<String>();
		airConditioningCheckbox.setChecked(false);
		unlimitedMileageCheckbox.setChecked(false);

		for (int i = 0; i < categories.size(); i++) {
			filterCategories.add(categories.get(i).category);
			for (int j = 0; j < categories.get(i).offers.size(); j++) {
				setFilterVisibilites(categories.get(i).offers, filterSuppliers, hasManual, hasAuto, hasUnlimitedMileage,
					hasAirConditioning);
			}
		}

		auto.setVisibility(hasAuto.get() ? VISIBLE : GONE);
		manual.setVisibility(hasManual.get() ? VISIBLE : GONE);
		airConditioningFilter.setVisibility(hasAirConditioning.get() ? VISIBLE : GONE);
		unlimitedMileageFilter.setVisibility(hasUnlimitedMileage.get() ? VISIBLE : GONE);

		filterCategoriesContainer.removeAllViews();
		filterSuppliersContainerResults.removeAllViews();

		allClicked();

		if (filterCategories != null) {
			for (CarCategory obj : filterCategories) {
				CarsCategoryFilterWidget categoryView = Ui
					.inflate(R.layout.section_cars_category_filter_row, filterCategoriesContainer, false);
				categoryView.bind(obj);
				filterCategoriesContainer.addView(categoryView);
			}
		}

		bind(filterSuppliers, false);
	}

	public void rebind() {
		hideCarCategories(false);
		hideCarResultsSupplies(false);
	}

	public void bind(Set<String> filterSuppliers, boolean isDetails) {
		this.isDetails = isDetails;
		workingCarFilterDetails.carSupplierCheckedFilter.clear();
		LinearLayout v = isDetails ? filterSuppliersContainerDetails : filterSuppliersContainerResults;
		LinkedHashSet set = getWorkingFilter().carSupplierCheckedFilter;
		getWorkingFilter().carSupplierCheckedFilter = set;
		v.removeAllViews();

		if (filterSuppliers != null) {
			for (String obj : filterSuppliers) {
				CarsSupplierFilterWidget supplierView = Ui.inflate(R.layout.section_cars_supplier_filter_row, v, false);
				supplierView.bind(obj);
				if (workingCarFilterResults.carSupplierCheckedFilter.contains(obj)) {
					supplierView.onCategoryClick();
					workingCarFilterDetails.carSupplierCheckedFilter.add(obj);
				}
				v.addView(supplierView);
			}
		}
	}

	public void setFilterVisibilites(List<SearchCarOffer> offers) {
		Set<String> filterSuppliers = new HashSet<>();
		AtomicBoolean hasManual = new AtomicBoolean();
		AtomicBoolean hasAuto = new AtomicBoolean();
		AtomicBoolean hasUnlimitedMileage = new AtomicBoolean();
		AtomicBoolean hasAirConditioning = new AtomicBoolean();
		setFilterVisibilites(offers, filterSuppliers, hasManual, hasAuto, hasUnlimitedMileage, hasAirConditioning);

		auto.setVisibility(hasAuto.get() ? VISIBLE : GONE);
		manual.setVisibility(hasManual.get() ? VISIBLE : GONE);
		airConditioningFilter.setVisibility(hasAirConditioning.get() ? VISIBLE : GONE);
		unlimitedMileageFilter.setVisibility(hasUnlimitedMileage.get() ? VISIBLE : GONE);

		bind(filterSuppliers, true);
	}

	public void setFilterVisibilites(List<SearchCarOffer> offers, Set<String> filterSuppliers, AtomicBoolean hasManual,
		AtomicBoolean hasAuto, AtomicBoolean hasUnlimitedMileage, AtomicBoolean hasAirConditioning) {
		for (SearchCarOffer j : offers) {
			filterSuppliers.add(j.vendor.name);
			if (j.vehicleInfo.transmission.equals(Transmission.MANUAL_TRANSMISSION)) {
				hasManual.set(true);
			}

			if (j.vehicleInfo.transmission.equals(Transmission.AUTOMATIC_TRANSMISSION)) {
				hasAuto.set(true);
			}

			if (j.vehicleInfo.hasAirConditioning) {
				hasAirConditioning.set(true);
			}

			if (j.hasUnlimitedMileage) {
				hasUnlimitedMileage.set(true);
			}
		}
	}

	@Subscribe
	public void onCategoryCheckChanged(Events.CarsCategoryFilterCheckChanged event) {
		if (event.checked) {
			workingCarFilterResults.carCategoryCheckedFilter.add(event.checkBoxDisplayName);
		}
		else {
			workingCarFilterResults.carCategoryCheckedFilter.remove(event.checkBoxDisplayName);
		}
	}

	@Subscribe
	public void onSupplierCheckChanged(Events.CarsSupplierFilterCheckChanged event) {
		if (event.checked) {
			getWorkingFilter().carSupplierCheckedFilter.add(event.checkBoxDisplayName);

			//Update the filters in another filter view
			if (getWorkingFilter() == workingCarFilterDetails) {
				workingCarFilterResults.carSupplierCheckedFilter.add(event.checkBoxDisplayName);
				for (int i = 0; i < filterSuppliersContainerResults.getChildCount(); i++) {
					CarsSupplierFilterWidget supplierView = (CarsSupplierFilterWidget) filterSuppliersContainerResults
						.getChildAt(i);

					if (supplierView.vendorTitle.getText().toString().equals(event.checkBoxDisplayName)
						&& !supplierView.vendorCheckBox.isChecked()) {
						supplierView.onCategoryClick();
					}
				}
			}
			else {
				for (int i = 0; i < filterSuppliersContainerDetails.getChildCount(); i++) {
					CarsSupplierFilterWidget supplierView = (CarsSupplierFilterWidget) filterSuppliersContainerDetails
						.getChildAt(i);

					if (supplierView.vendorTitle.getText().toString().equals(event.checkBoxDisplayName)
						&& !supplierView.vendorCheckBox.isChecked()) {
						supplierView.onCategoryClick();
						workingCarFilterDetails.carSupplierCheckedFilter.add(event.checkBoxDisplayName);
					}
				}
			}
		}
		else {
			getWorkingFilter().carSupplierCheckedFilter.remove(event.checkBoxDisplayName);

			//Update the filters in another filter view
			if (getWorkingFilter() == workingCarFilterDetails) {
				workingCarFilterResults.carSupplierCheckedFilter.remove(event.checkBoxDisplayName);
				for (int i = 0; i < filterSuppliersContainerResults.getChildCount(); i++) {
					CarsSupplierFilterWidget supplierView = (CarsSupplierFilterWidget) filterSuppliersContainerResults
						.getChildAt(i);

					if (supplierView.vendorTitle.getText().toString().equals(event.checkBoxDisplayName)
						&& supplierView.vendorCheckBox.isChecked()) {
						supplierView.onCategoryClick();
					}
				}
			}
			else {
				for (int i = 0; i < filterSuppliersContainerDetails.getChildCount(); i++) {
					CarsSupplierFilterWidget supplierView = (CarsSupplierFilterWidget) filterSuppliersContainerDetails
						.getChildAt(i);

					if (supplierView.vendorTitle.getText().toString().equals(event.checkBoxDisplayName)
						&& supplierView.vendorCheckBox.isChecked()) {
						supplierView.onCategoryClick();
						workingCarFilterDetails.carSupplierCheckedFilter.remove(event.checkBoxDisplayName);
					}
				}
			}
		}
	}

	public void hideCarCategories(boolean hide) {
		if (hide) {
			carTypeText.setVisibility(GONE);
			filterCategoriesContainer.setVisibility(GONE);
			divider.setVisibility(GONE);
		}
		else {
			carTypeText.setVisibility(VISIBLE);
			filterCategoriesContainer.setVisibility(VISIBLE);
			divider.setVisibility(VISIBLE);
		}
	}

	public void hideCarResultsSupplies(boolean hide) {
		if (hide) {
			filterSuppliersContainerResults.setVisibility(GONE);
			filterSuppliersContainerDetails.setVisibility(VISIBLE);
		}
		else {
			filterSuppliersContainerResults.setVisibility(VISIBLE);
			filterSuppliersContainerDetails.setVisibility(GONE);
		}
	}

	public void resetWorkingCarFilter() {
		//reset the working car filters
		workingCarFilterResults = new CarFilter(carFilterResults);
		workingCarFilterDetails = new CarFilter(carFilterDetails);

		//reset the UI checkboxes
		resetCategoryFilters();
		resetSupplierFiltersResults();
		resetSupplierFiltersDetails();
		resetOptionFilters();
	}

	public void resetCategoryFilters() {
		for (int i = 0; i < filterCategoriesContainer.getChildCount(); i++) {
			CarsCategoryFilterWidget categoryView = (CarsCategoryFilterWidget) filterCategoriesContainer.getChildAt(i);
				categoryView.categoryCheckBox
					.setChecked(carFilterResults.carCategoryCheckedFilter.contains(categoryView.carCategory.toString()));
		}
	}

	public void resetSupplierFiltersResults() {
		for (int i = 0; i < filterSuppliersContainerResults.getChildCount(); i++) {
			CarsSupplierFilterWidget supplierView = (CarsSupplierFilterWidget) filterSuppliersContainerResults
				.getChildAt(i);
			supplierView.vendorCheckBox
				.setChecked(carFilterResults.carSupplierCheckedFilter.contains(supplierView.vendorTitle.getText().toString()));
		}
	}

	public void resetSupplierFiltersDetails() {
		for (int i = 0; i < filterSuppliersContainerDetails.getChildCount(); i++) {
			CarsSupplierFilterWidget supplierView = (CarsSupplierFilterWidget) filterSuppliersContainerDetails
				.getChildAt(i);
			supplierView.vendorCheckBox
				.setChecked(carFilterDetails.carSupplierCheckedFilter.contains(supplierView.vendorTitle.getText().toString()));
		}
	}

	public void resetOptionFilters() {
		airConditioningCheckbox.setChecked(getFilter().hasAirConditioning);
		unlimitedMileageCheckbox.setChecked(getFilter().hasUnlimitedMileage);

		if (getFilter().carTransmissionType == Transmission.AUTOMATIC_TRANSMISSION) {
			autoClicked();
		}
		else if (getFilter().carTransmissionType == Transmission.MANUAL_TRANSMISSION) {
			manualClicked();
		}
		else {
			allClicked();
		}
	}
}
