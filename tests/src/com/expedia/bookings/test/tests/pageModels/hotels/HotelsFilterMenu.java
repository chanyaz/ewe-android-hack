package com.expedia.bookings.test.tests.pageModels.hotels;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.view.View;
import android.widget.EditText;

import com.expedia.bookings.R;
import com.expedia.bookings.test.tests.pageModels.common.ScreenActions;
import com.expedia.bookings.test.utils.TestPreferences;

public class HotelsFilterMenu extends ScreenActions {

	private static final int FILTER_EDIT_TEXT_ID = R.id.filter_hotel_name_edit_text;

	private static final int SMALL_RADIUS_BUTTON_ID = R.id.radius_small_button;
	private static final int MEDIUM_RADIUS_BUTTON_ID = R.id.radius_medium_button;
	private static final int LARGE_RADIUS_BUTTON_ID = R.id.radius_large_button;
	private static final int ALL_RADIUS_BUTTON_ID = R.id.radius_all_button;

	private static final int LOW_RATING_BUTTON_ID = R.id.rating_low_button;
	private static final int MEDIUM_RATING_BUTTON_ID = R.id.rating_medium_button;
	private static final int HIGH_RATING_BUTTON_ID = R.id.rating_high_button;
	private static final int ALL_RATING_BUTTON_ID = R.id.rating_all_button;

	private static final int LOW_PRICE_BUTTON_ID = R.id.price_cheap_button;
	private static final int MODERATE_PRICE_BUTTON_ID = R.id.price_moderate_button;
	private static final int EXPENSIVE_PRICE_BUTTON_ID = R.id.price_expensive_button;
	private static final int ALL_PRICE_BUTTON_ID = R.id.price_all_button;

	private static final int VIP_ACCESS_BUTTON_ID = R.id.filter_vip_access;

	public HotelsFilterMenu(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	// Object access

	public EditText filterEditText() {
		return (EditText) getView(FILTER_EDIT_TEXT_ID);
	}

	public View smallRadiusFilterButton() {
		return getView(SMALL_RADIUS_BUTTON_ID);
	}

	public View mediumRadiusFilterButton() {
		return getView(MEDIUM_RADIUS_BUTTON_ID);
	}

	public View largeRadiusFilterButton() {
		return getView(LARGE_RADIUS_BUTTON_ID);
	}

	public View allRadiusFilterButton() {
		return getView(ALL_RADIUS_BUTTON_ID);
	}

	public View lowRatingFilterButton() {
		return getView(LOW_RATING_BUTTON_ID);
	}

	public View mediumRatingFilterButton() {
		return getView(MEDIUM_RATING_BUTTON_ID);
	}

	public View highRatingFilterButton() {
		return getView(HIGH_RATING_BUTTON_ID);
	}

	public View allRatingFilterButton() {
		return getView(ALL_RATING_BUTTON_ID);
	}

	public View lowPriceFilterButton() {
		return getView(LOW_PRICE_BUTTON_ID);
	}

	public View moderatePriceFilterButton() {
		return getView(MODERATE_PRICE_BUTTON_ID);
	}

	public View expensivePriceFilterButton() {
		return getView(EXPENSIVE_PRICE_BUTTON_ID);
	}

	public View allPriceFilterButton() {
		return getView(ALL_PRICE_BUTTON_ID);
	}

	public View filterVIPAccessButton() {
		return getView(VIP_ACCESS_BUTTON_ID);
	}

	// Object interactions

	public void enterFilterText(String text) {
		enterText(filterEditText(), text);
	}

	public void clickSmallRadiusFilterButton() {
		clickOnView(smallRadiusFilterButton());
	}

	public void clickMediumRadiusFilterButton() {
		clickOnView(mediumRadiusFilterButton());
	}

	public void clickLargeRadiusFilterButton() {
		clickOnView(largeRadiusFilterButton());
	}

	public void clickAllRadiusFilterButton() {
		clickOnView(smallRadiusFilterButton());
	}

	public void clickLowRatingFilterButton() {
		clickOnView(lowRatingFilterButton());
	}

	public void clickMediumRatingFilterButton() {
		clickOnView(mediumRatingFilterButton());
	}

	public void clickHighRatingFilterButton() {
		clickOnView(highRatingFilterButton());
	}

	public void clickAllRatingFilterButton() {
		clickOnView(allRatingFilterButton());
	}

	public void clickLowPriceFilterButton() {
		clickOnView(lowPriceFilterButton());
	}

	public void clickModeratePriceFilterButton() {
		clickOnView(moderatePriceFilterButton());
	}

	public void clickExpensivePriceFilterButton() {
		clickOnView(expensivePriceFilterButton());
	}

	public void clickAllPriceFilterButton() {
		clickOnView(allPriceFilterButton());
	}

	public void clickVIPAccessFilterButton() {
		clickOnView(filterVIPAccessButton());
	}
}
