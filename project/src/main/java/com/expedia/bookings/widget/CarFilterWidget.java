package com.expedia.bookings.widget;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
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

	public CarFilter carFilter;

	public CarFilterWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOrientation(VERTICAL);
		inflate(context, R.layout.widget_car_filter, this);
	}

	@InjectView(R.id.car_filter_done)
	Button doneButton;

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

	LinkedHashSet carSupplierCheckedFilterResults;
	LinkedHashSet carSupplierCheckedFilterDetails;

	@OnClick(R.id.transmission_filter_all)
	public void allClicked() {
		auto.setSelected(false);
		manual.setSelected(false);
		all.setSelected(true);
		carFilter.carTransmissionType = CarDataUtils.transmissionFromString(getContext(), all.getText().toString());
	}

	@OnClick(R.id.transmission_filter_manual)
	public void manualClicked() {
		auto.setSelected(false);
		manual.setSelected(true);
		all.setSelected(false);
		carFilter.carTransmissionType = CarDataUtils.transmissionFromString(getContext(), manual.getText().toString());
	}

	@OnClick(R.id.transmission_filter_automatic)
	public void autoClicked() {
		auto.setSelected(true);
		manual.setSelected(false);
		all.setSelected(false);
		carFilter.carTransmissionType = CarDataUtils.transmissionFromString(getContext(), auto.getText().toString());
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
		carFilter.hasAirConditioning = checked;
	}

	@OnCheckedChanged(R.id.umlimited_mileage_filter_checkbox)
	public void onMileageFilterCheckedChanged(boolean checked) {
		carFilter.hasUnlimitedMileage = checked;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
		Events.register(this);

		allClicked();
		Drawable navIcon = getResources().getDrawable(R.drawable.ic_check_white_24dp).mutate();
		navIcon.setColorFilter(getResources().getColor(R.color.cars_actionbar_text_color), PorterDuff.Mode.SRC_IN);
		doneButton.setCompoundDrawablesWithIntrinsicBounds(navIcon, null, null, null);

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

	@OnClick(R.id.car_filter_done)
	public void onFilterDoneSelect() {
		Events.post(new Events.CarsFilterDone(carFilter));
	}

	public void bind(CarSearch search) {
		AtomicBoolean hasManual = new AtomicBoolean();
		AtomicBoolean hasAuto = new AtomicBoolean();
		AtomicBoolean hasUnlimitedMileage = new AtomicBoolean();
		AtomicBoolean hasAirConditioning = new AtomicBoolean();

		List<CategorizedCarOffers> categories = search.categories;
		carFilter = new CarFilter();
		Set<CarCategory> filterCategories = new HashSet<CarCategory>();
		Set<String> filterSuppliers = new HashSet<String>();
		carFilter.carCategoryCheckedFilter = new LinkedHashSet();
		carSupplierCheckedFilterResults = new LinkedHashSet();
		carFilter.carSupplierCheckedFilter = carSupplierCheckedFilterResults;
		airConditioningCheckbox.setChecked(false);
		unlimitedMileageCheckbox.setChecked(false);

		for (int i = 0; i < categories.size(); i++) {
			filterCategories.add(categories.get(i).category);
			for (int j = 0; j < categories.get(i).offers.size(); j++) {
				setFilterVisibilites(categories.get(i).offers, filterSuppliers, hasManual, hasAuto, hasUnlimitedMileage, hasAirConditioning);
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
		LinearLayout v = isDetails ? filterSuppliersContainerDetails : filterSuppliersContainerResults;
		LinkedHashSet set = isDetails ? carSupplierCheckedFilterDetails : carSupplierCheckedFilterResults;
		carFilter.carSupplierCheckedFilter = set;
		v.removeAllViews();

		if (filterSuppliers != null) {
			for (String obj : filterSuppliers) {
				CarsSupplierFilterWidget supplierView = Ui.inflate(R.layout.section_cars_supplier_filter_row, v, false);
				supplierView.bind(obj);
				if (carSupplierCheckedFilterResults.contains(obj)) {
					supplierView.onCategoryClick();
					carSupplierCheckedFilterDetails.add(obj);
				}
				v.addView(supplierView);
			}
		}
	}

	public void setFilterVisibilites(List<SearchCarOffer> offers) {
		carSupplierCheckedFilterDetails = new LinkedHashSet();
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

	public void setFilterVisibilites(List<SearchCarOffer> offers, Set<String> filterSuppliers, AtomicBoolean hasManual, AtomicBoolean hasAuto, AtomicBoolean hasUnlimitedMileage, AtomicBoolean hasAirConditioning) {
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
			carFilter.carCategoryCheckedFilter.add(event.checkBoxDisplayName);
		}
		else {
			carFilter.carCategoryCheckedFilter.remove(event.checkBoxDisplayName);
		}
	}

	@Subscribe
	public void onSupplierCheckChanged(Events.CarsSupplierFilterCheckChanged event) {
		if (event.checked) {
			carFilter.carSupplierCheckedFilter.add(event.checkBoxDisplayName);
			if (carFilter.carSupplierCheckedFilter == carSupplierCheckedFilterDetails) {
				carSupplierCheckedFilterResults.add(event.checkBoxDisplayName);
				for (int i = 0; i < filterSuppliersContainerResults.getChildCount(); i++) {
					CarsSupplierFilterWidget supplierView = (CarsSupplierFilterWidget) filterSuppliersContainerResults.getChildAt(i);

					if (supplierView.vendorTitle.getText().toString().equals(event.checkBoxDisplayName) && !supplierView.vendorCheckBox.isChecked()) {
						supplierView.onCategoryClick();
					}
				}
			}
		}
		else {
			carFilter.carSupplierCheckedFilter.remove(event.checkBoxDisplayName);
			if (carFilter.carSupplierCheckedFilter == carSupplierCheckedFilterDetails) {
				carSupplierCheckedFilterResults.remove(event.checkBoxDisplayName);
				for (int i = 0; i < filterSuppliersContainerResults.getChildCount(); i++) {
					CarsSupplierFilterWidget supplierView = (CarsSupplierFilterWidget) filterSuppliersContainerResults.getChildAt(i);

					if (supplierView.vendorTitle.getText().toString().equals(event.checkBoxDisplayName) && supplierView.vendorCheckBox.isChecked()) {
						supplierView.onCategoryClick();
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
			carFilter.carSupplierCheckedFilter = carSupplierCheckedFilterDetails;
		}
		else {
			filterSuppliersContainerResults.setVisibility(VISIBLE);
			filterSuppliersContainerDetails.setVisibility(GONE);
			carFilter.carSupplierCheckedFilter = carSupplierCheckedFilterResults;
		}
	}
}
