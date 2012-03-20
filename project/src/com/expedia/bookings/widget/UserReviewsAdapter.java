package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.UserReviewsListActivity.ReviewWrapper;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.ReviewRating;
import com.mobiata.android.util.AndroidUtils;

public class UserReviewsAdapter extends BaseAdapter {

	// CONSTANTS

	// Private members
	private Context mContext;
	private LayoutInflater mInflater;

	public ArrayList<ReviewWrapper> mLoadedReviews;

	private ListView mListView;

	public UserReviewsAdapter(Context context, Property property, ListView listView) {
		mContext = context;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mListView = listView;
	}

	@Override
	public int getCount() {
		if (mLoadedReviews == null) {
			return 0;
		}
		return mLoadedReviews.size();
	}

	@Override
	public boolean isEmpty() {
		return (mLoadedReviews == null || mLoadedReviews.isEmpty());
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
			viewHolder.readMore = convertView.findViewById(R.id.read_more_layout);
			viewHolder.nameAndLocation = (TextView) convertView
					.findViewById(R.id.user_review_name_and_location_text_view);
			viewHolder.submissionDate = (TextView) convertView.findViewById(R.id.user_review_date_text_view);
			convertView.setTag(viewHolder);
		}
		else {
			viewHolder = (UserReviewHolder) convertView.getTag();
		}

		// This click listener is set outside of the convertView so that it displays the right data
		viewHolder.readMore.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				viewHolder.body.setText(userReviewLoaded.mReview.getBody());
				userReviewLoaded.mIsDisplayingFull = true;
				setupFullReviewDisplay(viewHolder);

				// #8783: If the review would be off the screen, scroll it into view.
				// Since smoothScrollToPosition() was only added in api 8, we only do
				// this on newer versions.  "Jumping" would be too sudden for this.
				if (AndroidUtils.getSdkVersion() >= 8) {
					mListView.smoothScrollToPosition(position + 1); //scroll to item (account for header)
				}
			}
		});

		viewHolder.title.setText(userReviewLoaded.mReview.getTitle());

		ReviewRating rating = userReviewLoaded.mReview.getRating();
		if (rating != null) {
			viewHolder.ratingBar.setRating((float) rating.getOverallSatisfaction());
		}

		if (userReviewLoaded.mIsDisplayingFull) {
			setupFullReviewDisplay(viewHolder);
			viewHolder.body.setText(userReviewLoaded.mReview.getBody());
		}
		else if (userReviewLoaded.mBodyWasReduced) {
			setupReducedReviewDisplay(viewHolder);
			viewHolder.body.setText(userReviewLoaded.mBodyReduced);
		}
		else {
			setupFullReviewDisplay(viewHolder);
			viewHolder.body.setText(userReviewLoaded.mReview.getBody());
		}

		String nameAndLocationText = "";
		String name = userReviewLoaded.mReview.getReviewerName();
		String location = userReviewLoaded.mReview.getReviewerLocation();

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
		Time date = userReviewLoaded.mReview.getSubmissionDate();
		Date submissionDate = new Date(date.year - 1900, date.month, date.monthDay); //Y2K is going to end the world! (years since 1900)
		java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(mContext);
		String submissionDateText = dateFormat.format(submissionDate);
		viewHolder.submissionDate.setText(submissionDateText);

		return convertView;
	}

	private void setupReducedReviewDisplay(final UserReviewHolder viewHolder) {
		viewHolder.readMore.setVisibility(View.VISIBLE);
		viewHolder.nameAndLocation.setVisibility(View.GONE);
		viewHolder.submissionDate.setVisibility(View.GONE);
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
		public View readMore;
	}

	public void setUserReviews(ArrayList<ReviewWrapper> reviews) {
		mLoadedReviews = reviews;
		notifyDataSetChanged();
	}

	private void setupFullReviewDisplay(final UserReviewHolder viewHolder) {
		viewHolder.readMore.setVisibility(View.GONE);
		viewHolder.submissionDate.setVisibility(View.VISIBLE);
		viewHolder.nameAndLocation.setVisibility(View.VISIBLE);
	}
}