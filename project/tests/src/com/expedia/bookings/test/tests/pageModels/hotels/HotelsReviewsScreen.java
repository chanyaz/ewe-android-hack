package com.expedia.bookings.test.tests.pageModels.hotels;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.test.tests.pageModels.common.ScreenActions;
import com.expedia.bookings.test.utils.TestPreferences;

public class HotelsReviewsScreen extends ScreenActions {

	private static final int sSelectButtonID = R.id.menu_select_hotel;
	private static final int sLoadingReviewsStringID = R.string.user_review_loading_text;
	private static final int sTitleViewID = R.id.title;
	private static final int sRatingViewID = R.id.rating;
	private static final int sFavorableStringID = R.string.user_review_sort_button_favorable;
	private static final int sRecentStringID = R.string.user_review_sort_button_recent;
	private static final int sCriticalStringID = R.string.user_review_sort_button_critical;
	private static final int sBackButtonID = android.R.id.home;

	public HotelsReviewsScreen(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	// Object access

	public TextView titleView() {
		return (TextView) getView(sTitleViewID);
	}

	public RatingBar ratingBar() {
		return (RatingBar) getView(sRatingViewID);
	}

	public View selectButton() {
		return getView(sSelectButtonID);
	}

	public String favorableString() {
		return getString(sFavorableStringID);
	}

	public String recentString() {
		return getString(sRecentStringID);
	}

	public String criticalString() {
		return getString(sCriticalStringID);
	}

	public View backButton() {
		return getView(sBackButtonID);
	}

	public String loadingUserReviews() {
		return getString(sLoadingReviewsStringID);
	}

	// Object interaction

	public void clickSelectButton() {
		clickOnView(selectButton());
	}

	public void clickFavorableTab() {
		clickOnText(favorableString());
	}

	public void clickRecentTab() {
		clickOnText(recentString());
	}

	public void clickCriticalTab() {
		clickOnText(criticalString());
	}

	public void clickBackButton() {
		clickOnView(backButton());
	}
}
