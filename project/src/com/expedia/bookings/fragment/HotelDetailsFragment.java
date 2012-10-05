package com.expedia.bookings.fragment;

import java.util.ArrayList;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.FloatMath;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewStub;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletUserReviewsListActivity;
import com.expedia.bookings.data.AvailabilityResponse;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelDescription;
import com.expedia.bookings.data.HotelDescription.DescriptionSection;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.Review;
import com.expedia.bookings.data.ReviewsResponse;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.widget.AvailabilitySummaryWidget;
import com.expedia.bookings.widget.AvailabilitySummaryWidget.AvailabilitySummaryListener;
import com.expedia.bookings.widget.HotelCollage;
import com.expedia.bookings.widget.HotelCollage.OnCollageImageClickedListener;

@TargetApi(11)
public class HotelDetailsFragment extends Fragment implements AvailabilitySummaryListener {

	public static HotelDetailsFragment newInstance() {
		HotelDetailsFragment fragment = new HotelDetailsFragment();
		return fragment;
	}

	//////////////////////////////////////////////////////////////////////////
	// MEMBER VARIABLES

	//----------------------------------
	// CONSTANTS
	//----------------------------------

	private static final int ANIMATION_SPEED = 350;

	//----------------------------------
	// VIEWS
	//----------------------------------

	private TextView mHotelLocationTextView;
	private TextView mHotelNameTextView;
	private TextView mReviewsTitle;
	private View mReviewsSection;
	private View mReviewsContainer;
	private ViewGroup mSomeReviewsContainer;
	private View mReviewsLoadingContainer;
	private TextView mReviewsErrorTextView;
	private TextView mAmenitiesTitle;
	private ViewGroup mAmenitiesContainer;
	private View mAmenitiesNoneText;
	private RatingBar mUserRating;
	private RatingBar mStarRating;
	private ProgressBar mProgressBar;
	private boolean mIsSearchError;
	private ViewGroup mHotelDescriptionContainer;
	private View mSeeAllReviewsButton;
	private ScrollView mHotelDetailsScrollView;

	private AvailabilitySummaryWidget mAvailabilityWidget;

	//----------------------------------
	// OTHERS
	//----------------------------------

	private HotelDetailsFragmentListener mListener;

	private LayoutInflater mInflater;
	private HotelCollage mCollageHandler;

	private Handler mHandler = new Handler();

	// Used to prevent click-happy jerks from opening the user reviews activity with
	// fast clicks to the button.
	private boolean mOpeningUserReviews;

	private boolean mLoadedReviews;

	//////////////////////////////////////////////////////////////////////////
	// LIFECYCLE EVENTS

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof HotelDetailsFragmentListener)) {
			throw new RuntimeException("HotelDetailsFragment Activity must implement HotelDetailsFragmentListener!");
		}
		else if (!(activity instanceof OnCollageImageClickedListener)) {
			throw new RuntimeException("HotelDetailsFragment Activity must implement OnCollageImageClickedListener!");
		}

		mListener = (HotelDetailsFragmentListener) activity;

		mAvailabilityWidget = new AvailabilitySummaryWidget(activity);
		mAvailabilityWidget.setListener(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_hotel_details, container, false);
		mInflater = inflater;

		mHotelDetailsScrollView = (ScrollView) view.findViewById(R.id.hotel_details_scroll_view);
		mHotelNameTextView = (TextView) view.findViewById(R.id.hotel_name_text_view);
		mHotelLocationTextView = (TextView) view.findViewById(R.id.hotel_address_text_view);
		mCollageHandler = new HotelCollage(view, (OnCollageImageClickedListener) getActivity());
		mReviewsTitle = (TextView) view.findViewById(R.id.reviews_title);
		mUserRating = (RatingBar) view.findViewById(R.id.user_rating_bar);
		mStarRating = (RatingBar) view.findViewById(R.id.hotel_rating_bar);
		mSomeReviewsContainer = (ViewGroup) view.findViewById(R.id.some_reviews_container);
		mReviewsSection = view.findViewById(R.id.reviews_section);
		mReviewsContainer = view.findViewById(R.id.reviews_container);
		mProgressBar = (ProgressBar) view.findViewById(R.id.remaining_info_progress_bar);
		mAmenitiesTitle = (TextView) view.findViewById(R.id.amenities_title);
		mAmenitiesContainer = (ViewGroup) view.findViewById(R.id.amenities_table_row);
		mAmenitiesNoneText = (View) view.findViewById(R.id.amenities_none_text);
		mHotelDescriptionContainer = (ViewGroup) view.findViewById(R.id.hotel_description_section);
		mSeeAllReviewsButton = view.findViewById(R.id.see_all_reviews_button);
		mReviewsLoadingContainer = view.findViewById(R.id.reviews_loading_container);

		mAvailabilityWidget.init(view);
		mAvailabilityWidget.setButtonText(R.string.select_room);

		//#10588 disabling amenities layout animations
		mAmenitiesContainer.setLayoutAnimation(null);

		// Disable the scrollbar on the amenities HorizontalScrollView
		HorizontalScrollView amenitiesScrollView = (HorizontalScrollView) view.findViewById(R.id.amenities_scroll_view);
		amenitiesScrollView.setHorizontalScrollBarEnabled(false);

		mAmenitiesTitle.setVisibility(View.GONE);
		mAmenitiesContainer.setVisibility(View.GONE);
		mAmenitiesNoneText.setVisibility(View.GONE);

		if (Db.getSelectedProperty() != null) {
			updateViews(Db.getSelectedProperty(), view);
		}

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();

		mOpeningUserReviews = false;
	}

	//////////////////////////////////////////////////////////////////////////
	// VIEWS

	public void updateViews(Property property) {
		updateViews(property, getView());
	}

	public void updateViews(final Property property, final View view) {
		mHotelNameTextView.setText(property.getName());
		String hotelAddressWithNewLine = StrUtils.formatAddress(property.getLocation(), StrUtils.F_STREET_ADDRESS
				+ StrUtils.F_CITY + StrUtils.F_STATE_CODE);
		mStarRating.setRating((float) property.getHotelRating());
		mHotelLocationTextView.setText(hotelAddressWithNewLine.replace("\n", ", "));
		mCollageHandler.updateCollage(property);

		mUserRating.setRating((float) property.getAverageExpediaRating());

		setupAvailabilityContainer(property);
		updateAmenities(property);
		addHotelDescription(property);

		updateReviews(property);

		// post a message to the event queue to be run on the
		// next event loop to scroll up to the top of the view
		// the purpose of posting this to the message queue versus
		// running it immediately is to enable smooth scrolling animation
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				mHotelDetailsScrollView.smoothScrollTo(0, 0);
			}
		});
	}

	private void updateReviews(final Property property) {
		if (mLoadedReviews) {
			return;
		}

		int reviewsCount = property.getTotalReviews();
		if (reviewsCount == 0) {
			mReviewsTitle.setText(getString(R.string.no_reviews));
		}
		else {
			mReviewsTitle.setText(getResources().getQuantityString(R.plurals.number_of_reviews, reviewsCount,
					reviewsCount));
		}

		if (property.hasExpediaReviews()) {
			mReviewsSection.setVisibility(View.VISIBLE);
			mSeeAllReviewsButton.setVisibility(View.VISIBLE);
			mSeeAllReviewsButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (!mOpeningUserReviews) {
						mOpeningUserReviews = true;

						Intent i = new Intent(getActivity(), TabletUserReviewsListActivity.class);
						i.putExtra(Codes.PROPERTY, property.toJson().toString());
						i.putExtra(Codes.DISPLAY_MODAL_VIEW, true);
						startActivity(i);
					}
				}
			});

			// H551: Increase the touchable region for the "see all reviews" button
			//http://stackoverflow.com/questions/1343222/is-there-an-example-of-how-to-use-a-touchdelegate-in-android-to-increase-the-siz
			mReviewsSection.post(new Runnable() {
				// Post in the parent's message queue to make sure the parent
				// lays out its children before we call getHitRect()
				public void run() {
					final Rect r = new Rect();
					mSeeAllReviewsButton.getHitRect(r);
					r.top -= 20;
					r.bottom += 20;
					r.left -= 20;
					r.right += 20;
					mReviewsSection.setTouchDelegate(new TouchDelegate(r, mSeeAllReviewsButton));
				}
			});

			int numReviewRows = getResources().getInteger(R.integer.num_review_rows);
			float heightPerReviewRow = getResources().getDimension(R.dimen.min_height_per_row_review);
			float minHeight = 0.0f;
			if ((property.getTotalReviews() / numReviewRows) > 1) {
				minHeight = heightPerReviewRow * numReviewRows;
			}
			else {
				minHeight = heightPerReviewRow * (property.getTotalReviews() % numReviewRows);
			}

			mReviewsContainer.setMinimumHeight((int) minHeight);
			mReviewsLoadingContainer.setVisibility(View.VISIBLE);

			if (mReviewsErrorTextView != null) {
				mReviewsErrorTextView.setVisibility(View.GONE);
			}

			ReviewsResponse reviews = Db.getSelectedReviewsResponse();
			if (reviews != null) {
				addReviews(reviews);

				if (mReviewsErrorTextView != null) {
					mReviewsErrorTextView.setMinimumHeight((int) minHeight);
				}
			}
		}
		else {
			mSeeAllReviewsButton.setVisibility(View.GONE);
			mReviewsLoadingContainer.setVisibility(View.GONE);
			mReviewsSection.setVisibility(View.GONE);
		}
	}

	private void updateAmenities(Property property) {
		mAmenitiesContainer.removeAllViews();

		if (property.hasAmenitiesSet()) {
			mProgressBar.setVisibility(View.GONE);

			mAmenitiesTitle.setVisibility(View.VISIBLE);
			mAmenitiesContainer.setVisibility(View.VISIBLE);
			mAmenitiesNoneText.setVisibility(View.VISIBLE);
			if (property.hasAmenities()) {
				LayoutUtils.addAmenities(getActivity(), property, mAmenitiesContainer);
				mAmenitiesNoneText.setVisibility(View.GONE);

			}
		}
		else {
			if (mIsSearchError) {
				// TODO: Indicate more error?
				mProgressBar.setVisibility(View.GONE);
			}
			else {
				mProgressBar.setVisibility(View.VISIBLE);
			}

			mAmenitiesTitle.setVisibility(View.GONE);
			mAmenitiesContainer.setVisibility(View.GONE);
			mAmenitiesNoneText.setVisibility(View.GONE);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Fragment Control

	public void notifyPropertySelected() {
		mLoadedReviews = false;
		updateViews(Db.getSelectedProperty());
	}

	public void notifyAvailabilityQueryStarted() {
		if (mAvailabilityWidget != null) {
			mAvailabilityWidget.setButtonEnabled(false);
			mAvailabilityWidget.showProgressBar();
		}
	}

	public void notifyAvailabilityQueryError(String errMsg) {
		if (mAvailabilityWidget != null) {
			mAvailabilityWidget.setButtonEnabled(false);
			mAvailabilityWidget.showError(errMsg);
			mIsSearchError = true;
		}
	}

	public void notifyAvailabilityQueryComplete() {
		AvailabilityResponse response = Db.getSelectedAvailabilityResponse();
		if (response != null) {
			mAvailabilityWidget.setButtonEnabled(true);
			mAvailabilityWidget.showRates(response);
		}
		else {
			updateViews(Db.getSelectedProperty());
		}
	}

	public void notifyReviewsQueryStarted() {
		if (mSomeReviewsContainer == null) {
			return;
		}

		mSomeReviewsContainer.removeAllViews();
		mSomeReviewsContainer.setVisibility(View.GONE);
		mReviewsLoadingContainer.setVisibility(View.VISIBLE);
		if (mReviewsErrorTextView != null) {
			mReviewsErrorTextView.setVisibility(View.GONE);
		}
	}

	public void notifyReviewsQueryError() {
		showReviewsError();
	}

	public void notifyReviewsQueryComplete() {
		addReviews(Db.getSelectedReviewsResponse());
	}

	//////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS

	private void setupAvailabilityContainer(Property property) {
		mAvailabilityWidget.updateProperty(property);

		AvailabilityResponse availabilityResponse = Db.getSelectedAvailabilityResponse();
		mAvailabilityWidget.setButtonEnabled(availabilityResponse != null && !availabilityResponse.hasErrors()
				&& !availabilityResponse.canRequestMoreData());

		if (availabilityResponse != null) {
			mAvailabilityWidget.showRates(availabilityResponse);
		}
	}

	private void addReviews(ReviewsResponse reviewsResponse) {
		if (mLoadedReviews) {
			return;
		}

		mSomeReviewsContainer.removeAllViews();
		mSomeReviewsContainer.setVisibility(View.GONE);

		if (reviewsResponse == null) {
			showReviewsError();
			return;
		}

		int tenDp = (int) FloatMath.ceil(getResources().getDisplayMetrics().density * 10);
		int fiveDp = (int) FloatMath.ceil(getResources().getDisplayMetrics().density * 5);

		int numReviewsPerRow = getResources().getInteger(R.integer.num_review_rows);
		int numReviewColumns = getResources().getInteger(R.integer.num_review_columns);

		int reviewCount = reviewsResponse.getReviewCount();
		if (reviewCount > 0) {
			for (int i = 0; i < numReviewsPerRow && reviewCount > 0; i++) {
				LinearLayout row = new LinearLayout(getActivity());
				LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, 0);

				rowParams.weight = 1;
				row.setOrientation(LinearLayout.HORIZONTAL);
				row.setLayoutParams(rowParams);
				row.setPadding(0, tenDp, 0, 0);

				for (int j = 0; j < numReviewColumns && reviewCount > 0; j++) {
					final Review review = reviewsResponse.getReviews().get((i * numReviewColumns + j));
					ViewGroup reviewSection = (ViewGroup) mInflater.inflate(R.layout.snippet_review, null);

					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT);
					params.weight = 1;
					params.rightMargin = fiveDp;
					reviewSection.setLayoutParams(params);

					TextView reviewTitle = (TextView) reviewSection.findViewById(R.id.review_title);
					reviewTitle.setText(review.getTitle());

					RatingBar reviewRating = (RatingBar) reviewSection.findViewById(R.id.user_review_rating);
					reviewRating.setRating((float) review.getRating().getOverallSatisfaction());

					final TextView reviewBody = (TextView) reviewSection.findViewById(R.id.review_body);
					reviewBody.setLines(2);
					reviewBody.setText(review.getBody());
					reviewBody.post(new Runnable() {
						@Override
						public void run() {
							final TextPaint paint = reviewBody.getPaint();
							final float width = reviewBody.getWidth();
							final float ellipsesWidth = paint.measureText("...");

							// Get review body
							String text = review.getBody();

							// Truncate the first line by using the textview width and the width of ellipses
							String firstLine = (String) TextUtils.ellipsize(text, paint, width + ellipsesWidth,
									TruncateAt.END);

							// Find the last space in the first line
							final int lastSpace = firstLine.lastIndexOf(" ");
							if (lastSpace > -1) {
								// The first line only goes to the last space, at which point it wraps
								firstLine = firstLine.substring(0, lastSpace);
							}

							// Get the second string by subtracting the first string and truncating by the textview width
							String secondLine = (String) TextUtils.ellipsize(text.substring(firstLine.length()), paint,
									width, TruncateAt.END);

							// set the text as the first line plus the second line truncated
							reviewBody.setText(firstLine + secondLine);
						}
					});

					row.addView(reviewSection);

					reviewCount--;
				}
				mSomeReviewsContainer.addView(row);
			}
			mReviewsLoadingContainer.setVisibility(View.GONE);
			mSomeReviewsContainer.setVisibility(View.VISIBLE);
			ObjectAnimator animator = ObjectAnimator.ofFloat(mSomeReviewsContainer, "alpha", 0, 1);
			animator.setDuration(ANIMATION_SPEED);
			animator.start();

			mLoadedReviews = true;
		}
		else {
			showReviewsError();
		}
	}

	private void showReviewsError() {
		mReviewsLoadingContainer.setVisibility(View.GONE);

		if (mReviewsErrorTextView == null) {
			ViewStub stub = (ViewStub) mReviewsContainer.findViewById(R.id.stub_reviews_error);
			mReviewsErrorTextView = (TextView) stub.inflate();
		}

		mReviewsErrorTextView.setVisibility(View.VISIBLE);
		mReviewsErrorTextView.setMinimumHeight(mReviewsContainer.getHeight());
	}

	private void addHotelDescription(Property property) {
		mHotelDescriptionContainer.removeAllViews();

		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT);
		lp.weight = 1;

		// setup the number of columns we expect to have
		int numHotelDescriptionColumns = getResources().getInteger(R.integer.num_hotel_description_sections_per_row);
		ArrayList<LinearLayout> columns = new ArrayList<LinearLayout>();
		for (int i = 0; i < numHotelDescriptionColumns; i++) {
			LinearLayout column = new LinearLayout(getActivity());
			column.setOrientation(LinearLayout.VERTICAL);
			column.setLayoutParams(lp);

			columns.add(column);
			mHotelDescriptionContainer.addView(column);
		}

		HotelDescription.SectionStrings.initSectionStrings(getActivity());
		HotelDescription hotelDescription = new HotelDescription(getActivity());

		if (property.hasDescriptionText()) {
			hotelDescription.parseDescription(property.getDescriptionText());
		}

		int sectionCount = hotelDescription.getSections().size();
		int tenDp = (int) FloatMath.ceil(getResources().getDisplayMetrics().density * 10);
		int fiveDp = (int) FloatMath.ceil(getResources().getDisplayMetrics().density * 5);
		int numHotelDescriptionsPerRow = getResources().getInteger(R.integer.num_hotel_description_sections_per_row);
		for (int i = 0; sectionCount > 0; i++) {

			for (int j = 0; j < numHotelDescriptionsPerRow && sectionCount > 0; j++) {
				DescriptionSection section = hotelDescription.getSections().get(i * numHotelDescriptionsPerRow + j);
				ViewGroup descriptionSection = (ViewGroup) mInflater.inflate(
						R.layout.snippet_hotel_description_section, null);

				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
						LayoutParams.WRAP_CONTENT);
				params.bottomMargin = tenDp;
				params.rightMargin = fiveDp;
				descriptionSection.setLayoutParams(params);

				TextView descriptionTitle = (TextView) descriptionSection
						.findViewById(R.id.title_description_text_view);
				descriptionTitle.setText(section.title);

				TextView descriptionBody = (TextView) descriptionSection.findViewById(R.id.body_description_text_view);
				descriptionBody.setText(Html.fromHtml(section.description));

				columns.get(i % columns.size()).addView(descriptionSection);

				sectionCount--;
			}
		}

	}

	//////////////////////////////////////////////////////////////////////////
	// AvailabilitySummaryListener

	@Override
	public void onRateClicked(Rate rate) {
		mListener.onDetailsRateClicked(rate);
	}

	@Override
	public void onButtonClicked() {
		mListener.onBookNowClicked();
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface HotelDetailsFragmentListener {
		public void onDetailsRateClicked(Rate rate);

		public void onBookNowClicked();
	}
}
