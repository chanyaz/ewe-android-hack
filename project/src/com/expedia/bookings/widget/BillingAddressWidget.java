package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.utils.LocaleUtils;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.validation.TextViewValidator;
import com.mobiata.android.validation.ValidationProcessor;

public class BillingAddressWidget {
	private static final String BILLING_ADDRESS_USER_EXPANDED = "BILLING_ADDRESS_USER_EXPANDED";
	private static final String BILLING_ADDRESS_KEEP_EXPANDED = "BILLING_ADDRESS_KEEP_EXPANDED";

	private Context mContext;

	private View mSectionTitle;

	private ViewGroup mBillingSavedLayout;
	private EditText mAddress1EditText;
	private EditText mAddress2EditText;

	private ViewGroup mBillingFormLayout;
	private EditText mCityEditText;
	private EditText mPostalCodeEditText;
	private EditText mStateEditText;
	private Spinner mCountrySpinner;

	// Cached data from arrays
	private String[] mCountryCodes;

	// This is a tracking variable to solve a nasty problem.  The problem is that Spinner.onItemSelectedListener()
	// fires wildly when you set the Spinner's position manually (sometimes twice at a time).  We only want to track
	// when a user *explicitly* clicks on a new country.  What this does is keep track of what the system thinks
	// is the selected country - only the user can get this out of alignment, thus causing tracking.
	private int mSelectedCountryPosition;

	// Tracks if the user has explicitly expanded the billing information
	private boolean mUserExpanded = false;
	private boolean mKeepExpanded = false;

	private boolean mIsVisible = true;

	private ValidationProcessor mAddressValidationProcessor;

	public BillingAddressWidget(Context context, View rootView) {
		mContext = context;

		mSectionTitle = rootView.findViewById(R.id.billing_info_section_title);
		mBillingSavedLayout = (ViewGroup) rootView.findViewById(R.id.saved_billing_info_layout);
		mBillingFormLayout = (ViewGroup) rootView.findViewById(R.id.billing_info_layout);

		mAddress1EditText = (EditText) mBillingFormLayout.findViewById(R.id.address1_edit_text);
		mAddress2EditText = (EditText) mBillingFormLayout.findViewById(R.id.address2_edit_text);
		mCityEditText = (EditText) mBillingFormLayout.findViewById(R.id.city_edit_text);
		mPostalCodeEditText = (EditText) mBillingFormLayout.findViewById(R.id.postal_code_edit_text);
		mStateEditText = (EditText) mBillingFormLayout.findViewById(R.id.state_edit_text);
		mCountrySpinner = (Spinner) mBillingFormLayout.findViewById(R.id.country_spinner);

		// 10758: rendering the saved layouts on a software layer
		// to avoid the fuzziness of the saved section background
		LayoutUtils.sayNoToJaggies(mBillingSavedLayout);

		// Retrieve some data we keep using
		Resources r = mContext.getResources();
		String[] twoLetterCountryCodes = r.getStringArray(R.array.country_codes);
		String[] threeLetterCountryCodes = new String[twoLetterCountryCodes.length];
		for (int i = 0; i < twoLetterCountryCodes.length; i++) {
			threeLetterCountryCodes[i] = LocaleUtils.convertCountryCode(twoLetterCountryCodes[i]);
		}
		mCountryCodes = threeLetterCountryCodes;

		mBillingSavedLayout.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mUserExpanded = true;
				expand(true);
			}
		});

		// Setup automatic filling of state/country information based on city entered.
		// Works for some popular cities.
		mCityEditText.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// Do nothing
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// Do nothing
			}

			public void afterTextChanged(Editable s) {
				String key = s.toString().toLowerCase();
				if (BookingInfoUtils.COMMON_US_CITIES.containsKey(key)) {
					mStateEditText.setText(BookingInfoUtils.COMMON_US_CITIES.get(key));
					mStateEditText.setError(null);
					setSpinnerSelection(mCountrySpinner, mContext.getString(R.string.country_us));
				}
			}
		});

		// Set the default country as locale country
		final String targetCountry = mContext.getString(LocaleUtils.getDefaultCountryResId(mContext));
		setSpinnerSelection(mCountrySpinner, targetCountry);
		mCountrySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				// Adjust the postal code textview.  Do this regardless of how the country spinner changed selection
				if (mCountryCodes[mCountrySpinner.getSelectedItemPosition()].equals("USA")) {
					mPostalCodeEditText.setInputType(InputType.TYPE_CLASS_NUMBER
							| InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS);
				}
				else {
					mPostalCodeEditText.setInputType(InputType.TYPE_CLASS_TEXT
							| InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
				}

				// See description of mSelectedCountryPosition to understand why we're doing this
				if (mSelectedCountryPosition != position) {
					// TODO: important?
					//if (mFormHasBeenFocused) {
					//	BookingInfoUtils.focusAndOpenKeyboard(getActivity(), mPostalCodeEditText);
					//}
					BookingInfoUtils.onCountrySpinnerClick(mContext);

					// Once a user has explicitly changed the country, track every change thereafter
					mSelectedCountryPosition = -1;
				}
			}

			public void onNothingSelected(AdapterView<?> parent) {
				// Do nothing
			}
		});

		TextViewValidator requiredFieldValidator = new TextViewValidator();
		mAddressValidationProcessor = new ValidationProcessor();
		mAddressValidationProcessor.add(mAddress1EditText, requiredFieldValidator);
		mAddressValidationProcessor.add(mCityEditText, requiredFieldValidator);
		mAddressValidationProcessor.add(mStateEditText, requiredFieldValidator);
		mAddressValidationProcessor.add(mPostalCodeEditText, requiredFieldValidator);
	}

	public void restoreInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mUserExpanded = savedInstanceState.getBoolean(BILLING_ADDRESS_USER_EXPANDED);
			mKeepExpanded = savedInstanceState.getBoolean(BILLING_ADDRESS_KEEP_EXPANDED);
		}
	}

	public void saveInstanceState(Bundle outState) {
		if (outState != null) {
			outState.putBoolean(BILLING_ADDRESS_USER_EXPANDED, mUserExpanded);
			outState.putBoolean(BILLING_ADDRESS_KEEP_EXPANDED, mKeepExpanded);
		}
	}

	public void update(Location newLocation) {
		mIsVisible = true;
		if (newLocation == null) {
			clear();
			expand(false);
			return;
		}

		String address = StrUtils.formatAddress(newLocation);

		// Add country
		String countryCode = newLocation.getCountryCode();
		if (countryCode != null) {
			for (int n = 0; n < mCountryCodes.length; n++) {
				if (mCountryCodes[n].equals(countryCode)) {
					address += "\n" + mContext.getResources().getStringArray(R.array.country_names)[n];
					break;
				}
			}
		}

		// Set address in saved info view
		TextView addressView = (TextView) mBillingSavedLayout.findViewById(R.id.address_text_view);
		addressView.setText(address);

		// Sync the editable billing info fields
		if (newLocation.getStreetAddress() != null) {
			if (newLocation.getStreetAddress().size() > 0) {
				mAddress1EditText.setText(newLocation.getStreetAddress().get(0));
			}
			if (newLocation.getStreetAddress().size() > 1) {
				mAddress2EditText.setText(newLocation.getStreetAddress().get(1));
			}
		}
		mCityEditText.setText(newLocation.getCity());
		mPostalCodeEditText.setText(newLocation.getPostalCode());
		if (newLocation.getCountryCode() != null) {
			setSpinnerSelection(mCountrySpinner, mCountryCodes, newLocation.getCountryCode());
		}
		mStateEditText.setText(newLocation.getStateCode());

		if (isExpanded()) {
			expand(false);
		}
		else {
			collapse();
		}
	}

	public Location getLocation() {
		Location location = new Location();
		List<String> streetAddress = new ArrayList<String>();
		streetAddress.add(mAddress1EditText.getText().toString());
		String address2 = mAddress2EditText.getText().toString();
		if (address2 != null && address2.length() > 0) {
			streetAddress.add(address2);
		}
		location.setStreetAddress(streetAddress);
		location.setCity(mCityEditText.getText().toString());
		location.setPostalCode(mPostalCodeEditText.getText().toString());
		location.setStateCode(mStateEditText.getText().toString());
		location.setCountryCode(mCountryCodes[mCountrySpinner.getSelectedItemPosition()]);
		return location;
	}

	public void setOnFocusChangeListener(OnFocusChangeListener l) {
		mAddress1EditText.setOnFocusChangeListener(l);
		mAddress2EditText.setOnFocusChangeListener(l);
		mCityEditText.setOnFocusChangeListener(l);
		mPostalCodeEditText.setOnFocusChangeListener(l);
		mStateEditText.setOnFocusChangeListener(l);
		mCountrySpinner.setOnFocusChangeListener(l);
	}

	private void expand(boolean animateAndFocus) {
		if (mSectionTitle != null) {
			mSectionTitle.setVisibility(View.VISIBLE);
		}
		mBillingSavedLayout.setVisibility(View.GONE);
		mBillingFormLayout.setVisibility(View.VISIBLE);

		// TODO: Fix focus
		// TODO: Focus
	}

	public void collapse() {
		if (mSectionTitle != null) {
			mSectionTitle.setVisibility(View.VISIBLE);
		}
		mBillingSavedLayout.setVisibility(View.VISIBLE);
		mBillingFormLayout.setVisibility(View.GONE);
	}

	public void show() {
		mIsVisible = true;
		if (mSectionTitle != null) {
			mSectionTitle.setVisibility(View.GONE);
		}

		if (isExpanded()) {
			expand(false);
		}
		else {
			collapse();
		}
		return;
	}

	public void hide() {
		mIsVisible = false;
		if (mSectionTitle != null) {
			mSectionTitle.setVisibility(View.GONE);
		}
		mBillingSavedLayout.setVisibility(View.GONE);
		mBillingFormLayout.setVisibility(View.GONE);
		return;
	}

	public boolean isVisible() {
		return mIsVisible;
	}

	public boolean isExpanded() {
		return mKeepExpanded || mUserExpanded || !isComplete();
	}

	public void clear() {
		mKeepExpanded = true;
		mAddress1EditText.setText(null);
		mAddress2EditText.setText(null);
		mCityEditText.setText(null);
		mPostalCodeEditText.setText(null);
		mStateEditText.setText(null);
		final int countryResId = LocaleUtils.getDefaultCountryResId(mContext);
		setSpinnerSelection(mCountrySpinner, mContext.getString(countryResId));
	}

	public boolean isComplete() {
		return mAddressValidationProcessor.validate().size() == 0;
	}

	private void setSpinnerSelection(Spinner spinner, String target) {
		final int position = findAdapterIndex(spinner.getAdapter(), target);
		mSelectedCountryPosition = position;
		spinner.setSelection(position);
	}

	private int findAdapterIndex(SpinnerAdapter adapter, String target) {
		int numItems = adapter.getCount();
		for (int n = 0; n < numItems; n++) {
			String name = (String) adapter.getItem(n);
			if (name.equalsIgnoreCase(target)) {
				return n;
			}
		}
		return -1;
	}

	private void setSpinnerSelection(Spinner spinner, String[] codes, String targetCode) {
		for (int n = 0; n < codes.length; n++) {
			if (targetCode.equals(codes[n])) {
				mSelectedCountryPosition = n;
				spinner.setSelection(n);
				return;
			}
		}
	}

}
