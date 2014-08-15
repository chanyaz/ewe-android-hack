package com.expedia.bookings.test.phone.pagemodels.hotels;

import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;

/**
 * Created by dmadan on 4/10/14.
 */
public class HotelSearchResultRow {
	private static final int VIP_IMAGE_VIEW_ID = R.id.vip_badge;

	private static final int HOTEL_NAME_TEXT_VIEW_ID = R.id.name_text_view;
	private static final int RATING_BAR_ID = R.id.user_rating_bar;
	private static final int PROXIMITY_TEXT_VIEW_ID = R.id.proximity_text_view;
	private static final int NOT_RATED_TEXT_VIEW_ID = R.id.not_rated_text_view;
	private static final int URGENCY_TEXT_VIEW_ID = R.id.urgency_text_view;

	private static final int STRIKETHROUGH_PRICE_TEXT_VIEW_ID = R.id.strikethrough_price_text_view;
	private static final int PRICE_TEXT_VIEW_ID = R.id.price_text_view;

	private static final int SALE_IMAGE_VIEW_ID = R.id.sale_image_view;
	private static final int SALE_TEXT_VIEW_ID = R.id.sale_text_view;

	private ImageView mVIPImageView;
	private TextView mNameTextView;
	private RatingBar mRatingBar;
	private TextView mProximityTextView;
	private TextView mNotRatedTextView;
	private TextView mUrgencyTextView;
	private TextView mStrikethroughPriceTextView;
	private TextView mPriceTextView;
	private ImageView mSaleImageView;
	private TextView mSaleTextView;

	public HotelSearchResultRow(View view) {
		mVIPImageView = (ImageView) view.findViewById(VIP_IMAGE_VIEW_ID);
		mNameTextView = (TextView) view.findViewById(HOTEL_NAME_TEXT_VIEW_ID);
		mRatingBar = (RatingBar) view.findViewById(RATING_BAR_ID);
		mProximityTextView = (TextView) view.findViewById(PROXIMITY_TEXT_VIEW_ID);
		mNotRatedTextView = (TextView) view.findViewById(NOT_RATED_TEXT_VIEW_ID);
		mUrgencyTextView = (TextView) view.findViewById(URGENCY_TEXT_VIEW_ID);
		mStrikethroughPriceTextView = (TextView) view.findViewById(STRIKETHROUGH_PRICE_TEXT_VIEW_ID);
		mPriceTextView = (TextView) view.findViewById(PRICE_TEXT_VIEW_ID);
		mSaleImageView = (ImageView) view.findViewById(SALE_IMAGE_VIEW_ID);
		mSaleTextView = (TextView) view.findViewById(SALE_TEXT_VIEW_ID);
	}

	// Object access
	public ImageView getVIPImageView() {
		return mVIPImageView;
	}

	public TextView getNameTextView() {
		return mNameTextView;
	}

	public RatingBar getRatingBar() {
		return mRatingBar;
	}

	public TextView getProximityTextView() {
		return mProximityTextView;
	}

	public TextView getNotRatedTextView() {
		return mNotRatedTextView;
	}

	public TextView getUrgencyTextView() {
		return mUrgencyTextView;
	}

	public TextView getStrikethroughPriceTextView() {
		return mStrikethroughPriceTextView;
	}

	public TextView getPriceTextView() {
		return mPriceTextView;
	}

	public ImageView getSaleImageView() {
		return mSaleImageView;
	}

	public TextView getSaleTextView() {
		return mSaleTextView;
	}
}

