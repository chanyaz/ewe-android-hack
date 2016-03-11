package com.expedia.bookings.test.phone.traveler;

import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.presenter.packages.TravelerPresenter;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.rules.PlaygroundRule;
import com.expedia.vm.traveler.CheckoutTravelerViewModel;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class TravelerPresenterTest {
	private TravelerPresenter testTravelerPresenter;

	private CheckoutTravelerViewModel mockViewModel;

	@Rule
	public PlaygroundRule activityTestRule = new PlaygroundRule(R.layout.test_traveler_presenter, R.style.V2_Theme_Packages);

	@Before
	public void setUp() {
		testTravelerPresenter = (TravelerPresenter) activityTestRule.getRoot();
		mockViewModel = mock(CheckoutTravelerViewModel.class);
	}

	@Test
	public void testTransitionsOneTraveler() {
		List<Traveler> mockTravelerList = mock(List.class);
		when(mockTravelerList.size()).thenReturn(1);
		when(mockViewModel.getTravelers()).thenReturn(mockTravelerList);
		when(mockViewModel.getTraveler(anyInt())).thenReturn(new Traveler());
		testTravelerPresenter.setViewModel(mockViewModel);

		onView(withId(R.id.traveler_default_state)).perform(click());
		EspressoUtils.assertViewIsDisplayed(R.id.traveler_entry_widget);
	}
}
