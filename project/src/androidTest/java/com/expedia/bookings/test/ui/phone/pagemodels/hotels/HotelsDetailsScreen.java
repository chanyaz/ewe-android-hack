package com.expedia.bookings.test.ui.phone.pagemodels.hotels;

import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;

/**
 * Created by dmadan on 4/10/14.
 */
public class HotelsDetailsScreen extends ScreenActions {
	private static final int TITLE_VIEW_ID = R.id.title;
	private static final int RATING_VIEW_ID = R.id.rating;
	private static final int BOOK_NOW_BUTTON_ID = R.id.book_now_button;
	private static final int BOOK_BY_PHONE_BUTTON_ID = R.id.book_by_phone_button;
	private static final int REVIEWS_TITLE_VIEW_ID = R.id.user_rating_text_view;
	private static final int BANNER_VIEW_ID = R.id.banner_message_text_view;
	private static final int READ_MORE_VIEW_ID = R.id.read_more;
	private static final int SELECT_BUTTON_ID = R.id.menu_select_hotel;
	private static final int BACK_BUTTON_ID = android.R.id.home;
	private static final int NO_REVIEWS_STRING_ID = R.string.no_reviews;
	private static final int VIP_IMAGE_VIEW = R.id.vip_badge;
	private static final int AMENITIES_CONTAINER_ID = R.id.amenities_table_row;
	private static final int VIP_STRING_ID = R.string.vip_access_message;
	private static final int GALLERY_VIEW_ID = R.id.images_gallery;

// Object access

	public static ViewInteraction titleView() {
		return onView(withId(TITLE_VIEW_ID));
	}

	public static ViewInteraction ratingBar() {
		return onView(withId(RATING_VIEW_ID));
	}

	public static ViewInteraction bookNowButton() {
		return onView(withId(BOOK_NOW_BUTTON_ID));
	}

	public static ViewInteraction bookByPhoneButton() {
		return onView(withId(BOOK_BY_PHONE_BUTTON_ID));
	}

	public static ViewInteraction reviewsTitle() {
		return onView(withId(REVIEWS_TITLE_VIEW_ID));
	}

	public static ViewInteraction bannerTextView() {
		return onView(withId(BANNER_VIEW_ID));
	}

	public static ViewInteraction readMore() {
		return onView(withId(READ_MORE_VIEW_ID));
	}

	public static ViewInteraction selectButton() {
		return onView(withId(SELECT_BUTTON_ID));
	}

	public static ViewInteraction backButton() {
		return onView(withId(BACK_BUTTON_ID));
	}

	public static ViewInteraction noReviews() {
		return onView(withText(NO_REVIEWS_STRING_ID));
	}

	public static ViewInteraction vipImageView() {
		return onView(withId(VIP_IMAGE_VIEW));
	}

	public static ViewInteraction amenitiesContainer() {
		return onView(withId(AMENITIES_CONTAINER_ID));
	}

	public static ViewInteraction vipAccessMessage() {
		return onView(withText(VIP_STRING_ID));
	}

	public static ViewInteraction hotelGallery() {
		return onView(withId(GALLERY_VIEW_ID));
	}

	// Object interaction

	public static void clickBookNowButton() {
		(bookNowButton()).perform(click());
	}

	public static void clickBookByPhoneButton() {
		(bookByPhoneButton()).perform(click());
	}

	public static void clickReviewsTitle() {
		(reviewsTitle()).perform(click());
	}

	public static void clickBannerView() {
		(bannerTextView()).perform(click());
	}

	public static void clickReadMore() {
		(readMore()).perform(click());
	}

	public static void clickSelectButton() {
		(selectButton()).perform(click());
	}

	public static void clickBackButton() {
		(backButton()).perform(click());
	}

	public static void clickVIPImageView() {
		(vipImageView()).perform(click());
	}

}
