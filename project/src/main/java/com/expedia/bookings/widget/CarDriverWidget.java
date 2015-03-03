package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.section.SectionTravelerInfo;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class CarDriverWidget extends ExpandableCardView implements TravelerButton.ITravelerButtonListener {

	public CarDriverWidget(Context context, AttributeSet attr) {
		super(context, attr);
	}

	@InjectView(R.id.section_traveler_info_container)
	SectionTravelerInfo sectionTravelerInfo;

	@InjectView(R.id.driver_info_text)
	TextView driverInfoText;

	@InjectView(R.id.edit_first_name)
	EditText firstName;

	@InjectView(R.id.edit_middle_name)
	EditText middleName;

	@InjectView(R.id.edit_last_name)
	EditText lastName;

	@InjectView(R.id.edit_email_address)
	EditText emailAddress;

	@InjectView(R.id.edit_phone_number_country_code_spinner)
	TelephoneSpinner phoneSpinner;

	@InjectView(R.id.edit_phone_number)
	EditText phoneNumber;

	@InjectView(R.id.traveler_button)
	TravelerButton travelerButton;

	@OnClick(R.id.driver_info_card_view)
	public void onCardExpanded() {
		if (sectionTravelerInfo.getVisibility() != VISIBLE && mToolbarListener != null) {
			mToolbarListener.onWidgetExpanded(this);
			setExpanded(true);
		}
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
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
		sectionTravelerInfo.bind(new Traveler());
		sectionTravelerInfo.setEmailFieldsEnabled(true);
		firstName.setOnFocusChangeListener(this);
		middleName.setOnFocusChangeListener(this);
		lastName.setOnFocusChangeListener(this);
		emailAddress.setOnFocusChangeListener(this);
		lastName.setNextFocusDownId(emailAddress.getId());
		lastName.setNextFocusRightId(emailAddress.getId());
		phoneNumber.setOnFocusChangeListener(this);
		bind();
	}

	public void bind() {
		if (User.isLoggedIn(getContext())) {
			Traveler traveler = Db.getUser().getPrimaryTraveler();
			sectionTravelerInfo.bind(traveler);
			driverInfoText.setText(traveler.getFullName());
			driverInfoText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.driver_large, 0, R.drawable.checkmark, 0);
		}
		else if (sectionTravelerInfo.performValidation()) {
			Traveler traveler = sectionTravelerInfo.getTraveler();
			driverInfoText.setText(traveler.getFullName());
			driverInfoText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.driver_large, 0, R.drawable.checkmark, 0);
		}
		else {
			driverInfoText.setText(R.string.enter_driver_details);
			driverInfoText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.driver_large, 0, 0, 0);

		}
	}

	@Override
	public void setExpanded(boolean expand) {
		super.setExpanded(expand);
		if (expand && mToolbarListener != null) {
			mToolbarListener.setActionBarTitle(getActionBarTitle());
		}
		bind();
		driverInfoText.setVisibility(expand ? GONE : VISIBLE);
		sectionTravelerInfo.setVisibility(expand ? VISIBLE : GONE);
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
}
