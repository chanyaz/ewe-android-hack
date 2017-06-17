package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CarCategory;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.CarDataUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class CarsCategoryFilterWidget extends LinearLayout {

	public CarsCategoryFilterWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	//@InjectView(R.id.category)
	TextView categoryTitle;

	//@InjectView(R.id.category_check_box)
	CheckBox categoryCheckBox;

	CarCategory category;

	//@OnClick(R.id.filter_categories_widget)
	public void onCategoryClick() {
		categoryCheckBox.setChecked(!categoryCheckBox.isChecked());
	}

	//@OnCheckedChanged(R.id.category_check_box)
	public void onCategoryCheckedChanged(boolean checked) {
		Events.post(new Events.CarsCategoryFilterCheckChanged(category, checked));
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);
	}

	public void bind(CarCategory category) {
		this.category = category;
		String categoryStringForResults = CarDataUtils.getCategoryStringForResults(getContext(), category);
		categoryTitle.setText(categoryStringForResults);
		categoryCheckBox.setChecked(false);
		categoryCheckBox.setContentDescription(categoryStringForResults);
	}

	public void setChecked(boolean checked) {
		categoryCheckBox.setChecked(checked);
	}
}
