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

	private static final int sFilterEditTextID = R.id.filter_hotel_name_edit_text;

	private static final int sSmallRadiusButtonID = R.id.radius_small_button;
	private static final int sMediumRadiusButtonID = R.id.radius_medium_button;
	private static final int sLargeRadiusButtonID = R.id.radius_large_button;
	private static final int sAllRadiusButtonID = R.id.radius_all_button;

	private static final int sLowRatingButtonID = R.id.rating_low_button;
	private static final int sMediumRatingButtonID = R.id.rating_medium_button;
	private static final int sHighRatingButtonID = R.id.rating_high_button;
	private static final int sAllRatingButtonID = R.id.rating_all_button;

	private static final int sLowPriceButtonID = R.id.price_cheap_button;
	private static final int sModeratePriceButtonID = R.id.price_moderate_button;
	private static final int sExpensivePriceButtonID = R.id.price_expensive_button;
	private static final int sAllPriceButtonID = R.id.price_all_button;

	private static final int sVIPAccessButtonID = R.id.filter_vip_access;

	public HotelsFilterMenu(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	// Object access

	public EditText filterEditText() {
		return (EditText) getView(sFilterEditTextID);
	}

	public View smallRadiusFilterButton() {
		return getView(sSmallRadiusButtonID);
	}

	public View mediumRadiusFilterButton() {
		return getView(sMediumRadiusButtonID);
	}

	public View largeRadiusFilterButton() {
		return getView(sLargeRadiusButtonID);
	}

	public View allRadiusFilterButton() {
		return getView(sAllRadiusButtonID);
	}

	public View lowRatingFilterButton() {
		return getView(sLowRatingButtonID);
	}

	public View mediumRatingFilterButton() {
		return getView(sMediumRatingButtonID);
	}

	public View highRatingFilterButton() {
		return getView(sHighRatingButtonID);
	}

	public View allRatingFilterButton() {
		return getView(sAllRatingButtonID);
	}

	public View lowPriceFilterButton() {
		return getView(sLowPriceButtonID);
	}

	public View moderatePriceFilterButton() {
		return getView(sModeratePriceButtonID);
	}

	public View expensivePriceFilterButton() {
		return getView(sExpensivePriceButtonID);
	}

	public View allPriceFilterButton() {
		return getView(sAllPriceButtonID);
	}

	public View filterVIPAccessButton() {
		return getView(sVIPAccessButtonID);
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
