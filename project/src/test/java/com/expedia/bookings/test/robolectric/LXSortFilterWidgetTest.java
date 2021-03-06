package com.expedia.bookings.test.robolectric;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.data.lx.LXCategoryMetadata;
import com.expedia.bookings.widget.LXSortFilterWidget;

import butterknife.ButterKnife;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricRunner.class)
public class LXSortFilterWidgetTest {

	@Test
	public void testLXPriceEquality() {
		LXActivity left = new LXActivity();
		Money moneyLeft = new Money("10","USD");
		left.price = moneyLeft;

		LXActivity right = new LXActivity();
		Money moneyRight = new Money("10","USD");
		right.price = moneyRight;

		assertEquals(moneyLeft,moneyRight);
		assertEquals(moneyLeft,moneyLeft);

	}

	@Test
	public void testLXSortAndFilterWidgetViews() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		LXSortFilterWidget widget = (LXSortFilterWidget) LayoutInflater.from(activity)
			.inflate(R.layout.test_lx_sort_filter_widget, null);
		assertNotNull(widget);
		ButterKnife.inject(activity);

		widget.bind(buildCategories());

		View filterContainer = widget.findViewById(R.id.filter_categories);
		Button priceSortButton = (Button) widget.findViewById(R.id.price_sort_button);
		Button popularitySortButton = (Button) widget.findViewById(R.id.popularity_sort_button);

		assertNotNull(filterContainer);
		assertNotNull(priceSortButton);
		assertNotNull(popularitySortButton);


		LinearLayout categoriesView = (LinearLayout)filterContainer.findViewById(R.id.filter_categories_widget);
		assertNotNull(categoriesView);

		TextView category = (TextView) categoriesView.findViewById(R.id.category);
		CheckBox categoryCheckBox = (CheckBox) categoriesView.findViewById(R.id.category_check_box);

		assertNotNull(category);
		assertNotNull(categoryCheckBox);

		String expectedCategory = getLxCategoryMetadata().displayValue;
		boolean expectedCheckedState = getLxCategoryMetadata().checked;

		assertEquals(expectedCategory,category.getText());
		assertEquals(expectedCheckedState, categoryCheckBox.isChecked());
	}

	private Map<String, LXCategoryMetadata> buildCategories() {
		Map<String, LXCategoryMetadata> filterCategories = new LinkedHashMap<>();
		filterCategories.put("Attractions", getLxCategoryMetadata());

		return filterCategories;
	}

	private LXCategoryMetadata getLxCategoryMetadata() {
		LXCategoryMetadata lxCategoryMetadata = new LXCategoryMetadata();
		lxCategoryMetadata.checked = false;
		lxCategoryMetadata.displayValue = "Attractions";
		return lxCategoryMetadata;
	}
}
