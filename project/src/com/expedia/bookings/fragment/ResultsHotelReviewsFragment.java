package com.expedia.bookings.fragment;

import java.util.Set;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BedType;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelSearch;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.fragment.UserReviewsFragment;
import com.expedia.bookings.fragment.UserReviewsFragment.UserReviewsFragmentListener;
import com.expedia.bookings.interfaces.IAddToBucketListener;
import com.expedia.bookings.interfaces.IResultsHotelReviewsBackClickedListener;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.UserReviewsFragmentPagerAdapter;
import com.mobiata.android.widget.SegmentedControlGroup;
import com.squareup.otto.Subscribe;

public class ResultsHotelReviewsFragment extends Fragment implements UserReviewsFragmentListener,
		OnPageChangeListener, OnCheckedChangeListener {

	public static ResultsHotelReviewsFragment newInstance() {
		ResultsHotelReviewsFragment frag = new ResultsHotelReviewsFragment();
		return frag;
	}

	private ViewGroup mRootC;
	private ViewPager mViewPager;
	private SegmentedControlGroup mSortGroup;
	private TextView mReviewSectionTitle;

	private UserReviewsFragmentPagerAdapter mPagerAdapter;

	private IAddToBucketListener mAddToBucketListener;
	private IResultsHotelReviewsBackClickedListener mBackListener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mBackListener = Ui.findFragmentListener(this, IResultsHotelReviewsBackClickedListener.class);
		mAddToBucketListener = Ui.findFragmentListener(this, IAddToBucketListener.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootC = (ViewGroup) inflater.inflate(R.layout.fragment_tablet_hotel_reviews, null);
		mViewPager = Ui.findView(mRootC, R.id.pager);
		mReviewSectionTitle = Ui.findView(mRootC, R.id.reviews_title);
		mReviewSectionTitle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mBackListener != null) {
					mBackListener.onHotelReviewsBackClicked();
				}
			}
		});

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

		mRootC.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			public void onGlobalLayout() {
				int paddingx = (int) (mRootC.getWidth() * 0.14f);
				mRootC.setPadding(paddingx, mRootC.getPaddingTop(), paddingx, mRootC.getPaddingBottom());
			}
		});

		return mRootC;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (Db.getHotelSearch() != null && Db.getHotelSearch().getSelectedProperty() != null) {
			Property property = Db.getHotelSearch().getSelectedProperty();
			bind(property);
		}
		Events.register(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		Events.unregister(this);
	}

	private void bind(Property property) {
		mReviewSectionTitle.setText(getString(R.string.reviews_for_TEMPLATE, property.getName()));
		mPagerAdapter.bind();

		if (Db.getHotelSearch() != null && Db.getHotelSearch().getSelectedProperty() != null && Db.getHotelSearch().getSelectedRate() != null) {
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
			if (bedTypes.iterator().hasNext()) {
				bedType.setVisibility(View.VISIBLE);
				bedType.setText(bedTypes.iterator().next().getBedTypeDescription());
			}
			else {
				bedType.setVisibility(View.GONE);
			}

			String formattedRoomRate = rate.getDisplayPrice().getFormattedMoney(Money.F_NO_DECIMAL);
			pricePerNight.setText(Html.fromHtml(getString(R.string.room_rate_per_night_template, formattedRoomRate)));

			View row = Ui.findView(mRootC, R.id.room_rate_add_select_container);
			final ColorDrawable colorDrawable = new ColorDrawable(getResources().getColor(R.color.bg_row_state_pressed));
			row.setBackgroundDrawable(colorDrawable);
		}
	}

	public void onHotelSelected() {
		bind(Db.getHotelSearch().getSelectedProperty());
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
}
