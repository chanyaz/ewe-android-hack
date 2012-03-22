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
	private static final int TYPE_REVIEW = 0;
	private static final int TYPE_DIVIDER = 1;
	private static final int NUM_VIEW_TYPES = 2;

	// Private members
	private Context mContext;
	private LayoutInflater mInflater;

	public ArrayList<ReviewWrapper> mLoadedReviews;

	private ListView mListView;

	public boolean mAddDivider;

	public UserReviewsAdapter(Context context, Property property, ListView listView) {
		mContext = context;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mListView = listView;
		mAddDivider = false;
	}

	////////////////////////////////////////////////////////////////////////////
	// Overrides

	@Override
	public int getCount() {
		if (mLoadedReviews == null) {
			return 0;
		}
		return mLoadedReviews.size();
	}

	@Override
	public int getViewTypeCount() {
		return NUM_VIEW_TYPES;
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
	public int getItemViewType(int position) {
		ReviewWrapper rw = getItem(position);
		if (rw.mIsDivider) {
			return TYPE_DIVIDER;
		}
		return TYPE_REVIEW;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ReviewWrapper userReviewLoaded = (ReviewWrapper) getItem(position);
		int type = getItemViewType(position);

		if (type == TYPE_REVIEW) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.row_user_review, parent, false);
				UserReviewViewHolder holder = getUserReviewViewHolder(convertView);
				convertView.setTag(holder);
				populateUserReviewsView(holder, userReviewLoaded, position);
			}
			else {
				UserReviewViewHolder holder = (UserReviewViewHolder) convertView.getTag();
				populateUserReviewsView(holder, userReviewLoaded, position);
			}
		}
		else if (type == TYPE_DIVIDER) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.divider_user_reviews_list, parent, false);
				DividerViewHolder holderDiv = getDividerViewHolder(convertView);
				convertView.setTag(holderDiv);
				populateDividerView(holderDiv, userReviewLoaded);
			}
			else {
				DividerViewHolder holder = (DividerViewHolder) convertView.getTag();
				populateDividerView(holder, userReviewLoaded);
			}
		}
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

	/**
	 * public method used within the list activity to add a divider to the list at appropriate times
	 */
	public void addDivider() {
		ReviewWrapper divider = new ReviewWrapper(true);
		mLoadedReviews.add(divider);
		notifyDataSetChanged();
	}

	private static class UserReviewViewHolder {
		public TextView title;
		public RatingBar ratingBar;
		public TextView nameAndLocation;
		public TextView submissionDate;
		public TextView body;
		public View readMore;
	}

	private static class DividerViewHolder {
		public TextView title;
	}

	public UserReviewViewHolder getUserReviewViewHolder(View convertView) {
		UserReviewViewHolder viewHolder = new UserReviewViewHolder();
		viewHolder.title = (TextView) convertView.findViewById(R.id.user_review_title_text_view);
		viewHolder.ratingBar = (RatingBar) convertView.findViewById(R.id.user_review_rating_bar);
		viewHolder.body = (TextView) convertView.findViewById(R.id.user_review_body_text_view);
		viewHolder.readMore = convertView.findViewById(R.id.read_more_layout);
		viewHolder.nameAndLocation = (TextView) convertView
				.findViewById(R.id.user_review_name_and_location_text_view);
		viewHolder.submissionDate = (TextView) convertView.findViewById(R.id.user_review_date_text_view);
		return viewHolder;
	}

	public DividerViewHolder getDividerViewHolder(View convertView) {
		DividerViewHolder holder = new DividerViewHolder();
		holder.title = (TextView) convertView.findViewById(R.id.user_review_divider_text);
		return holder;
	}

	private void populateDividerView(final DividerViewHolder holder, ReviewWrapper reviewWrapped) {
		TextView tv = holder.title;
		tv.setText(mContext.getResources().getString(R.string.user_review_other_languages_title));
	}

	private void populateUserReviewsView(final UserReviewViewHolder viewHolder, final ReviewWrapper userReviewLoaded,
			final int position) {
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

	}

	private void setupReducedReviewDisplay(final UserReviewViewHolder viewHolder) {
		viewHolder.readMore.setVisibility(View.VISIBLE);
		viewHolder.nameAndLocation.setVisibility(View.GONE);
		viewHolder.submissionDate.setVisibility(View.GONE);
	}

	public void setUserReviews(ArrayList<ReviewWrapper> reviews) {
		mLoadedReviews = reviews;
		notifyDataSetChanged();
	}

	private void setupFullReviewDisplay(final UserReviewViewHolder viewHolder) {
		viewHolder.readMore.setVisibility(View.GONE);
		viewHolder.submissionDate.setVisibility(View.VISIBLE);
		viewHolder.nameAndLocation.setVisibility(View.VISIBLE);
	}
}