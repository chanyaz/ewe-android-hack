package com.expedia.bookings.widget;

import java.util.List;

import org.joda.time.DateTime;

import android.annotation.TargetApi;
import android.content.Context;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.fragment.UserReviewsFragment.ReviewWrapper;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.SpannableBuilder;
import com.mobiata.android.util.Ui;

public class UserReviewsAdapter extends BaseAdapter {

	// CONSTANTS
	private static final int TYPE_REVIEW = 0;
	private static final int TYPE_DIVIDER = 1;
	private static final int TYPE_LOADING = 2;
	private static final int NUM_VIEW_TYPES = 3;

	// Private members
	private Context mContext;
	private LayoutInflater mInflater;

	public List<ReviewWrapper> mLoadedReviews;

	private ListView mListView;

	public boolean mAddDivider;

	public UserReviewsAdapter(Context context, ListView listView) {
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
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
		else if (rw.mIsLoadingFooter) {
			return TYPE_LOADING;
		}
		else {
			return TYPE_REVIEW;
		}
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ReviewWrapper userReviewLoaded = getItem(position);
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
			}
		}
		else if (type == TYPE_LOADING) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.footer_user_reviews_list_loading_more, parent, false);
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

	public void setUserReviews(List<ReviewWrapper> reviews) {
		mLoadedReviews = reviews;
		notifyDataSetChanged();
	}

	private static class UserReviewViewHolder {
		public TextView title;
		public RatingBar ratingBar;
		public TextView nameLocationDate;
		public TextView submissionDate;
		public TextView body;
		public View separator;
	}

	public UserReviewViewHolder getUserReviewViewHolder(View convertView) {
		UserReviewViewHolder viewHolder = new UserReviewViewHolder();
		viewHolder.title = Ui.findView(convertView, R.id.user_review_title_text_view);
		viewHolder.ratingBar = Ui.findView(convertView, R.id.user_review_rating_bar);
		viewHolder.body = Ui.findView(convertView, R.id.user_review_body_text_view);
		viewHolder.nameLocationDate = Ui.findView(convertView, R.id.user_review_name_location_date_text_view);
		viewHolder.submissionDate = Ui.findView(convertView, R.id.user_review_date_text_view);
		viewHolder.separator = Ui.findView(convertView, R.id.user_review_separator);
		return viewHolder;
	}

	private void populateUserReviewsView(final UserReviewViewHolder viewHolder, final ReviewWrapper userReviewLoaded,
			final int position) {
		// This click listener is set outside of the convertView so that it displays the right data
		viewHolder.body.setOnClickListener(new OnClickListener() {
			@TargetApi(8)
			@Override
			public void onClick(View v) {
				viewHolder.body.setText(userReviewLoaded.mReview.getBody());
				userReviewLoaded.mIsDisplayingFull = true;
				setupFullReviewDisplay(viewHolder);
				mListView.smoothScrollToPosition(position + 1); //scroll to item (account for header)
			}
		});

		if (position == 0) {
			viewHolder.separator.setVisibility(View.GONE);
		}
		else {
			viewHolder.separator.setVisibility(View.VISIBLE);
		}

		viewHolder.title.setText(userReviewLoaded.mReview.getTitle());
		viewHolder.ratingBar.setRating(userReviewLoaded.mReview.getOverallSatisfaction());

		if (userReviewLoaded.mIsDisplayingFull) {
			setupFullReviewDisplay(viewHolder);
			viewHolder.body.setText(userReviewLoaded.mReview.getBody());
		}
		else if (userReviewLoaded.mBodyWasReduced) {
			setupReducedReviewDisplay(viewHolder);
			SpannableBuilder builder = new SpannableBuilder();
			builder.append(userReviewLoaded.mBodyReduced);
			builder.append(" ");
			builder.append(mContext.getString(R.string.more), new ForegroundColorSpan(0xFF245FB3), FontCache.getSpan(FontCache.Font.ROBOTO_BOLD));
			viewHolder.body.setText(builder.build());
		}
		else {
			setupFullReviewDisplay(viewHolder);
			viewHolder.body.setText(userReviewLoaded.mReview.getBody());
		}

		String nameAndLocationText = "";
		String name = userReviewLoaded.mReview.getReviewerName();
		String location = userReviewLoaded.mReview.getReviewerLocation();
		boolean hasName = !TextUtils.isEmpty(name);
		boolean hasLocation = !TextUtils.isEmpty(location);

		if (hasName && hasLocation) {
			nameAndLocationText = String.format(mContext.getString(R.string.user_review_name_and_location_signature),
					name, location);
		}
		else if (!hasName && hasLocation) {
			nameAndLocationText = location;
		}
		else if (hasName && !hasLocation) {
			nameAndLocationText = name;
		}


		DateTime dateTime = userReviewLoaded.mReview.getSubmissionDate();
		String submissionDateText = ProductFlavorFeatureConfiguration.getInstance()
			.formatDateTimeForHotelUserReviews(mContext, dateTime);

		String combinedText = nameAndLocationText;
		if (!TextUtils.isEmpty(submissionDateText)) {
			if (TextUtils.isEmpty(nameAndLocationText)) {
				combinedText = submissionDateText;
			}
			else {
				combinedText = mContext.getString(R.string.submitted_by_on_date_TEMPLATE, combinedText, submissionDateText);
			}
		}

		viewHolder.nameLocationDate.setText(combinedText);
	}

	private void setupReducedReviewDisplay(final UserReviewViewHolder viewHolder) {
		// FIXME
	}

	private void setupFullReviewDisplay(final UserReviewViewHolder viewHolder) {
		// FIXME
	}
}
