package com.expedia.bookings.test.tests.pageModels.hotels;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.test.tests.pageModels.common.ScreenActions;
import com.expedia.bookings.test.utils.TestPreferences;

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

	public HotelsDetailsScreen(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	// Object access

	public TextView titleView() {
		return (TextView) getView(TITLE_VIEW_ID);
	}

	public RatingBar ratingBar() {
		return (RatingBar) getView(RATING_VIEW_ID);
	}

	public View bookNowButton() {
		return getView(BOOK_NOW_BUTTON_ID);
	}

	public View bookByPhoneButton() {
		return getView(BOOK_BY_PHONE_BUTTON_ID);
	}

	public TextView reviewsTitle() {
		return (TextView) getView(REVIEWS_TITLE_VIEW_ID);
	}

	public TextView bannerTextView() {
		return (TextView) getView(BANNER_VIEW_ID);
	}

	public View readMore() {
		return getView(READ_MORE_VIEW_ID);
	}

	public View selectButton() {
		return getView(SELECT_BUTTON_ID);
	}

	public View backButton() {
		return getView(BACK_BUTTON_ID);
	}

	public String noReviews() {
		return getString(NO_REVIEWS_STRING_ID);
	}

	public ImageView vipImageView() {
		return (ImageView) getView(VIP_IMAGE_VIEW);
	}

	public ViewGroup amenitiesContainer() {
		return (ViewGroup) getView(AMENITIES_CONTAINER_ID);
	}

	public String vipAccessMessage() {
		return getString(VIP_STRING_ID);
	}

	public View hotelGallery() {
		return getView(GALLERY_VIEW_ID);
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

	public void clickVIPImageView() {
		clickOnView(vipImageView());
	}

}
