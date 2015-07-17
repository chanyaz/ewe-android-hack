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
	CheckBox mMerchendiseOptCheckBox;

	boolean mAttemptToLeaveMade = false;
	boolean mIsMerEmailOptIn = true;
	boolean isUserBucketedForTest;

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
		mMerchendiseOptCheckBox = Ui.findView(v, R.id.merchandise_guest_opt_checkbox);

		isUserBucketedForTest = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidHotelCKOMerEmailGuestOpt);
		if (isUserBucketedForTest && !User.isLoggedIn(getActivity())) {
			Ui.hideKeyboard(getActivity(), WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			mMerchendiseOptCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
					mIsMerEmailOptIn = (mMerchandiseSpam == MerchandiseSpam.CONSENT_TO_OPT_IN ? isChecked : !isChecked);
					// Save to DB
					Db.getTripBucket().getHotel().setIsMerEmailOptIn(mIsMerEmailOptIn);
					Db.saveTripBucket(getActivity());

					updateMerEmailOptCheckBox(isChecked);
				}
			});
			TripBucketItemHotel tripHotel = Db.getTripBucket().getHotel();
			CreateTripResponse createTripResponse = tripHotel.getCreateTripResponse();
			boolean isChecked = false;
			mMerchandiseSpam = createTripResponse.getMerchandiseSpam();
			if (mMerchandiseSpam != null) {
				switch (mMerchandiseSpam) {
				case ALWAYS:
					break;
				case CONSENT_TO_OPT_IN:
					mMerchendiseOptCheckBox.setText(Phrase.from(getActivity(), R.string.hotel_checkout_merchandise_guest_opt_in_TEMPLATE).put("brand", BuildConfig.brand).format());
					isChecked = tripHotel.isMerEmailOptIn();
					mMerchendiseOptCheckBox.setVisibility(View.VISIBLE);
					break;
				case CONSENT_TO_OPT_OUT:
					mMerchendiseOptCheckBox.setText(Phrase.from(getActivity(), R.string.hotel_checkout_merchandise_guest_opt_out_TEMPLATE).put("brand", BuildConfig.brand).format());
					isChecked = !tripHotel.isMerEmailOptIn();
					mMerchendiseOptCheckBox.setVisibility(View.VISIBLE);
					break;
				}
			}

			mMerchendiseOptCheckBox.setChecked(isChecked);
			updateMerEmailOptCheckBox(isChecked);
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

	private void updateMerEmailOptCheckBox(boolean isChecked) {
		Drawable drawable = getResources().getDrawable(R.drawable.abc_btn_check_material);
		drawable.setColorFilter(isChecked ? getResources().getColor(R.color.mer_email_checkbox_checked_color) :
			getResources().getColor(R.color.mer_email_checkbox_unchecked_color), PorterDuff.Mode.SRC_IN);
		mMerchendiseOptCheckBox.setButtonDrawable(drawable);
	}

	@Override
	public void onStart() {
		super.onStart();
		OmnitureTracking.trackPageLoadHotelsTravelerEditInfo(getActivity());
	}

	@Override
	public void onResume() {
		super.onResume();
		mTraveler = Db.getWorkingTravelerManager().getWorkingTraveler();
		mSectionTravelerInfo.bind(mTraveler);

		if (!isUserBucketedForTest) {
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
	}

	@Override
	public boolean validate() {
		mAttemptToLeaveMade = true;
		return mSectionTravelerInfo != null ? mSectionTravelerInfo.performValidation() : false;
	}
}
