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

public class HotelsDetailsScreen extends ScreenActions {

	private static int sTitleViewID = R.id.title;
	private static int sRatingViewID = R.id.rating;
	private static int sBookNowButtonID = R.id.book_now_button;
	private static int sBookByPhoneButtonID = R.id.book_by_phone_button;
	private static int sReviewsTitleViewID = R.id.user_rating_text_view;
	private static int sBannerViewID = R.id.banner_message_text_view;
	private static int sReadMoreViewID = R.id.read_more;
	private static int sSelectButtonID = R.id.menu_select_hotel;
	private static int sBackButtonID = android.R.id.home;
	private static int sNoReviewsStringID = R.string.no_reviews;

	public HotelsDetailsScreen(Instrumentation instrumentation, Activity activity, Resources res,
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

	public View bookNowButton() {
		return getView(sBookNowButtonID);
	}

	public View bookByPhoneButton() {
		return getView(sBookByPhoneButtonID);
	}

	public TextView reviewsTitle() {
		return (TextView) getView(sReviewsTitleViewID);
	}

	public TextView bannerTextView() {
		return (TextView) getView(sBannerViewID);
	}

	public View readMore() {
		return getView(sReadMoreViewID);
	}

	public View selectButton() {
		return getView(sSelectButtonID);
	}

	public View backButton() {
		return getView(sBackButtonID);
	}

	public String noReviews() {
		return getString(sNoReviewsStringID);
	}

	// Object interaction

	public void clickBookNowButton() {
		clickOnView(bookNowButton());
	}

	public void clickBookByPhoneButton() {
		clickOnView(bookByPhoneButton());
	}

	public void clickReviewsTitle() {
		clickOnView(reviewsTitle());
	}

	public void clickBannerView() {
		clickOnView(bannerTextView());
	}

	public void clickReadMore() {
		clickOnView(readMore());
	}

	public void clickSelectButton() {
		clickOnView(selectButton());
	}

	public void clickBackButton() {
		clickOnView(backButton());
	}

}
