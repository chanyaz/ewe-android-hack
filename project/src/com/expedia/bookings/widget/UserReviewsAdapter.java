package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expedia.bookings.R;

import com.mobiata.hotellib.data.Property;
import com.mobiata.hotellib.data.Review;
import com.mobiata.hotellib.data.ReviewRating;

public class UserReviewsAdapter extends BaseAdapter {

	// CONSTANTS
	private static final int BODY_LENGTH_CUTOFF = 270;

	// Private members
	private Context mContext;
	private LayoutInflater mInflater;

	private ArrayList<ReviewWrapper> mLoadedReviews;

	private class ReviewWrapper {
		public Review review;
		public boolean bodyWasReduced;
		public boolean isDisplayingFull;
		public String bodyReduced;
	}

	public UserReviewsAdapter(Context context, Property property) {
		mContext = context;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		if (mLoadedReviews == null) {
			return 0;
		}
		return mLoadedReviews.size();
	}

	@Override
	public ReviewWrapper getItem(int position) {
		return mLoadedReviews.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final UserReviewHolder viewHolder;
		final ReviewWrapper userReviewLoaded = (ReviewWrapper) getItem(position);

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.row_user_review, parent, false);
			viewHolder = new UserReviewHolder();
			viewHolder.title = (TextView) convertView.findViewById(R.id.user_review_title_text_view);
			viewHolder.ratingBar = (RatingBar) convertView.findViewById(R.id.user_review_rating_bar);
			viewHolder.body = (TextView) convertView.findViewById(R.id.user_review_body_text_view);
			viewHolder.readMoreContainer = (RelativeLayout) convertView
					.findViewById(R.id.user_review_read_more_container);
			viewHolder.nameAndLocation = (TextView) convertView
					.findViewById(R.id.user_review_name_and_location_text_view);
			viewHolder.submissionDate = (TextView) convertView.findViewById(R.id.user_review_date_text_view);
			convertView.setTag(viewHolder);
		}
		else {
			viewHolder = (UserReviewHolder) convertView.getTag();
		}

		// This click listener is set outside of the convertView so that it displays the right data
		viewHolder.readMoreContainer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				viewHolder.body.setText(userReviewLoaded.review.getBody());
				userReviewLoaded.isDisplayingFull = true;
				viewHolder.readMoreContainer.setVisibility(View.GONE);
			}
		});
		viewHolder.title.setText(userReviewLoaded.review.getTitle());

		ReviewRating rating = userReviewLoaded.review.getRating();
		if (rating != null) {
			viewHolder.ratingBar.setRating((float) rating.getOverallSatisfaction());
		}

		if (userReviewLoaded.isDisplayingFull) {
			viewHolder.readMoreContainer.setVisibility(View.GONE);
			viewHolder.body.setText(userReviewLoaded.review.getBody());
		}
		else if (userReviewLoaded.bodyWasReduced) {
			viewHolder.readMoreContainer.setVisibility(View.VISIBLE);
			viewHolder.body.setText(userReviewLoaded.bodyReduced);
		}
		else {
			viewHolder.readMoreContainer.setVisibility(View.GONE);
			viewHolder.body.setText(userReviewLoaded.review.getBody());
		}

		String nameAndLocationText = "";
		String name = userReviewLoaded.review.getReviewerName();
		String location = userReviewLoaded.review.getReviewerLocation();

		if (name != null && location != null) {
			nameAndLocationText = String.format(mContext.getString(R.string.user_review_name_and_location_signature),
					name, location);
		}
		else if (name == null && location != null) {
			nameAndLocationText = location;
		}
		else if (name != null && location == null) {
			nameAndLocationText = name;
		}

		viewHolder.nameAndLocation.setText(nameAndLocationText);

		// This code ensure that the date is displayed according to the current locale
		Time date = userReviewLoaded.review.getSubmissionDate();
		Date submissionDate = new Date(date.year - 1900, date.month, date.monthDay); //Y2K is going to end the world! (years since 1900)
		java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(mContext);
		String submissionDateText = dateFormat.format(submissionDate);
		viewHolder.submissionDate.setText(submissionDateText);

		return convertView;
	}

	// These two enabled overrides ensure that the ListView can not have focus, 
	// i.e. now cannot change color during a scroll and look ugly

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		return false;
	}

	private static class UserReviewHolder {
		public TextView title;
		public RatingBar ratingBar;
		public TextView nameAndLocation;
		public TextView submissionDate;
		public TextView body;
		public RelativeLayout readMoreContainer;
	}

	public void addUserReviews(ArrayList<Review> reviews) {
		if (mLoadedReviews == null) {
			mLoadedReviews = reviewWrapperListInit(reviews);
		}
		else {
			mLoadedReviews.addAll(reviewWrapperListInit(reviews));
		}
		notifyDataSetChanged();
	}

	public void switchUserReviews(ArrayList<Review> reviews) {
		mLoadedReviews = reviewWrapperListInit(reviews);
		notifyDataSetChanged();
	}

	private ArrayList<ReviewWrapper> reviewWrapperListInit(List<Review> reviews) {
		ArrayList<ReviewWrapper> loadedReviews = new ArrayList<ReviewWrapper>();
		if (reviews == null) {
			return null;
		}
		for (Review review : reviews) {
			ReviewWrapper loadedReview = new ReviewWrapper();
			loadedReview.review = review;

			String body = review.getBody();
			if (body.length() > BODY_LENGTH_CUTOFF) {
				loadedReview.bodyReduced = body.substring(0, BODY_LENGTH_CUTOFF);
				loadedReview.bodyWasReduced = true;
			}
			else {
				loadedReview.bodyWasReduced = false;
			}

			loadedReview.isDisplayingFull = false;
			loadedReviews.add(loadedReview);
		}
		return loadedReviews;
	}
}
