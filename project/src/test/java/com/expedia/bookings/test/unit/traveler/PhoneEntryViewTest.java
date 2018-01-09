package com.expedia.bookings.test.unit.traveler;

import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.widget.EditText;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Phone;
import com.expedia.bookings.test.robolectric.RobolectricRunner;
import com.expedia.bookings.widget.traveler.PhoneEntryView;
import com.expedia.testutils.RobolectricPlaygroundRule;
import com.expedia.vm.traveler.TravelerPhoneViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricRunner.class)
public class PhoneEntryViewTest {
	private final String testNumber = "773 202 5862";
	private PhoneEntryView phoneEntryView;

	@Rule
	public RobolectricPlaygroundRule activityTestRule = new RobolectricPlaygroundRule(R.layout.test_phone_entry_view, R.style.V2_Theme_Packages);

	@Before
	public void before() {
		phoneEntryView = activityTestRule.findRoot();
	}

	@Test
	public void testMaterialForm() throws Throwable {
		TextInputLayout textInputLayout = phoneEntryView.findViewById(R.id.edit_phone_layout_number);
		assertNotNull(textInputLayout);
	}

	@Test
	public void updatePhone() throws Throwable {
		final TravelerPhoneViewModel phoneVM = new TravelerPhoneViewModel(activityTestRule.activity);
		Phone phone = new Phone();
		phoneVM.updatePhone(phone);
		phoneEntryView.setViewModel(phoneVM);

		EditText editPhoneNumber = phoneEntryView.findViewById(R.id.edit_phone_number);
		editPhoneNumber.setText(testNumber);

		assertEquals(testNumber, phone.getNumber());
	}

	@Test
	public void phonePrePopulated() throws Throwable {
		final TravelerPhoneViewModel phoneVM = new TravelerPhoneViewModel(activityTestRule.activity);
		Phone phone = new Phone();
		phone.setNumber(testNumber);
		String testCodeString = "355";
		phone.setCountryCode(testCodeString);
		String testCountryName = "Albania";
		phone.setCountryName(testCountryName);
		phoneVM.updatePhone(phone);
		phoneEntryView.setViewModel(phoneVM);

		EditText editPhoneNumber = phoneEntryView.findViewById(R.id.edit_phone_number);
		assertEquals(testNumber, editPhoneNumber.getText().toString());
		assertEquals(View.VISIBLE, editPhoneNumber.getVisibility());

		EditText materialCountryCode = phoneEntryView.findViewById(R.id.material_edit_phone_number_country_code);
		assertEquals("+" + testCodeString, materialCountryCode.getText().toString());
		assertEquals(View.VISIBLE, materialCountryCode.getVisibility());
	}

	@Test
	public void phoneErrorState() throws Throwable {
		final TravelerPhoneViewModel phoneVM = new TravelerPhoneViewModel(activityTestRule.activity);
		phoneVM.updatePhone(new Phone());

		phoneEntryView.setViewModel(phoneVM);
		phoneVM.getPhoneViewModel().getErrorSubject().onNext(true);

		AppCompatTextView errorView = phoneEntryView.findViewById(R.id.textinput_error);
		assertNotNull(errorView);
		assertEquals(activityTestRule.activity.getResources().getString(R.string.phone_validation_error_message), errorView.getText().toString());

		EditText editPhoneNumber = phoneEntryView.findViewById(R.id.edit_phone_number);
		editPhoneNumber.setText(testNumber);

		errorView = phoneEntryView.findViewById(R.id.textinput_error);
		assertNull(errorView);
	}
}
