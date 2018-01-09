package com.expedia.bookings.test.unit.traveler;

import android.app.Activity;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.TravelerName;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.MultiBrand;
import com.expedia.bookings.test.RunForBrands;
import com.expedia.bookings.test.robolectric.RobolectricRunner;
import com.expedia.bookings.widget.traveler.NameEntryView;
import com.expedia.testutils.RobolectricPlaygroundRule;
import com.expedia.vm.traveler.TravelerNameViewModel;
import com.mobiata.android.util.SettingUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricRunner.class)
public class NameEntryViewTest {
	private final String testFirstName = "Oscar";
	private final String testMiddleInitial = "T";
	private final String testLastName = "Grouch";
	private NameEntryView nameView;
	private Activity activity;

	@Rule
	public RobolectricPlaygroundRule playgroundRule = new RobolectricPlaygroundRule(R.layout.test_name_entry_view, R.style.V2_Theme_Packages);

	@Before
	public void setUp() {
		activity = playgroundRule.activity;
		nameView = playgroundRule.findRoot();
	}

	@Test
	public void testMaterialForm() throws Throwable {
		NameEntryView nameEntryView = (NameEntryView) LayoutInflater.from(activity)
			.inflate(R.layout.test_name_entry_view, null);
		TextInputLayout textInputLayout = nameEntryView.findViewById(R.id.first_name_layout_input);
		assertNotNull(textInputLayout);
	}

	@Test
	public void testNameUpdates() throws Throwable {
		TravelerNameViewModel testViewModel = new TravelerNameViewModel();
		TravelerName traveler = new TravelerName();
		testViewModel.updateTravelerName(traveler);
		setViewModel(testViewModel);

		setTestFirstMiddleAndLastNames();

		assertEquals(testFirstName, traveler.getFirstName());
		assertEquals(testMiddleInitial, traveler.getMiddleName());
		assertEquals(testLastName, traveler.getLastName());
	}

	@Test
	public void testTravelerPrePopulated() throws Throwable {
		TravelerName name = new TravelerName();
		name.setFirstName(testFirstName);
		name.setMiddleName(testMiddleInitial);
		name.setLastName(testLastName);
		TravelerNameViewModel testViewModel = new TravelerNameViewModel();
		testViewModel.updateTravelerName(name);
		setViewModel(testViewModel);

		assertEquals(testFirstName, name.getFirstName());
		assertEquals(testMiddleInitial, name.getMiddleName());
		assertEquals(testLastName, name.getLastName());
	}

	@Test
	public void testErrorState() throws Throwable {
		final TravelerNameViewModel testViewModel = new TravelerNameViewModel();
		TravelerName traveler = new TravelerName();
		testViewModel.updateTravelerName(traveler);
		setViewModel(testViewModel);
		testViewModel.getFirstNameViewModel().getErrorSubject().onNext(true);
		testViewModel.getMiddleNameViewModel().getErrorSubject().onNext(true);
		testViewModel.getLastNameViewModel().getErrorSubject().onNext(true);

		//test for accessibility content description
		assertEquals("Enter first name using letters only", ((TextInputLayout) nameView.getFirstName().getParent().getParent()).getError());
		assertEquals("Enter middle name using letters only", ((TextInputLayout) nameView.getMiddleName().getParent().getParent()).getError());
		assertEquals("Enter last name using letters only (minimum 2 characters)", ((TextInputLayout) nameView.getLastName().getParent().getParent()).getError());

		setTestFirstMiddleAndLastNames();

		assertNull(((TextInputLayout) nameView.getFirstName().getParent().getParent()).getError());
		assertNull(((TextInputLayout) nameView.getMiddleName().getParent().getParent()).getError());
		assertNull(((TextInputLayout) nameView.getLastName().getParent().getParent()).getError());
	}

	@Test
	public void testMiddleNameError() throws Throwable {
		NameEntryView nameEntryView = (NameEntryView) LayoutInflater.from(activity)
				.inflate(R.layout.test_name_entry_view, null);

		TravelerNameViewModel testViewModel = new TravelerNameViewModel();
		TravelerName traveler = new TravelerName();
		testViewModel.updateTravelerName(traveler);

		nameEntryView.setViewModel(testViewModel);
		testViewModel.getMiddleNameViewModel().getErrorSubject().onNext(true);

		TextInputLayout textInputLayout = nameEntryView.findViewById(R.id.middle_name_layout_input);
		assertEquals("Enter middle name using letters only", textInputLayout.getError());
	}

	@Test
	public void testMiddleNameAcceptsOneOrMoreLetter() throws Throwable {
		final String validMiddleName = "The";
		String initial = "The";
		TravelerNameViewModel testViewModel = new TravelerNameViewModel();
		TravelerName traveler = new TravelerName();
		testViewModel.updateTravelerName(traveler);
		setViewModel(testViewModel);

		setViewText(R.id.middle_name_input, validMiddleName);
		assertEquals("More than one letter allowed", initial, traveler.getMiddleName());
		assertEquals("More than one letter allowed", initial, nameView.getMiddleName().getText().toString());
	}

	@Test
	@RunForBrands(brands = {MultiBrand.EXPEDIA})
	public void testMaterialReversedNameLayout() throws Throwable {
		SettingUtils.save(activity, R.string.PointOfSaleKey, Integer.toString(PointOfSaleId.HONG_KONG.getId()));
		PointOfSale.onPointOfSaleChanged(activity);

		NameEntryView nameEntryView = (NameEntryView) LayoutInflater.from(activity)
			.inflate(R.layout.test_name_entry_view, null);

		assertTrue(PointOfSale.getPointOfSale().showLastNameFirst());

		TextInputLayout lastNameInputLayout = (TextInputLayout) nameEntryView.getChildAt(0);
		assertEquals(lastNameInputLayout, nameEntryView.getLastName().getParent().getParent());

		TextInputLayout firstNameInputLayout = (TextInputLayout) nameEntryView.getChildAt(1);
		assertEquals(firstNameInputLayout, nameEntryView.getFirstName().getParent().getParent());

		assertNull(nameEntryView.getMiddleName());
	}

	@Test
	public void testMaterialNameLayoutOrder() throws Throwable {
		NameEntryView nameEntryView = (NameEntryView) LayoutInflater.from(activity)
			.inflate(R.layout.test_name_entry_view, null);

		LinearLayout linearLayoutParent = (LinearLayout) nameEntryView.getChildAt(0);
		TextInputLayout firstNameParentLayout = (TextInputLayout) linearLayoutParent.getChildAt(0);
		assertEquals(firstNameParentLayout, nameEntryView.getFirstName().getParent().getParent());

		TextInputLayout middleNameInputLayout = (TextInputLayout) linearLayoutParent.getChildAt(1);
		assertEquals(middleNameInputLayout, (nameEntryView.getMiddleName().getParent().getParent()));

		TextInputLayout lastNameInputLayout = (TextInputLayout) nameEntryView.getChildAt(1);
		assertEquals(lastNameInputLayout, (nameEntryView.getLastName().getParent().getParent()));
	}

	private void setTestFirstMiddleAndLastNames() {
		setViewText(R.id.first_name_input, testFirstName);
		setViewText(R.id.middle_name_input, testMiddleInitial);
		setViewText(R.id.last_name_input, testLastName);
	}

	private void setViewText(int viewId, String testMiddleInitial) {
		EditText middleNameView = nameView.findViewById(viewId);
		middleNameView.setText(testMiddleInitial);
	}

	private void setViewModel(final TravelerNameViewModel viewModel) throws Throwable {
		nameView.setViewModel(viewModel);
	}
}
