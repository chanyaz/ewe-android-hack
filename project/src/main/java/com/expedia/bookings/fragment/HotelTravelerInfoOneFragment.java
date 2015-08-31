package com.expedia.bookings.fragment;

import android.app.Activity;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.HotelTravelerInfoOptionsActivity.Validatable;
import com.expedia.bookings.data.CreateTripResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.TripBucketItemHotel;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.enums.MerchandiseSpam;
import com.expedia.bookings.section.ISectionEditable.SectionChangeListener;
import com.expedia.bookings.section.InvalidCharacterHelper;
import com.expedia.bookings.section.InvalidCharacterHelper.InvalidCharacterListener;
import com.expedia.bookings.section.InvalidCharacterHelper.Mode;
import com.expedia.bookings.section.SectionTravelerInfo;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.FocusViewRunnable;
import com.expedia.bookings.utils.Ui;
import com.squareup.phrase.Phrase;

public class HotelTravelerInfoOneFragment extends Fragment implements Validatable {

	Traveler mTraveler;
	SectionTravelerInfo mSectionTravelerInfo;
	CheckBox mMerchandiseOptCheckBox;

	boolean mAttemptToLeaveMade = false;
	boolean mIsMerEmailOptIn;
	boolean mIsUserBucketedForTest;

	MerchandiseSpam mMerchandiseSpam;

	public static HotelTravelerInfoOneFragment newInstance() {
		return new HotelTravelerInfoOneFragment();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mAttemptToLeaveMade = false;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_hotel_traveler_info_step1, container, false);
		mAttemptToLeaveMade = false;
		mSectionTravelerInfo = Ui.findView(v, R.id.traveler_info);
		mMerchandiseOptCheckBox = Ui.findView(v, R.id.merchandise_guest_opt_checkbox);

		mIsUserBucketedForTest = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidHotelCKOMerEmailGuestOpt);
		if (mIsUserBucketedForTest && !User.isLoggedIn(getActivity())) {

			TripBucketItemHotel tripHotel = Db.getTripBucket().getHotel();
			CreateTripResponse createTripResponse = tripHotel.getCreateTripResponse();
			boolean isChecked = false;
			mMerchandiseSpam = createTripResponse.getMerchandiseSpam();
			if (mMerchandiseSpam != null) {
				switch (mMerchandiseSpam) {
				case ALWAYS:
					// Save to DB
					Db.getTripBucket().getHotel().setIsMerEmailOptIn(true);
					Db.saveTripBucket(getActivity());
					break;
				case CONSENT_TO_OPT_IN:
					mMerchandiseOptCheckBox.setText(Phrase.from(getActivity(), R.string.hotel_checkout_merchandise_guest_opt_in_TEMPLATE).put("brand", BuildConfig.brand).format());
					isChecked = tripHotel.isMerEmailOptInShownOnce() ? tripHotel.isMerEmailOptIn() : isChecked;
					initBucketedUsers(isChecked);
					break;
				case CONSENT_TO_OPT_OUT:
					mMerchandiseOptCheckBox.setText(Phrase.from(getActivity(), R.string.hotel_checkout_merchandise_guest_opt_out_TEMPLATE).put("brand", BuildConfig.brand).format());
					isChecked = tripHotel.isMerEmailOptInShownOnce() ? !tripHotel.isMerEmailOptIn() : isChecked;
					initBucketedUsers(isChecked);
					break;
				}
			}
		}

		mSectionTravelerInfo.addChangeListener(new SectionChangeListener() {
			@Override
			public void onChange() {
				if (mAttemptToLeaveMade) {
					//If we tried to leave, but we had invalid input, we should update the validation feedback with every change
					mSectionTravelerInfo.performValidation();
				}
			}
		});

		mSectionTravelerInfo.addInvalidCharacterListener(new InvalidCharacterListener() {
			@Override
			public void onInvalidCharacterEntered(CharSequence text, Mode mode) {
				InvalidCharacterHelper.showInvalidCharacterPopup(getFragmentManager(), mode);
			}
		});

		return v;
	}

	private void initBucketedUsers(boolean isChecked) {
		mMerchandiseOptCheckBox.setChecked(isChecked);
		updateMerEmailOptCheckBox(isChecked);

		mMerchandiseOptCheckBox.setVisibility(View.VISIBLE);
		getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		mMerchandiseOptCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
				updateMerEmailOptCheckBox(isChecked);
				trackMerEmailOptInOut();
			}
		});
	}

	private void trackMerEmailOptInOut() {
		if (mIsMerEmailOptIn) {
			OmnitureTracking.trackHotelsGuestMerEmailOptIn();
		}
		else {
			OmnitureTracking.trackHotelsGuestMerEmailOptOut();
		}
	}

	private void updateMerEmailOptCheckBox(boolean isChecked) {
		Drawable drawable = getResources().getDrawable(R.drawable.abc_btn_check_material);
		drawable.setColorFilter(isChecked ? getResources().getColor(R.color.mer_email_checkbox_checked_color) :
			getResources().getColor(R.color.mer_email_checkbox_unchecked_color), PorterDuff.Mode.SRC_IN);
		mMerchandiseOptCheckBox.setButtonDrawable(drawable);
		mIsMerEmailOptIn = (mMerchandiseSpam == MerchandiseSpam.CONSENT_TO_OPT_IN ? isChecked : !isChecked);
		// Save to DB
		Db.getTripBucket().getHotel().setIsMerEmailOptIn(mIsMerEmailOptIn);
		Db.saveTripBucket(getActivity());
	}

	@Override
	public void onStart() {
		super.onStart();
		OmnitureTracking.trackPageLoadHotelsTravelerEditInfo();
	}

	@Override
	public void onResume() {
		super.onResume();
		mTraveler = Db.getWorkingTravelerManager().getWorkingTraveler();
		mSectionTravelerInfo.bind(mTraveler);

		if (!mIsUserBucketedForTest) {
			View focused = this.getView().findFocus();
			if (focused == null || !(focused instanceof EditText)) {
				int firstFocusResId = R.id.edit_first_name;
				if (PointOfSale.getPointOfSale().showLastNameFirst()) {
					firstFocusResId = R.id.edit_last_name;
				}
				focused = Ui.findView(mSectionTravelerInfo, firstFocusResId);
			}
			if (focused != null && focused instanceof EditText) {
				FocusViewRunnable.focusView(this, focused);
			}
		}
		Db.getTripBucket().getHotel().setIsMerEmailOptInShownOnce(true);
	}

	@Override
	public boolean validate() {
		mAttemptToLeaveMade = true;
		return mSectionTravelerInfo != null ? mSectionTravelerInfo.performValidation() : false;
	}
}
