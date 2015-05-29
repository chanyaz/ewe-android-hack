package com.expedia.bookings.test.ui.phone.pagemodels.hotels;

import static android.support.test.espresso.action.ViewActions.click;

import com.expedia.bookings.R;

import android.support.test.espresso.ViewInteraction;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class HotelsDetailsScreen {
	private static final int RATING_VIEW_ID = R.id.rating_stars;
	private static final int BOOK_NOW_BUTTON_ID = R.id.book_now_button;
	private static final int REVIEWS_TITLE_VIEW_ID = R.id.user_rating_text_view;
	private static final int BANNER_VIEW_ID = R.id.banner_message_text_view;
	private static final int SELECT_BUTTON_ID = R.id.menu_select_hotel;
	private static final int VIP_IMAGE_VIEW = R.id.vip_badge;
	private static final int GALLERY_VIEW_ID = R.id.images_gallery;

// Object access

	public static ViewInteraction ratingBar() {
		return onView(withId(RATING_VIEW_ID));
	}

	public static ViewInteraction bookNowButton() {
		return onView(withId(BOOK_NOW_BUTTON_ID));
	}

	public static ViewInteraction reviewsTitle() {
		return onView(withId(REVIEWS_TITLE_VIEW_ID));
	}

	public static ViewInteraction bannerTextView() {
		return onView(withId(BANNER_VIEW_ID));
	}

	public static ViewInteraction selectButton() {
		return onView(withId(SELECT_BUTTON_ID));
	}

	public static ViewInteraction vipImageView() {
		return onView(withId(VIP_IMAGE_VIEW));
	}

	public static ViewInteraction hotelGallery() {
		return onView(withId(GALLERY_VIEW_ID));
	}

	// Object interaction

	public static void clickReviewsTitle() {
		(reviewsTitle()).perform(click());
	}

	public static void clickBannerView() {
		(bannerTextView()).perform(click());
	}

	public static void clickSelectButton() {
		(selectButton()).perform(click());
	}

	public static void clickVIPImageView() {
		(vipImageView()).perform(click());
	}

	public static void clickBookNowPayLater() {
		onView(withId(R.id.pay_later_info_text)).perform(click());
	}

}
