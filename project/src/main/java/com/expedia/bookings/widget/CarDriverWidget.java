package com.expedia.bookings.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.section.SectionTravelerInfo;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CarDriverWidget extends ExpandableCardView implements TravelerButton.ITravelerButtonListener {

	public CarDriverWidget(Context context, AttributeSet attr) {
		super(context, attr);
	}

	@InjectView(R.id.travelerStatusIcon)
	ContactDetailsCompletenessStatusImageView driverCheckoutStatusRightImageView;

	@InjectView(R.id.travelerNameIcon)
	ContactInitialsImageView driverCheckoutStatusLeftImageView;

	@InjectView(R.id.section_traveler_info_container)
	SectionTravelerInfo sectionTravelerInfo;

	@InjectView(R.id.driver_info_text)
	TextView driverInfoText;

	@InjectView(R.id.driver_phone_text)
	TextView driverPhoneText;

	@InjectView(R.id.edit_first_name)
	EditText firstName;

	@InjectView(R.id.edit_last_name)
	EditText lastName;

	@InjectView(R.id.edit_email_address)
	EditText emailAddress;

	@InjectView(R.id.edit_phone_number_country_code_spinner)
	TelephoneSpinner phoneSpinner;

	@InjectView(R.id.edit_phone_number)
	EditText phoneNumber;

	@InjectView(R.id.driver_info_container)
	ViewGroup driverInfoContainer;

	@InjectView(R.id.traveler_button)
	TravelerButton travelerButton;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		LayoutInflater inflater = LayoutInflater.from(getContext());
		inflater.inflate(R.layout.car_driver_widget, this);
		ButterKnife.inject(this);

		phoneSpinner.selectPOSCountry();
		travelerButton.setVisibility(GONE);
		travelerButton.setTravelButtonListener(this);
		phoneNumber.setOnEditorActionListener(new android.widget.TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(android.widget.TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					setExpanded(false);
					mToolbarListener.onWidgetClosed();
				}
				return false;
			}
		});
		travelerButton.setVisibility(GONE);
		travelerButton.setTravelButtonListener(this);
		firstName.setOnFocusChangeListener(this);
		lastName.setOnFocusChangeListener(this);
		emailAddress.setOnFocusChangeListener(this);
		phoneNumber.setOnFocusChangeListener(this);
		bind();
	}

	public void bind() {

		// Cases
		// User is logged in - default to primary
		// User is logged in - selected a saved traveler
		// User is logged in - entering a new traveler(but not saving it)

		// User not logged in - empty form
		// User not logged in - entering a new traveler
		boolean isLoggedIn = User.isLoggedIn(getContext());
		Traveler traveler = sectionTravelerInfo.getTraveler();

		if (isLoggedIn) {
			// User is logged in - default to primary
			if (traveler == null) {
				traveler = Db.getUser().getPrimaryTraveler();
			}
			traveler.setEmail(Db.getUser().getPrimaryTraveler().getEmail());
			sectionTravelerInfo.bind(traveler);
			sectionTravelerInfo.refreshOnLoginStatusChange();
			lastName.setNextFocusRightId(phoneNumber.getId());
			lastName.setNextFocusDownId(phoneNumber.getId());
			driverInfoText.setText(traveler.getFullName());
			driverPhoneText.setText(traveler.getPhoneNumber());
			driverPhoneText.setVisibility(VISIBLE);
		}
		else {
			// Default
			if (traveler == null) {
				traveler = new Traveler();
				sectionTravelerInfo.bind(traveler);
			}
			sectionTravelerInfo.refreshOnLoginStatusChange();
			lastName.setNextFocusRightId(emailAddress.getId());
			lastName.setNextFocusDownId(emailAddress.getId());
		}

		if (TextUtils.isEmpty(traveler.getFullName())) {
			driverInfoText.setText(R.string.enter_driver_details);
			driverPhoneText.setText("");
			driverPhoneText.setVisibility(GONE);
			driverCheckoutStatusLeftImageView.setTraveler(null);
			driverCheckoutStatusLeftImageView.setStatus(ContactDetailsCompletenessStatus.DEFAULT);
			driverCheckoutStatusRightImageView.setStatus(ContactDetailsCompletenessStatus.DEFAULT);
			return;
		}

		// Validate
		boolean isValid = sectionTravelerInfo.performValidation();
		driverInfoText.setText(traveler.getFullName());
		driverPhoneText.setText(traveler.getPhoneNumber());
		driverPhoneText.setVisibility(!TextUtils.isEmpty(traveler.getPhoneNumber()) ? VISIBLE : GONE);
		driverCheckoutStatusLeftImageView.setTraveler(traveler);
		driverCheckoutStatusLeftImageView.setStatus(
			isValid ? ContactDetailsCompletenessStatus.COMPLETE : ContactDetailsCompletenessStatus.INCOMPLETE);
		driverCheckoutStatusRightImageView.setStatus(
			isValid ? ContactDetailsCompletenessStatus.COMPLETE : ContactDetailsCompletenessStatus.INCOMPLETE);
	}

	@Override
	public void setExpanded(boolean expand, boolean animate) {
		super.setExpanded(expand, animate);
		driverInfoContainer.setVisibility(expand ? GONE : VISIBLE);
		sectionTravelerInfo.setVisibility(expand ? VISIBLE : GONE);
		if (expand) {
			if (mToolbarListener != null) {
				mToolbarListener.setActionBarTitle(getActionBarTitle());
			}
			if (User.isLoggedIn(getContext())) {
				travelerButton.setVisibility(VISIBLE);
			}
			else {
				travelerButton.setVisibility(GONE);
			}
			firstName.requestFocus();
		}
		bind();
	}

	@Override
	public void onTravelerChosen(Traveler traveler) {
		sectionTravelerInfo.bind(traveler);
	}

	@Override
	public void onAddNewTravelerSelected() {

	}

	@Override
	public boolean getDoneButtonFocus() {
		if (phoneNumber != null) {
			return phoneNumber.hasFocus();
		}
		return false;
	}

	@Override
	public String getActionBarTitle() {
		return getResources().getString(R.string.cars_driver_details_text);
	}

	@Override
	public void onDonePressed() {
		if (sectionTravelerInfo.performValidation()) {
			setExpanded(false);
		}
	}

	@Override
	public void onLogin() {
		sectionTravelerInfo.bind(null);
		setExpanded(false);
	}

	@Override
	public void onLogout() {
		sectionTravelerInfo.bind(new Traveler());
		setExpanded(false);
	}

	@Override
	public boolean isComplete() {
		return sectionTravelerInfo.performValidation();
	}

}
