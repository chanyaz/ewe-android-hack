package com.expedia.bookings.fragment;

import java.util.Set;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.BitmapUtils;
import com.expedia.bookings.bitmaps.L2ImageCache;
import com.expedia.bookings.data.BedType;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelSearch;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.fragment.UserReviewsFragment.UserReviewsFragmentListener;
import com.expedia.bookings.interfaces.IAddToBucketListener;
import com.expedia.bookings.interfaces.IResultsHotelReviewsBackClickedListener;
import com.expedia.bookings.interfaces.helpers.MeasurementHelper;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.ColorBuilder;
import com.expedia.bookings.utils.GridManager;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.UserReviewsFragmentPagerAdapter;
import com.mobiata.android.widget.SegmentedControlGroup;
import com.squareup.otto.Subscribe;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ResultsHotelReviewsFragment extends Fragment implements UserReviewsFragmentListener,
	OnPageChangeListener, OnCheckedChangeListener {

	public static ResultsHotelReviewsFragment newInstance() {
		ResultsHotelReviewsFragment frag = new ResultsHotelReviewsFragment();
		return frag;
	}

	private ViewGroup mRootC;
	private ImageView mHotelImage;
	private TextView mReviewSectionTitle;
	private View mDoneButton;
	private SegmentedControlGroup mSortGroup;
	private ViewPager mViewPager;
	private View mDominantMask;

	private UserReviewsFragmentPagerAdapter mPagerAdapter;

	private IAddToBucketListener mAddToBucketListener;
	private IResultsHotelReviewsBackClickedListener mBackListener;

	private GridManager mGrid = new GridManager();

	private ColorDrawable mDominantColorBackground;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mBackListener = Ui.findFragmentListener(this, IResultsHotelReviewsBackClickedListener.class);
		mAddToBucketListener = Ui.findFragmentListener(this, IAddToBucketListener.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootC = Ui.inflate(inflater, R.layout.fragment_tablet_hotel_reviews, null);
		mHotelImage = Ui.findView(mRootC, R.id.hotel_header_image);
		mViewPager = Ui.findView(mRootC, R.id.pager);
		mReviewSectionTitle = Ui.findView(mRootC, R.id.reviews_title);
		mDoneButton = Ui.findView(mRootC, R.id.done_button);
		mDoneButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mBackListener != null) {
					mBackListener.onHotelReviewsBackClicked();
				}
			}
		});
		resetDominantColor();
		mDominantMask = Ui.findView(mRootC, R.id.dominant_color_header_mask);
		mDominantMask.setBackgroundDrawable(mDominantColorBackground);

		mPagerAdapter = new UserReviewsFragmentPagerAdapter(getChildFragmentManager(), savedInstanceState);

		// Pager
		mViewPager.setOffscreenPageLimit(2);
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setOnPageChangeListener(this);

		// Tabs
		mSortGroup = Ui.findView(mRootC, R.id.user_review_sort_group);
		mSortGroup.setOnCheckedChangeListener(this);
		int position = mViewPager.getCurrentItem();
		onPageSelected(position);

		// Header
		float header = getResources().getDimension(R.dimen.tablet_reviews_header_height);
		float image = getResources().getDimension(R.dimen.hotel_header_height);
		float shift = (header - image) / 2.0f;
		mHotelImage.setTranslationY(shift);

		return mRootC;
	}

	@Override
	public void onResume() {
		super.onResume();
		mMeasurementHelper.registerWithProvider(this);
		if (Db.getHotelSearch() != null && Db.getHotelSearch().getSelectedProperty() != null) {
			Property property = Db.getHotelSearch().getSelectedProperty();
			bind(property);
		}
		Events.register(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		mMeasurementHelper.unregisterWithProvider(this);
		Events.unregister(this);
	}

	private void bind(Property property) {
		mReviewSectionTitle.setText(getString(R.string.reviews_for_TEMPLATE, property.getName()));
		mPagerAdapter.bind();

		// Hotel Image
		int placeholderResId = Ui.obtainThemeResID(getActivity(), R.attr.hotelImagePlaceHolderDrawable);
		if (property.getThumbnail() != null) {
			property.getThumbnail().fillImageView(mHotelImage, placeholderResId, mHeaderBitmapLoadedCallback);
		}
		else {
			mHotelImage.setImageResource(placeholderResId);
		}

		if (Db.getHotelSearch() != null && Db.getHotelSearch().getSelectedProperty() != null
			&& Db.getHotelSearch().getSelectedRate() != null) {
			Rate rate = Db.getHotelSearch().getSelectedRate();

			View selectRoomButton = Ui.findView(mRootC, R.id.room_rate_button_select);
			selectRoomButton.setVisibility(View.GONE);

			View addRoomButton = Ui.findView(mRootC, R.id.room_rate_button_add);
			addRoomButton.setVisibility(View.VISIBLE);
			addRoomButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					addSelectedRoomToTrip();
				}
			});

			TextView description = Ui.findView(mRootC, R.id.text_room_description);
			TextView pricePerNight = Ui.findView(mRootC, R.id.text_price_per_night);
			TextView bedType = Ui.findView(mRootC, R.id.text_bed_type);

			description.setText(rate.getRoomDescription());

			Set<BedType> bedTypes = rate.getBedTypes();
			if (bedTypes != null && bedTypes.iterator().hasNext()) {
				bedType.setVisibility(View.VISIBLE);
				bedType.setText(bedTypes.iterator().next().getBedTypeDescription());
			}
			else {
				bedType.setVisibility(View.GONE);
			}

			String formattedRoomRate = rate.getDisplayPrice().getFormattedMoney(Money.F_NO_DECIMAL);
			pricePerNight.setText(Html.fromHtml(getString(R.string.room_rate_per_night_template, formattedRoomRate)));

			View row = Ui.findView(mRootC, R.id.room_rate_add_select_container);
			final ColorDrawable colorDrawable = new ColorDrawable(
				getResources().getColor(R.color.bg_row_state_pressed));
			row.setBackgroundDrawable(colorDrawable);
		}
	}

	public void onHotelSelected() {
		bind(Db.getHotelSearch().getSelectedProperty());
		mViewPager.setCurrentItem(0);
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// OnPageChangeListener

	@Override
	public void onPageScrollStateChanged(int state) {
		// nothing to do here
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		// nothing to do here
	}

	@Override
	public void onPageSelected(int position) {
		int id = 0;
		if (position == 0) {
			id = R.id.user_review_button_recent;
		}
		else if (position == 1) {
			id = R.id.user_review_button_favorable;
		}
		else {
			id = R.id.user_review_button_critical;
		}
		RadioButton button = Ui.findView(mSortGroup, id);
		if (button != null) {
			button.setChecked(true);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// OnCheckedChangeListener

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		String referrerId = null;
		int position = 0;
		switch (checkedId) {
		case R.id.user_review_button_recent:
			referrerId = "App.Hotels.Reviews.Sort.Recent";
			position = 0;
			break;
		case R.id.user_review_button_favorable:
			referrerId = "App.Hotels.Reviews.Sort.Favorable";
			position = 1;
			break;
		case R.id.user_review_button_critical:
			referrerId = "App.Hotels.Reviews.Sort.Critical";
			position = 2;
			break;
		}

		mViewPager.setCurrentItem(position);
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// FragmentListener

	@Override
	public void onUserReviewsFragmentReady(UserReviewsFragment frag) {
		frag.bind();
	}

	@Override
	public void addMoreReviewsSeen(Set<String> reviews) {
		// TODO: Omniture tracking
	}

	@Subscribe
	public void onHotelAvailabilityUpdated(Events.HotelAvailabilityUpdated event) {
		bind(Db.getHotelSearch().getSelectedProperty());
	}

	@Subscribe
	public void onHotelRateSelected(Events.HotelRateSelected event) {
		bind(Db.getHotelSearch().getSelectedProperty());
	}

	private void addSelectedRoomToTrip() {
		HotelSearch search = Db.getHotelSearch();
		Property property = search.getSelectedProperty();
		Rate rate = search.getSelectedRate();
		if (rate == null) {
			rate = property.getLowestRate();
		}
		Db.getTripBucket().clearHotel();
		Db.getTripBucket().add(property, rate);
		Db.saveTripBucket(getActivity());

		mAddToBucketListener.onItemAddedToBucket();
	}

	public void resetDominantColor() {
		setDominantColor(getResources().getColor(R.color.hotel_details_sticky_header_background));
	}

	public void setDominantColor(int color) {
		if (mDominantColorBackground == null) {
			mDominantColorBackground = new ColorDrawable();
			mDominantColorBackground.setAlpha(0);
		}
		mDominantColorBackground.setColor(color);
		mDominantColorBackground.setAlpha(229);
		mDominantColorBackground.invalidateSelf();
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// Async handling of Header / ColorScheme

	L2ImageCache.OnBitmapLoaded mHeaderBitmapLoadedCallback = new L2ImageCache.OnBitmapLoaded() {
		@Override
		public void onBitmapLoaded(String url, Bitmap bitmap) {
			ColorBuilder builder = new ColorBuilder(BitmapUtils.getAvgColorOnePixelTrick(bitmap)).darkenBy(0.4f);
			setDominantColor(builder.build());
		}

		@Override
		public void onBitmapLoadFailed(String url) {
			resetDominantColor();
		}
	};

	/*
	MEASUREMENT HELPER
	 */

	private MeasurementHelper mMeasurementHelper = new MeasurementHelper() {

		@Override
		public void onContentSizeUpdated(int totalWidth, int totalHeight, boolean isLandscape) {
			//This attempts to replicate the global layout by doing 3 columns for our general results layout
			//and 2 rows (where the first one represents the actionbar).
			GridManager globalGm = new GridManager(2, 3);
			globalGm.setDimensions(totalWidth, totalHeight);
			globalGm.setRowSize(0, getActivity().getActionBar().getHeight());

			//Now we set up our local positions
			mGrid.setDimensions(globalGm.getColSpanWidth(1, 3), globalGm.getRowHeight(1));
			mGrid.setNumCols(3);
			mGrid.setNumRows(3);

			int topBottomSpaceSize = getResources()
				.getDimensionPixelSize(R.dimen.tablet_hotel_details_vertical_padding);
			float leftRightSpacePerc = getResources()
				.getFraction(R.fraction.tablet_hotel_details_horizontal_spacing_percentage, 1, 1);
			mGrid.setRowSize(0, topBottomSpaceSize);
			mGrid.setRowSize(2, topBottomSpaceSize);
			mGrid.setColumnPercentage(0, leftRightSpacePerc);
			mGrid.setColumnPercentage(2, leftRightSpacePerc);

			mGrid.setContainerToRow(mRootC, 1);
			mGrid.setContainerToColumn(mRootC, 1);
		}
	};
}
